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

package com.github.checkstyle.github;

import java.io.IOException;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

/**
 * Class for Checkstyle's GitHub connection.
 */
public final class CsGitHub {
    /** Private constructor. */
    private CsGitHub() {
    }

    /**
     * Creates the connection to the remote repository.
     *
     * @param authToken the authorization token.
     * @param remoteRepoPath path to remote git repository.
     * @return the remote repository object.
     * @throws IOException if an I/O error occurs.
     */
    public static GHRepository createRemoteRepo(String authToken, String remoteRepoPath)
            throws IOException {
        final GitHub connection;
        if (authToken == null) {
            connection = GitHub.connectAnonymously();
        }
        else {
            connection = GitHub.connectUsingOAuth(authToken);
        }

        return connection.getRepository(remoteRepoPath);
    }
}
