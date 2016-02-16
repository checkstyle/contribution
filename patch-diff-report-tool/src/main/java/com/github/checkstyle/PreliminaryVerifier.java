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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Initial checks performer.
 *
 * @author atta_troll
 *
 */
public final class PreliminaryVerifier {

    /**
     * Message when necessary file is absent.
     */
    public static final String MSG_NOT_EXISTS = "XML file doesn't exist: ";

    /**
     * Utility ctor.
     */
    private PreliminaryVerifier() {

    }

    /**
     * Perform preliminary file existence checks, also exports to disc necessary static resources.
     *
     * @param paths
     *        POJO holding all input paths.
     * @throws IOException
     *         thrown on failure to perform checks.
     */
    public static void prepare(CliPathsHolder paths)
            throws IOException {
        verifyExistense(paths);
        final Path resultPath = paths.getResultPath();
        FilesystemUtils.createOverwriteDirectory(resultPath);
        FilesystemUtils.createOverwriteDirectory(resultPath
                .resolve(com.github.checkstyle.Main.CSS_FILEPATH));
        FilesystemUtils.createOverwriteDirectory(resultPath
                .resolve(com.github.checkstyle.Main.XREF_FILEPATH));
        FilesystemUtils.exportResource("/maven-theme.css",
                resultPath.resolve(com.github.checkstyle.Main.CSS_FILEPATH)
                        .resolve("maven-theme.css"));
        FilesystemUtils.exportResource("/maven-base.css",
                resultPath.resolve(com.github.checkstyle.Main.CSS_FILEPATH)
                        .resolve("maven-base.css"));
    }

    /**
     * Performs file existence checks.
     * @param paths
     *        POJO holding all input paths.
     * @throws IllegalArgumentException
     *         on failure of any check.
     */
    private static void verifyExistense(CliPathsHolder paths)
                    throws IllegalArgumentException {
        if (!Files.isRegularFile(paths.getBaseReportPath())) {
            throw new IllegalArgumentException(MSG_NOT_EXISTS + paths.getBaseReportPath());
        }
        if (!Files.isRegularFile(paths.getPatchReportPath())) {
            throw new IllegalArgumentException(MSG_NOT_EXISTS + paths.getPatchReportPath());
        }
        if (paths.getPatchReportPath().equals(paths.getBaseReportPath())) {
            throw new IllegalArgumentException("Both input XML files have the same path.");
        }
        if (Files.isRegularFile(paths.getResultPath())) {
            throw new IllegalArgumentException("Unknown regular file exists with this name: "
                    + paths.getResultPath());
        }
        if (paths.getSourcePath() != null && !Files.isDirectory(paths.getSourcePath())) {
            throw new IllegalArgumentException(MSG_NOT_EXISTS + paths.getSourcePath());
        }
    }
}
