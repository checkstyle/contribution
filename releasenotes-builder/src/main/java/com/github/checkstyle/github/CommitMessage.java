///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2024 the original author or authors.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Commit message wrapper.
 */
/* package */ class CommitMessage {

    /** Regexp pattern for revert commit messages. */
    private static final Pattern REVERT_COMMIT_MESSAGES_PATTERN =
        Pattern.compile("Revert \"[^\"]+?\"");

    /** Regexp pattern for ignoring commit messages. */
    private static final Pattern IGNORED_COMMIT_MESSAGES_PATTERN =
        Pattern.compile("^\\[maven-release-plugin]|"
            + "^update to ([0-9]|\\.)+-SNAPSHOT|"
            + "^doc: release notes|"
            + "^(config|dependency|infra|minor|supplemental):");

    /** Full commit message. */
    private final String message;

    /**
     * Creates new instance of {@code CommitMessage}.
     *
     * @param message the full commit message
     */
    /* package */ CommitMessage(String message) {
        this.message = message;
    }

    /**
     * Returns the commit message.
     *
     * @return the commit message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Checks whether commits message is associated with a pull request or an issue.
     * Commit message which is associated with a pull request or an issue follows one
     * of the following formats: 'Pull #[number]: [title]' or 'Issue #[number]: [title]'.
     * This method trims '\r' and '\n'.
     *
     * @return true if commits message is associated with a pull request or an issue.
     */
    public boolean isIssueOrPull() {
        return message
            .replace("\r", "")
            .replace("\n", "")
            .matches("^(Pull|Issue) #\\d+: .*$");
    }

    /**
     * Extracts an issue number from commit message.
     *
     * @return issue number.
     */
    public int getIssueNumber() {
        final int numberSignIndex = message.indexOf('#');
        final int colonIndex = message.indexOf(':');
        return Integer.parseInt(message.substring(numberSignIndex + 1, colonIndex));
    }

    /**
     * Checks commit message to determine whether commit should be ignored.
     *
     * @return {@code true} if commit with the message should be ignored.
     */
    public boolean isIgnored() {
        final Matcher matcher = IGNORED_COMMIT_MESSAGES_PATTERN.matcher(message);
        return matcher.find();
    }

    /**
     * Checks whether a commit message starts with the 'Revert' word.
     *
     * @return {@code true} if a commit message starts with the 'Revert' word.
     */
    public boolean isRevert() {
        final Matcher matcher = REVERT_COMMIT_MESSAGES_PATTERN.matcher(message);
        return matcher.find();
    }

    /**
     * Returns the original commit hash that was reverted by this commit.
     *
     * @return the commit SHA, if present, the string {@code "nonexistingsha"} otherwise.
     */
    public String getRevertedCommitReference() {
        final int lastSpaceIndex = message.lastIndexOf(' ');
        final int lastPeriodIndex = message.lastIndexOf('.');
        final String result;
        if (lastSpaceIndex > lastPeriodIndex) {
            // Something is wrong with commit message, revert commit was changed manually.
            result = "nonexistingsha";
        }
        else {
            result = message.substring(lastSpaceIndex + 1, lastPeriodIndex);
        }
        return result;
    }

    /**
     * Returns the original commit message that was reverted by this commit.
     *
     * @return the commit message
     */
    public String getRevertedCommitMessage() {
        final int firstQuoteIndex = message.indexOf('"');
        final int lastQuoteIndex = message.lastIndexOf('"');
        return message.substring(firstQuoteIndex + 1, lastQuoteIndex);
    }

}
