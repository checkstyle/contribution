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

package com.puppycrawl.tools.socialposts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * Class for command line managing.
 * @author Vladislav Lisetskii
 */
public final class Main {

    /** Name for the option 'r'. */
    public static final String OPTION_R = "r";

    /** Name for the option 'o'. */
    public static final String OPTION_O = "o";

    /** Name for the option 'generateAll'. */
    public static final String OPTION_GENERATE_ALL = "generateAll";

    /** Name for the option 'publishAll'. */
    public static final String OPTION_PUBLISH_ALL = "publishAll";

    /** Name for the option 'generateTw'. */
    public static final String OPTION_GENERATE_TW = "generateTw";

    /** Name for the option 'publishTw'. */
    public static final String OPTION_PUBLISH_TW = "publishTw";

    /** Name for the option 'consKeyTw'. */
    public static final String OPTION_CONS_KEY_TW = "consKeyTw";

    /** Name for the option 'consSecretTw'. */
    public static final String OPTION_CONS_SECRET_TW = "consSecretTw";

    /** Name for the option 'accessTokenTw'. */
    public static final String OPTION_ACCESS_TOKEN_TW = "accessTokenTw";

    /** Name for the option 'accessTokenSecretTw'. */
    public static final String OPTION_ACCESS_TOKEN_SECRET_TW = "accessTokenSecretTw";

    /** Name for the option 'accessTokenSecretTw'. */
    public static final String OPTION_PROP_TW = "propTw";

    /** Prevent instantiating. */
    private Main() { }

    /**
     * Enter point.
     * @param args the command line arguments.
     * @throws IOException if there is a problem with files access.
     * @throws JDOMException if there is a problem with xml structure.
     */
    public static void main(String... args) throws IOException, JDOMException {
        int exitStatus = 0;
        final int exitWithCliViolation = -1;

        try {
            final CommandLine commandLine = parseCli(args);
            final List<String> messages = validateCli(commandLine);
            if (messages.isEmpty()) {
                runCli(commandLine);
            }
            else {
                exitStatus = exitWithCliViolation;
                messages.forEach(System.out::println);
            }
        }
        catch (ParseException pex) {
            exitStatus = exitWithCliViolation;
            System.out.println(pex.getMessage());
        }
        finally {
            if (exitStatus != 0) {
                System.exit(exitStatus);
            }
        }
    }

    /**
     * Parses command line parameters.
     * @param args command line parameters.
     * @return parsed information about passed parameters.
     * @throws ParseException when parsed arguments are not valid.
     */
    private static CommandLine parseCli(String... args) throws ParseException {
        final CommandLineParser clp = new DefaultParser();
        return clp.parse(buildOptions(), args);
    }

    /**
     * Do validation of Command line options.
     * @param commandLine command line object
     * @return list of violations
     */
    private static List<String> validateCli(CommandLine commandLine) {
        final List<String> result = new ArrayList<>();
        if (!commandLine.hasOption(OPTION_R)) {
            result.add("A release notes input file must be specified.");
        }
        if (commandLine.hasOption(OPTION_PUBLISH_TW)
                && !commandLine.hasOption(OPTION_PROP_TW)
                    && (!commandLine.hasOption(OPTION_ACCESS_TOKEN_TW)
                        || !commandLine.hasOption(OPTION_ACCESS_TOKEN_SECRET_TW)
                        || !commandLine.hasOption(OPTION_CONS_KEY_TW)
                        || !commandLine.hasOption(OPTION_CONS_SECRET_TW))) {
            result.add("Authorization information for publishing on "
                + "Twitter is not full: consumer key, consumer secret, "
                + "access token and access secret token are expected");
        }
        return result;
    }

    /**
     * Do execution of PostsBuilder based on Command line options.
     * @param commandLine command line object
     * @throws IOException if a file could not be read.
     * @throws JDOMException if there is a problem with xml structure.
     */
    private static void runCli(CommandLine commandLine) throws JDOMException, IOException {
        final CliOptions config = convertCliToPojo(commandLine);

        final File input = new File(config.getReleaseNotes());
        final SAXBuilder builder = new SAXBuilder();

        final Document document = builder.build(input);
        final Element rootNode = document.getRootElement();

        final PostBuilder postBuilder = new PostBuilder(rootNode, config);
        postBuilder.generatePosts();
        postBuilder.publishPosts();
    }

    /**
     * Util method to convert CommandLine type to POJO object.
     * @param commandLine command line object.
     * @return command line option as a POJO object.
     */
    private static CliOptions convertCliToPojo(CommandLine commandLine) {
        return CliOptions.newBuilder()
                .releaseNotes(commandLine.getOptionValue(OPTION_R))
                .outputLocation(commandLine.getOptionValue(OPTION_O))
                .generateAll(commandLine.hasOption(OPTION_GENERATE_ALL))
                .publishAll(commandLine.hasOption(OPTION_PUBLISH_ALL))
                .generateTw(commandLine.hasOption(OPTION_GENERATE_TW))
                .publishTw(commandLine.hasOption(OPTION_PUBLISH_TW))
                .consKeyTw(commandLine.getOptionValue(OPTION_CONS_KEY_TW))
                .consSecretTw(commandLine.getOptionValue(OPTION_CONS_SECRET_TW))
                .accessTokenTw(commandLine.getOptionValue(OPTION_ACCESS_TOKEN_TW))
                .accessTokenSecretTw(commandLine.getOptionValue(OPTION_ACCESS_TOKEN_SECRET_TW))
                .propTw(commandLine.getOptionValue(OPTION_PROP_TW))
                .build();
    }

    /**
     * Builds and returns list of parameters supported by cli.
     * @return available options.
     */
    private static Options buildOptions() {
        final Options options = new Options();
        options.addOption(OPTION_R, true, "Sets the path to the input release notes file.");
        options.addOption(OPTION_O, true, "Sets the result destination.");
        options.addOption(OPTION_GENERATE_ALL, "Generate all posts.");
        options.addOption(OPTION_PUBLISH_ALL, "Publish all posts.");
        options.addOption(OPTION_GENERATE_TW, "Generate a post for Twitter.");
        options.addOption(OPTION_PUBLISH_TW, "Publish a post on Twitter.");
        options.addOption(OPTION_CONS_KEY_TW, true, "Sets the consumer key for Twitter.");
        options.addOption(OPTION_CONS_SECRET_TW, true, "Sets the consumer secret for Twitter.");
        options.addOption(OPTION_ACCESS_TOKEN_TW, true, "Sets the access token for Twitter.");
        options.addOption(OPTION_ACCESS_TOKEN_SECRET_TW, true,
                "Sets the access token secret for Twitter.");
        options.addOption(OPTION_PROP_TW, true, "Sets the path to the properties with "
                + "authorization information for Twitter.");
        return options;
    }
}
