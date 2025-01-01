///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.globals.ReleaseNotesMessage;
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
    /** Filename for a generated GitHub Post. */
    public static final String GITHUB_FILENAME = "github_post.txt";

    /** FreeMarker xdoc template file name. */
    public static final String FREEMARKER_XDOC_TEMPLATE_FILE =
        "com/github/checkstyle/templates/xdoc_freemarker.template";
    /** Twitter template file name. */
    public static final String TWITTER_TEMPLATE_FILE =
        "com/github/checkstyle/templates/twitter.template";
    /** GitHub Post template file name. */
    public static final String GITHUB_TEMPLATE_FILE =
        "com/github/checkstyle/templates/github_post.template";

    /** Checkstyle supports only 3 digit versions for now. */
    public static final String RELEASE_NUMBER_PATTERN = "^\\d+\\.\\d+\\.\\d+$";

    /** Compiled pattern for release number. */
    private static final Pattern RELEASE_PATTERN = Pattern.compile(RELEASE_NUMBER_PATTERN);

    /** Default constructor. */
    private MainProcess() {
    }

    /**
     * Generate posts and publish them.
     *
     * @param releaseNotes map of release notes messages.
     * @param cliOptions command line options.
     * @param shouldRunPublication if post publication should be run
     * @return list of publication errors.
     * @throws IOException if I/O error occurs.
     * @throws TemplateException if an error occurs while generating freemarker template.
     */
    public static List<String> runPostGenerationAndPublication(
            Multimap<String, ReleaseNotesMessage> releaseNotes, CliOptions cliOptions,
            boolean shouldRunPublication)
            throws IOException, TemplateException {
        runPostGeneration(releaseNotes, cliOptions);
        final List<String> errors = new ArrayList<>();
        if (cliOptions.isValidateVersion()) {
            errors.addAll(validateNotes(releaseNotes, cliOptions));
        }
        if (shouldRunPublication && errors.isEmpty()) {
            errors.addAll(runPostPublication(cliOptions));
        }
        return errors;
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
        final boolean containsNewOrBreakingCompatabilityLabel =
            releaseNotes.containsKey(Constants.NEW_FEATURE_LABEL)
                || releaseNotes.containsKey(Constants.NEW_MODULE_LABEL)
                || releaseNotes.containsKey(Constants.BREAKING_COMPATIBILITY_LABEL);

        final List<String> errors = new ArrayList<>();

        if (!RELEASE_PATTERN.matcher(releaseVersion).find()) {
            errors.add("Release number should match pattern " + RELEASE_NUMBER_PATTERN);
        }

        final String errorBeginning = "[ERROR] Validation of release number failed.";
        final String errorEnding = "Please correct release number by running https://github.com/"
            + "checkstyle/checkstyle/actions/workflows/bump-version-and-update-milestone.yml";

        if (isPatch(releaseVersion) && containsNewOrBreakingCompatabilityLabel) {
            final String offendingIssues = getOffendingIssues(releaseNotes);
            errors.add(
                String.format("%s Release number is a patch(%s), but release notes contain 'new' "
                        + "or 'breaking compatability' labels. %s . The offending issue(s): %s",
                    errorBeginning, releaseVersion, errorEnding, offendingIssues)
            );
        }
        else if (isMinor(releaseVersion) && !containsNewOrBreakingCompatabilityLabel) {
            errors.add(
                String.format("%s Release number is minor(%s), but release notes do not contain "
                    + "'new' or 'breaking compatability' labels. %s",
                    errorBeginning, releaseVersion, errorEnding)
            );
        }

        return errors;
    }

    /**
     * Construct a string of links to offending issues separated by a space. An offending issue is
     * one which is labeled as 'new' or 'breaking compatability' when the release is supposed to
     * be a patch.
     *
     * @param releaseNotes map of release notes messages.
     * @return a string with offending issues.
     */
    private static String getOffendingIssues(
        Multimap<String, ReleaseNotesMessage> releaseNotes) {
        final Set<String> offendingIssues = new HashSet<>();
        for (Map.Entry<String, ReleaseNotesMessage> entry : releaseNotes.entries()) {
            final String issueLabel = entry.getKey();
            final boolean isOffendingIssue = issueLabel.equals(Constants.NEW_FEATURE_LABEL)
                || issueLabel.equals(Constants.NEW_MODULE_LABEL)
                || issueLabel.equals(Constants.BREAKING_COMPATIBILITY_LABEL);
            if (isOffendingIssue) {
                offendingIssues.add(String.format(
                    "https://github.com/checkstyle/checkstyle/issues/%d",
                    entry.getValue().getIssueNo()
                ));
            }
        }
        return String.join(" ", offendingIssues);
    }

    /**
     * Check if a release version is minor. A release version is minor when
     * it ends with a zero(0), for example 10.4.0.
     *
     * @param releaseVersion checkstyle release version.
     * @return {@code true} if release version is minor.
     */
    private static boolean isMinor(String releaseVersion) {
        return endsWithZero(releaseVersion);
    }

    /**
     * Check if a release version is minor. A release version is minor when
     * it ends with any other digit but a zero(0), for example 10.4.1.
     *
     * @param releaseVersion checkstyle release version.
     * @return {@code true} if release version is patch.
     */
    private static boolean isPatch(String releaseVersion) {
        return !endsWithZero(releaseVersion);
    }

    /**
     * Check if provided string ends with a zero(0).
     *
     * @param str string to check.
     * @return {@code true} if string to check ends with a zero(0).
     */
    private static boolean endsWithZero(String str) {
        return !str.isEmpty() && str.charAt(str.length() - 1) == '0';
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
        return errors;
    }

    /**
     * Publish on Twitter.
     *
     * @param cliOptions command line options.
     * @param errors list of publication errors.
     */
    private static void runTwitterPublication(CliOptions cliOptions, List<String> errors) {
        try {
            final String post = Files.readString(
                    Paths.get(cliOptions.getOutputLocation() + TWITTER_FILENAME),
                    StandardCharsets.UTF_8);

            TwitterPublisher.publish(cliOptions.getTwitterConsumerKey(),
                    cliOptions.getTwitterConsumerSecret(), cliOptions.getTwitterAccessToken(),
                    cliOptions.getTwitterAccessTokenSecret(), post);
        }
        // -@cs[IllegalCatch] We should execute all publishers, so cannot fail-fast
        catch (Exception ex) {
            errors.add(ex.toString());
        }
    }

}
