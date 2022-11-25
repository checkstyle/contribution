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

import java.util.Date;
import java.util.Random;

import org.eclipse.jgit.revwalk.RevCommit;

public final class RevCommitUtil {
    private static final Random RANDOM = new Random();

    private RevCommitUtil() {
    }

    public static RevCommit create(String commitMessage) {
        return create("CheckstyleUser", commitMessage);
    }

    public static RevCommit create(String author, String commitMessage) {
        final String commitData = String.format("tree %040x\n"
            + "parent %040x\n"
            + "author " + author + " <test@email.com> %d +0100\n"
            + "committer " + author + " <test@email.com> %d +0100\n\n"
            + commitMessage,
                RANDOM.nextLong(),
                RANDOM.nextLong(),
                new Date().getTime(),
                new Date().getTime());
        return RevCommit.parse(commitData.getBytes());
    }
}
