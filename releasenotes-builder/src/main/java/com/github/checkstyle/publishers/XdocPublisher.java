////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2016 the original author or authors.
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

package com.github.checkstyle.publishers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

/**
 * Class for xdoc publication.
 * @author Vladislav Lisetskii
 */
public class XdocPublisher {

    /** Path to xdoc objective in a local repo. */
    private static final String PATH_TO_XDOC_IN_REPO = "src/xdocs/releasenotes.xml";

    /** Template for a commit message. */
    private static final String COMMIT_MESSAGE_TEMPLATE = "doc: release notes %s";

    /** Line separator in files. */
    private static final String LINE_SEPARATOR = "\n";

    /** Placeholder for a new section comment text in xdoc. */
    private static final String PLACEHOLDER_TEXT = "<!-- placeholder for a new section -->";

    /** The name of the file to get post text from. */
    private final String localRepoPath;
    /** Path to a local git repository. */
    private final String postFilename;
    /** Release number. */
    private final String releaseNumber;
    /** Whether to do push. */
    private final boolean doPush;
    /** Auth token for Github. */
    private final String authToken;

    /**
     * Default constructor.
     * @param postFilename the name of the file to get post xml from.
     * @param localRepoPath path to a local git repository.
     * @param releaseNumber release number.
     * @param doPush whether to do push.
     * @param authToken auth token for Github.
     */
    public XdocPublisher(String postFilename, String localRepoPath, String releaseNumber,
            boolean doPush, String authToken) {
        this.postFilename = postFilename;
        this.localRepoPath = localRepoPath;
        this.releaseNumber = releaseNumber;
        this.doPush = doPush;
        this.authToken = authToken;
    }

    /**
     * Publish release notes.
     * @throws IOException if problem with access to files appears.
     * @throws GitAPIException for problems with jgit.
     */
    public void publish() throws IOException, GitAPIException {
        changeLocalRepoXdoc();
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final File localRepo = new File(localRepoPath);
        final Repository repo = builder.findGitDir(localRepo).readEnvironment().build();
        final Git git = new Git(repo);
        git.add()
            .addFilepattern(PATH_TO_XDOC_IN_REPO)
            .call();
        git.commit()
            .setMessage(String.format(Locale.ENGLISH, COMMIT_MESSAGE_TEMPLATE, releaseNumber))
            .call();
        if (doPush) {
            final CredentialsProvider credentialsProvider =
                new UsernamePasswordCredentialsProvider(authToken, "");
            git.push()
                .setCredentialsProvider(credentialsProvider)
                .call();
        }
    }

    /**
     * Do modification of an xdoc release notes in a local repo.
     * @throws IOException if problem with access to files appears.
     */
    private void changeLocalRepoXdoc() throws IOException {
        final Path pathToXdoc = Paths.get(localRepoPath + PATH_TO_XDOC_IN_REPO);
        final List<String> xdocLines = Files.readAllLines(pathToXdoc);
        final List<String> noteLines = Files.readAllLines(Paths.get(postFilename));

        int placeIndex = -1;
        for (int i = 0; i < xdocLines.size(); i++) {
            if (xdocLines.get(i).contains(PLACEHOLDER_TEXT)) {
                placeIndex = i + 1;
                break;
            }
        }

        if (placeIndex >= 0) {
            xdocLines.addAll(placeIndex, noteLines);
            xdocLines.add(placeIndex, "");
            System.setProperty("line.separator", LINE_SEPARATOR);
            Files.write(pathToXdoc, xdocLines);
        }
        else {
            throw new IllegalStateException("Placeholder for a new section is not found in "
                + localRepoPath + PATH_TO_XDOC_IN_REPO);
        }
    }
}
