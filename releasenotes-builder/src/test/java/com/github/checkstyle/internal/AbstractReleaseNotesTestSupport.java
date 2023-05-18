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

package com.github.checkstyle.internal;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHRepository;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import com.github.checkstyle.Main;
import com.github.checkstyle.git.CsGit;
import com.github.checkstyle.github.CsGitHub;
import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.publishers.TwitterPublisher;

public abstract class AbstractReleaseNotesTestSupport extends AbstractPathTestSupport {

    protected static final String USAGE = String.format(Locale.ROOT,
        "usage:%n"
        + "java -jar releasenotes-builder-[version]-all.jar [options]%n"
        + " -help                             Whether to display help.%n"
        + " -localRepoPath <arg>              Path to a local git repository.%n"
        + " -remoteRepoPath <arg>             Path to a remote github repository.%n"
        + " -startRef <arg>                   Start reference to grab commits from.%n"
        + " -endRef <arg>                     End reference to stop grabbing the%n"
        + "                                   commits.%n"
        + " -releaseNumber <arg>              Release number.%n"
        + " -githubAuthToken <arg>            GitHub auth access token to establish%n"
        + "                                   connection.%n"
        + " -outputLocation <arg>             Location for output files.%n"
        + " -generateAll                      Whether all posts should be generated.%n"
        + " -generateXdoc                     Whether a xdoc should be generated.%n"
        + " -generateTwit                     Whether a twitter post should be%n"
        + "                                   generated.%n"
        + " -xdocTemplate <arg>               Path to xdoc template.%n"
        + " -twitterTemplate <arg>            Path to twitter template.%n"
        + " -generateGitHub                   Whether a github post should be%n"
        + "                                   generated.%n"
        + " -publishAllSocial                 Whether to publish all social posts.%n"
        + " -publishTwit                      Whether to publish a Twitter post.%n"
        + " -gitHubTemplate <arg>             Path to github page template.%n"
        + " -twitterConsumerKey <arg>         Consumer key for Twitter.%n"
        + " -twitterConsumerSecret <arg>      Consumer secret for Twitter.%n"
        + " -twitterAccessToken <arg>         Access token for Twitter.%n"
        + " -twitterAccessTokenSecret <arg>   Access token secret for Twitter.%n"
        + " -twitterProperties <arg>          Properties for publication on Twitter.%n"
        + " -validateVersion                  Whether release number should be%n"
        + "                                   validated to match release notes%n"
        + "                                   labels.%n");

    protected static final String BUG = Constants.BUG_LABEL;
    protected static final String NEW_FEATURE = Constants.NEW_FEATURE_LABEL;
    protected static final String NEW_MODULE = Constants.NEW_MODULE_LABEL;
    protected static final String MISC = Constants.MISCELLANEOUS_LABEL;
    protected static final String BREAKING = Constants.BREAKING_COMPATIBILITY_LABEL;

    protected static final String MSG_EXECUTION_SUCCEEDED = System.lineSeparator()
            + "Execution succeeded!" + System.lineSeparator();

    private static final Set<RevCommit> TEST_COMMITS = new LinkedHashSet<>();

    private static final Set<GHIssue> TEST_ISSUES = new LinkedHashSet<>();

    private static final Answer<GHIssue> ISSUE_ANSWER = invocation -> {
        final int findIssue = invocation.getArgument(0);
        for (GHIssue item : TEST_ISSUES) {
            if (item.getNumber() == findIssue) {
                return item;
            }
        }

        throw new GHFileNotFoundException("Cannot find Issue: " + findIssue);
    };

    private static boolean mocked;

    @Rule
    public final SystemErrRule systemErr = new SystemErrRule().enableLog().mute();
    @Rule
    public final SystemOutRule systemOut = new SystemOutRule().enableLog().mute();

    @BeforeClass
    public static void setUp() throws IOException {
        if (!mocked) {
            mocked = true;

            // GHRepository
            final GHRepository mockGhRepository = mock(GHRepository.class);
            when(mockGhRepository.getIssue(anyInt())).then(ISSUE_ANSWER);
            // CsGitHub static
            final MockedStatic<CsGitHub> mockCsGitHubStatic = mockStatic(CsGitHub.class);
            mockCsGitHubStatic.when(() -> CsGitHub.createRemoteRepo(anyString(), anyString()))
                    .thenReturn(mockGhRepository);
            // CsGit static
            final MockedStatic<CsGit> mockCsGitStatic = mockStatic(CsGit.class);
            mockCsGitStatic.when(
                    () -> CsGit.getCommitsBetweenReferences(anyString(), anyString(), anyString()))
                    .thenReturn(TEST_COMMITS);

            // TwitterPublisher static
            mockStatic(TwitterPublisher.class);
        }
    }

    @Before
    public void reset() {
        TEST_COMMITS.clear();
        TEST_ISSUES.clear();
    }

    /**
     * Adds a new commit for the current test.
     *
     * @param commitMessage The commit message to add.
     * @param author The commit author to add.
     */
    protected void addCommit(String commitMessage, String author) {
        TEST_COMMITS.add(RevCommitUtil.create(commitMessage, author));
    }

    /**
     * Adds a new issue for the current test.
     *
     * @param ghNumber The number of the issue.
     * @param ghState The state of the issue.
     * @param ghTitle The title of the issue.
     * @param labels The labels of the issue.
     */
    protected void addIssue(int ghNumber, GHIssueState ghState, String ghTitle,
            String... labels) {
        TEST_ISSUES.add(GhIssueUtil.create(ghNumber, ghState, ghTitle, labels));
    }

    /**
     * Retrieve a copy of all current commits.
     *
     * @return Copy of all current commits.
     */
    protected Set<RevCommit> getCommits() {
        return new LinkedHashSet<>(TEST_COMMITS);
    }

    /**
     * Retrieve a copy of all current issues.
     *
     * @return Copy of all current issues.
     */
    protected Set<GHIssue> getIssues() {
        return new LinkedHashSet<>(TEST_ISSUES);
    }

    /**
     * Helper method to run {@link Main#main(String...)} and verify the exit code.
     * Uses {@link Mockito#mockStatic(Class)} to mock method {@link Runtime#exit(int)}
     * to avoid VM termination.
     *
     * @param expectedExitCode the expected exit code to verify
     * @param arguments the command line arguments
     * @noinspection CallToSystemExit, ResultOfMethodCallIgnored
     * @noinspectionreason CallToSystemExit - test helper method requires workaround to
     *      verify exit code
     * @noinspectionreason ResultOfMethodCallIgnored - temporary suppression until #11589
     */
    protected void runMainAndAssertReturnCode(int expectedExitCode, String... arguments) {
        systemOut.clearLog();
        systemErr.clearLog();

        final Runtime mock = mock(Runtime.class);
        try (MockedStatic<Runtime> runtime = mockStatic(Runtime.class)) {
            runtime.when(Runtime::getRuntime)
                    .thenReturn(mock);
            Main.main(arguments);
        }
        catch (Exception exception) {
            fail(String.format("Unexpected exception: %s", exception));
        }
        finally {
            verify(mock).exit(expectedExitCode);
        }
    }

    /**
     * Helper method to call {@code runMainAndAssertReturnCode}
     * with some default required arguments.
     *
     * @param expectedExitCode the expected exit code to verify
     * @param arguments the command line arguments
     */
    protected void runMainContentGenerationAndAssertReturnCode(int expectedExitCode,
                                                               String... arguments) {
        final ArrayList<String> listOfArgs = new ArrayList<>(Arrays.asList(arguments));
        listOfArgs.addAll(
            List.of(
                "-localRepoPath", getTempFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-outputLocation", getTempFolder().getAbsolutePath(),
                "-githubAuthToken", "TOKEN"
            )
        );
        runMainAndAssertReturnCode(expectedExitCode, listOfArgs.toArray(new String[0]));
    }

    protected static String getExecutionFailedMessage(int amountOfErrors) {
        return System.lineSeparator()
            + "Generation ends with " + amountOfErrors + " errors."
            + System.lineSeparator();
    }

    protected static String getReleaseIsMinorMessage(String releaseVersion) {
        return System.lineSeparator()
            + "[ERROR] Validation of release number failed. "
            + "Release number is minor(" + releaseVersion + "), but release notes do not contain "
            + "'new' or 'breaking compatability' labels. Please correct release number by running "
            + "https://github.com/checkstyle/checkstyle/actions/workflows/bump-version-and-"
            + "update-milestone.yml"
            + System.lineSeparator();
    }

    protected static String getReleaseIsPatchMessage(String releaseVersion,
                                                     int... offendingIssues) {
        return System.lineSeparator()
            + "[ERROR] Validation of release number failed. "
            + "Release number is a patch(" + releaseVersion + "), but release notes contain 'new' "
            + "or 'breaking compatability' labels. Please correct release number by running "
            + "https://github.com/checkstyle/checkstyle/actions/workflows/bump-version-and-"
            + "update-milestone.yml . " + constructOffendingIssuesMessage(offendingIssues)
            + System.lineSeparator();
    }

    protected static String constructOffendingIssuesMessage(int... offendingIssues) {
        final StringBuilder builder = new StringBuilder("The offending issue(s): ");
        for (int issue : offendingIssues) {
            builder.append(constructGithubIssueLink(issue)).append(" ");
        }
        return builder.toString().trim();
    }

    protected static String constructGithubIssueLink(int issueNumber) {
        return String.format("https://github.com/checkstyle/checkstyle/issues/%d", issueNumber);
    }
}
