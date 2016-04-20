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

import java.nio.file.Files;

import com.github.checkstyle.data.CliPaths;

/**
 * Checker for paths validity.
 *
 * @author attatrol
 *
 */
public final class CliArgsValidator {

    /**
     * Message when necessary file is absent.
     */
    public static final String MSG_NOT_EXISTS = "XML file doesn't exist: ";

    /**
     * Private ctor, use static methods.
     */
    private CliArgsValidator() {

    }

    /**
     * Performs file existence checks.
     *
     * @param paths
     *        POJO holding all input paths.
     * @throws IllegalArgumentException
     *         on failure of any check.
     */
    public static void checkPaths(CliPaths paths)
                    throws IllegalArgumentException {
        if (paths.getBaseReportPath() == null) {
            throw new IllegalArgumentException("obligatory argument --baseReportPath"
                    + " not present, -h for help");
        }
        if (paths.getPatchReportPath() == null) {
            throw new IllegalArgumentException("obligatory argument --patchReportPath "
                    + "not present, -h for help");
        }
        if (!Files.isRegularFile(paths.getBaseReportPath())) {
            throw new IllegalArgumentException("Base XML Report file doesn't exist: "
                    + paths.getBaseReportPath());
        }
        if (!Files.isRegularFile(paths.getPatchReportPath())) {
            throw new IllegalArgumentException("Patch XML Report file doesn't exist: "
                    + paths.getPatchReportPath());
        }
        if (paths.getPatchReportPath().equals(paths.getBaseReportPath())) {
            throw new IllegalArgumentException(
                    "Both Base and Patch XML report files have the same path.");
        }
        if (Files.isRegularFile(paths.getResultPath())) {
            throw new IllegalArgumentException("Output path is not a directory: "
                    + paths.getResultPath());
        }
        if (paths.getSourcePath() != null && !Files.isDirectory(paths.getSourcePath())) {
            throw new IllegalArgumentException("Source path is not a directory: "
                    + paths.getSourcePath());
        }
        if ((paths.getBaseConfigPath() != null) && (paths.getPatchConfigPath() == null)) {
            throw new IllegalArgumentException(
                    "Patch checkstyle configuration xml path is missing while base "
                            + "configuration path is present");
        }
        if ((paths.getPatchConfigPath() != null) && (paths.getBaseConfigPath() == null)) {
            throw new IllegalArgumentException(
                    "Base checkstyle configuration xml path is missing while patch "
                            + "configuration path is present");
        }
        if (paths.getBaseConfigPath() != null && !Files.isRegularFile(paths.getBaseConfigPath())) {
            throw new IllegalArgumentException(
                    "Base checkstyle configuration xml file is missing: "
                            + paths.getBaseConfigPath());
        }
        if (paths.getPatchConfigPath() != null
                && !Files.isRegularFile(paths.getPatchConfigPath())) {
            throw new IllegalArgumentException(
                    "Patch checkstyle configuration xml file is missing: "
                            + paths.getPatchConfigPath());
        }
    }

}
