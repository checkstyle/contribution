///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.checkstyle.parser.CheckstyleReportsParser;

/**
 * POJO that accumulates all statistics gathered during parsing stage.
 *
 * @author attatrol
 */
public class Statistics {

    /**
     * Map storing severity numbers for records removed.
     */
    private Map<String, BigInteger> severityNumDiffRemoved = new HashMap<>();

    /**
     * Map storing severity numbers for records added.
     */
    private Map<String, BigInteger> severityNumDiffAdded = new HashMap<>();

    /**
     * Map storing module numbers for records removed.
     */
    private Map<String, BigInteger> moduleNumDiffRemoved = new HashMap<>();

    /**
     * Map storing module numbers for records added.
     */
    private Map<String, BigInteger> moduleNumDiffAdded = new HashMap<>();

    /**
     * Number of files in difference.
     */
    private int fileNumDiff;

    /**
     * Map storing severity numbers for base source.
     */
    private Map<String, BigInteger> severityNumBase = new HashMap<>();

    /**
     * Map storing module numbers for base source.
     */
    private Map<String, BigInteger> moduleNumBase = new HashMap<>();

    /**
     * Number of files in the base source.
     */
    private int fileNumBase;

    /**
     * Number of unique messages in the base source.
     */
    private int uniqueMessagesBase;

    /**
     * Map storing severity numbers for patch source.
     */
    private Map<String, BigInteger> severityNumPatch = new HashMap<>();

    /**
     * Map storing module numbers for patch source.
     */
    private Map<String, BigInteger> moduleNumPatch = new HashMap<>();

    /**
     * Number of files in the patch source.
     */
    private int fileNumPatch;

    /**
     * Number of unique messages in the patch source.
     */
    private int uniqueMessagesPatch;

    /**
     * Getter for number of records per severity for difference.
     *
     * @return number of records per severity.
     */
    public final Map<String, BigInteger> getSeverityNumDiff() {
        final Map<String, BigInteger> severityNumDiff = new HashMap<>(severityNumDiffRemoved);
        severityNumDiffAdded.forEach(
            (key, value) -> severityNumDiff.merge(key, value, BigInteger::add));
        return severityNumDiff;
    }

    /**
     * Getter for total number of severity records for difference.
     *
     * @return total number of severity records.
     */
    public final BigInteger getTotalNumDiff() {
        BigInteger totalSeverityNumber = BigInteger.ZERO;
        for (BigInteger number : getSeverityNumDiff().values()) {
            totalSeverityNumber = totalSeverityNumber.add(number);
        }
        return totalSeverityNumber;
    }

    /**
     * Getter for number of records per module for difference.
     *
     * @return number of records per module.
     */
    public final Map<String, BigInteger> getModuleNumDiff() {
        final Map<String, BigInteger> moduleNumDiff = new HashMap<>(moduleNumDiffRemoved);
        moduleNumDiffAdded.forEach(
            (key, value) -> moduleNumDiff.merge(key, value, BigInteger::add));
        return moduleNumDiff;
    }

    /**
     * Returns the number of files in difference.
     *
     * @return the number of files in difference
     */
    public final int getFileNumDiff() {
        return fileNumDiff;
    }

    /**
     * Returns the map storing severity numbers for base source.
     *
     * @return the map storing severity numbers for base source
     */
    public final Map<String, BigInteger> getSeverityNumBase() {
        return severityNumBase;
    }

    /**
     * Returns the map storing module numbers for base source.
     *
     * @return the map storing module numbers for base source
     */
    public final Map<String, BigInteger> getModuleNumBase() {
        return moduleNumBase;
    }

    /**
     * Getter for total number of severity records for base source.
     *
     * @return total number of severity records.
     */
    public final BigInteger getTotalNumBase() {
        BigInteger totalSeverityNumber = BigInteger.ZERO;
        for (BigInteger number : severityNumBase.values()) {
            totalSeverityNumber = totalSeverityNumber.add(number);
        }
        return totalSeverityNumber;
    }

    /**
     * Builds the severity statistics patch.
     *
     * @return the severity statistics patch
     */
    public final Map<String, String> getSeverityStatisticsPatch() {
        return buildStatisticsMap(severityNumPatch, severityNumDiffRemoved, severityNumDiffAdded);
    }

    /**
     * Builds the module statistics patch.
     *
     * @return the module statistics patch
     */
    public final Map<String, String> getModuleStatisticsPatch() {
        return buildStatisticsMap(moduleNumPatch, moduleNumDiffRemoved, moduleNumDiffAdded);
    }

    /**
     * Getter for statistics strings of records per key in the given maps.
     *
     * @param numPatchMap map with total numbers.
     * @param numDiffRemovedMap map with removed numbers.
     * @param numDiffAddedMap map with added numbers.
     * @return statistics of records per severity.
     */
    private static Map<String, String> buildStatisticsMap(
        Map<String, BigInteger> numPatchMap,
        Map<String, BigInteger> numDiffRemovedMap,
        Map<String, BigInteger> numDiffAddedMap) {

        final Map<String, BigInteger> statistics = new HashMap<>(numPatchMap);
        Stream.concat(
            numDiffRemovedMap.keySet().stream(),
            numDiffAddedMap.keySet().stream())
            .distinct()
            .forEach(module -> statistics.putIfAbsent(module, BigInteger.ZERO));

        return statistics.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                final String module = entry.getKey();
                return buildStatisticsString(
                    entry.getValue(),
                    numDiffRemovedMap.getOrDefault(module, null),
                    numDiffAddedMap.getOrDefault(module, null));
            }));
    }

    /**
     * Returns the number of files in the base source.
     *
     * @return the number of files in the base source
     */
    public final int getFileNumBase() {
        return fileNumBase;
    }

    /**
     * Returns the number of unique messages in the base source.
     *
     * @return the number of unique messages in the base source
     */
    public final int getUniqueMessagesBase() {
        return uniqueMessagesBase;
    }

    /**
     * Getter for total statistics of severity records for patch source.
     *
     * @return total statistics of severity records.
     */
    public final String getTotalStatisticsPatch() {
        BigInteger totalSeverityNumber = BigInteger.ZERO;
        for (BigInteger number : severityNumPatch.values()) {
            totalSeverityNumber = totalSeverityNumber.add(number);
        }

        final BigInteger removedNumber;
        if (uniqueMessagesBase > 0) {
            removedNumber = BigInteger.valueOf(uniqueMessagesBase);
        }
        else {
            removedNumber = null;
        }

        final BigInteger addedNumber;
        if (uniqueMessagesPatch > 0) {
            addedNumber = BigInteger.valueOf(uniqueMessagesPatch);
        }
        else {
            addedNumber = null;
        }

        return buildStatisticsString(totalSeverityNumber, removedNumber, addedNumber);
    }

    /**
     * Builds a statistics string. Format is like one of the following:
     * <ul>
     *     <li>{@code <totalNumber>}</li>
     *     <li>{@code <totalNumber> (<removedNumber> removed)}</li>
     *     <li>{@code <totalNumber> (<addedNumber> added)}</li>
     *     <li>{@code <totalNumber> (<removedNumber> removed, <addedNumber> added)}</li>
     * </ul>
     *
     * @param totalNumber the total number.
     * @param removedNumber the removed number.
     * @param addedNumber the added number.
     * @return the statistics string.
     */
    private static String buildStatisticsString(
        BigInteger totalNumber, BigInteger removedNumber, BigInteger addedNumber) {
        final StringBuilder result = new StringBuilder();
        result.append(totalNumber);
        if (removedNumber != null || addedNumber != null) {
            result.append(" (");
            if (removedNumber != null) {
                result.append(removedNumber).append(" removed");
                if (addedNumber != null) {
                    result.append(", ");
                }
            }
            if (addedNumber != null) {
                result.append(addedNumber).append(" added");
            }
            result.append(")");
        }
        return result.toString();
    }

    /**
     * Returns the number of files in the patch source.
     *
     * @return the number of files in the patch source
     */
    public final int getFileNumPatch() {
        return fileNumPatch;
    }

    /**
     * Returns the number of unique messages in the patch source.
     *
     * @return the number of unique messages in the patch source
     */
    public final int getUniqueMessagesPatch() {
        return uniqueMessagesPatch;
    }

    /**
     * Setter for number of files in difference.
     *
     * @param fileNumDiff1 number of files in difference.
     */
    public final void setFileNumDiff(final int fileNumDiff1) {
        fileNumDiff = fileNumDiff1;
    }

    /**
     * Registers single severity record from indexed source.
     *
     * @param severity value of severity record.
     * @param index index of the source.
     */
    public final void addSeverityRecord(String severity, int index) {
        final Map<String, BigInteger> severityRecorder;
        if (index == CheckstyleReportsParser.BASE_REPORT_INDEX) {
            severityRecorder = severityNumBase;
        }
        else {
            severityRecorder = severityNumPatch;
        }
        severityRecorder.merge(severity, BigInteger.ONE, BigInteger::add);
    }

    /**
     * Registers single removed severity record.
     *
     * @param severity value of severity record.
     */
    public final void addSeverityRecordRemoved(String severity) {
        severityNumDiffRemoved.merge(severity, BigInteger.ONE, BigInteger::add);
    }

    /**
     * Registers single added severity record.
     *
     * @param severity value of severity record.
     */
    public final void addSeverityRecordAdded(String severity) {
        severityNumDiffAdded.merge(severity, BigInteger.ONE, BigInteger::add);
    }

    /**
     * Registers single module record from indexed source.
     *
     * @param moduleName value of module record.
     * @param index index of the source.
     */
    public void addModuleRecord(String moduleName, int index) {
        final Map<String, BigInteger> moduleRecorder;
        if (index == CheckstyleReportsParser.BASE_REPORT_INDEX) {
            moduleRecorder = moduleNumBase;
        }
        else {
            moduleRecorder = moduleNumPatch;
        }
        moduleRecorder.merge(moduleName, BigInteger.ONE, BigInteger::add);
    }

    /**
     * Registers single removed module record.
     *
     * @param moduleName value of module record.
     */
    public final void addModuleRecordRemoved(String moduleName) {
        moduleNumDiffRemoved.merge(moduleName, BigInteger.ONE, BigInteger::add);
    }

    /**
     * Registers single added module record.
     *
     * @param moduleName value of module record.
     */
    public final void addModuleRecordAdded(String moduleName) {
        moduleNumDiffAdded.merge(moduleName, BigInteger.ONE, BigInteger::add);
    }

    /**
     * Registers single file from numbered source.
     *
     * @param index index of the source.
     */
    public final void incrementFileCount(int index) {
        if (index == CheckstyleReportsParser.BASE_REPORT_INDEX) {
            fileNumBase++;
        }
        else if (index == CheckstyleReportsParser.PATCH_REPORT_INDEX) {
            fileNumPatch++;
        }
    }

    /**
     * Getter for all severity level names encountered during
     * statistics generation.
     *
     * @return severity level names.
     */
    public final Set<String> getSeverityNames() {
        final Set<String> names = new HashSet<>(severityNumDiffRemoved.keySet());
        names.addAll(severityNumDiffAdded.keySet());
        names.addAll(severityNumBase.keySet());
        names.addAll(severityNumPatch.keySet());
        return names;
    }

    /**
     * Getter for all module names encountered during
     * statistics generation.
     *
     * @return module names.
     */
    public final Set<String> getModuleNames() {
        final Set<String> names = new HashSet<>(moduleNumDiffRemoved.keySet());
        names.addAll(moduleNumDiffAdded.keySet());
        names.addAll(moduleNumBase.keySet());
        names.addAll(moduleNumPatch.keySet());
        return names;
    }

    /**
     * Registers unique message from numbered source.
     *
     * @param index index of the source.
     */
    public void incrementUniqueMessageCount(int index) {
        if (index == CheckstyleReportsParser.BASE_REPORT_INDEX) {
            uniqueMessagesBase++;
        }
        else if (index == CheckstyleReportsParser.PATCH_REPORT_INDEX) {
            uniqueMessagesPatch++;
        }
    }
}
