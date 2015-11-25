////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
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
public class ReleaseNotesMessage {

    /** Issue number. */
    private int issueNo;
    /** Title. */
    private String title;
    /** Author. */
    private String author;

    /**
     * Constructs a release notes message for issue.
     * @param issue issue.
     * @param author author.
     */
    public ReleaseNotesMessage(GHIssue issue, String author) {
        issueNo = issue.getNumber();
        title = issue.getTitle();
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
}
