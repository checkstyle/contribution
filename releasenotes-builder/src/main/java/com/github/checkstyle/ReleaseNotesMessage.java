////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2018 the original author or authors.
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

package com.github.checkstyle;

import org.kohsuke.github.GHIssue;

/**
 * Represents a release notes message.
 * @author Andrei Selkin
 */
public final class ReleaseNotesMessage {

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
        this.title = title;
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
    private String getActualTitle(GHIssue issue) {
        final String actualTitle;
        final String issueTitle = issue.getTitle();
        if (issueTitle.startsWith("Pull")) {
            actualTitle = issueTitle.substring(issueTitle.indexOf(':') + 2);
        }
        else {
            actualTitle = issueTitle;
        }
        return actualTitle;
    }
}
