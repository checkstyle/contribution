////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
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
import java.util.TreeMap;

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
        final List<CheckstyleRecord> diff = new ArrayList<>();
        for (CheckstyleRecord rec1 : list1) {
            if (!isInList(list2, rec1)) {
                diff.add(rec1);
            }
        }
        for (CheckstyleRecord rec2 : list2) {
            if (!isInList(list1, rec2)) {
                diff.add(rec2);
            }
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
        for (CheckstyleRecord record : list) {
            if (testedRecord.specificEquals(record)) {
                belongsToList = true;
                break;
            }
        }
        return belongsToList;
    }

    /**
     * Generates statistical information and puts in in the accumulator.
     */
    public void getDiffStatistics() {
        statistics.setFileNumDiff(records.size());
        for (Map.Entry<String, List<CheckstyleRecord>> entry
                : records.entrySet()) {
            final List<CheckstyleRecord> list = entry.getValue();
            for (CheckstyleRecord rec : list) {
                statistics.addSeverityRecord(rec.getSeverity(),
                        CheckstyleReportsParser.DIFF_REPORT_INDEX);
                statistics.addModuleRecord(rec.getSource(),
                        CheckstyleReportsParser.DIFF_REPORT_INDEX);
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

}
