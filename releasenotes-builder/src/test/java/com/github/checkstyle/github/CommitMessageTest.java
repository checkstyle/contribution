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

package com.github.checkstyle.github;

import org.junit.Assert;
import org.junit.Test;

public class CommitMessageTest {

    @Test
    public void testIssue() {
        final CommitMessage message = new CommitMessage(
            "Issue #1487: workaround for cobertura at CheckstyleAntTask.java to get 100% coverage");
        Assert.assertTrue("Message should match issue/pull pattern", message.isIssueOrPull());
    }

    @Test
    public void testPull() {
        final CommitMessage message = new CommitMessage(
            "Pull #9314: fix escaped char pattern in AvoidEscapedUnicodeCharactersCheck");
        Assert.assertTrue("Message should match issue/pull pattern", message.isIssueOrPull());
    }

    @Test
    public void testRevert() {
        final CommitMessage message = new CommitMessage(
            "Revert \"issue 530, was removed from release notes, as it was reverted\"\n"
                + "\n"
                + "This reverts commit 5fe5bcee40e39eb6a23864f7f55128cbf2f10641.");
        Assert.assertTrue("Message should match revert pattern", message.isRevert());
        Assert.assertEquals("Invalid commit message",
            "issue 530, was removed from release notes, as it was reverted",
            message.getRevertedCommitMessage());
        Assert.assertEquals("Invalid commit reference",
            "5fe5bcee40e39eb6a23864f7f55128cbf2f10641", message.getRevertedCommitReference());
    }

    @Test
    public void testRevertNotInBeginningOfMessage() {
        final CommitMessage message = new CommitMessage(
            "minor: Revert \"Issue #3323: use Orekit fork to pass CI for this issue\"\n"
                + "\n"
                + "This reverts commit 7a8b92371b9b1a605ec4caa8ef138cbd194738d5.");
        Assert.assertTrue("Message should match revert pattern", message.isRevert());
    }

    @Test
    public void testReleaseIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "[maven-release-plugin] prepare release checkstyle-8.35");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testUpdateToIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "update to 7.0-SNAPSHOT");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testDocReleaseNotesIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "doc: release notes 8.43");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testConfigIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "config: update to 8.42-SNAPSHOT");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testDependencyIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "dependency: bump spotbugs-maven-plugin from 4.2.3 to 4.3.0\n"
                + "\n"
                + "Bumps [spotbugs-maven-plugin](https://github.com/spotbugs/spotbugs-maven-plugin)"
                + " from 4.2.3 to 4.3.0.\n"
                + "- [Release notes](https://github.com/spotbugs/spotbugs-maven-plugin/releases)\n"
                + "- [Commits](https://github.com/spotbugs/spotbugs-maven-plugin/compare/"
                + "spotbugs-maven-plugin-4.2.3...spotbugs-maven-plugin-4.3.0)\n"
                + "\n"
                + "---\n"
                + "updated-dependencies:\n"
                + "- dependency-name: com.github.spotbugs:spotbugs-maven-plugin\n"
                + "  dependency-type: direct:production\n"
                + "  update-type: version-update:semver-minor\n"
                + "...\n"
                + "\n"
                + "Signed-off-by: dependabot[bot] <support@github.com>");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testInfraIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "infra: update README to have links to travis.com");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testMinorIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "minor: refactor boolean expression to fix TC build");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testSupplementalIsIgnored() {
        final CommitMessage message = new CommitMessage(
            "supplemental: divide description into paragraph for Issue #8928");
        Assert.assertTrue("Message should match ignore pattern", message.isIgnored());
    }

    @Test
    public void testMarkersNotInBeginningOfMessage() {
        final CommitMessage message = new CommitMessage(
            "Regular message.\n"
                + "[maven-release-plugin]|\n"
                + "update to 1.23-SNAPSHOT|\n"
                + "doc: release notes\n"
                + "config:\n"
                + "dependency:\n"
                + "infra:\n"
                + "minor:\n"
                + "supplemental:\n"
                + "\n");
        Assert.assertFalse("Message should not match ignore pattern", message.isIgnored());
    }

    @Test
    public void testUnclassifiedCommit() {
        final CommitMessage message = new CommitMessage(
            "Some commit message of unknown type");
        Assert.assertFalse("Message should not match revert pattern", message.isRevert());
        Assert.assertFalse("Message should not match ignore pattern", message.isIgnored());
        Assert.assertFalse("Message should not match issue/pull pattern", message.isIssueOrPull());
    }

}
