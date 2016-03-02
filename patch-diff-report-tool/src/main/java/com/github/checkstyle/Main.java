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

package com.github.checkstyle;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.checkstyle.data.CliPaths;
import com.github.checkstyle.data.ParsedContent;
import com.github.checkstyle.data.Statistics;
import com.github.checkstyle.parser.StaxParserProcessor;
import com.github.checkstyle.site.JxrDummyLog;
import com.github.checkstyle.site.SiteGenerator;

/**
 * Utility class, contains main function and its auxiliary routines.
 * @author atta_troll
 */
public final class Main {

    /**
     * Link to the help html.
     */
    public static final String HELP_HTML_PATH = "help.html";

    /**
     * Help message.
     */
    public static final String MSG_HELP = "This program creates symmetric difference "
            + "from two checkstyle-result.xml reports\n"
            + "generated for checkstyle build.\n"
            + "Command line arguments:\n"
            + "\t--baseReportPath - path to the directory containing base checkstyle-result.xml, "
            + "obligatory argument;\n"
            + "\t--patchReportPath - path to the directory containing patch checkstyle-result.xml, "
            + "also obligatory argument;\n"
            + "\t--sourcePath - path to the data under check (optional, if absent then file "
            + "structure for cross reference files won't be relativized, "
            + "full paths will be used);\n"
            + "\t--resultPath - path to the resulting site (optional, if absent then default "
            + "path will be used: ~/XMLDiffGen_report_yyyy.mm.dd_hh:mm:ss), remember, "
            + "if this folder exists its content will be purged;\n"
            + "\t-h - simply shows help message.";

    /**
     * Number of "file" xml tags parsed at one iteration of parser.
     */
    public static final int XML_PARSE_PORTION_SIZE = 50;

    /**
     * Name for the site file.
     */
    public static final Path SITEPATH = Paths.get("index.html");

    /**
     * Name for the XREF files folder.
     */
    public static final Path XREF_FILEPATH = Paths.get("xref");

    /**
     * Name for standart checkstyle xml report.
     */
    public static final Path XML_FILEPATH = Paths.get("checkstyle-result.xml");

    /**
     * Name for the CSS files folder.
     */
    public static final Path CSS_FILEPATH = Paths.get("css");

    /**
     * Internal index of the base report file.
     */
    public static final int BASE_REPORT_INDEX = 1;

    /**
     * Internal index of the patch report file.
     */
    public static final int PATCH_REPORT_INDEX = 2;

    /**
     * Internal index of the generated difference.
     */
    public static final int DIFF_REPORT_INDEX = 0;

    /**
     * Name for command line option "baseReportPath".
     */
    private static final String OPTION_BASE_FOLDER = "baseReportPath";

    /**
     * Name for command line option "patchReportPath".
     */
    private static final String OPTION_PATCH_FOLDER = "patchReportPath";

    /**
     * Name for command line option "sourcePath".
     */
    private static final String OPTION_SOURCE_PATH = "sourcePath";

    /**
     * Name for command line option "resultPath".
     */
    private static final String OPTION_PESULT_FOLDER = "resultPath";

    /**
     * Name for command line option that shows help message.
     */
    private static final String OPTION_HELP = "h";

    /**
     * Private ctor, see main method.
     */
    private Main() {

    }

    /**
     * Executes all three processing stages according to CLI options.
     *
     * @param args
     *        cli arguments.
     * @throws Exception
     *         on failure to execute stages.
     */
    public static void main(final String... args) throws Exception {
        final CommandLine commandLine = parseCli(args);
        if (commandLine.hasOption(OPTION_HELP)) {
            System.out.println(MSG_HELP);
        }
        else {
            final CliPaths paths = parseCliToPojo(commandLine);
            if (paths.getBaseReportPath() == null) {
                throw new IllegalArgumentException("obligatory argument --baseReportPath"
                        + " not present, -h for help");
            }
            if (paths.getPatchReportPath() == null) {
                throw new IllegalArgumentException("obligatory argument --patchReportPath "
                        + "not present, -h for help");
            }

            //preparation stage, checks validity of input paths
            System.out.println("Preparation stage is started.");
            PreparationUtils.checkFilesExistence(paths);
            PreparationUtils.exportResources(paths);

            //XML parsing stage
            System.out.println("XML parsing is started.");
            final ParsedContent content = new ParsedContent();
            final Statistics statistics = new Statistics();
            StaxParserProcessor.parse(content, paths.getBaseReportPath(),
                    paths.getPatchReportPath(), XML_PARSE_PORTION_SIZE, statistics);

            //Site and XREF generation stage
            System.out.println("Creation of diff html site is started.");
            try {
                SiteGenerator.generate(content, statistics, paths);
            }
            finally {
                for (String message:JxrDummyLog.getLog()) {
                    System.out.println(message);
                }
            }
            System.out.println("Creation of the site succeed.");
        }
    }

    /**
     * Parses CLI.
     *
     * @param args
     *        command line parameters
     * @return parsed information about passed parameters
     * @throws ParseException
     *         when passed arguments are not valid
     */
    private static CommandLine parseCli(String... args)
            throws ParseException {
        // parse the parameters
        final CommandLineParser clp = new DefaultParser();
        // always returns not null value
        return clp.parse(buildOptions(), args);
    }

    /**
     * Forms POJO containing input paths.
     *
     * @param commandLine
     *        parsed CLI.
     * @return POJO instance.
     * @throws IllegalArgumentException
     *         on failure to find necessary arguments.
     */
    private static CliPaths parseCliToPojo(CommandLine commandLine)
            throws IllegalArgumentException {
        final Path pathXmlBase;
        if (commandLine.hasOption(OPTION_BASE_FOLDER)) {
            final Path pathDirBase = Paths
                    .get(commandLine.getOptionValue(OPTION_BASE_FOLDER));
            pathXmlBase = pathDirBase.resolve(XML_FILEPATH);
        }
        else {
            pathXmlBase = null;
        }
        final Path pathXmlPatch;
        if (commandLine.hasOption(OPTION_PATCH_FOLDER)) {
            final Path pathDirPatch = Paths
                    .get(commandLine.getOptionValue(OPTION_PATCH_FOLDER));
            pathXmlPatch = pathDirPatch.resolve(XML_FILEPATH);
        }
        else {
            pathXmlPatch = null;
        }
        final Path pathSource;
        if (commandLine.hasOption(OPTION_SOURCE_PATH)) {
            pathSource = Paths
                    .get(commandLine.getOptionValue(OPTION_SOURCE_PATH));
        }
        else {
            pathSource = null;
        }
        final Path pathResult;
        if (commandLine.hasOption(OPTION_PESULT_FOLDER)) {
            pathResult = Paths
                    .get(commandLine.getOptionValue(OPTION_PESULT_FOLDER));
        }
        else {
            pathResult = Paths.get(System.getProperty("user.home"))
                    .resolve("XMLDiffGen_report_" + new SimpleDateFormat("yyyy.MM.dd_HH:mm:ss")
                            .format(Calendar.getInstance().getTime()));
        }
        return new CliPaths(pathXmlBase, pathXmlPatch, pathSource, pathResult);
    }

    /**
     * Builds and returns list of parameters supported by this utility.
     *
     * @return available options.
     */
    private static Options buildOptions() {
        final Options options = new Options();
        options.addOption(null, OPTION_BASE_FOLDER, true,
                "Path to the directory containing base checkstyle-report.xml");
        options.addOption(null, OPTION_PATCH_FOLDER, true,
                "Path to the directory containing patch checkstyle-report.xml");
        options.addOption(null, OPTION_SOURCE_PATH, true,
                "Path to the directory containing source under checkstyle check, optional.");
        options.addOption(null, OPTION_PESULT_FOLDER, true,
                "Path to directory where result path will be stored.");
        options.addOption(OPTION_HELP, false,
                "Simply show help message.");
        return options;
    }

}
