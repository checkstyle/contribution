////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2021 the original author or authors.
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

package com.github.checkstyle.github;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.globals.ReleaseNotesMessage;
import com.github.checkstyle.globals.Result;
import com.google.common.base.Verify;
import com.google.common.collect.Sets;

/**
 * Contains methods for release notes generation.
 *
 * @author Andrei Selkin
 */
public final class NotesBuilder {

    /** Array elements separator. */
    private static final String SEPARATOR = ", ";

    /** String format pattern for github issue. */
    private static final String GITHUB_ISSUE_TEMPLATE =
            " https://github.com/%s/issues/%d";
    /** String format pattern for warning if issue is not closed. */
    private static final String MESSAGE_NOT_CLOSED = "[WARN] Issue #%d \"%s\" is not closed!"
                            + " Please review issue"
                            + GITHUB_ISSUE_TEMPLATE;
    /** String format pattern for error if no label on issue. */
    private static final String MESSAGE_NO_LABEL = "[ERROR] Issue #%d does not have %s label!"
                            + " Please set label at"
                            + GITHUB_ISSUE_TEMPLATE;

    /** Default constructor. */
    private NotesBuilder() {
    }

    /**
     * Forms release notes as a map.
     *
     * @param localRepoPath path to local git repository.
     * @param authToken the authorization token.
     * @param remoteRepoPath path to remote git repository.
     * @param startRef start reference.
     * @param endRef end reference.
     * @return a map which represents release notes.
     * @throws IOException if an I/O error occurs.
     * @throws GitAPIException if an error occurs when accessing Git API.
     */
    public static Result buildResult(String localRepoPath, String authToken, String remoteRepoPath,
        String startRef, String endRef) throws IOException, GitAPIException {

        final Result result = new Result();

        final GHRepository remoteRepo = createRemoteRepo(authToken, remoteRepoPath);
        final Set<RevCommit> commitsForRelease =
            getCommitsBetweenReferences(localRepoPath, startRef, endRef);
        commitsForRelease.removeAll(getIgnoredCommits(commitsForRelease));

        final Set<Integer> processedIssueNumbers = new HashSet<>();
        for (RevCommit commit : commitsForRelease) {
            CommitMessage commitMessage = new CommitMessage(commit.getFullMessage());
            if (commitMessage.isRevert()) {
                System.out.println(commitMessage.getMessage());
                commitMessage = new CommitMessage(commitMessage.getRevertedCommitMessage());
            }
            if (commitMessage.isIssueOrPull()) {
                final int issueNo = commitMessage.getIssueNumber();
                if (processedIssueNumbers.contains(issueNo)) {
                    continue;
                }
                processedIssueNumbers.add(issueNo);

                final GHIssue issue = remoteRepo.getIssue(issueNo);
                if (issue.getState() != GHIssueState.CLOSED) {
                    result.addWarning(String.format(MESSAGE_NOT_CLOSED,
                        issueNo, issue.getTitle(), remoteRepoPath, issueNo));
                }

                final String issueLabel = getIssueLabelFrom(issue);
                if (issueLabel.isEmpty()) {
                    final String error = String.format(MESSAGE_NO_LABEL,
                        issueNo,
                        Arrays.stream(Constants.ISSUE_LABELS)
                                .collect(Collectors.joining(SEPARATOR)),
                        remoteRepoPath, issueNo);
                    result.addError(error);
                }
                final Set<RevCommit> issueCommits = getCommitsForIssue(commitsForRelease, issueNo);
                final String authors = getAuthorsOf(issueCommits);
                final ReleaseNotesMessage releaseNotesMessage =
                    new ReleaseNotesMessage(issue, authors);
                result.putReleaseNotesMessage(issueLabel, releaseNotesMessage);
            }
            else {
                // Commits that have messages which do not contain issue or pull number
                final String commitShortMessage = commit.getShortMessage();
                final String author = commit.getAuthorIdent().getName();
                final ReleaseNotesMessage releaseNotesMessage =
                    new ReleaseNotesMessage(commitShortMessage, author);
                result.putReleaseNotesMessage(Constants.MISCELLANEOUS_LABEL, releaseNotesMessage);
            }
        }
        return result;
    }

    /**
     * Creates the connection to the remote repository.
     *
     * @param authToken the authorization token.
     * @param remoteRepoPath path to remote git repository.
     * @return the remote repository object.
     * @throws IOException if an I/O error occurs.
     */
    private static GHRepository createRemoteRepo(String authToken, String remoteRepoPath)
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
    private static Set<RevCommit> getCommitsBetweenReferences(String repoPath, String startRef,
            String endRef) throws IOException, GitAPIException {

        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final Path path = Paths.get(repoPath);
        final Repository repo = builder.findGitDir(path.toFile()).readEnvironment().build();

        final ObjectId startCommit = getActualRefObjectId(repo, startRef);
        Verify.verifyNotNull(startCommit, "Start reference \"" + startRef + "\" is invalid!");

        final ObjectId endCommit = getActualRefObjectId(repo, endRef);
        final Iterable<RevCommit> commits =
            new Git(repo).log().addRange(startCommit, endCommit).call();

        return Sets.newLinkedHashSet(commits);
    }

    /**
     * Returns a set of ignored commits.
     * Ignored commits are 'revert' commits and commits which were reverted by the 'revert' commits
     * in current release.
     *
     * @param commitsForRelease commits for release.
     * @return a set of ignored commits.
     */
    private static Set<RevCommit> getIgnoredCommits(Set<RevCommit> commitsForRelease) {
        final Set<RevCommit> ignoredCommits = new HashSet<>();
        for (RevCommit commit : commitsForRelease) {
            final CommitMessage commitMessage = new CommitMessage(commit.getFullMessage());
            if (commitMessage.isRevert()) {
                final String revertedCommitReference = commitMessage.getRevertedCommitReference();
                final Optional<RevCommit> revertedCommit = commitsForRelease.stream()
                    .filter(revCommit -> revertedCommitReference.equals(revCommit.getName()))
                    .findFirst();

                if (revertedCommit.isPresent()) {
                    ignoredCommits.add(commit);
                    ignoredCommits.add(revertedCommit.get());
                }
            }
            else if (commitMessage.isIgnored()) {
                ignoredCommits.add(commit);
            }
        }
        return ignoredCommits;
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
            final Ref repoPeeled = repo.peel(referenceObj);
            actualObjectId = Optional.ofNullable(repoPeeled.getPeeledObjectId())
                .orElse(referenceObj.getObjectId());
        }
        return actualObjectId;
    }

    /**
     * Returns a list of commits which are associated with the current issue.
     *
     * @param commits commits.
     * @param issueNo issue number.
     * @return a list of commits which are associated with the current issue.
     */
    private static Set<RevCommit> getCommitsForIssue(Set<RevCommit> commits, int issueNo) {
        final Set<RevCommit> currentIssueCommits = new HashSet<>();
        for (RevCommit commit : commits) {
            final CommitMessage commitMessage = new CommitMessage(commit.getFullMessage());
            if (commitMessage.isIssueOrPull()) {
                final int currentIssueNo = commitMessage.getIssueNumber();
                if (issueNo == currentIssueNo) {
                    currentIssueCommits.add(commit);
                }
            }
        }
        return currentIssueCommits;
    }

    /**
     * Forms a string which represents the authors who are referenced in the commits.
     *
     * @param commits commits.
     * @return string which represents the authors who are referenced in the commits.
     */
    private static String getAuthorsOf(Set<RevCommit> commits) {
        final Set<String> commitAuthors = findCommitAuthors(commits);
        return commitAuthors.stream().collect(Collectors.joining(SEPARATOR));
    }

    /**
     * Finds authors of the commits.
     *
     * @param commits current issue commits.
     * @return a list of authors who work on the current issue.
     */
    private static Set<String> findCommitAuthors(Set<RevCommit> commits) {
        final Set<String> commitAuthors = new HashSet<>();
        for (RevCommit commit : commits) {
            final String author = commit.getAuthorIdent().getName();
            commitAuthors.add(author);
        }
        return commitAuthors;
    }

    /**
     * Returns issue label for release notes.
     *
     * @param issue issue.
     * @return issue label for release notes
     * @throws IOException if an I/o error occurs.
     */
    private static String getIssueLabelFrom(GHIssue issue) throws IOException {
        final Collection<GHLabel> issueLabels = issue.getLabels();
        final Optional<GHLabel> label = issueLabels.stream()
            .filter(input -> Arrays.binarySearch(Constants.ISSUE_LABELS, input.getName()) >= 0)
            .findFirst();
        return label.map(GHLabel::getName).orElse("");
    }
}
