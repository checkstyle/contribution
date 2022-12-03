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

import static org.junit.Assert.fail;
import static org.kohsuke.github.GHIssueState.OPEN;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.checkstyle.git.CsGit;
import com.github.checkstyle.github.CsGitHub;
import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.internal.GhIssueUtil;
import com.github.checkstyle.internal.RevCommitUtil;
import com.github.checkstyle.publishers.TwitterPublisher;

public class MainTest {
    private static final String MISC = Constants.MISCELLANEOUS_LABEL;

    private static final Set<RevCommit> TEST_COMMITS = new HashSet<>();

    private static final Set<GHIssue> TEST_ISSUES = new HashSet<>();

    private static final Answer<GHIssue> ISSUE_ANSWER = new Answer<GHIssue>() {
        @Override
        public GHIssue answer(InvocationOnMock invocation) throws Throwable {
            final int findIssue = invocation.getArgument(0);

            for (GHIssue item : TEST_ISSUES) {
                if (item.getNumber() == findIssue) {
                    return item;
                }
            }

            throw new GHFileNotFoundException("Cannot find Issue: " + findIssue);
        }
    };

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUp() throws IOException {
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

    @Before
    public void reset() {
        TEST_COMMITS.clear();
    }

    @Test
    public void testValidateVersion() {
        runMainAndAssertReturnCode(0,
                "-releaseNumber", "10.0.1",
                "-validateVersion"
        );
    }

    @Test
    public void testNoCommits() throws Exception {
        runMainAndAssertReturnCode(0,
                "-localRepoPath", temporaryFolder.newFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", temporaryFolder.newFolder().getAbsolutePath(),
                "-githubAuthToken", "TOKEN",
                "-generateAll",
                "-publishTwit",
                "-twitterConsumerKey", "KEY",
                "-twitterConsumerSecret", "SECRET",
                "-twitterAccessToken", "TOKEN",
                "-twitterAccessTokenSecret", "SECRET",
                "-validateVersion"
        );
    }

    @Test
    public void testUnknownCommit() throws Exception {
        TEST_COMMITS.add(RevCommitUtil.create("Hello World"));

        runMainAndAssertReturnCode(0,
                "-localRepoPath", temporaryFolder.newFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", temporaryFolder.newFolder().getAbsolutePath(),
                "-githubAuthToken", "TOKEN",
                "-generateAll",
                "-publishTwit",
                "-twitterConsumerKey", "KEY",
                "-twitterConsumerSecret", "SECRET",
                "-twitterAccessToken", "TOKEN",
                "-twitterAccessTokenSecret", "SECRET",
                "-validateVersion"
        );
    }

    @Test
    public void testIssueCommitWithIssueNotFound() throws IOException {
        TEST_COMMITS.add(RevCommitUtil.create("Issue #1: Hello World"));

        runMainAndAssertReturnCode(-2,
                "-localRepoPath", temporaryFolder.newFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", temporaryFolder.newFolder().getAbsolutePath(),
                "-githubAuthToken", "TOKEN",
                "-generateAll",
                "-publishTwit",
                "-twitterConsumerKey", "KEY",
                "-twitterConsumerSecret", "SECRET",
                "-twitterAccessToken", "TOKEN",
                "-twitterAccessTokenSecret", "SECRET",
                "-validateVersion"
        );
    }

    @Test
    public void testIssueCommit() throws Exception {
        TEST_COMMITS.add(RevCommitUtil.create("Issue #1: Hello World"));
        TEST_ISSUES.add(GhIssueUtil.create(1, OPEN, "Hello World", MISC));

        runMainAndAssertReturnCode(0,
                "-localRepoPath", temporaryFolder.newFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", temporaryFolder.newFolder().getAbsolutePath(),
                "-githubAuthToken", "TOKEN",
                "-generateAll",
                "-publishTwit",
                "-twitterConsumerKey", "KEY",
                "-twitterConsumerSecret", "SECRET",
                "-twitterAccessToken", "TOKEN",
                "-twitterAccessTokenSecret", "SECRET",
                "-validateVersion"
        );
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
    private static void runMainAndAssertReturnCode(int expectedExitCode, String... arguments) {
        final Runtime mock = mock(Runtime.class);
        try (MockedStatic<Runtime> runtime = mockStatic(Runtime.class)) {
            runtime.when(Runtime::getRuntime)
                    .thenReturn(mock);
            Main.main(arguments);
        }
        catch (Exception exception) {
            fail(String.format("Unexpected exception: %s", exception));
        }
        if (expectedExitCode == 0) {
            verify(mock, never()).exit(expectedExitCode);
        }
        else {
            verify(mock).exit(expectedExitCode);
        }
    }
}
