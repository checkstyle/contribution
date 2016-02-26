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

import java.nio.file.Path;

/**
 * POJO class that hold input paths.
 * @author atta_troll
 */
public class CliPaths {
    /**
     * Path to the first checkstyle-report.xml.
     */
    private Path baseReportPath;

    /**
     * Path to the second checkstyle-report.xml.
     */
    private Path patchReportPath;

    /**
     * Path to the data, tested by checkstyle.
     */
    private Path sourcePath;

    /**
     * Path to the result site.
     */
    private Path resultPath;

    /**
     * POJO ctor.
     *
     * @param baseReportPath1
     *        path to the first checkstyle-report.xml.
     * @param patchReportPath1
     *        path to the second checkstyle-report.xml.
     * @param sourcePath1
     *        path to the data, tested by checkstyle.
     * @param resultPath1
     *        path to the result site.
     */
    public CliPaths(Path baseReportPath1, Path patchReportPath1,
            Path sourcePath1, Path resultPath1) {
        this.baseReportPath = baseReportPath1;
        this.patchReportPath = patchReportPath1;
        this.sourcePath = sourcePath1;
        this.resultPath = resultPath1;
    }

    public Path getBaseReportPath() {
        return baseReportPath;
    }

    public Path getPatchReportPath() {
        return patchReportPath;
    }

    public Path getSourcePath() {
        return sourcePath;
    }

    public Path getResultPath() {
        return resultPath;
    }

}
