////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2018 the original author or authors.
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

    /** Name for the option 'twitterConsumerKey'. */
    public static final String OPTION_TWITTER_CONSUMER_KEY = "twitterConsumerKey";
    /** Name for the option 'twitterConsumerSecret'. */
    public static final String OPTION_TWITTER_CONSUMER_SECRET = "twitterConsumerSecret";
    /** Name for the option 'twitterAccessToken'. */
    public static final String OPTION_TWITTER_ACCESS_TOKEN = "twitterAccessToken";
    /** Name for the option 'twitterAccessTokenSecret'. */
    public static final String OPTION_TWITTER_ACCESS_TOKEN_SECRET = "twitterAccessTokenSecret";

    /** Name for the option 'mlistUsername'. */
    public static final String OPTION_MLIST_USERNAME = "mlistUsername";
    /** Name for the option 'mlistPassword'. */
    public static final String OPTION_MLIST_PASSWORD = "mlistPassword";

    /** Name for the option 'sfRssBearerToken'. */
    public static final String OPTION_SF_RSS_BEARER_TOKEN = "sfRssBearerToken";

    /** Name for the option 'localRepoPath'. */
    private static final String OPTION_LOCAL_REPO_PATH = "localRepoPath";
    /** Name for the option 'remoteRepoPath'. */
    private static final String OPTION_REMOTE_REPO_PATH = "remoteRepoPath";
    /** Name for the option 'startRef'. */
    private static final String OPTION_START_REF = "startRef";
    /** Name for the option 'endRef'. */
    private static final String OPTION_END_REF = "endRef";
    /** Name for the option 'releaseNumber'. */
    private static final String OPTION_RELEASE_NUMBER = "releaseNumber";
    /** Name for the option 'outputFile'. */
    private static final String OPTION_OUTPUT_LOCATION = "outputLocation";
    /** Name for the option 'githubAuthToken'. */
    private static final String OPTION_AUTH_TOKEN = "githubAuthToken";

    /** Name for the option 'generateAll'. */
    private static final String OPTION_GENERATE_ALL = "generateAll";
    /** Name for the option 'generateXdoc'. */
    private static final String OPTION_GENERATE_XDOC = "generateXdoc";
    /** Name for the option 'generateTw'it. */
    private static final String OPTION_GENERATE_TW = "generateTwit";
    /** Name for the option 'generateGplus'. */
    private static final String OPTION_GENERATE_GPLUS = "generateGplus";
    /** Name for the option 'generateRss'. */
    private static final String OPTION_GENERATE_RSS = "generateRss";
    /** Name for the option 'generateMlist'. */
    private static final String OPTION_GENERATE_MLIST = "generateMlist";

    /** Name for the option 'publishAllSocial'. */
    private static final String OPTION_PUBLISH_ALL_SOCIAL = "publishAllSocial";
    /** Name for the option 'publishTwit'. */
    private static final String OPTION_PUBLISH_TWIT = "publishTwit";
    /** Name for the option 'publishXdoc'. */
    private static final String OPTION_PUBLISH_XDOC = "publishXdoc";
    /** Name for the option 'publishXdocWithPush'. */
    private static final String OPTION_PUBLISH_XDOC_WITH_PUSH = "publishXdocWithPush";
    /** Name for the option 'publishMlist'. */
    private static final String OPTION_PUBLISH_MLIST = "publishMlist";
    /** Name for the option 'publishSfRss'. */
    private static final String OPTION_PUBLISH_SF_RSS = "publishSfRss";

    /** Name for the option 'twitterProperties'. */
    private static final String OPTION_TWITTER_PROPERTIES = "twitterProperties";
    /** Name for the option 'mlistProperties'. */
    private static final String OPTION_MLIST_PROPERTIES = "mlistProperties";
    /** Name for the option 'sfRssProperties'. */
    private static final String OPTION_SF_RSS_PROPERTIES = "sfRssProperties";

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
        if (!cmdLine.hasOption(OPTION_REMOTE_REPO_PATH)) {
            result.add("Remote repository path has not been specified!");
        }
        if (!cmdLine.hasOption(OPTION_START_REF)) {
            result.add("Start reference has not been specified!");
        }
        if (!cmdLine.hasOption(OPTION_RELEASE_NUMBER)) {
            result.add("Release number has not been specified!");
        }

        if (cmdLine.hasOption(OPTION_PUBLISH_XDOC_WITH_PUSH)
                && cmdLine.getOptionValue(OPTION_AUTH_TOKEN) == null) {
            result.add("Auth token should not be null for xdoc publication with push.");
        }

        return result;
    }

    /**
     * Util method to convert CommandLine type to POJO object.
     * @return command line options as POJO object.
     */
    public CliOptions getCliOptions() {
        return CliOptions.newBuilder()
            .setLocalRepoPath(cmdLine.getOptionValue(OPTION_LOCAL_REPO_PATH))
            .setRemoteRepoPath(cmdLine.getOptionValue(OPTION_REMOTE_REPO_PATH))
            .setStartRef(cmdLine.getOptionValue(OPTION_START_REF))
            .setEndRef(cmdLine.getOptionValue(OPTION_END_REF))
            .setReleaseNumber(cmdLine.getOptionValue(OPTION_RELEASE_NUMBER))
            .setOutputLocation(cmdLine.getOptionValue(OPTION_OUTPUT_LOCATION))
            .setAuthToken(cmdLine.getOptionValue(OPTION_AUTH_TOKEN))
            .setGenerateAll(cmdLine.hasOption(OPTION_GENERATE_ALL))
            .setGenerateXdoc(cmdLine.hasOption(OPTION_GENERATE_XDOC))
            .setGenerateTw(cmdLine.hasOption(OPTION_GENERATE_TW))
            .setGenerateGplus(cmdLine.hasOption(OPTION_GENERATE_GPLUS))
            .setGenerateRss(cmdLine.hasOption(OPTION_GENERATE_RSS))
            .setGenerateMlist(cmdLine.hasOption(OPTION_GENERATE_MLIST))
            .setPublishAllSocial(cmdLine.hasOption(OPTION_PUBLISH_ALL_SOCIAL))
            .setPublishTwit(cmdLine.hasOption(OPTION_PUBLISH_TWIT))
            .setPublishXdoc(cmdLine.hasOption(OPTION_PUBLISH_XDOC))
            .setPublishXdocWithPush(cmdLine.hasOption(OPTION_PUBLISH_XDOC_WITH_PUSH))
            .setTwitterConsumerKey(cmdLine.getOptionValue(OPTION_TWITTER_CONSUMER_KEY))
            .setTwitterConsumerSecret(cmdLine.getOptionValue(OPTION_TWITTER_CONSUMER_SECRET))
            .setTwitterAccessToken(cmdLine.getOptionValue(OPTION_TWITTER_ACCESS_TOKEN))
            .setTwitterAccessTokenSecret(
                cmdLine.getOptionValue(OPTION_TWITTER_ACCESS_TOKEN_SECRET))
            .setTwitterProperties(cmdLine.getOptionValue(OPTION_TWITTER_PROPERTIES))
            .setPublishMlist(cmdLine.hasOption(OPTION_PUBLISH_MLIST))
            .setMlistUsername(cmdLine.getOptionValue(OPTION_MLIST_USERNAME))
            .setMlistPassword(cmdLine.getOptionValue(OPTION_MLIST_PASSWORD))
            .setMlistProperties(cmdLine.getOptionValue(OPTION_MLIST_PROPERTIES))
            .setPublishSfRss(cmdLine.hasOption(OPTION_PUBLISH_SF_RSS))
            .setSfBearerToken(cmdLine.getOptionValue(OPTION_SF_RSS_BEARER_TOKEN))
            .setSfRssProperties(cmdLine.getOptionValue(OPTION_SF_RSS_PROPERTIES))
            .build();
    }

    /**
     * Builds and returns list of parameters supported by cli Main.
     * @return available options.
     */
    private static Options buildOptions() {
        final Options options = new Options();
        options.addOption(OPTION_LOCAL_REPO_PATH, true, "Path to a local git repository.");
        options.addOption(OPTION_REMOTE_REPO_PATH, true, "Path to a remote github repository.");
        options.addOption(OPTION_START_REF, true, "Start reference to grab commits from.");
        options.addOption(OPTION_END_REF, true, "End reference to stop grabbing the commits.");
        options.addOption(OPTION_RELEASE_NUMBER, true, "Release number.");
        options.addOption(OPTION_AUTH_TOKEN, true,
            "GitHub auth access token to establish connection.");
        options.addOption(OPTION_OUTPUT_LOCATION, true, "Location for output files.");
        options.addOption(OPTION_GENERATE_ALL, "Whether all posts should be generated.");
        options.addOption(OPTION_GENERATE_XDOC, "Whether a xdoc should be generated.");
        options.addOption(OPTION_GENERATE_TW, "Whether a twitter post should be generated.");
        options.addOption(OPTION_GENERATE_GPLUS,
            "Whether a google plus post should be generated.");
        options.addOption(OPTION_GENERATE_RSS, "Whether a RSS post should be generated.");
        options.addOption(OPTION_GENERATE_MLIST,
            "Whether a mailing list post should be generated.");
        options.addOption(OPTION_PUBLISH_ALL_SOCIAL, "Whether to publish all social posts");
        options.addOption(OPTION_PUBLISH_TWIT, "Whether to publish a Twitter post.");
        options.addOption(OPTION_TWITTER_CONSUMER_KEY, true, "Consumer key for Twitter.");
        options.addOption(OPTION_TWITTER_CONSUMER_SECRET, true, "Consumer secret for Twitter.");
        options.addOption(OPTION_TWITTER_ACCESS_TOKEN, true, "Access token for Twitter.");
        options.addOption(OPTION_TWITTER_ACCESS_TOKEN_SECRET, true,
            "Access token secret for Twitter.");
        options.addOption(OPTION_TWITTER_PROPERTIES, true,
            "Properties for publication on Twitter.");
        options.addOption(OPTION_PUBLISH_XDOC, "Whether to publish xdoc.");
        options.addOption(OPTION_PUBLISH_XDOC_WITH_PUSH, "Whether to publish xdoc with push.");
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
