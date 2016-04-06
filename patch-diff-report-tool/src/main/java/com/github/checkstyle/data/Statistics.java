////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.checkstyle.parser.StaxContentParser;

/**
 * POJO that accumulates all statistics gathered during parsing stage.
 *
 * @author attatrol
 */
public class Statistics {

    /**
     * Map storing severity numbers for difference.
     */
    private Map<String, Integer> severityNumDiff = new HashMap<>();

    /**
     * Number of files in difference.
     */
    private int fileNumDiff;

    /**
     * Map storing severity numbers for base source.
     */
    private Map<String, Integer> severityNumBase = new HashMap<>();

    /**
     * Number of files in the base source.
     */
    private int fileNumBase;

    /**
     * Map storing severity numbers for patch source.
     */
    private Map<String, Integer> severityNumPatch = new HashMap<>();

    /**
     * Number of files in the patch source.
     */
    private int fileNumPatch;

    public final Map<String, Integer> getSeverityNumDiff() {
        return severityNumDiff;
    }

    /**
     * Getter for total number of severity records for difference.
     *
     * @return total number of severity records.
     */
    public final int getTotalNumDiff() {
        int totalSeverityNumber = 0;
        for (Integer number : severityNumDiff.values()) {
            totalSeverityNumber += number;
        }
        return totalSeverityNumber;
    }

    public final int getFileNumDiff() {
        return fileNumDiff;
    }

    public final Map<String, Integer> getSeverityNumBase() {
        return severityNumBase;
    }

    /**
     * Getter for total number of severity records for base source.
     *
     * @return total number of severity records.
     */
    public final int getTotalNumBase() {
        int totalSeverityNumber = 0;
        for (Integer number : severityNumBase.values()) {
            totalSeverityNumber += number;
        }
        return totalSeverityNumber;
    }

    public final Map<String, Integer> getSeverityNumPatch() {
        return severityNumPatch;
    }

    public final int getFileNumBase() {
        return fileNumBase;
    }

    /**
     * Getter for total number of severity records for patch source.
     *
     * @return total number of severity records.
     */
    public final int getTotalNumPatch() {
        int totalSeverityNumber = 0;
        for (Integer number : severityNumPatch.values()) {
            totalSeverityNumber += number;
        }
        return totalSeverityNumber;
    }

    public final int getFileNumPatch() {
        return fileNumPatch;
    }

    /**
     * Setter for number of files in difference.
     *
     * @param fileNumDiff1 number of files in difference.
     */
    public final void setFileNumDiff(final int fileNumDiff1) {
        this.fileNumDiff = fileNumDiff1;
    }

    /**
     * Registers single severity record from indexed source.
     *
     * @param severity value of severity record.
     * @param index index of the source.
     */
    public final void addSeverityRecord(String severity, int index) {
        final Map<String, Integer> severityRecorder;
        if (index == StaxContentParser.BASE_REPORT_INDEX) {
            severityRecorder = severityNumBase;
        }
        else if (index == StaxContentParser.PATCH_REPORT_INDEX) {
            severityRecorder = severityNumPatch;
        }
        else {
            severityRecorder = severityNumDiff;
        }
        final Integer newNumber = severityRecorder.get(severity);
        if (newNumber != null) {
            severityRecorder.put(severity, newNumber + 1);
        }
        else {
            severityRecorder.put(severity, 1);
        }
    }

    /**
     * Registers single file from numbered source.
     *
     * @param index index of the source.
     */
    public final void incrementFileCount(int index) {
        this.fileNumBase++;
        if (index == StaxContentParser.BASE_REPORT_INDEX) {
            this.fileNumBase++;
        }
        else if (index == StaxContentParser.PATCH_REPORT_INDEX) {
            this.fileNumPatch++;
        }
    }

    /**
     * Getter for all severity level names encountered during
     * statistics generation.
     *
     * @return severity level names.
     */
    public final Set<String> getSeverityNames() {
        final Set<String> names = new HashSet<>(severityNumDiff.keySet());
        names.addAll(severityNumBase.keySet());
        names.addAll(severityNumPatch.keySet());
        return names;
    }

}
