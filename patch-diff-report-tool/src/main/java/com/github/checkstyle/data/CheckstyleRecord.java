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

/**
 * POJO, maps into single "error" tag of the XML.
 *
 * @author attatrol
 *
 */
public class CheckstyleRecord {

    /**
     * Index of the source.
     */
    private int index;

    /**
     * Record line index.
     */
    private int line;

    /**
     * Record column index.
     */
    private int column;

    /**
     * Severity of this record.
     */
    private String severity;

    /**
     * Name of the check that generated this record.
     */
    private String source;

    /**
     * The message.
     */
    private String message;

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
     * @param message
     *        error message.
     */
    public CheckstyleRecord(int index, int line, int column,
            String severity, String source, String message) {
        this.index = index;
        this.line = line;
        this.column = column;
        this.severity = severity;
        this.source = source;
        this.message = message;
    }

    /**
     * Below are multiple getters.
     *
     * @return internal index of the file.
     */
    public int getIndex() {
        return index;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

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
     * Compares CheckstyleRecord instances by their content.
     * It is used in a single controlled occasion in the code.
     *
     * @param other
     *        another ChechstyleRecord instance under comparison
     *        with this instance.
     * @return true if instances are equal.
     */
    public final boolean specificEquals(final CheckstyleRecord other) {
        return this.line == other.line && this.column == other.column
                && this.source.equals(other.source)
                && this.message.equals(other.message);
    }

}
