///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2026 the original author or authors.
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

package com.github.checkstyle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.github.checkstyle.data.CliOptions;
import com.github.checkstyle.data.CompareMode;
import com.github.checkstyle.data.DiffReport;
import com.github.checkstyle.data.MergedConfigurationModule;
import com.github.checkstyle.parser.CheckstyleConfigurationsParser;
import com.github.checkstyle.parser.CheckstyleReportsParser;
import com.github.checkstyle.parser.CheckstyleTextParser;
import com.github.checkstyle.site.SiteGenerator;

/**
 * Utility class, contains main function and its auxiliary routines.
 *
 * @author attatrol
 */
public final class Main {

    /**
     * Help message.
     */
    public static final String MSG_HELP = "This program creates symmetric difference "
            + "from two checkstyle-result.xml reports\n"
            + "generated for checkstyle build.\n"
            + "Command line arguments:\n"
            + "--compareMode - type of comparison to do with the files; 'XML' parses the report "
            + "files as Checkstyle XML files with violation results. 'TEXT' parses the report "
            + "files as pure text files, line by line. (required argument); default: XML"
            + "\t--baseReport - path to the base checkstyle-result.xml (optional, if absent "
            + "then only configuration and violations for patch will be in the report)\n"
            + "\t--patchReport - path to the patch checkstyle-result.xml, "
            + "obligatory argument;\n"
            + "\t--refFiles - path to the source files under check (optional)\n"
            + "\t--output - path to store the resulting diff report (optional, if absent then "
            + "default path will be used: ~/XMLDiffGen_report_yyyy.mm.dd_hh_mm_ss), remember, "
            + "if this folder exists its content will be purged;\n"
            + "\t--baseConfig - path to the base checkstyle configuration xml file (optional)"
            + "\t--patchConfig - path to the patch checkstyle configuration xml (optional)"
            + "\t--shortFilePaths - Option to save report file paths as a shorter version to "
            + "prevent long paths. This option is useful for Windows users where they are "
            + "restricted to maximum directory depth. "
            + "\t-h - simply shows help message.";

    /**
     * Number of "file" xml tags parsed at one iteration of parser.
     */
    public static final int XML_PARSE_PORTION_SIZE = 50;

    /**
     * Name for the site file.
     */
    public static final Path CONFIG_PATH = Paths.get("configuration.html");

    /**
     * Name for the XREF files folder.
     */
    public static final Path XREF_FILEPATH = Paths.get("xref");

    /**
     * Name for the CSS files folder.
     */
    public static final Path CSS_FILEPATH = Paths.get("css");

    /**
     * Name for command line option "compareMode".
     */
    private static final String OPTION_COMPARE_MODE = "compareMode";

    /**
     * Name for command line option "baseReportPath".
     */
    private static final String OPTION_BASE_REPORT_PATH = "baseReport";

    /**
     * Name for command line option "patchReportPath".
     */
    private static final String OPTION_PATCH_REPORT_PATH = "patchReport";

    /**
     * Name for command line option "refFiles".
     */
    private static final String OPTION_REFFILES_PATH = "refFiles";

    /**
     * Name for command line option "outputPath".
     */
    private static final String OPTION_OUTPUT_PATH = "output";

    /**
     * Name for command line option "baseConfigPath".
     */
    private static final String OPTION_BASE_CONFIG_PATH = "baseConfig";

    /**
     * Name for command line option "patchConfigPath".
     */
    private static final String OPTION_PATCH_CONFIG_PATH = "patchConfig";

    /**
     * Name for command line option to save report file paths as a shorter
     * version to prevent long paths.
     */
    private static final String OPTION_SHORT_PATHS = "shortFilePaths";

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
        System.out.println("patch-diff-report-tool execution started.");
        final CommandLine commandLine = parseCli(args);
        if (commandLine.hasOption(OPTION_HELP)) {
            System.out.println(MSG_HELP);
        }
        else {
            final CliOptions options = getCliOptions(commandLine);
            final DiffReport diffReport;

            if (options.getCompareMode() == CompareMode.XML) {
                // XML parsing stage
                System.out.println("XML parsing is started.");
                diffReport = CheckstyleReportsParser.parse(options.getBaseReportPath(),
                        options.getPatchReportPath(), XML_PARSE_PORTION_SIZE);
            }
            else {
                // file parsing stage
                System.out.println("File parsing is started.");
                diffReport = CheckstyleTextParser.parse(options.getBaseReportPath(),
                        options.getPatchReportPath());
            }

            // Configuration processing stage.
            MergedConfigurationModule diffConfiguration = null;
            if (options.configurationPresent()) {
                System.out.println("Creation of configuration report is started.");
                diffConfiguration = CheckstyleConfigurationsParser
                        .parse(options.getBaseConfigPath(), options.getPatchConfigPath());
            }
            else {
                System.out.println(
                        "Configuration processing skipped: " + "no configuration paths provided.");
            }

            // Site and XREF generation stage
            System.out.println("Creation of diff html site is started.");
            exportResources(options);
            SiteGenerator.generate(diffReport, diffConfiguration, options);
            System.out.println("Creation of the result site succeed.");
        }
        System.out.println("patch-diff-report-tool execution finished.");
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
    private static CommandLine parseCli(String... args) throws ParseException {
        // parse the parameters
        final CommandLineParser clp = new DefaultParser();
        // always returns not null value
        return clp.parse(buildOptions(), args);
    }

    /**
     * Generates a CliOptions instance from commandLine and checks it for
     * validity.
     *
     * @param commandLine
     *        CLI arguments.
     * @return CliOptions instance.
     */
    private static CliOptions getCliOptions(CommandLine commandLine) {
        final CliOptions options = parseCliToPojo(commandLine);
        CliArgsValidator.validate(options);
        return options;
    }

    /**
     * Exports to disc necessary static resources.
     *
     * @param options
     *        POJO holding all options.
     * @throws IOException
     *         thrown on failure to perform checks.
     */
    private static void exportResources(CliOptions options) throws IOException {
        final Path outputPath = options.getOutputPath();
        Files.createDirectories(outputPath);
        FilesystemUtils.createOverwriteDirectory(outputPath.resolve(CSS_FILEPATH));
        FilesystemUtils.createOverwriteDirectory(outputPath.resolve(XREF_FILEPATH));
        FilesystemUtils.exportResource("/maven-theme.css",
                outputPath.resolve(CSS_FILEPATH).resolve("maven-theme.css"));
        FilesystemUtils.exportResource("/maven-base.css",
                outputPath.resolve(CSS_FILEPATH).resolve("maven-base.css"));
        FilesystemUtils.exportResource("/site.css",
            outputPath.resolve(CSS_FILEPATH).resolve("site.css"));
    }

    /**
     * Builds and returns list of parameters supported by this utility.
     *
     * @return available options.
     */
    private static Options buildOptions() {
        final Options options = new Options();
        options.addOption(null, OPTION_COMPARE_MODE, true,
                "Option to control which type of diff comparison to do.");
        options.addOption(null, OPTION_BASE_REPORT_PATH, true,
                "Path to the base checkstyle-report.xml");
        options.addOption(null, OPTION_PATCH_REPORT_PATH, true,
                "Path to the patch checkstyle-report.xml");
        options.addOption(null, OPTION_REFFILES_PATH, true,
                "Path to the directory containing source under checkstyle check, optional.");
        options.addOption(null, OPTION_OUTPUT_PATH, true,
                "Path to directory where diff results will be stored.");
        options.addOption(null, OPTION_BASE_CONFIG_PATH, true,
                "Path to the checkstyle configuration xml of the base report.");
        options.addOption(null, OPTION_PATCH_CONFIG_PATH, true,
                "Path to the checkstyle configuration xml of the patch report.");
        options.addOption(null, OPTION_SHORT_PATHS, false,
                "Option to save report file paths as a shorter version to prevent long paths.");
        options.addOption(OPTION_HELP, false, "Shows help message, nothing else.");
        return options;
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
    private static CliOptions parseCliToPojo(CommandLine commandLine)
            throws IllegalArgumentException {
        final CompareMode compareMode = getCompareMode(OPTION_COMPARE_MODE, commandLine,
                CompareMode.XML);
        final Path xmlBasePath = getPath(OPTION_BASE_REPORT_PATH, commandLine, null);
        final Path xmlPatchPath = getPath(OPTION_PATCH_REPORT_PATH, commandLine, null);
        final Path refFilesPath = getPath(OPTION_REFFILES_PATH, commandLine, null);
        final Path defaultResultPath = Paths.get(System.getProperty("user.home"))
                .resolve("XMLDiffGen_report_" + new SimpleDateFormat("yyyy.MM.dd_HH_mm_ss")
                        .format(Calendar.getInstance().getTime()));
        final Path outputPath = getPath(OPTION_OUTPUT_PATH, commandLine, defaultResultPath);
        final Path configBasePath = getPath(OPTION_BASE_CONFIG_PATH, commandLine, null);
        final Path configPatchPath = getPath(OPTION_PATCH_CONFIG_PATH, commandLine, null);
        final boolean shortFilePaths = commandLine.hasOption(OPTION_SHORT_PATHS);
        return new CliOptions(compareMode, xmlBasePath, xmlPatchPath, refFilesPath, outputPath,
                configBasePath, configPatchPath, shortFilePaths);
    }

    /**
     * Generates compare mode from CLI option.
     *
     * @param optionName
     *        name of the option.
     * @param commandLine
     *        parsed CLI.
     * @param defaultMode
     *        mode which is used if CLI option is absent.
     * @return compare mode.
     */
    private static CompareMode getCompareMode(String optionName, CommandLine commandLine,
            CompareMode defaultMode) {
        final CompareMode result;
        if (commandLine.hasOption(optionName)) {
            result = CompareMode.valueOf(commandLine.getOptionValue(optionName).toUpperCase());
        }
        else {
            result = defaultMode;
        }
        return result;
    }

    /**
     * Generates path from CLI option.
     *
     * @param optionName
     *        name of the option.
     * @param commandLine
     *        parsed CLI.
     * @param alternativePath
     *        path which is used if CLI option is absent.
     * @return generated path.
     */
    private static Path getPath(String optionName, CommandLine commandLine, Path alternativePath) {
        final Path path;
        if (commandLine.hasOption(optionName)) {
            path = Paths.get(commandLine.getOptionValue(optionName));
        }
        else {
            path = alternativePath;
        }
        return path;
    }

}
