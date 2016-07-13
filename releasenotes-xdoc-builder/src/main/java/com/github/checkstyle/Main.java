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

package com.github.checkstyle;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.google.common.collect.Multimap;

/**
 * Class for command line usage.
 * @author Andrei Selkin
 */
public final class Main {

    /** A path to remote checkstyle repository. */
    private static final String REMOTE_REPO_PATH = "checkstyle/checkstyle";

    /** Exit code returned when execution finishes with errors. */
    private static final int EXIT_WITH_ERRORS_CODE = -2;

    /** Default constructor. */
    private Main() { }

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
        catch (ParseException | GitAPIException | IOException ex) {
            errorCounter = 1;
            System.out.println(ex.getMessage());
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
        final Result result = NotesBuilder.buildResult(remoteRepo, localRepoPath, startRef, endRef);
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
