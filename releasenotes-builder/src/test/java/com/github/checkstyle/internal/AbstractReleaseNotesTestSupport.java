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

package com.github.checkstyle.internal;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.LinkedHashSet;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.github.checkstyle.Main;
import com.github.checkstyle.git.CsGit;
import com.github.checkstyle.github.CsGitHub;
import com.github.checkstyle.publishers.TwitterPublisher;

public abstract class AbstractReleaseNotesTestSupport extends AbstractPathTestSupport {

    private static final Set<RevCommit> TEST_COMMITS = new LinkedHashSet<>();

    private static final Set<GHIssue> TEST_ISSUES = new LinkedHashSet<>();

    private static final Answer<GHIssue> ISSUE_ANSWER = new Answer<>() {
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
     * @return {@code true} if the commit added is new.
     */
    protected boolean addCommit(String commitMessage, String author) {
        return TEST_COMMITS.add(RevCommitUtil.create(commitMessage, author));
    }

    /**
     * Adds a new issue for the current test.
     *
     * @param ghNumber The number of the issue.
     * @param ghState The state of the issue.
     * @param ghTitle The title of the issue.
     * @param labels The labels of the issue.
     * @return {@code true} if the issue added is new.
     */
    protected boolean addIssue(int ghNumber, GHIssueState ghState, String ghTitle,
            String... labels) {
        return TEST_ISSUES.add(GhIssueUtil.create(ghNumber, ghState, ghTitle, labels));
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
        if (expectedExitCode == 0) {
            verify(mock, never()).exit(expectedExitCode);
        }
        else {
            verify(mock).exit(expectedExitCode);
        }
    }

}
