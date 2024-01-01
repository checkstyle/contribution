///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2024 the original author or authors.
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
///////////////////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.data;

import java.util.Collections;
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

    /**
     * Returns the container for statistical data.
     *
     * @return the container for statistical data
     */
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
            Collections.sort(newRecords);
            final List<CheckstyleRecord> popped =
                records.put(filename, newRecords);
            if (popped != null) {
                final List<CheckstyleRecord> diff =
                    DiffUtils.produceDiff(popped, newRecords);
                if (diff.isEmpty()) {
                    records.remove(filename);
                }
                else {
                    records.put(filename, diff);
                }
            }
        }
    }

    /**
     * Generates statistical information and puts in in the accumulator.
     * This method will wait for completion of all asynchronous tasks.
     */
    public void getDiffStatistics() {
        statistics.setFileNumDiff(records.size());
        records.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream())
            .forEach(this::addRecordStatistics);
    }

    /**
     * Generates statistical information for one CheckstyleRecord.
     *
     * @param checkstyleRecord the checkstyleRecord to process
     */
    private void addRecordStatistics(CheckstyleRecord checkstyleRecord) {
        if (checkstyleRecord.getIndex() == CheckstyleReportsParser.BASE_REPORT_INDEX) {
            statistics.addSeverityRecordRemoved(checkstyleRecord.getSeverity());
            statistics.addModuleRecordRemoved(checkstyleRecord.getSource());
        }
        else {
            statistics.addSeverityRecordAdded(checkstyleRecord.getSeverity());
            statistics.addModuleRecordAdded(checkstyleRecord.getSource());
        }
        statistics.incrementUniqueMessageCount(checkstyleRecord.getIndex());
    }
}
