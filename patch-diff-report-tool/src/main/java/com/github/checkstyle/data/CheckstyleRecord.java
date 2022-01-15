////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
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
////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.data;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.thymeleaf.util.StringUtils;

/**
 * POJO, maps into single "error" tag of the XML.
 *
 * @author attatrol
 *
 */
public final class CheckstyleRecord implements Comparable<CheckstyleRecord> {

    /**
     * It is usual for sources of records to have name that
     * matches this pattern. It is used for shortening source names.
     */
    private static final Pattern CHECKSTYLE_CHECK_NAME = Pattern.compile(".+Check");

    /**
     * Predefined severities for sorting. All other severities has lower priority
     * and will be arranged in the default order for strings.
     */
    private static final List<String> PREDEFINED_SEVERITIES =
        Arrays.asList("info", "warning", "error");

    /**
     * Length of "Check" string.
     */
    private static final int CHECK_STRING_LENGTH = 5;

    /**
     * Index of the source.
     */
    private final int index;

    /**
     * Record line index.
     */
    private final int line;

    /**
     * Record column index.
     */
    private final int column;

    /**
     * Severity of this record.
     */
    private final String severity;

    /**
     * Name of the check that generated this record.
     */
    private final String source;

    /**
     * The message.
     */
    private final String message;

    /**
     * The xref.
     */
    private String xref;

    /**
     * POJO ctor.
     *
     * @param index
     *        internal index of the source.
     * @param line
     *        line number.
     * @param column
     *        column number.
     * @param severity
     *        record severity level.
     * @param source
     *        name of check that generated record.
     * @param xref
     *        external file reference.
     * @param message
     *        error message.
     */
    public CheckstyleRecord(int index, int line, int column,
            String severity, String source, String message, String xref) {
        this.index = index;
        this.line = line;
        this.column = column;
        this.severity = severity;
        this.source = source;
        this.message = message;
        this.xref = xref;
    }

    /**
     * Returns internal index of the file.
     *
     * @return internal index of the file
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the line index.
     *
     * @return the line index
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column index.
     *
     * @return the column index
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the name of the check that generated this record.
     *
     * @return the name of the check
     */
    public String getSource() {
        return source;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the message in HTML format.
     *
     * @return the message in HTML format
     */
    public String getMessageHtml() {
        return StringUtils.escapeXml(message).replace("\n", "<br />\n");
    }

    /**
     * Returns the record xref.
     *
     * @return the record xref
     */
    public String getXref() {
        return xref;
    }

    /**
     * Setter for the record xref.
     *
     * @param xref the new xref
     */
    public void setXref(String xref) {
        this.xref = xref;
    }

    /**
     * Returns the record severity.
     *
     * @return the record severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * Generates and returns simple form of check's name.
     *
     * @return simple form of check's name.
     */
    public String getSimpleSourceName() {
        final int lastPointIndex = source.lastIndexOf('.');
        return source.substring(lastPointIndex + 1);
    }

    /**
     * Generates and returns simple form of check's name.
     * Tries to delete "Check" ending substring of the result.
     *
     * @return simple form of check's name without ending "Check".
     */
    public String getSimpleCuttedSourceName() {
        final String simpleName = getSimpleSourceName();
        final Matcher matcher = CHECKSTYLE_CHECK_NAME.matcher(simpleName);
        if (matcher.matches()) {
            return simpleName.substring(0, simpleName.length() - CHECK_STRING_LENGTH);
        }
        else {
            return simpleName;
        }
    }

    /**
     * Compares CheckstyleRecord instances by their content.
     * The order is source, line, column, severity, message.
     * Properties index and xref are ignored.
     *
     * @param other
     *        another CheckstyleRecord instance under comparison
     *        with this instance.
     * @return 0 if the objects are equal, a negative integer if this record is before the specified
     *         record, or a positive integer if this record is after the specified record.
     */
    public int compareTo(final CheckstyleRecord other) {
        int diff = Integer.compare(line, other.line);
        if (diff == 0) {
            diff = Integer.compare(column, other.column);
        }
        if (diff == 0) {
            diff = compareSeverity(severity, other.severity);
        }
        if (diff == 0) {
            diff = message.compareTo(other.message);
        }
        if (diff == 0) {
            diff = source.compareTo(other.source);
        }
        return diff;
    }

    /**
     * Compares record severities in the order "info", "warning", "error", all other.
     *
     * @param severity1 first severity
     * @param severity2 second severity
     * @return the value {@code 0} if both severities are the same
     *         a value less than {@code 0} if severity1 should be first and
     *         a value greater than {@code 0} if severity2 should be first
     */
    private int compareSeverity(String severity1, String severity2) {
        final int result;
        if (severity1.equals(severity2)) {
            result = 0;
        }
        else {
            final int index1 = PREDEFINED_SEVERITIES.indexOf(severity1);
            final int index2 = PREDEFINED_SEVERITIES.indexOf(severity2);
            if (index1 < 0 && index2 < 0) {
                // Both severity levels are unknown, so use regular order for strings.
                result = severity1.compareTo(severity2);
            }
            else if (index1 < 0) {
                // First is unknown, second is known: second before
                result = 1;
            }
            else if (index2 < 0) {
                // First is known, second is unknown: first before
                result = -1;
            }
            else {
                // Both result are well-known, use predefined order
                result = Integer.compare(index1, index2);
            }
        }
        return result;
    }

}
