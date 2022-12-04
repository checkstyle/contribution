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

import static org.kohsuke.github.GHIssueState.OPEN;

import org.junit.Test;

import com.github.checkstyle.internal.AbstractReleaseNotesTestSupport;

public class MainTest extends AbstractReleaseNotesTestSupport {

    @Test
    public void testValidateVersion() {
        runMainAndAssertReturnCode(0,
                "-releaseNumber", "10.0.1",
                "-validateVersion"
        );
    }

    @Test
    public void testNoCommits() {
        runMainAndAssertReturnCode(0,
                "-localRepoPath", getTempFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", getTempFolder().getAbsolutePath(),
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
    public void testUnknownCommit() {
        addCommit("Hello World", "CheckstyleUser");

        runMainAndAssertReturnCode(0,
                "-localRepoPath", getTempFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", getTempFolder().getAbsolutePath(),
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
    public void testIssueCommitWithIssueNotFound() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");

        runMainAndAssertReturnCode(-2,
                "-localRepoPath", getTempFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", getTempFolder().getAbsolutePath(),
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
    public void testIssueCommit() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, OPEN, "Hello World", MISC);

        runMainAndAssertReturnCode(0,
                "-localRepoPath", getTempFolder().getAbsolutePath(),
                "-remoteRepoPath", "checkstyle/checkstyle",
                "-startRef", "12345678",
                "-releaseNumber", "10.0.1",
                "-outputLocation", getTempFolder().getAbsolutePath(),
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
}
