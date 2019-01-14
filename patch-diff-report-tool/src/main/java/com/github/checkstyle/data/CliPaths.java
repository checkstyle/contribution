////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2019 the original author or authors.
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

package com.github.checkstyle.data;

import java.nio.file.Path;

/**
 * POJO class that hold input paths.
 * @author attatrol
 */
public final class CliPaths {
    /**
     * Option to control which type of diff comparison to do.
     */
    private final CompareMode compareMode;

    /**
     * Path to the first checkstyle-report.xml.
     */
    private final Path baseReportPath;

    /**
     * Path to the second checkstyle-report.xml.
     */
    private final Path patchReportPath;

    /**
     * Path to the data, tested by checkstyle.
     */
    private final Path refFilesPath;

    /**
     * Path to the result site.
     */
    private final Path outputPath;

    /**
     * Path to the data, tested by checkstyle.
     */
    private final Path baseConfigPath;

    /**
     * Path to the result site.
     */
    private final Path patchConfigPath;

    /**
     * Switch specifying if only short file names should be used with no path.
     */
    private final boolean shortFilePaths;

    /**
     * POJO ctor.
     *
     * @param compareMode
     *        type of diff comparison to do.
     * @param baseReportPath
     *        path to the base checkstyle-report.xml.
     * @param patchReportPath
     *        path to the patch checkstyle-report.xml.
     * @param refFilesPath
     *        path to the data, tested by checkstyle.
     * @param outputPath
     *        path to the result site.
     * @param patchConfigPath
     *        path to the configuration of the base report.
     * @param baseConfigPath
     *        path to the configuration of the patch report.
     * @param shortFilePaths
     *           {@code true} if only short file names should be used with no paths.
     */
    // -@cs[ParameterNumber] Helper class to pass all CLI attributes around.
    public CliPaths(CompareMode compareMode, Path baseReportPath, Path patchReportPath,
            Path refFilesPath, Path outputPath, Path baseConfigPath, Path patchConfigPath,
            boolean shortFilePaths) {
        this.compareMode = compareMode;
        this.baseReportPath = baseReportPath;
        this.patchReportPath = patchReportPath;
        this.refFilesPath = refFilesPath;
        this.outputPath = outputPath;
        this.baseConfigPath = baseConfigPath;
        this.patchConfigPath = patchConfigPath;
        this.shortFilePaths = shortFilePaths;
    }

    public CompareMode getCompareMode() {
        return compareMode;
    }

    public Path getBaseReportPath() {
        return baseReportPath;
    }

    public Path getPatchReportPath() {
        return patchReportPath;
    }

    public Path getRefFilesPath() {
        return refFilesPath;
    }

    public Path getOutputPath() {
        return outputPath;
    }

    public Path getBaseConfigPath() {
        return baseConfigPath;
    }

    public Path getPatchConfigPath() {
        return patchConfigPath;
    }

    public boolean isShortFilePaths() {
        return shortFilePaths;
    }

    /**
     * Checks if the necessary configuration paths are present to display them on the reports.
     *
     * @return true if they are not null.
     */
    public boolean configurationPresent() {
        return patchConfigPath != null;
    }

}
