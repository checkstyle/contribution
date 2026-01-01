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

package com.github.checkstyle;

import java.nio.file.Files;

import com.github.checkstyle.data.CliOptions;
import com.github.checkstyle.data.CompareMode;

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
     * Performs validation of the options.
     *
     * @param options
     *            POJO holding all options.
     * @throws IllegalArgumentException
     *             on failure of any check.
     */
    public static void validate(CliOptions options) throws IllegalArgumentException {
        if (options.getPatchReportPath() == null) {
            throw new IllegalArgumentException("obligatory argument --patchReportPath "
                    + "not present, -h for help");
        }
        if (options.getRefFilesPath() != null && !Files.isDirectory(options.getRefFilesPath())) {
            throw new IllegalArgumentException("Ref Files path is not a directory: "
                    + options.getRefFilesPath());
        }
        if (Files.isRegularFile(options.getOutputPath())) {
            throw new IllegalArgumentException("Output path is not a directory: "
                    + options.getOutputPath());
        }

        if (options.getCompareMode() == CompareMode.XML) {
            validateXmlMode(options);
        }
        else {
            validateTextMode(options);
        }
    }

    /**
     * Performs validation of the options in XML compare mode.
     *
     * @param options
     *            POJO holding all options.
     * @throws IllegalArgumentException
     *             on failure of any check.
     */
    private static void validateXmlMode(CliOptions options) {
        if (!Files.isRegularFile(options.getPatchReportPath())) {
            throw new IllegalArgumentException("Patch XML Report file doesn't exist: "
                    + options.getPatchReportPath());
        }
        if (options.getPatchConfigPath() != null
                && !Files.isRegularFile(options.getPatchConfigPath())) {
            throw new IllegalArgumentException(
                    "Patch checkstyle configuration xml file is missing: "
                            + options.getPatchConfigPath());
        }
        if (options.getBaseReportPath() == null) {
            if (options.getBaseConfigPath() != null) {
                throw new IllegalArgumentException("Base checkstyle configuration xml path is "
                        + "missing while base configuration path is present.");
            }
        }
        else {
            if (!Files.isRegularFile(options.getBaseReportPath())) {
                throw new IllegalArgumentException("Base XML Report file doesn't exist: "
                        + options.getBaseReportPath());
            }
            if (options.getPatchReportPath().equals(options.getBaseReportPath())) {
                throw new IllegalArgumentException(
                        "Both Base and Patch XML report files have the same path.");
            }
            if (options.getBaseConfigPath() != null && options.getPatchConfigPath() == null) {
                throw new IllegalArgumentException(
                        "Patch checkstyle configuration xml path is missing while base "
                                + "configuration path is present");
            }
            if (options.getPatchConfigPath() != null && options.getBaseConfigPath() == null) {
                throw new IllegalArgumentException(
                        "Base checkstyle configuration xml path is missing while patch "
                                + "configuration path is present");
            }
            if (options.getBaseConfigPath() != null
                    && !Files.isRegularFile(options.getBaseConfigPath())) {
                throw new IllegalArgumentException(
                        "Base checkstyle configuration xml file is missing: "
                                + options.getBaseConfigPath());
            }
        }
    }

    /**
     * Performs validation of the options in Text compare mode.
     *
     * @param options
     *            POJO holding all options.
     * @throws IllegalArgumentException
     *             on failure of any check.
     */
    private static void validateTextMode(CliOptions options) {
        if (options.getBaseConfigPath() != null || options.getPatchConfigPath() != null) {
            throw new IllegalArgumentException(
                    "Checkstyle configuration xml paths do not need to be present for "
                            + "text mode.");
        }
        if (!Files.isDirectory(options.getPatchReportPath())) {
            throw new IllegalArgumentException("Patch Report directory doesn't exist: "
                    + options.getPatchReportPath());
        }
        if (!Files.isDirectory(options.getBaseReportPath())) {
            throw new IllegalArgumentException("Base Report directory doesn't exist: "
                    + options.getBaseReportPath());
        }
    }
}
