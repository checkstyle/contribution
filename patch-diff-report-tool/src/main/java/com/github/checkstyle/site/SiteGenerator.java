package com.github.checkstyle.site;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.checkstyle.parser.CheckstyleRecord;
import com.github.checkstyle.parser.ParsedContent;
import com.github.checkstyle.parser.StatisticsHolder;

import static com.github.checkstyle.Main.HELP_HTML_PATH;
import static com.github.checkstyle.Main.SITEPATH;

/**
 * Site generator from parsed data.
 *
 * @author atta_troll
 *
 */
public final class SiteGenerator {
    /**
     * CSS style codes.
     */
    public static final char FIRST_REPORT_CSS_STYLE = 'a';
    public static final char SECOND_REPORT_CSS_STYLE = 'b';

    /**
     * Abbreviations used in site tables.
     */
    public static final char SEVERITY_WARNING_CHAR = 'W';
    public static final char SEVERITY_INFO_CHAR = 'I';
    public static final char SEVERITY_ERROR_CHAR = 'E';

    /**
     * Conventional names of the initial XML files.
     */
    public static final String FIRST_REPORT_NAME = "first";
    public static final String SECOND_REPORT_NAME = "second";

    /**
     * Delimiter used in full names of checks.
     */
    private static final char CHECK_NAME_DELIMITER = '.';

    /**
     * Counter used in anchor links generation.
     */
    private static long anchorCounter = 0;

    /**
     * Site head tag content.
     */
    private static final String HTML_HEAD =
            "<title>checkstyle xml difference report</title>\n"
            + "<style type=\"text/css\" media=\"all\">"
            + "@import url(\"./css/maven-base.css\");"
            + "@import url(\"./css/maven-theme.css\"); );"
            + "</style>\n"
            + "<http-equiv http-equiv=\"Content-Language\" content=\"en\">"
            + "</http-equiv>";

    /**
     * Site title.
     */
    private static final String SITE_TITLE =
            "<div class=\"section\">\n"
            + "<h2 a=\"Checkstyle XML difference report\">"
            + "Checkstyle XML difference report</h2>\n"
            + "<a href=\"" + HELP_HTML_PATH + "\">"
            + "<h4>explanation</h4></a>\n</div>\n";

    /**
     * Statistics section title.
     */
    private static final String STATISTICS_TITLE =
            "<h2 a=\"Summary:\">Summary:</h2>\n";

    /**
     * Statistics table header.
     */
    private static final String STATISTICS_TABLE_HEADER =
            "<div class=\"section\">\n"
            + "<table border=\"0\" class=\"bodyTable\">\n"
            + "<tr class=\"a\">\n"
            + "<th>Report index</th>\n"
            + "<th>Files</th>\n"
            + "<th>Unique rows</th>\n"
            + "<th>Info</th>\n"
            + "<th>Warnings</th>\n"
            + "<th>Errors</th>\n"
            + "</tr>\n";

    /**
     * Statistics table row template.
     */
    private static final String STATISTICS_TABLE_ROW =
            "<tr class=\"%c\">\n"
            + "<td>%s</td>\n"
            + "<td>%d</td>\n"
            + "<td>%d</td>\n"
            + "<td>%d</td>\n"
            + "<td>%d</td>\n"
            + "<td>%d</td>\n"
            + "<tr>\n";

    /**
     * Title for the content section.
     */
    private static final String CONTENT_TITLE =
            "<div class=\"section\">\n"
            + "<h2 a=\"Unique rows:\">Unique rows:</h2>\n";

    /**
     * Header for every content section table.
     */
    private static final String TABLE_HEADER =
            "<div class=\"section\"><h3 id=\"%s\">%s</h3>\n<tr class=\"b\">\n"
            + "<table border=\"0\" class=\"bodyTable\">"
            + "<th>Anchor</th>\n"
            + "<th>Severity</th>\n"
            + "<th>Rule</th>\n"
            + "<th>Message</th>\n"
            + "<th>Line</th>\n"
            + "<th>Column</th>\n"
            + "<th>Report</th>\n"
            + "</tr>\n";

    /**
     * Content section table row template.
     */
    private static final String TABLE_ROW = "<tr class=\"%c\">\n"
            + "<td><a name=\"%s\" href=\"#%s\">%s</a></td>\n"
            + "<td>%c</td>\n"
            + "<td>%s</td>\n"
            + "<td>%s</td>\n"
            + "<td><a href=\"%s#L%d\">%d</a></td>\n"
            + "<td>%d</td>\n"
            + "<td>%s</td>\n"
            + "<tr>\n";

    /**
     * Content for help file.
     */
    private static final String HELP_FILE_CONTENT = "<div id=\"contentBox\">\n"
            + "<div class=\"section\">\n <h2 a=\"Explanation:\">Explanation:"
            + "</h2>This is symmetric difference generated "
            + "from two checkstyle-result.xml reports.\n"
            + "<br>All matching records from each XML file are deleted, "
            + "then remaining records are merged into single report.\n"
            + "<br>\n"
            + "<br>\n"
            + "<a href=\"https://github.com/checkstyle/contribution/"
            + "tree/master/patch-diff-report-tool\">"
            + "Utility that generated this report.</a>\n"
            + "<h3><a href=\"" + SITEPATH + "\">back to report</a></h3>\n"
            + "</div>\n"
            + "</div>\n";

    /**
     * Utility class ctor.
     */
    private SiteGenerator() {

    }

    /**
     * Generates site from ParsedContent and StatisticsHolder
     * and writes it on disk.
     *
     * @param content
     *        container for parsed data.
     * @param path
     *        of the result site.
     * @param holder
     *        StatisticsHolder instance.
     * @param generator
     *        XrefGenerator instance.
     * @return true if overall success.
     */
    public static boolean writeParsedContentToHtml(ParsedContent content,
            Path path, StatisticsHolder holder, XrefGenerator generator) {
        boolean success = true;
        try {
            Files.createFile(path);
            try (BufferedWriter writer =
                    new BufferedWriter(new FileWriter(path.toFile()))) {
                writer.write("<html>\n");
                writeHead(writer);
                writeBody(content, writer, holder, generator);
                writer.write("</html>\n");
            }
            catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
            success = false;

        }

        return success;
    }

    /**
     * Writes to disk html help.
     *
     * @param path
     *        path to the help file.
     * @return true on success.
     */
    public static boolean writeHtmlHelp(Path path) {
        boolean success = true;
        try {
            Files.createFile(path);
            try (BufferedWriter writer =
                    new BufferedWriter(new FileWriter(path.toFile()))) {
                writer.write("<html>\n");
                writeHead(writer);
                writer.write("<body class=\"composite\">\n");
                writer.write(HELP_FILE_CONTENT);
                writer.write("</body>\n");
                writer.write("</html>\n");
            }
            catch (IOException e) {
                e.printStackTrace();
                success = false;
            }
        }
        catch (IOException e1) {
            e1.printStackTrace();
            success = false;

        }

        return success;
    }

    /**
     * Writes out HTML head.
     *
     * @param writer
     *        BufferedWriter used to write HTML to file on disk.
     * @throws IOException
     *         thrown on writer failure.
     */
    private static void writeHead(BufferedWriter writer)
            throws IOException {
        writer.write("<head>\n");
        writer.write(HTML_HEAD);
        writer.write("</head>\n");
    }

    /**
     * Writes out html body.
     *
     * @param content
     *        container for parsed data.
     * @param writer
     *        BufferedWriter used to write HTML to file on disk.
     * @param holder
     *        StatisticsHolder instance.
     * @param generator
     *        XrefGenerator instance.
     * @throws IOException
     *         thrown on writer failure.
     */
    private static void writeBody(ParsedContent content, BufferedWriter writer,
            StatisticsHolder holder, XrefGenerator generator)
                    throws IOException {
        writer.write("<body class=\"composite\">\n");
        writer.write("<div id=\"contentBox\">\n");
        writer.write(SITE_TITLE);
        writeStatistics(writer, holder);
        writeContent(content, writer, generator);
        writer.write("</div>\n");
        writer.write("</body>\n");
    }

    /**
     * Writes statistics section to the file.
     *
     * @param writer
     *        BufferedWriter used to write HTML to file on disk.
     * @param holder
     *        StatisticsHolder instance
     * @throws IOException
     *         thrown on writer failure.
     */
    private static void writeStatistics(BufferedWriter writer,
            StatisticsHolder holder) throws IOException {
        writer.write("<div class=\"section\">\n");
        writer.write(STATISTICS_TITLE);
        writer.write(STATISTICS_TABLE_HEADER);
        writer.write(String.format(STATISTICS_TABLE_ROW, 'a', "first",
                holder.getFileNum1(),
                holder.getTotalNum1(),
                holder.getInfoNum1(),
                holder.getWarningNum1(),
                holder.getErrorNum1()));
        writer.write(String.format(STATISTICS_TABLE_ROW, 'b', "second",
                holder.getFileNum2(),
                holder.getTotalNum2(),
                holder.getInfoNum2(),
                holder.getWarningNum2(),
                holder.getErrorNum2()));
        writer.write(String.format(STATISTICS_TABLE_ROW, 'a', "difference",
                holder.getFileNumDiff(),
                holder.getTotalNumDiff(),
                holder.getInfoNumDiff(),
                holder.getWarningNumDiff(),
                holder.getErrorNumDiff()));
        writer.write("</table>\n</div>\n");
        writer.write("</div>\n");
    }

    /**
     * Writes content section to the file.
     *
     * @param content
     *        container for parsed data.
     * @param writer
     *        BufferedWriter used to write HTML to file on disk.
     * @param generator
     *        XrefGenerator instance.
     * @throws IOException
     *         thrown on writer failure.
     */
    private static void writeContent(ParsedContent content,
            BufferedWriter writer, XrefGenerator generator)
                    throws IOException {
        writer.write(CONTENT_TITLE);
        Iterator<Map.Entry<String, List<CheckstyleRecord>>> it =
                content.getRecords()
                .entrySet()
                .iterator();
        while (it.hasNext()) {
            final Map.Entry<String, List<CheckstyleRecord>> entry = it.next();
            final String filename = entry.getKey();
            final String xreference = generator.generateXref(filename);
            writer.write(String.format(TABLE_HEADER, filename, filename));
            final List<CheckstyleRecord> records = entry.getValue();
            for (CheckstyleRecord record : records) {
                writeTableRow(record, writer, xreference);
            }
            writer.write("</table>\n</div>\n");
        }
        writer.write("</div>\n");
    }

    /**
     * Writes single CheckstyleRecord as a row of content section table
     * to the file.
     *
     * @param record
     *        CheckstyleRecord instance
     * @param writer
     *        BufferedWriter used to write HTML to file on disk.
     * @param xreference
     *        link to the XDOC file.
     * @throws IOException
     *         thrown on writer failure.
     */
    private static void writeTableRow(CheckstyleRecord record,
            BufferedWriter writer, String xreference)
                    throws IOException {
        final char styleChar = record.belongsToFirstReport()
                ? FIRST_REPORT_CSS_STYLE : SECOND_REPORT_CSS_STYLE;
        final String reportName = record.belongsToFirstReport()
                ? FIRST_REPORT_NAME : SECOND_REPORT_NAME;
        final String anchor = generateAnchor();
        final char severityChar;
        switch (record.getSeverity()) {
        case INFORMATIONAL:
            severityChar = SEVERITY_INFO_CHAR;
            break;
        case WARNING:
            severityChar = SEVERITY_WARNING_CHAR;
            break;
        default:
            severityChar = SEVERITY_ERROR_CHAR;
        }
        final int lineNum = record.getLine();
        final int columnNum = record.getColumn();
        final String fullCheckName = record.getSource();
        final String shortCheckName = fullCheckName
                .substring(fullCheckName
                        .lastIndexOf(CHECK_NAME_DELIMITER) + 1);
        writer.write(String.format(TABLE_ROW, styleChar, anchor, anchor,
                anchor, severityChar, shortCheckName, record.getMessage(),
                xreference, lineNum, lineNum, columnNum, reportName));

    }

    /**
     * Generates unique string value to be used as anchor link names.
     *
     * @return unique string.
     */
    private static String generateAnchor() {
        anchorCounter++;
        return String.format("A%d", anchorCounter);
    }
}
