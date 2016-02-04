package com.github.checkstyle.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container for all parsed data, expunges all abundant information
 * immediately when there is an opportunity to do so,
 * thus keeping memory usage as minimal as possible.
 *
 * @author atta_troll
 *
 */
public final class ParsedContent {
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

    /**
     * Container for parsed data,
     * note it is a TreeMap for memory keeping purposes.
     */
    private Map<String, List<CheckstyleRecord>> records =
            new TreeMap<>();

    /**
     * Utility ctor.
     */
    public ParsedContent() {

    }

    /**
     * Getter for data container.
     *
     * @return map containing parsed data.
     */
    public Map<String, List<CheckstyleRecord>> getRecords() {
        return records;
    }

    /**
     * Adds new records to the container,
     * when there are records with this filename, comparison
     * between them and new record is performed and only difference is saved.
     *
     * @param filename
     *        name of a file which is a cause of records generation.
     * @param newRecords
     *        a new records list.
     */
    public void addRecords(String filename,
            List<CheckstyleRecord> newRecords) {
        List<CheckstyleRecord> popped = records.put(filename, newRecords);
        if (popped != null) {
            List<CheckstyleRecord> diff = produceDiff(popped, newRecords);
            if (diff.isEmpty()) {
                records.remove(filename);
            }
            else {
                Collections.sort(diff, new PositionOrderComparator());
                records.put(filename, diff);
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
        List<CheckstyleRecord> diff = new ArrayList<>();
        for (CheckstyleRecord rec1 : list1) {
            if (!isInList(rec1, list2)) {
                diff.add(rec1);
            }
        }
        for (CheckstyleRecord rec2 : list2) {
            if (!isInList(rec2, list1)) {
                diff.add(rec2);
            }
        }
        return diff;
    }

    /**
     * Compares the record against list of records.
     *
     * @param testedRecord
     *        the record.
     * @param list
     *        of records.
     * @return true, if has its copy in a list.
     */
    private static boolean isInList(CheckstyleRecord testedRecord,
            List<CheckstyleRecord> list) {
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
     * Generates statistical information and puts in in the holder.
     *
     * @param holder a StatisticsHolder instance.
     */
    public void getStatistics(StatisticsHolder holder) {
        holder.setFileNumDiff(records.size());
        for (Map.Entry<String, List<CheckstyleRecord>> entry
                : records.entrySet()) {
            final List<CheckstyleRecord> list = entry.getValue();
            for (CheckstyleRecord rec : list) {
                holder.incrementTotalNumDiff();
                switch (rec.getSeverity()) {
                case ERROR:
                    holder.incrementErrorNumDiff();
                    break;
                case WARNING:
                    holder.incrementWarningNumDiff();
                    break;
                default:
                    holder.incrementInfoNumDiff();
                    break;
                }
            }
        }
    }

}
