///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2026 the original author or authors.
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

import static com.github.checkstyle.MainProcess.RELEASE_NUMBER_PATTERN;
import static org.kohsuke.github.GHIssueState.CLOSED;

import org.junit.Assert;
import org.junit.Test;

import com.github.checkstyle.internal.AbstractReleaseNotesTestSupport;

public class MainTest extends AbstractReleaseNotesTestSupport {
    @Test
    public void test() {
        runMainAndAssertReturnCode(0);

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", USAGE + MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testHelp() {
        runMainAndAssertReturnCode(0, "-help");

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", USAGE + MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testNoCommits() {
        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testUnknownCommit() {
        addCommit("Hello World", "CheckstyleUser");

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testBadVersion3Dots() {
        addCommit("Hello World", "CheckstyleUser");

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.1.",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1)
                + System.lineSeparator()
                + "Release number should match pattern " + RELEASE_NUMBER_PATTERN
                + System.lineSeparator(),
            systemOut.getLog());
    }

    @Test
    public void testBadVersionWithChars() {
        addCommit("Hello World", "CheckstyleUser");

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.p.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1)
                + System.lineSeparator()
                + "Release number should match pattern " + RELEASE_NUMBER_PATTERN
                + System.lineSeparator(),
            systemOut.getLog());
    }

    @Test
    public void testBadVersion2Dots() {
        addCommit("Hello World", "CheckstyleUser");

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1)
                + System.lineSeparator()
                + "Release number should match pattern " + RELEASE_NUMBER_PATTERN
                + System.lineSeparator(),
            systemOut.getLog());
    }

    @Test
    public void testIssueCommit() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", MISC);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorNoIssues() {
        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsMinorMessage("10.0.0"), systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorBreaking() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", BREAKING);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorNewFeature() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", NEW_FEATURE);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorBug() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", BUG);

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsMinorMessage("10.0.0"), systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorMiscellaneous() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", MISC);

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsMinorMessage("10.0.0"), systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorNewModule() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", NEW_MODULE);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorBugAndMiscellaneous() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addCommit("Issue #2: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", BUG);
        addIssue(2, CLOSED, "Hello World", MISC);

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsMinorMessage("10.0.0"), systemOut.getLog());
    }

    @Test
    public void testValidateVersionMinorNewModuleNewFeatureAndBreaking() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addCommit("Issue #2: Hello World", "CheckstyleUser");
        addCommit("Issue #3: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", NEW_MODULE);
        addIssue(2, CLOSED, "Hello World", NEW_FEATURE);
        addIssue(3, CLOSED, "Hello World", BREAKING);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.0",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchNoIssues() {
        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchBreaking() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", BREAKING);

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsPatchMessage("10.0.1", 1),
            systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchNewFeature() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", NEW_FEATURE);

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsPatchMessage("10.0.1", 1),
            systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchBug() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", BUG);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchMiscellaneous() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", MISC);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchNewModule() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", NEW_MODULE);

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsPatchMessage("10.0.1", 1),
            systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchNewFeatureNewModuleAndBreaking() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addCommit("Issue #2: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", NEW_MODULE);
        addIssue(2, CLOSED, "Hello World", BREAKING);

        runMainContentGenerationAndAssertReturnCode(-2,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output",
            getExecutionFailedMessage(1) + getReleaseIsPatchMessage("10.0.1", 1, 2),
            systemOut.getLog());
    }

    @Test
    public void testValidateVersionPatchBugAndMiscellaneous() {
        addCommit("Issue #1: Hello World", "CheckstyleUser");
        addCommit("Issue #2: Hello World", "CheckstyleUser");
        addIssue(1, CLOSED, "Hello World", BUG);
        addIssue(2, CLOSED, "Hello World", MISC);

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }

    @Test
    public void testMistypedCommitMessage() {
        addCommit("Issue 1: Hello World", "CheckstyleUser");
        addCommit("Pull 2: Hello World", "CheckstyleUser");
        addCommit("Issue #asd: asd", "CheckstyleUser");

        runMainContentGenerationAndAssertReturnCode(0,
            "-releaseNumber", "10.0.1",
            "-generateAll",
            "-validateVersion"
        );

        Assert.assertEquals("expected error output", "", systemErr.getLog());
        Assert.assertEquals("expected output", MSG_EXECUTION_SUCCEEDED, systemOut.getLog());
    }
}
