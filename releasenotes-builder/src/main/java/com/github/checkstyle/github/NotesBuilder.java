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

package com.github.checkstyle.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.GHFileNotFoundException;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;
import org.kohsuke.github.GHRepository;

import com.github.checkstyle.git.CsGit;
import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.globals.ReleaseNotesMessage;
import com.github.checkstyle.globals.Result;

/**
 * Contains methods for release notes generation.
 *
 * @author Andrei Selkin
 */
public final class NotesBuilder {

    /** Array elements separator. */
    private static final String SEPARATOR = ", ";

    /** String format pattern for GitHub issue. */
    private static final String GITHUB_ISSUE_TEMPLATE =
            " https://github.com/%s/issues/%d";
    /** String format pattern for warning if issue is not closed. */
    private static final String MESSAGE_ISSUE_NOT_FOUND = "[WARN] Issue #%d could not be found!";
    /** String format pattern for warning if issue is not closed. */
    private static final String MESSAGE_NOT_CLOSED = "[WARN] Issue #%d \"%s\" is not closed!"
                            + " Please review issue"
                            + GITHUB_ISSUE_TEMPLATE;
    /** String format pattern for error if no label on issue. */
    private static final String MESSAGE_NO_LABEL = "[ERROR] Issue #%d does not have %s label!"
                            + " Please set label at"
                            + GITHUB_ISSUE_TEMPLATE;

    /** String format pattern for error if more than one label on issue. */
    private static final String MESSAGE_MORE_THAN_ONE_RELEASE_LABEL =
        "[ERROR] Issue #%d have more than one release label! Please set only one label from %s at"
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
     * @noinspection UseOfSystemOutOrSystemErr
     * @noinspectionreason UseOfSystemOutOrSystemErr - used for CLI output
     */
    public static Result buildResult(String localRepoPath, String authToken, String remoteRepoPath,
                                     String startRef, String endRef) throws IOException,
                                      GitAPIException {

        final Result result = new Result();

        final GHRepository remoteRepo = CsGitHub.createRemoteRepo(authToken, remoteRepoPath);
        final Set<RevCommit> commitsForRelease =
                CsGit.getCommitsBetweenReferences(localRepoPath, startRef, endRef);
        commitsForRelease.removeAll(getIgnoredCommits(commitsForRelease));

        final Set<Integer> processedIssueNumbers = new HashSet<>();
        for (RevCommit commit : commitsForRelease) {
            CommitMessage commitMessage = new CommitMessage(commit.getFullMessage());
            if (commitMessage.isRevert()) {
                System.out.println(commitMessage.getMessage());
                commitMessage = new CommitMessage(commitMessage.getRevertedCommitMessage());
            }
            buildResultWithLabel(remoteRepoPath, result, remoteRepo, commitsForRelease,
                                 processedIssueNumbers,
                                 commit,
                                 commitMessage);
        }
        return result;
    }

    /**
     * Forms release notes as a map.
     *
     * @param remoteRepoPath path to remote git repository.
     * @param result result
     * @param remoteRepo remote repository
     * @param commitsForRelease commit with release label
     * @param processedIssueNumbers issue number.
     * @param commit commit information.
     * @param commitMessage commit message.
     * @throws IOException if an I/O error occurs.
     * @noinspection MethodWithTooManyParameters
     * @noinspectionreason MethodWithTooManyParameters - Method requires a lot of parameters to
     *                     build the result with label.
     */
    // -@cs[ExecutableStatementCount] central method for processing
    private static void buildResultWithLabel(String remoteRepoPath, Result result,
                                             GHRepository remoteRepo,
                                             Set<RevCommit> commitsForRelease,
                                             Set<Integer> processedIssueNumbers, RevCommit commit,
                                             CommitMessage commitMessage) throws IOException {
        if (commitMessage.isIssueOrPull()) {
            final int issueNo = commitMessage.getIssueNumber();
            if (!processedIssueNumbers.contains(issueNo)) {
                processedIssueNumbers.add(issueNo);

                int issueNumber;
                String issueTitle;
                String issueLabel = null;

                try {
                    final GHIssue issue = remoteRepo.getIssue(issueNo);

                    if (issue.getState() != GHIssueState.CLOSED) {
                        result.addWarning(String.format(MESSAGE_NOT_CLOSED, issueNo,
                                issue.getTitle(), remoteRepoPath, issueNo));
                    }

                    issueNumber = issue.getNumber();
                    issueTitle = issue.getTitle();
                    issueLabel = getIssueLabelFrom(issue);

                    final List<GHLabel> releaseLabels = getAllIssueLabels(issue);

                    if (releaseLabels.size() > 1) {
                        final String error = String.format(MESSAGE_MORE_THAN_ONE_RELEASE_LABEL,
                                                           issueNo,
                                                           String.join(SEPARATOR,
                                                               Constants.ISSUE_LABELS),
                                                           remoteRepoPath, issueNo);
                        result.addError(error);
                    }
                }
                catch (GHFileNotFoundException exc) {
                    result.addWarning(String.format(MESSAGE_ISSUE_NOT_FOUND, issueNo));

                    issueNumber = issueNo;
                    issueTitle = commitMessage.getMessage();
                    issueLabel = Constants.MISCELLANEOUS_LABEL;
                }

                if (issueLabel.isEmpty()) {
                    final String error = String.format(MESSAGE_NO_LABEL,
                                                       issueNo,
                                                       String.join(SEPARATOR,
                                                           Constants.ISSUE_LABELS),
                                                       remoteRepoPath, issueNo);
                    result.addError(error);
                }
                final Set<RevCommit> issueCommits = getCommitsForIssue(commitsForRelease, issueNo);
                final String authors = getAuthorsOf(issueCommits);
                final ReleaseNotesMessage releaseNotesMessage =
                    new ReleaseNotesMessage(issueNumber, issueTitle, authors);
                result.putReleaseNotesMessage(issueLabel, releaseNotesMessage);
            }
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
        return String.join(SEPARATOR, commitAuthors);
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
     */
    private static String getIssueLabelFrom(GHIssue issue) {
        final Collection<GHLabel> issueLabels = issue.getLabels();
        final Optional<GHLabel> label = issueLabels.stream()
            .filter(input -> Arrays.binarySearch(Constants.ISSUE_LABELS, input.getName()) >= 0)
            .findFirst();
        return label.map(GHLabel::getName).orElse("");
    }

    /**
     * Returns release label for release notes.
     *
     * @param issue issue.
     * @return release label for release notes
     */
    private static List<GHLabel> getAllIssueLabels(GHIssue issue) {
        final Collection<GHLabel> issueLabels = issue.getLabels();
        return issueLabels.stream()
            .filter(input -> Arrays.binarySearch(Constants.ISSUE_LABELS, input.getName()) >= 0)
            .collect(Collectors.toList());
    }
}
