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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.github.checkstyle.publishers.MailingListPublisher;
import com.github.checkstyle.publishers.SourceforgeRssPublisher;
import com.github.checkstyle.publishers.TwitterPublisher;
import com.github.checkstyle.publishers.XdocPublisher;
import com.google.common.collect.Multimap;
import freemarker.template.TemplateException;

/**
 * Class for command line usage.
 * @author Andrei Selkin
 */
public final class Main {

    /** A path to remote checkstyle repository. */
    private static final String REMOTE_REPO_PATH = "checkstyle/checkstyle";

    /** Filename for a generated xdoc. */
    private static final String XDOC_FILENAME = "xdoc.xml";
    /** Filename for a generated Twitter post. */
    private static final String TWITTER_FILENAME = "twitter.txt";
    /** Filename for a generated Google Plus post. */
    private static final String GPLUS_FILENAME = "gplus.txt";
    /** Filename for a generated RSS post. */
    private static final String RSS_FILENAME = "rss.txt";
    /** Filename for a generated Mailing List post. */
    private static final String MLIST_FILENAME = "mailing_list.txt";

    /** FreeMarker xdoc template file name. */
    private static final String FREEMARKER_XDOC_TEMPLATE_FILE = "xdoc_freemarker.template";
    /** Twitter template file name. */
    private static final String TWITTER_TEMPLATE_FILE = "twitter.template";
    /** Google Plus template file name. */
    private static final String GPLUS_TEMPLATE_FILE = "gplus.template";
    /** RSS template file name. */
    private static final String RSS_TEMPLATE_FILE = "rss.template";
    /** Mailing List template file name. */
    private static final String MLIST_TEMPLATE_FILE = "mailing_list.template";

    /** Exit code returned when execution finishes with errors. */
    private static final int ERROR_EXIT_CODE = -2;

    /** Default constructor. */
    private Main() { }

    /**
     * Entry point.
     * @param args command line arguments.
     */
    public static void main(String... args) {
        int errorCounter;
        List<String> publicationErrors = null;
        try {
            final CliProcessor cliProcessor = new CliProcessor(args);
            cliProcessor.process();
            if (cliProcessor.hasErrors()) {
                printListOf(cliProcessor.getErrorMessages());
                errorCounter = cliProcessor.getErrorMessages().size();
            }
            else {
                final CliOptions cliOptions = cliProcessor.getCliOptions();
                final Result notesBuilderResult = runNotesBuilder(cliOptions);
                errorCounter = notesBuilderResult.getErrorMessages().size();
                if (errorCounter == 0) {
                    runPostGeneration(notesBuilderResult.getReleaseNotes(), cliOptions);
                    publicationErrors = runPostPublication(cliOptions);
                }
            }
        }
        catch (ParseException | GitAPIException | IOException | TemplateException ex) {
            errorCounter = 1;
            System.out.println(ex.getMessage());
            CliProcessor.printUsage();
        }
        if (errorCounter == 0) {
            if (publicationErrors != null && !publicationErrors.isEmpty()) {
                System.out.println(String.format("%nPublication ends with %d errors:",
                        publicationErrors.size()));
                printListOf(publicationErrors);
            }
            else {
                System.out.println(String.format("%nExecution succeeded!"));
            }
        }
        else {
            System.out.println(String.format("%nGeneration ends with %d errors.",
                errorCounter));
            System.exit(ERROR_EXIT_CODE);
        }
    }

    /**
     * Executes NotesBuilder based on passed command line options.
     * @param cliOptions command line options.
     * @return result of NotesBuilder work.
     * @throws IOException if an I/O error occurs.
     * @throws GitAPIException if an error occurs while accessing GitHub API.
     */
    private static Result runNotesBuilder(CliOptions cliOptions)
            throws IOException, GitAPIException {

        final String localRepoPath = cliOptions.getLocalRepoPath();
        final String startRef = cliOptions.getStartRef();
        final String endRef = cliOptions.getEndRef();
        final String authToken = cliOptions.getAuthToken();

        final GitHub connection;
        if (authToken == null) {
            connection = GitHub.connectAnonymously();
        }
        else {
            connection = GitHub.connectUsingOAuth(authToken);
        }

        final GHRepository remoteRepo = connection.getRepository(REMOTE_REPO_PATH);
        final Result result = NotesBuilder.buildResult(remoteRepo, localRepoPath, startRef, endRef);
        if (result.hasWarnings()) {
            printListOf(result.getWarningMessages());
        }
        if (result.hasErrors()) {
            printListOf(result.getErrorMessages());
        }
        return result;
    }

    /**
     * Generate posts and write them to files.
     * @param releaseNotes map of realeasenotes messages.
     * @param cliOptions command line options.
     * @throws IOException if I/O error occurs.
     * @throws TemplateException if an error occurs while generating freemarker template.
     */
    // -@cs[CyclomaticComplexity] This code is not complicated and is better to keep in one method
    private static void runPostGeneration(Multimap<String, ReleaseNotesMessage> releaseNotes,
            CliOptions cliOptions) throws IOException, TemplateException {

        final String releaseNumber = cliOptions.getReleaseNumber();
        final String outputLocation = cliOptions.getOutputLocation();
        final Map<String, Object> templateVariables =
                TemplateProcessor.getTemplateVariables(releaseNotes, releaseNumber);

        if (cliOptions.isGenerateAll() || cliOptions.isGenerateXdoc()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + XDOC_FILENAME, FREEMARKER_XDOC_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateTw()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + TWITTER_FILENAME, TWITTER_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateGplus()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + GPLUS_FILENAME, GPLUS_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateRss()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + RSS_FILENAME, RSS_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateMlist()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + MLIST_FILENAME, MLIST_TEMPLATE_FILE);
        }
    }

    /**
     * Publish social posts.
     * @param cliOptions command line options.
     * @return list of publication errors.
     */
    private static List<String> runPostPublication(CliOptions cliOptions) {
        final List<String> errors = new ArrayList<>();
        if (cliOptions.isPublishXdoc()) {
            runXdocPublication(cliOptions, errors);
        }
        if (cliOptions.isPublishAllSocial() || cliOptions.isPublishTwit()) {
            runTwitterPublication(cliOptions, errors);
        }
        if (cliOptions.isPublishAllSocial() || cliOptions.isPublishMlist()) {
            runMailingListPublication(cliOptions, errors);
        }
        if (cliOptions.isPublishAllSocial() || cliOptions.isPublishSfRss()) {
            runSfRssPublication(cliOptions, errors);
        }
        return errors;
    }

    /**
     * Publish on xdoc.
     * @param cliOptions command line options.
     * @param errors list of publication errors.
     */
    private static void runXdocPublication(CliOptions cliOptions, List<String> errors) {
        final XdocPublisher xdocPublisher = new XdocPublisher(
            cliOptions.getOutputLocation() + XDOC_FILENAME, cliOptions.getLocalRepoPath(),
            cliOptions.getReleaseNumber(), cliOptions.isPublishXdocWithPush(),
            cliOptions.getAuthToken());
        try {
            xdocPublisher.publish();
        }
        // -@cs[IllegalCatch] We should execute all publishers, so cannot fail-fast
        catch (Exception ex) {
            errors.add(ex.toString());
        }
    }

    /**
     * Publish on Twitter.
     * @param cliOptions command line options.
     * @param errors list of publication errors.
     */
    private static void runTwitterPublication(CliOptions cliOptions, List<String> errors) {
        final TwitterPublisher twitterPublisher = new TwitterPublisher(
            cliOptions.getOutputLocation() + TWITTER_FILENAME,
            cliOptions.getTwitterConsumerKey(), cliOptions.getTwitterConsumerSecret(),
            cliOptions.getTwitterAccessToken(), cliOptions.getTwitterAccessTokenSecret());
        try {
            twitterPublisher.publish();
        }
        // -@cs[IllegalCatch] We should execute all publishers, so cannot fail-fast
        catch (Exception ex) {
            errors.add(ex.toString());
        }
    }

    /**
     * Publish on mailing list.
     * @param cliOptions command line options.
     * @param errors list of publication errors.
     */
    private static void runMailingListPublication(CliOptions cliOptions, List<String> errors) {
        final MailingListPublisher mailingListPublisher = new MailingListPublisher(
            cliOptions.getOutputLocation() + MLIST_FILENAME, cliOptions.getMlistUsername(),
            cliOptions.getMlistPassword(), cliOptions.getReleaseNumber());
        try {
            mailingListPublisher.publish();
        }
        // -@cs[IllegalCatch] We should execute all publishers, so cannot fail-fast
        catch (Exception ex) {
            errors.add(ex.toString());
        }
    }

    /**
     * Publish on RSS.
     * @param cliOptions command line options.
     * @param errors list of publication errors.
     */
    private static void runSfRssPublication(CliOptions cliOptions, List<String> errors) {
        final SourceforgeRssPublisher rssPublisher = new SourceforgeRssPublisher(
                cliOptions.getOutputLocation() + RSS_FILENAME, cliOptions.getSfRssBearerToken(),
                cliOptions.getReleaseNumber());
        try {
            rssPublisher.publish();
        }
        // -@cs[IllegalCatch] We should execute all publishers, so cannot fail-fast
        catch (Exception ex) {
            errors.add(ex.toString());
        }
    }

    /**
     * Prints a list of elements in standard out.
     * @param entities a list.
     */
    private static void printListOf(List<String> entities) {
        System.out.println();
        for (String e : entities) {
            System.out.println(e);
        }
    }
}
