////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
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
////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.globals;

import org.kohsuke.github.GHIssue;

/**
 * Represents a release notes message.
 * @author Andrei Selkin
 */
public final class ReleaseNotesMessage {

    /** Max size of line. */
    private static final int MAX_TITLE_LINE_SIZE = 70;

    /** Converted size of html character. */
    private static final int HTML_CHAR_SIZE = 5;

    /** Issue number. */
    private final int issueNo;
    /** Title. */
    private final String title;
    /** Author. */
    private final String author;

    /**
     * Constructs a release notes message for issue.
     * @param issue issue.
     * @param author author.
     */
    public ReleaseNotesMessage(GHIssue issue, String author) {
        issueNo = issue.getNumber();
        title = getActualTitle(issue);
        this.author = author;
    }

    /**
     * Constructs a release notes message for commit.
     * @param title commit title.
     * @param author commit author.
     */
    public ReleaseNotesMessage(String title, String author) {
        issueNo = -1;
        this.title = split(title);
        this.author = author;
    }

    public int getIssueNo() {
        return issueNo;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    /**
     * Returns actual title of issue or pull request which is represented as an issue.
     * @param issue issue object.
     * @return actual title of issue or pull request which is represented as an issue.
     */
    private static String getActualTitle(GHIssue issue) {
        final String actualTitle;
        final String issueTitle = issue.getTitle();
        if (issueTitle.startsWith("Pull")) {
            actualTitle = issueTitle.substring(issueTitle.indexOf(':') + 2);
        }
        else {
            actualTitle = issueTitle;
        }
        return split(actualTitle);
    }

    /**
     * Splits the given string by the max line size into multiple lines.
     * @param str The string to examine.
     * @return The split string.
     */
    // -@cs[CyclomaticComplexity|ExecutableStatementCount] Can't be split apart easily.
    private static String split(String str) {
        final int length = str.length();
        final StringBuilder sb = new StringBuilder(length);
        int index = 0;
        int splitStart = index;
        int outPosition = 0;
        int lastSpacePosition = -1;
        int lastSpaceOutPosition = -1;

        while (index < length) {
            final char ch = str.charAt(index);
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
                    sb.append(str, splitStart, index - 1);
                    sb.append("-");

                    splitStart = index - 1;
                    outPosition = 0;
                }
                else {
                    sb.append(str, splitStart, lastSpacePosition);
                    splitStart = lastSpacePosition + 1;
                    outPosition -= lastSpaceOutPosition;
                }

                sb.append(System.lineSeparator());
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
            sb.append(str.substring(splitStart));
        }

        return sb.toString();
    }
}
