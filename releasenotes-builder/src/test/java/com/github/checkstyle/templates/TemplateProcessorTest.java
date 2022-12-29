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

package com.github.checkstyle.templates;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.kohsuke.github.GHIssueState.CLOSED;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.checkstyle.MainProcess;
import com.github.checkstyle.internal.AbstractReleaseNotesTestSupport;

public class TemplateProcessorTest extends AbstractReleaseNotesTestSupport {
    @Test
    public void testGenerateOnlyBreakingCompatibility() throws Exception {
        createAllIssues(BREAKING);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("xdocBreakingCompatibility.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterBreakingCompatibility.txt", MainProcess.TWITTER_FILENAME);
        assertFile("githubPageBreakingCompatibility.txt", MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateOnlyNewFeature() throws Exception {
        createAllIssues(NEW_FEATURE);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("xdocNew.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterNew.txt", MainProcess.TWITTER_FILENAME);
        assertFile("githubPageNew.txt", MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateOnlyBug() throws Exception {
        createAllIssues(BUG);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("xdocBug.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterBug.txt", MainProcess.TWITTER_FILENAME);
        assertFile("githubPageBug.txt", MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateOnlyMisc() throws Exception {
        createAllIssues(MISC);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("xdocMisc.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterMisc.txt", MainProcess.TWITTER_FILENAME);
    }

    @Test
    public void testGenerateOnlyNewModule() throws Exception {
        createAllIssues(NEW_MODULE);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("xdocNew.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterNew.txt", MainProcess.TWITTER_FILENAME);
        assertFile("githubPageNew.txt", MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateAll() throws Exception {
        addCommit("Issue #1: Title 1", "Author 1");
        addCommit("Issue #2: Title 2", "Author 2");
        addCommit("Issue #3: Title 3", "Author 3");
        addCommit("Title 4", "Author 4");
        addCommit("Issue #5: Title 5", "Author 5");
        addIssue(1, CLOSED, "Title 1", BREAKING);
        addIssue(2, CLOSED, "Title 2", NEW_FEATURE);
        addIssue(3, CLOSED, "Title 3", BUG);
        addIssue(5, CLOSED, "Title 5", NEW_MODULE);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("xdocAll.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterAll.txt", MainProcess.TWITTER_FILENAME);
        assertFile("githubPageAll.txt", MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateOnlyXdoc() throws Exception {
        createAllIssues(BREAKING);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateXdoc",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("xdocBreakingCompatibility.txt", MainProcess.XDOC_FILENAME);
        assertFile(MainProcess.TWITTER_FILENAME);
        assertFile(MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateOnlyTwitter() throws Exception {
        createAllIssues(BREAKING);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateTwit",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile(MainProcess.XDOC_FILENAME);
        assertFile("twitterBreakingCompatibility.txt", MainProcess.TWITTER_FILENAME);
        assertFile(MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateOnlyGitHub() throws Exception {
        createAllIssues(BREAKING);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateGitHub",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile(MainProcess.XDOC_FILENAME);
        assertFile(MainProcess.TWITTER_FILENAME);
        assertFile("githubPageBreakingCompatibility.txt", MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGitHub() throws Exception {
        createAllIssues(BREAKING);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-generateGitHub",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("githubPageBreakingCompatibility.txt", MainProcess.GITHUB_FILENAME);
    }

    @Test
    public void testGenerateCustomTemplate() throws Exception {
        final File file = temporaryFolder.newFile("temp.template");
        FileUtils.writeStringToFile(file, "hello world", UTF_8);
        final String template = file.getAbsolutePath();

        createAllIssues(BREAKING, MISC, NEW_FEATURE, NEW_FEATURE, BUG);
        createAllCommits();

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "1.0.0",
            "-xdocTemplate", template,
            "-twitterTemplate", template,
            "-gitHubTemplate", template,
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());

        assertFile("customTemplate.txt", MainProcess.XDOC_FILENAME);
        assertFile("customTemplate.txt", MainProcess.TWITTER_FILENAME);
        assertFile("customTemplate.txt", MainProcess.GITHUB_FILENAME);
    }

    private static void createAllIssues(String... labels) {
        for (String label: labels) {
            addIssue(1, CLOSED, "Mock issue title 1", label);
            addIssue(2, CLOSED, "Mock issue title 2", label);
            addIssue(3, CLOSED, "Mock issue title 3", label);
            addIssue(4, CLOSED, "Mock issue title 4", label);
            addIssue(5, CLOSED, "Mock issue title 5 ==> test", label);
            addIssue(6, CLOSED, "Mock issue title 6 L12345678901234567890123456789012345678"
                + "90123456789012345678901234567890oooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooong'\"", label);
            addIssue(7, CLOSED, "Mock issue title 7 thisIssueTitleIsExactly87Characters"
                + "LongAndThenYouThe13ChrIndentation", label);
            addIssue(8, CLOSED, "Mock issue title 8 thisIssueTitleIsExactly100Characters"
                + "LongAndWeExpectItToGetWrappedDueToBeingTooLng", label);
            addIssue(9, CLOSED, "Mock issue title 9 escape @ and @@@@@", label);
            addIssue(10, CLOSED, "Mock issue title 10 escape < > & <&<>&<<", label);
        }
    }

    private static void createAllCommits() {
        addCommit("Issue #1: Mock issue title 1", "Author 1");
        addCommit("Issue #2: Mock issue title 2", "Author 3, Author 4");
        addCommit("Issue #3: Mock issue title 3", "Author 5");
        addCommit("Issue #4: Mock issue title 4", "Author 6, Author 7");
        addCommit("Issue #5: Mock issue title 5 ==> test", "Author 6, Author 7");
        addCommit("Issue #6: Mock issue title 6 L12345678901234567890123456789012345678"
            + "90123456789012345678901234567890oooooooooooooooooooooooooooooooooooooooooooooo"
            + "ooong'\"", "Author 1");
        addCommit("Issue #7: Mock issue title 7 thisIssueTitleIsExactly87Characters"
            + "LongAndThenYouThe13ChrIndentation", "Author 10");
        addCommit("Issue #8: Mock issue title 8 thisIssueTitleIsExactly100Characters"
            + "LongAndWeExpectItToGetWrappedDueToBeingTooLng", "Author 11");
        addCommit("Issue #9: Mock issue title 9 escape @ and @@@@@", "Author 12");
        addCommit("Issue #10: Mock issue title 10 escape < > & <&<>&<<", "Author 13");
    }
}
