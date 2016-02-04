package com.github.checkstyle.parser;

/**
 * POJO, maps into single "error" tag of the XML.
 *
 * @author atta_troll
 *
 */
public class CheckstyleRecord {
    private boolean belongsToFirst;
    private int line;
    private int column;
    private Severity severity;
    private String source;
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
