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

package com.github.checkstyle.git;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.google.common.base.Verify;
import com.google.common.collect.Sets;

/**
 * Class for Checkstyle's Git connection.
 */
public final class CsGit {
    /** Private constructor. */
    private CsGit() {
    }

    /**
     * Returns a list of commits between two references.
     *
     * @param repoPath path to local git repository.
     * @param startRef start reference.
     * @param endRef end reference.
     * @return a list of commits.
     * @throws IOException if I/O error occurs.
     * @throws GitAPIException if an error occurs when accessing Git API.
     */
    public static Set<RevCommit> getCommitsBetweenReferences(String repoPath, String startRef,
            String endRef) throws IOException, GitAPIException {

        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Path path = Paths.get(repoPath);
        final Repository repo = builder.findGitDir(path.toFile()).readEnvironment().build();

        final ObjectId startCommit = getActualRefObjectId(repo, startRef);
        Verify.verifyNotNull(startCommit, "Start reference \"" + startRef + "\" is invalid!");

        final ObjectId endCommit = getActualRefObjectId(repo, endRef);
        try (Git git = new Git(repo)) {
            final Iterable<RevCommit> commits =
                git.log().addRange(startCommit, endCommit).call();

            return Sets.newLinkedHashSet(commits);
        }
    }

    /**
     * Returns actual SHA-1 object by commit reference.
     *
     * @param repo git repository.
     * @param ref string representation of commit reference.
     * @return actual SHA-1 object.
     * @throws IOException if an I/O error occurs.
     */
    private static ObjectId getActualRefObjectId(Repository repo, String ref) throws IOException {
        final ObjectId actualObjectId;
        final Ref referenceObj = repo.findRef(ref);
        if (referenceObj == null) {
            actualObjectId = repo.resolve(ref);
        }
        else {
            final Ref repoPeeled = repo.getRefDatabase().peel(referenceObj);
            actualObjectId = Optional.ofNullable(repoPeeled.getPeeledObjectId())
                .orElse(referenceObj.getObjectId());
        }
        return actualObjectId;
    }
}
