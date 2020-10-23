////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.checkstyle.parser.CheckstyleReportsParser;

/**
 * Contains diff from parsed data, expunges all abundant information
 * immediately when there is an opportunity to do so,
 * thus keeping memory usage as minimal as possible.
 *
 * @author attatrol
 *
 */
public final class DiffReport {

    /** Number of records to process at a time when looking for differences. */
    private static final int SPLIT_SIZE = 100;

    /**
     * Container for parsed data,
     * note it is a TreeMap for memory keeping purposes.
     */
    private Map<String, List<CheckstyleRecord>> records =
            new TreeMap<>();

    /**
     * Container for statistical data.
     */
    private Statistics statistics = new Statistics();

    /**
     * Getter for data container.
     *
     * @return map containing parsed data.
     */
    public Map<String, List<CheckstyleRecord>> getRecords() {
        return records;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Utility to merge the patch related contents of the {@code other} class to the current.
     *
     * @param other The other class to merge.
     */
    public void mergePatch(DiffReport other) {
        mergeRecords(other.records);
        statistics.copyPatch(other.statistics);
    }

    private void mergeRecords(Map<String, List<CheckstyleRecord>> other) {
        for (Entry<String, List<CheckstyleRecord>> item : other.entrySet()) {
            addRecords(item.getValue(), item.getKey());
        }
    }

    /**
     * Adds new records to the diff report,
     * when there are records with this filename, comparison
     * between them and new record is performed and only difference is saved.
     *
     * @param newRecords
     *        a new records list.
     * @param filename
     *        name of a file which is a cause of records generation.
     */
    public void addRecords(List<CheckstyleRecord> newRecords,
            String filename) {
        if (!newRecords.isEmpty()) {
            final List<CheckstyleRecord> popped =
                records.put(filename, newRecords);
            if (popped != null) {
                final List<CheckstyleRecord> diff =
                    produceDiff(popped, newRecords);
                if (diff.isEmpty()) {
                    records.remove(filename);
                }
                else {
                    Collections.sort(diff, new PositionOrderComparator());
                    records.put(filename, diff);
                }
            }
        }
    }

    /**
     * Creates difference between 2 lists of records.
     *
     * @param list1
     *        the first list.
     * @param list2
     *        the second list.
     * @return the difference list.
     */
    private static List<CheckstyleRecord> produceDiff(
            List<CheckstyleRecord> list1, List<CheckstyleRecord> list2) {
        final List<CheckstyleRecord> diff;
        try {
            diff = produceDiffEx(list1, list2);
            diff.addAll(produceDiffEx(list2, list1));
        }
        catch (InterruptedException | ExecutionException ex) {
            throw new IllegalStateException("Multi-threading failure reported", ex);
        }

        return diff;
    }

    private static List<CheckstyleRecord> produceDiffEx(
            List<CheckstyleRecord> list1, List<CheckstyleRecord> list2)
            throws InterruptedException, ExecutionException {
        final List<CheckstyleRecord> diff = new ArrayList<>();
        if (list1.size() < SPLIT_SIZE) {
            for (CheckstyleRecord rec1 : list1) {
                if (!isInList(list2, rec1)) {
                    diff.add(rec1);
                }
            }
        }
        else {
            final ExecutorService executor =
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            final List<Future<List<CheckstyleRecord>>> futures = new ArrayList<>();
            final int size = list1.size();
            for (int i = 0; i < size; i += SPLIT_SIZE) {
                futures.add(executor.submit(new MultiThreadedDiff(list1, list2, i, Math.min(size, i
                        + SPLIT_SIZE))));
            }

            for (Future<List<CheckstyleRecord>> future : futures) {
                diff.addAll(future.get());
            }

            executor.shutdown();
        }
        return diff;
    }

    /**
     * Compares the record against list of records.
     *
     * @param list
     *        of records.
     * @param testedRecord
     *        the record.
     * @return true, if has its copy in a list.
     */
    private static boolean isInList(List<CheckstyleRecord> list,
            CheckstyleRecord testedRecord) {
        boolean belongsToList = false;
        for (CheckstyleRecord checkstyleRecord : list) {
            if (testedRecord.specificEquals(checkstyleRecord)) {
                belongsToList = true;
                break;
            }
        }
        return belongsToList;
    }

    /**
     * Generates statistical information and puts in in the accumulator.
     */
    public void generateDiffStatistics() {
        statistics.setFileNumDiff(records.size());
        for (Map.Entry<String, List<CheckstyleRecord>> entry
                : records.entrySet()) {
            final List<CheckstyleRecord> list = entry.getValue();
            for (CheckstyleRecord rec : list) {
                if (rec.getIndex() == CheckstyleReportsParser.BASE_REPORT_INDEX) {
                    statistics.addSeverityRecordRemoved(rec.getSeverity());
                    statistics.addModuleRecordRemoved(rec.getSource());
                }
                else {
                    statistics.addSeverityRecordAdded(rec.getSeverity());
                    statistics.addModuleRecordAdded(rec.getSource());
                }
                statistics.incrementUniqueMessageCount(rec.getIndex());
            }
        }
    }

    /**
     * Comparator used to sort lists of CheckstyleRecord objects
     * by their position in code.
     *
     * @author atta_troll
     *
     */
    private static class PositionOrderComparator
        implements Comparator<CheckstyleRecord> {

        @Override
        public int compare(final CheckstyleRecord arg0,
                final CheckstyleRecord arg1) {
            final int difference = arg0.getLine() - arg1.getLine();
            if (difference == 0) {
                return arg0.getColumn() - arg1.getColumn();
            }
            else {
                return difference;
            }
        }
    }

    /** Separate class to multi-thread 2 lists checking if items from 1 is in the other. */
    private static final class MultiThreadedDiff implements Callable<List<CheckstyleRecord>> {
        /** First list to examine. */
        private List<CheckstyleRecord> list1;
        /** Second list to examine. */
        private List<CheckstyleRecord> list2;
        /** Inclusive start position of the first list. */
        private int list1Start;
        /** Non-inclusive End position of the first list. */
        private int list1End;

        /**
         * Default constructor.
         *
         * @param list1 First list to examine.
         * @param list2 Second list to examine.
         * @param list1Start Inclusive start position of the first list.
         * @param list1End Non-inclusive End position of the first list.
         */
        private MultiThreadedDiff(List<CheckstyleRecord> list1, List<CheckstyleRecord> list2,
                int list1Start, int list1End) {
            this.list1 = list1;
            this.list2 = list2;
            this.list1Start = list1Start;
            this.list1End = list1End;
        }

        @Override
        public List<CheckstyleRecord> call() throws Exception {
            final List<CheckstyleRecord> diff = new ArrayList<>();

            for (int i = list1Start; i < list1End; i++) {
                final CheckstyleRecord rec1 = list1.get(i);

                if (!isInList(list2, rec1)) {
                    diff.add(rec1);
                }
            }
            return diff;
        }
    }

}
