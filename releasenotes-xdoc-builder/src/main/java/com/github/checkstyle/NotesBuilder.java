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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.ParseException;
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

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Contains methods for release notes generation.
 * @author Andrei Selkin
 */
public final class NotesBuilder {

    /** A path to remote checkstyle repository. */
    private static final String REMOTE_REPO_PATH = "checkstyle/checkstyle";

    /** Array elements separator. */
    private static final String SEPARATOR = ", ";

    /** Exit code returned when execution finishes with errors. */
    private static final int EXIT_WITH_ERRORS_CODE = -2;

    /** Regexp pattern for ignoring commit messages. */
    private static final Pattern IGNORED_COMMIT_MESSAGES_PATTERN =
        Pattern.compile("^\\[maven-release-plugin\\].*(\r|\n)?$|"
            + "^update to ([0-9]|\\.)+-SNAPSHOT(\r|\n)?$|"
            + "^doc: release notes.*(\r|\n)?$|"
            + "^(config:|minor:|infra:)(.|\n)*[\r|\n]?$");

    /** Default constructor. */
    private NotesBuilder() { }

    /**
     * Entry point.
     * @param args command line arguments.
     */
    public static void main(String... args) {
        int errorCounter = 0;
        try {
            final CliProcessor cliProcessor = new CliProcessor(args);
            cliProcessor.process();
            if (cliProcessor.hasErrors()) {
                printListOf(cliProcessor.getErrorMessages());
                errorCounter = cliProcessor.getErrorMessages().size();
            }
            else {
                final CliOptions cliOptions = cliProcessor.getCliOptions();
                errorCounter = runNotesBuilder(cliOptions);
            }
        }
        catch (ParseException | GitAPIException | IOException e) {
            errorCounter = 1;
            System.out.println(e.getMessage());
            CliProcessor.printUsage();
        }
        finally {
            if (errorCounter == 0) {
                System.out.println(String.format("%nGeneration succeed!"));
            }
            else {
                System.out.println(String.format("%nGeneration ends with %d errors.",
                    errorCounter));
                System.exit(EXIT_WITH_ERRORS_CODE);
            }
        }
    }

    /**
     * Executes NotesBuilder based on passed command line options.
     * @param cliOptions command line options.
     * @return number of errors.
     * @throws IOException if an I/O error occurs.
     * @throws GitAPIException if an error occurs while accessing GitHub API.
     */
    private static int runNotesBuilder(CliOptions cliOptions) throws IOException, GitAPIException {
        final String localRepoPath = cliOptions.getLocalRepoPath();
        final String startRef = cliOptions.getStartRef();
        final String endRef = cliOptions.getEndRef();
        final String authToken = cliOptions.getAuthToken();

        final GitHub connection;
        if (authToken == null) {
            connection = GitHub.connectAnonymously();
        }
        else {
            connection = GitHub.connectUsingOAuth(authToken);
        }

        final GHRepository remoteRepo = connection.getRepository(REMOTE_REPO_PATH);
        final Result result = buildResult(remoteRepo, localRepoPath, startRef, endRef);
        if (result.hasWarnings()) {
            printListOf(result.getWarningMessages());
        }

        int errorCounter = 0;
        if (result.hasErrors()) {
            printListOf(result.getErrorMessages());
            errorCounter = result.getErrorMessages().size();
        }
        else {
            final Multimap<String, ReleaseNotesMessage> releaseNotes = result.getReleaseNotes();
            final String releaseNumber = cliOptions.getReleaseNumber();
            final String outputFile = cliOptions.getOutputFile();
            TemplateProcessor.generateWithThymeleaf(releaseNotes, releaseNumber, outputFile);
        }
        return errorCounter;
    }

    /**
     * Forms release notes as a map.
     * @param remoteRepo git remote repository object.
     * @param localRepoPath path to local git repository.
     * @param startRef start reference.
     * @param endRef end reference.
     * @return a map which represents release notes.
     * @throws IOException if an I/O error occurs.
     * @throws GitAPIException if an error occurs when accessing Git API.
     */
    private static Result buildResult(GHRepository remoteRepo, String localRepoPath,
        String startRef, String endRef) throws IOException, GitAPIException {

        final Result result = new Result();

        final Set<Integer> processedIssueNumbers = new HashSet<>();
        final Set<RevCommit> commitsForRelease =
            getCommitsBetweenReferences(localRepoPath, startRef, endRef);
        commitsForRelease.removeAll(getIgnoredCommits(commitsForRelease));

        for (RevCommit commit : commitsForRelease) {
            String commitMessage = commit.getFullMessage();
            if (isRevertCommit(commitMessage)) {
                final int firstQuoteIndex = commitMessage.indexOf('"');
                final int lastQuoteIndex = commitMessage.lastIndexOf('"');
                commitMessage = commitMessage.substring(firstQuoteIndex, lastQuoteIndex);
            }
            if (isIssueOrPull(commitMessage)) {
                final int issueNo = getIssueNumberFrom(commitMessage);
                if (processedIssueNumbers.contains(issueNo)) {
                    continue;
                }
                processedIssueNumbers.add(issueNo);

                final GHIssue issue = remoteRepo.getIssue(issueNo);
                if (issue.getState() != GHIssueState.CLOSED) {
                    result.addWarning(String.format("[WARN] Issue #%d \"%s\" is not closed!"
                        , issueNo, issue.getTitle()));
                }

                final String issueLabel = getIssueLabelFrom(issue);
                if (issueLabel.isEmpty()) {
                    final String error = String.format("[ERROR] Issue #%d does not have %s label!",
                        issueNo, Joiner.on(SEPARATOR).join(Constants.ISSUE_LABELS));
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
     * Returns a list of commits between two references.
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
        final ObjectId endCommit = getActualRefObjectId(repo, endRef);
        final Iterable<RevCommit> commits =
            new Git(repo).log().addRange(startCommit, endCommit).call();

        return Sets.newLinkedHashSet(commits);
    }

    /**
     * Checks whether a commit message starts with the 'Revert' word.
     * @param commitMessage commit message.
     * @return true if a commit message starts with the 'Revert' word.
     */
    private static boolean isRevertCommit(String commitMessage) {
        return commitMessage.startsWith("Revert");
    }

    /**
     * Returns a set of ignored commits.
     * Ignored commits are 'revert' commits and commits which were reverted by the 'revert' commits
     * in current release.
     * @param commitsForRelease commits for release.
     * @return a set of ignored commits.
     */
    private static Set<RevCommit> getIgnoredCommits(Set<RevCommit> commitsForRelease) {
        final Set<RevCommit> ignoredCommits = new HashSet<>();
        for (RevCommit commit : commitsForRelease) {
            final String commitMessage = commit.getFullMessage();
            if (isRevertCommit(commitMessage)) {
                final int lastSpaceIndex = commitMessage.lastIndexOf(' ');
                final int lastPeriodIndex = commitMessage.lastIndexOf('.');
                final String revertedCommitReference =
                    commitMessage.substring(lastSpaceIndex + 1, lastPeriodIndex);

                final RevCommit revertedCommit = Iterables.tryFind(commitsForRelease,
                    new Predicate<RevCommit>() {
                        @Override
                        public boolean apply(RevCommit commit) {
                            return revertedCommitReference.equals(commit.getName());
                        }
                    }).orNull();

                if (revertedCommit != null) {
                    ignoredCommits.add(commit);
                    ignoredCommits.add(revertedCommit);
                }
            }
            else if (isIgnoredCommit(commitMessage)) {
                ignoredCommits.add(commit);
            }
        }
        return ignoredCommits;
    }

    /**
     * Checks commit message to determine whether commit should be ignored.
     * @param commitMessage commit message.
     * @return if commit with the message should be ignored.
     */
    private static boolean isIgnoredCommit(String commitMessage) {
        final Matcher matcher = IGNORED_COMMIT_MESSAGES_PATTERN.matcher(commitMessage);
        return matcher.matches();
    }

    /**
     * Returns actual SHA-1 object by commit reference.
     * @param repo git repository.
     * @param ref string representation of commit reference.
     * @return actual SHA-1 object.
     * @throws IOException if an I/O error occurs.
     */
    private static ObjectId getActualRefObjectId(Repository repo, String ref) throws IOException {
        final ObjectId actualObjectId;
        final Ref referenceObj = repo.getRef(ref);
        if (referenceObj == null) {
            actualObjectId = repo.resolve(ref);
        }
        else {
            final Ref repoPeeled = repo.peel(referenceObj);
            if (repoPeeled.getPeeledObjectId() == null) {
                actualObjectId =  referenceObj.getObjectId();
            }
            else {
                actualObjectId = repoPeeled.getPeeledObjectId();
            }
        }
        return actualObjectId;
    }

    /**
     * Extracts an issue number from commit message.
     * @param commitMessage commit message.
     * @return issue number.
     */
    private static int getIssueNumberFrom(String commitMessage) {
        final int numberSignIndex = commitMessage.indexOf('#');
        final int colonIndex = commitMessage.indexOf(':');
        return Integer.parseInt(commitMessage.substring(numberSignIndex + 1, colonIndex));
    }

    /**
     * Returns a list of commits which are associated with the current issue.
     * @param commits commits.
     * @param issueNo issue number.
     * @return a list of commits which are associated with the current issue.
     */
    private static Set<RevCommit> getCommitsForIssue(Set<RevCommit> commits, int issueNo) {
        final Set<RevCommit> currentIssueCommits = new HashSet<>();
        for (RevCommit commit : commits) {
            final String commitMessage = commit.getFullMessage();
            if (isIssueOrPull(commitMessage)) {
                final int currentIssueNo = getIssueNumberFrom(commitMessage);
                if (issueNo == currentIssueNo) {
                    currentIssueCommits.add(commit);
                }
            }
        }
        return currentIssueCommits;
    }

    /**
     * Checks whether commits message is associated with a pull request or an issue.
     * Commit message which is associated with a pull request or an issue starts with 'Pull'
     * or 'Issue' prefix.
     * @param commitMessage commit message.
     * @return true if commits message is associated with a pull request or an issue.
     */
    private static boolean isIssueOrPull(String commitMessage) {
        return commitMessage.startsWith("Issue") || commitMessage.startsWith("Pull");
    }

    /**
     * Forms a string which represents the authors who are referenced in the commits.
     * @param commits commits.
     * @return string which represents the authors who are referenced in the commits.
     */
    private static String getAuthorsOf(Set<RevCommit> commits) {
        final Set<String> commitAuthors = findCommitAuthors(commits);
        return Joiner.on(SEPARATOR).join(commitAuthors.iterator());
    }

    /**
     * Finds authors of the commits.
     * @param commits current issue commits.
     * @return a list of authors who work on the current issue.
     */
    private static Set<String> findCommitAuthors(Set<RevCommit> commits) {
        final Set<String> commitAuthors = new HashSet<>();
        for (RevCommit commit : commits) {
            final String author = commit.getAuthorIdent().getName();
            if (!commitAuthors.contains(author)) {
                commitAuthors.add(author);
            }
        }
        return commitAuthors;
    }

    /**
     * Returns issue label for release notes.
     * @param issue issue.
     * @return issue label for release notes
     * @throws IOException if an I/o error occurs.
     */
    private static String getIssueLabelFrom(GHIssue issue) throws IOException {
        final Collection<GHLabel> issueLabels =  issue.getLabels();
        final GHLabel label =  Iterables.tryFind(issueLabels, new Predicate<GHLabel>() {
            @Override
            public boolean apply(GHLabel input) {
                return Arrays.binarySearch(Constants.ISSUE_LABELS, input.getName()) != -1;
            }
            }).orNull();

        final String issueLabelName;
        if (label == null) {
            issueLabelName = "";
        }
        else {
            issueLabelName = label.getName();
        }
        return issueLabelName;
    }

    /**
     * Prints a list of elements in standard out.
     * @param entities a list.
     */
    private static void printListOf(List<String> entities) {
        System.out.println();
        for (String e : entities) {
            System.out.println(e);
        }
    }
}
