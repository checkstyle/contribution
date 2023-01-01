///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2023 the original author or authors.
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

package com.github.checkstyle.site;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

/**
 * Custom transform implementation modeled after {@link org.apache.maven.jxr.JavaCodeTransform} to
 * handle any type of text file.
 *
 * @author Richard Veach
 */
// -@cs[ClassDataAbstractionCoupling] This class works with a lot of file formats.
public final class TextTransform {
    /**
     * The stylesheet file name.
     */
    public static final String STYLESHEET_FILENAME = "stylesheet.css";

    /**
     * The number 10.
     */
    private static final int NUMBER_10 = 10;

    /**
     * The number 100.
     */
    private static final int NUMBER_100 = 100;

    /**
     * End tag.
     */
    private static final String END_TAG = "\">";

    /**
     * Self-close end tag.
     */
    private static final String SELFCLOSE_TAG = "\"/>";

    /**
     * The file name.
     */
    private String fileName;

    /**
     * The wanted locale.
     */
    private Locale locale;

    /**
     * The output encoding.
     */
    private String encoding;

    /**
     * This is the public method for doing all transforms of the file.
     *
     * @param sourceFile
     *            String
     * @param destFile
     *            String
     * @param outputLocale
     *            String
     * @param inputEncoding
     *            String
     * @param outputEncoding
     *            String
     * @throws IOException
     *             if there is an error reading the file.
     */
    public void transform(String sourceFile, String destFile, Locale outputLocale,
            String inputEncoding, String outputEncoding) throws IOException {
        final File dest = new File(destFile);

        fileName = dest.getName();

        // make sure that the parent directories exist...
        new File(dest.getParent()).mkdirs();

        Reader reader = null;
        Writer writer = null;

        try {
            if (inputEncoding != null) {
                reader = new InputStreamReader(new FileInputStream(sourceFile), inputEncoding);
            }
            else {
                reader = new FileReader(sourceFile);
            }
            if (outputEncoding != null) {
                writer = new OutputStreamWriter(new FileOutputStream(dest), outputEncoding);
            }
            else {
                writer = new FileWriter(dest);
            }

            transform(reader, writer, outputLocale, outputEncoding);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * This is the method for doing all transforms of file.
     *
     * @param sourceReader
     *            Reader
     * @param destWriter
     *            Writer
     * @param outputLocale
     *            String
     * @param outputEncoding
     *            String
     * @throws IOException
     *             if there is an error reading.
     */
    private void transform(Reader sourceReader, Writer destWriter, Locale outputLocale,
            String outputEncoding) throws IOException {
        this.locale = outputLocale;
        encoding = outputEncoding;

        final BufferedReader input = new BufferedReader(sourceReader);
        final PrintWriter output = new PrintWriter(destWriter);
        String line = "";

        appendHeader(output);

        int linenumber = 1;
        while ((line = input.readLine()) != null) {
            output.print("<a class=\"jxr_linenumber\" name=\"L" + linenumber + "\" " + "href=\"#L"
                    + linenumber + END_TAG + linenumber + "</a>" + getLineWidth(linenumber));

            output.println(syntaxHighlight(line));

            ++linenumber;
        }

        appendFooter(output);

        output.flush();
    }

    /**
     * Appends the header attribute.
     *
     * @param out
     *            the writer where the header is appended to
     */
    private void appendHeader(PrintWriter out) {
        String outputEncoding = encoding;
        if (outputEncoding == null) {
            outputEncoding = "ISO-8859-1";
        }

        // header
        out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        out.print("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"");
        out.print(locale);
        out.print("\" lang=\"");
        out.print(locale);
        out.println(END_TAG);
        out.print("<head>");
        out.print("<meta http-equiv=\"content-type\" content=\"text/html; charset=");
        out.print(outputEncoding);
        out.println(SELFCLOSE_TAG);

        // title ("file name xref")
        out.print("<title>");
        out.print(fileName);
        out.println(" xref</title>");

        // stylesheet link
        out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
        out.print(STYLESHEET_FILENAME);
        out.println(SELFCLOSE_TAG);

        out.println("</head>");
        out.println("<body>");

        // start code section
        out.println("<pre>");
    }

    /**
     * Appends the footer attribute.
     *
     * @param out
     *            the writer where the footer is appended to
     */
    private static void appendFooter(PrintWriter out) {
        out.println("</pre>");
        out.println("<hr/>");
        out.print("<div id=\"footer\">");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Now different method of seeing if at end of input stream, closes inputs stream at end.
     *
     * @param line
     *            String
     * @return filtered line of code
     */
    private static String syntaxHighlight(String line) {
        return htmlFilter(line);
    }

    /**
     * Filter html tags into more benign text.
     *
     * @param line
     *            String
     * @return html encoded line
     */
    private static String htmlFilter(String line) {
        String result;

        if (line == null || "".equals(line)) {
            result = "";
        }
        else {
            result = replace(line, "&", "&amp;");
            result = replace(result, "<", "&lt;");
            result = replace(result, ">", "&gt;");
            result = replace(result, "\\\\", "&#92;&#92;");
            result = replace(result, "\\\"", "\\&quot;");
            result = replace(result, "'\"'", "'&quot;'");
        }

        return result;
    }

    /**
     * Handles line width which may need to change depending on which line number you are on.
     *
     * @param linenumber
     *            int
     * @return String
     */
    private static String getLineWidth(int linenumber) {
        final String result;

        if (linenumber < NUMBER_10) {
            result = "   ";
        }
        else if (linenumber < NUMBER_100) {
            result = "  ";
        }
        else {
            result = " ";
        }

        return result;
    }

    /**
     * Replace...
     *
     * @param line
     *            String
     * @param oldString
     *            String
     * @param newString
     *            String
     * @return String
     */
    private static String replace(String line, String oldString, String newString) {
        String result = line;
        int index = 0;
        while ((index = result.indexOf(oldString, index)) >= 0) {
            result = result.substring(0, index) + newString
                    + result.substring(index + oldString.length());
            index += newString.length();
        }
        return result;
    }
}
