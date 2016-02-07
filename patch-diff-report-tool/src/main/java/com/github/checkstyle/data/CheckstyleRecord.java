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
 * @author atta_troll
 *
 */
public class CheckstyleRecord {

    /**
     * Flag of belonging to the first source.
     */
    private boolean belongsToFirst;

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
    private Severity severity;

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
     * @param belongsToFirst1
     *        flag of belonging to first XML report.
     * @param line1
     *        line number.
     * @param column1
     *        column number.
     * @param severity1
     *        record severity level.
     * @param source1
     *        name of check that generated record.
     * @param message1
     *        error message.
     */
    public CheckstyleRecord(boolean belongsToFirst1, int line1, int column1,
            Severity severity1, String source1, String message1) {
        this.belongsToFirst = belongsToFirst1;
        this.line = line1;
        this.column = column1;
        this.severity = severity1;
        this.source = source1;
        this.message = message1;
    }

    /**
     * Below are multiple getters.
     *
     * @return true if belongs to the first report.
     */
    public final boolean belongsToFirstReport() {
        return belongsToFirst;
    }

    public final int getLine() {
        return line;
    }

    public final int getColumn() {
        return column;
    }

    public final String getSource() {
        return source;
    }

    public final String getMessage() {
        return message;
    }

    public final Severity getSeverity() {
        return severity;
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
