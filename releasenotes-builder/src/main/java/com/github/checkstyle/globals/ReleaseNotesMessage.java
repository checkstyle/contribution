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

package com.github.checkstyle.globals;

import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Represents a release notes message.
 *
 * @author Andrei Selkin
 */
public final class ReleaseNotesMessage {

    /** Max size of line. */
    private static final int MAX_TITLE_LINE_SIZE = 100;

    /** Converted size of html character. */
    private static final int HTML_CHAR_SIZE = 5;

    /** Amount of whitespace characters preceding the title. */
    private static final int TITLE_INDENTATION = 12;

    /** Twelve spaces. */
    private static final String TWELVE_SPACES = String.join(
        "", Collections.nCopies(TITLE_INDENTATION, " "));

    /** Pattern used to find things to escape in github. */
    private static final Pattern ESCAPE = Pattern.compile("([@<>&])");

    /** Issue number. */
    private final int issueNo;
    /** Title. */
    private final String title;
    /** Short Width Title. */
    private final String shortWidthTitle;
    /** Title with escaped GitHub characters. */
    private final String githubEscapedTitle;
    /** Author. */
    private final String author;

    /**
     * Constructs a release notes message for issue.
     *
     * @param issueNumber issue number.
     * @param issueTitle issue title.
     * @param author author.
     */
    public ReleaseNotesMessage(int issueNumber, String issueTitle, String author) {
        issueNo = issueNumber;
        title = getActualTitle(issueTitle);
        shortWidthTitle = split(title);
        githubEscapedTitle = escapeGithubCharacters(title);
        this.author = author;
    }

    /**
     * Constructs a release notes message for commit.
     *
     * @param title commit title.
     * @param author commit author.
     */
    public ReleaseNotesMessage(String title, String author) {
        issueNo = -1;
        this.title = title;
        shortWidthTitle = split(title);
        githubEscapedTitle = escapeGithubCharacters(title);
        this.author = author;
    }

    /**
     * Returns the issue number.
     *
     * @return the issue number
     */
    public int getIssueNo() {
        return issueNo;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the GitHub escaped title.
     *
     * @return the GitHub escaped title.
     */
    public String getGithubEscapedTitle() {
        return githubEscapedTitle;
    }

    /**
     * Returns the short width title.
     *
     * @return the short width title
     */
    public String getShortWidthTitle() {
        return shortWidthTitle;
    }

    /**
     * Returns the author.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns actual title of issue or pull request which is represented as an issue.
     *
     * @param issueTitle issue title.
     * @return actual title of issue or pull request which is represented as an issue.
     */
    private static String getActualTitle(String issueTitle) {
        final String actualTitle;
        if (issueTitle.startsWith("Pull")) {
            actualTitle = issueTitle.substring(issueTitle.indexOf(':') + 2);
        }
        else {
            actualTitle = issueTitle;
        }
        return actualTitle;
    }

    /**
     * Splits the given string by the max line size into multiple lines.
     *
     * @param str The string to examine.
     * @return The split string.
     */
    // -@cs[CyclomaticComplexity|ExecutableStatementCount] Can't be split apart easily.
    private static String split(String str) {
        final String indentedStr = TWELVE_SPACES + str;
        final int length = indentedStr.length();
        final StringBuilder sb = new StringBuilder(length);
        int index = 0;
        int splitStart = index;
        int outPosition = 0;
        int lastSpacePosition = -1;
        int lastSpaceOutPosition = -1;

        while (index < length) {
            final char ch = indentedStr.charAt(index);
            final int charSize;

            if (ch == '>' || ch == '<' || ch == '&' || ch == '\'' || ch == '"') {
                charSize = HTML_CHAR_SIZE;
            }
            else {
                charSize = 1;
            }

            outPosition += charSize;

            if (outPosition > MAX_TITLE_LINE_SIZE) {
                if (lastSpacePosition == -1) {
                    sb.append(indentedStr, splitStart, index - 1);
                    sb.append("-");

                    splitStart = index - 1;
                    outPosition = 0;
                }
                else {
                    sb.append(indentedStr, splitStart, lastSpacePosition);
                    splitStart = lastSpacePosition + 1;
                    outPosition -= lastSpaceOutPosition - TITLE_INDENTATION;
                }

                sb.append(System.lineSeparator());
                sb.append(TWELVE_SPACES);
                lastSpacePosition = -1;
                lastSpaceOutPosition = -1;
            }
            else if (ch == ' ') {
                if (splitStart == index) {
                    splitStart++;
                }
                else {
                    lastSpacePosition = index;
                    lastSpaceOutPosition = outPosition;
                }
            }

            index++;
        }

        if (splitStart != index) {
            sb.append(indentedStr.substring(splitStart));
        }

        return sb.toString();
    }

    /**
     * Escapes GitHub characters by placing them inside code block.
     *
     * @param str string to escape.
     * @return escaped string.
     */
    private static String escapeGithubCharacters(String str) {
        return ESCAPE.matcher(str).replaceAll("`$1`");
    }
}
