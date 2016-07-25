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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Helper class to process command line arguments for NotesBuilder.
 * @author Andrei Selkin
 */
public class CliProcessor {

    /** Name for the option 'localRepoPath'. */
    private static final String OPTION_LOCAL_REPO_PATH = "localRepoPath";
    /** Name for the option 'startRef'. */
    private static final String OPTION_START_REF = "startRef";
    /** Name for the option 'endRef'. */
    private static final String OPTION_END_REF = "endRef";
    /** Name for the option 'releaseNumber'. */
    private static final String OPTION_RELEASE_NUMBER = "releaseNumber";
    /** Name for the option 'outputFile'. */
    private static final String OPTION_OUTPUT_LOCATION = "outputLocation";
    /** Name for the option 'authToken'. */
    private static final String OPTION_AUTH_TOKEN = "authToken";
    /** Name for the option 'generateAll'. */
    private static final String OPTION_GENERATE_ALL = "generateAll";
    /** Name for the option 'generateXdoc'. */
    private static final String OPTION_GENERATE_XDOC = "generateXdoc";
    /** Name for the option 'generateTw'. */
    private static final String OPTION_GENERATE_TW = "generateTw";
    /** Name for the option 'generateGplus'. */
    private static final String OPTION_GENERATE_GPLUS = "generateGplus";
    /** Name for the option 'generateRss'. */
    private static final String OPTION_GENERATE_RSS = "generateRss";
    /** Name for the option 'generateMlist'. */
    private static final String OPTION_GENERATE_MLIST = "generateMlist";

    /** Command line cmdArgs. */
    private final String[] cmdArgs;
    /** Command line object. */
    private CommandLine cmdLine;
    /** Error messages. */
    private List<String> errorMessages;

    /**
     * Constructs CliProcessor object.
     * @param args command line cmdArgs.
     */
    public CliProcessor(String... args) {
        cmdArgs = args.clone();
        errorMessages = new ArrayList<>();
    }

    /**
     * Process command line arguments.
     * @throws ParseException if an error occurs while parsing command line arguments.
     */
    public void process() throws ParseException {
        final CommandLineParser clp = new DefaultParser();
        cmdLine = clp.parse(buildOptions(), cmdArgs);
        errorMessages = validateCli();
    }

    /**
     * Checks whether any errors occurred while processing command line arguments.
     * @return true if any errors occurred while processing command line arguments.
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    /**
     * Returns a list of error messages.
     * @return a list of error messages.
     */
    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    /**
     * Does validation of command line options.
     * @return list of violations.
     */
    private List<String> validateCli() {
        final List<String> result = new ArrayList<>();

        if (cmdLine.hasOption(OPTION_LOCAL_REPO_PATH)) {
            final String localGitRepositoryPath = cmdLine.getOptionValue(OPTION_LOCAL_REPO_PATH);
            if (!Files.isDirectory(Paths.get(localGitRepositoryPath))) {
                result.add(String.format("Could not find local git repository '%s'!",
                    localGitRepositoryPath));
            }
        }
        if (!cmdLine.hasOption(OPTION_START_REF)) {
            result.add("Start reference has not been specified!");
        }
        if (!cmdLine.hasOption(OPTION_RELEASE_NUMBER)) {
            result.add("Release number has not been specified!");
        }

        return result;
    }

    /**
     * Util method to convert CommandLine type to POJO object.
     * @return command line options as POJO object.
     */
    public CliOptions getCliOptions() {
        return CliOptions.newBuilder()
            .localRepoPath(cmdLine.getOptionValue(OPTION_LOCAL_REPO_PATH))
            .startRef(cmdLine.getOptionValue(OPTION_START_REF))
            .endRef(cmdLine.getOptionValue(OPTION_END_REF))
            .releaseNumber(cmdLine.getOptionValue(OPTION_RELEASE_NUMBER))
            .outputLocation(cmdLine.getOptionValue(OPTION_OUTPUT_LOCATION))
            .authToken(cmdLine.getOptionValue(OPTION_AUTH_TOKEN))
            .generateAll(cmdLine.hasOption(OPTION_GENERATE_ALL))
            .generateXdoc(cmdLine.hasOption(OPTION_GENERATE_XDOC))
            .generateTw(cmdLine.hasOption(OPTION_GENERATE_TW))
            .generateGplus(cmdLine.hasOption(OPTION_GENERATE_GPLUS))
            .generateRss(cmdLine.hasOption(OPTION_GENERATE_RSS))
            .generateMlist(cmdLine.hasOption(OPTION_GENERATE_MLIST))
            .build();
    }

    /**
     * Builds and returns list of parameters supported by cli Main.
     * @return available options.
     */
    private static Options buildOptions() {
        final Options options = new Options();
        options.addOption(OPTION_LOCAL_REPO_PATH, true, "Path to a local git repository.");
        options.addOption(OPTION_START_REF, true, "Start reference to grab commits from.");
        options.addOption(OPTION_END_REF, true, "End reference to stop grabbing the commits.");
        options.addOption(OPTION_RELEASE_NUMBER, true, "Release number.");
        options.addOption(OPTION_AUTH_TOKEN, true,
            "GitHub auth access token to establish connection.");
        options.addOption(OPTION_OUTPUT_LOCATION, true, "Location for output files.");
        options.addOption(OPTION_GENERATE_ALL, "Whether all posts should be generated.");
        options.addOption(OPTION_GENERATE_XDOC, "Whether a xdoc should be generated.");
        options.addOption(OPTION_GENERATE_TW, "Whether a twitter post should be generated.");
        options.addOption(OPTION_GENERATE_GPLUS, "Whether a google plus post should be generated.");
        options.addOption(OPTION_GENERATE_RSS, "Whether a RSS post should be generated.");
        options.addOption(OPTION_GENERATE_MLIST,
            "Whether a mailing list post should be generated.");
        return options;
    }

    /** Prints the usage information. */
    public static void printUsage() {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp("\njava -jar releasenotes-builder-[version]-all.jar [options]",
            buildOptions());
    }
}
