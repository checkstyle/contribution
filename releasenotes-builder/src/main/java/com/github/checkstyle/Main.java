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

package com.github.checkstyle;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.github.checkstyle.github.NotesBuilder;
import com.github.checkstyle.globals.Result;
import freemarker.template.TemplateException;

/**
 * Class for command line usage.
 *
 * @author Andrei Selkin
 */
public final class Main {

    /** Exit code returned when execution finishes with errors. */
    private static final int ERROR_EXIT_CODE = -2;

    /** Default constructor. */
    private Main() {
    }

    /**
     * Entry point.
     *
     * @param args command line arguments.
     */
    public static void main(String... args) {
        int errorCounter;
        List<String> publicationErrors = null;
        try {
            final CliProcessor cliProcessor = new CliProcessor(args);
            cliProcessor.process();
            if (cliProcessor.hasErrors()) {
                printListOf(cliProcessor.getErrorMessages());
                errorCounter = cliProcessor.getErrorMessages().size();
            }
            else {
                final CliOptions cliOptions = cliProcessor.getCliOptions();
                final Result notesBuilderResult = runGithubNotesBuilder(cliOptions);
                errorCounter = notesBuilderResult.getErrorMessages().size();
                if (errorCounter == 0) {
                    publicationErrors = MainProcess.run(notesBuilderResult.getReleaseNotes(),
                            cliOptions);
                }
            }
        }
        catch (ParseException | GitAPIException | IOException | TemplateException ex) {
            errorCounter = 1;
            System.out.println(ex.getMessage());
            CliProcessor.printUsage();
        }
        if (errorCounter == 0) {
            if (publicationErrors != null && !publicationErrors.isEmpty()) {
                System.out.println(String.format("%nPublication ends with %d errors:",
                        publicationErrors.size()));
                printListOf(publicationErrors);
            }
            else {
                System.out.println(String.format("%nExecution succeeded!"));
            }
        }
        else {
            System.out.println(String.format("%nGeneration ends with %d errors.",
                errorCounter));
            System.exit(ERROR_EXIT_CODE);
        }
    }

    /**
     * Executes NotesBuilder based on passed command line options.
     *
     * @param cliOptions command line options.
     * @return result of NotesBuilder work.
     * @throws IOException if an I/O error occurs.
     * @throws GitAPIException if an error occurs while accessing GitHub API.
     */
    private static Result runGithubNotesBuilder(CliOptions cliOptions)
            throws IOException, GitAPIException {

        final String localRepoPath = cliOptions.getLocalRepoPath();
        final String authToken = cliOptions.getAuthToken();
        final String remoteRepoPath = cliOptions.getRemoteRepoPath();
        final String startRef = cliOptions.getStartRef();
        final String endRef = cliOptions.getEndRef();

        final Result result = NotesBuilder.buildResult(localRepoPath, authToken, remoteRepoPath,
                startRef, endRef);
        if (result.hasWarnings()) {
            printListOf(result.getWarningMessages());
        }
        if (result.hasErrors()) {
            printListOf(result.getErrorMessages());
        }
        return result;
    }

    /**
     * Prints a list of elements in standard out.
     *
     * @param entities a list.
     */
    private static void printListOf(List<String> entities) {
        System.out.println();
        for (String e : entities) {
            System.out.println(e);
        }
    }
}
