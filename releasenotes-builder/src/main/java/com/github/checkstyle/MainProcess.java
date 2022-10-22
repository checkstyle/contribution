///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.globals.ReleaseNotesMessage;
import com.github.checkstyle.publishers.MailingListPublisher;
import com.github.checkstyle.publishers.SourceforgeRssPublisher;
import com.github.checkstyle.publishers.TwitterPublisher;
import com.github.checkstyle.templates.TemplateProcessor;
import com.google.common.collect.Multimap;
import freemarker.template.TemplateException;

/** Main processing class of the release notes builder. */
public final class MainProcess {

    /** Filename for a generated xdoc. */
    public static final String XDOC_FILENAME = "xdoc.xml";
    /** Filename for a generated Twitter post. */
    public static final String TWITTER_FILENAME = "twitter.txt";
    /** Filename for a generated RSS post. */
    public static final String RSS_FILENAME = "rss.txt";
    /** Filename for a generated Mailing List post. */
    public static final String MLIST_FILENAME = "mailing_list.txt";
    /** Filename for a generated GitHub Post. */
    public static final String GITHUB_FILENAME = "github_post.txt";

    /** FreeMarker xdoc template file name. */
    public static final String FREEMARKER_XDOC_TEMPLATE_FILE =
        "com/github/checkstyle/templates/xdoc_freemarker.template";
    /** Twitter template file name. */
    public static final String TWITTER_TEMPLATE_FILE =
        "com/github/checkstyle/templates/twitter.template";
    /** RSS template file name. */
    public static final String RSS_TEMPLATE_FILE =
        "com/github/checkstyle/templates/rss.template";
    /** Mailing List template file name. */
    public static final String MLIST_TEMPLATE_FILE =
        "com/github/checkstyle/templates/mailing_list.template";
    /** GitHub Post template file name. */
    public static final String GITHUB_TEMPLATE_FILE =
        "com/github/checkstyle/templates/github_post.template";

    /** Default constructor. */
    private MainProcess() {
    }

    /**
     * Generate posts and publish them.
     *
     * @param releaseNotes map of release notes messages.
     * @param cliOptions command line options.
     * @return list of publication errors.
     * @throws IOException if I/O error occurs.
     * @throws TemplateException if an error occurs while generating freemarker template.
     */
    public static List<String> runPostGenerationAndPublication(
            Multimap<String, ReleaseNotesMessage> releaseNotes, CliOptions cliOptions)
            throws IOException, TemplateException {
        runPostGeneration(releaseNotes, cliOptions);
        return runPostPublication(cliOptions);
    }

    /**
     * Validate that pom version matches issues labels in release notes.
     *
     * @param releaseNotes map of release notes messages.
     * @param cliOptions command line options.
     * @return list of notes validation errors.
     */
    public static List<String> validateNotes(Multimap<String, ReleaseNotesMessage> releaseNotes,
                                              CliOptions cliOptions) {
        final String releaseVersion = cliOptions.getReleaseNumber();
        final long amountOfCommas = getAmountOfCommasInReleaseVersion(releaseVersion);
        final boolean containsNewOrBreakingCompatabilityLabel =
            releaseNotes.containsKey(Constants.NEW_FEATURE_LABEL)
                || releaseNotes.containsKey(Constants.NEW_MODULE_LABEL)
                || releaseNotes.containsKey(Constants.BREAKING_COMPATIBILITY_LABEL);

        final List<String> errors = new ArrayList<>();
        final String errorBeginning = "Validation of release number failed.";
        final String errorEnding = "Please correct release number by running https://github.com/"
            + "checkstyle/checkstyle/actions/workflows/bump-version-and-update-milestone.yml";

        if (isPatch(amountOfCommas) && containsNewOrBreakingCompatabilityLabel) {
            errors.add(
                String.format("%s Release number is a patch(%s), but release notes contain 'new' "
                        + "or 'breaking compatability' labels. %s",
                    errorBeginning, releaseVersion, errorEnding)
            );
        }
        else if (isMinor(amountOfCommas) && !containsNewOrBreakingCompatabilityLabel) {
            errors.add(
                String.format("%s Release number is minor(%s), but release notes do not contain "
                    + "'new' or 'breaking compatability' labels. %s",
                    errorBeginning, releaseVersion, errorEnding)
            );
        }

        return errors;
    }

    /**
     * Retrieves the amount of commas in the release version. For instance,
     * 10.3.3 would return {@code 2}.
     *
     * @param releaseVersion release number in the command line interface.
     * @return amount of commas in release version.
     */
    private static long getAmountOfCommasInReleaseVersion(String releaseVersion) {
        return releaseVersion.chars().filter(character -> character == '.').count();
    }

    /**
     * Returns {@code true} if amount of commas is 2. This means
     * the release is a patch.
     *
     * @param amountOfCommas amount of commas in release number.
     * @return {@code true} if amount of commas is 2.
     */
    private static boolean isPatch(long amountOfCommas) {
        return amountOfCommas == 2;
    }

    /**
     * Returns {@code true} if amount of commas is 1. This means
     * the release is minor.
     *
     * @param amountOfCommas amount of commas in release number.
     * @return {@code true} if amount of commas is 1.
     */
    private static boolean isMinor(long amountOfCommas) {
        return amountOfCommas == 1;
    }

    /**
     * Generate posts and write them to files.
     *
     * @param releaseNotes map of release notes messages.
     * @param cliOptions command line options.
     * @throws IOException if I/O error occurs.
     * @throws TemplateException if an error occurs while generating freemarker template.
     */
    // -@cs[CyclomaticComplexity|NPathComplexity] This code is not complicated
    // and is better to keep in one method
    private static void runPostGeneration(Multimap<String, ReleaseNotesMessage> releaseNotes,
            CliOptions cliOptions) throws IOException, TemplateException {

        final String remoteRepoPath = cliOptions.getRemoteRepoPath();
        final String releaseNumber = cliOptions.getReleaseNumber();
        final String outputLocation = cliOptions.getOutputLocation();
        final Map<String, Object> templateVariables = TemplateProcessor.getTemplateVariables(
                releaseNotes, remoteRepoPath, releaseNumber);

        if (cliOptions.isGenerateAll() || cliOptions.isGenerateXdoc()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + XDOC_FILENAME, cliOptions.getXdocTemplate(),
                    FREEMARKER_XDOC_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateTw()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + TWITTER_FILENAME, cliOptions.getTwitterTemplate(),
                    TWITTER_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateRss()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + RSS_FILENAME, cliOptions.getRssTemplate(),
                    RSS_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateMlist()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + MLIST_FILENAME, cliOptions.getMlistTemplate(),
                    MLIST_TEMPLATE_FILE);
        }
        if (cliOptions.isGenerateAll() || cliOptions.isGenerateGitHub()) {
            TemplateProcessor.generateWithFreemarker(templateVariables,
                    outputLocation + GITHUB_FILENAME, cliOptions.getGitHubTemplate(),
                    GITHUB_TEMPLATE_FILE);
        }
    }

    /**
     * Publish social posts.
     *
     * @param cliOptions command line options.
     * @return list of publication errors.
     */
    private static List<String> runPostPublication(CliOptions cliOptions) {
        final List<String> errors = new ArrayList<>();
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
     * Publish on Twitter.
     *
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
     *
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
     *
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

}
