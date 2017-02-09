////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
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

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.github.checkstyle.internal.AbstractTest;

public class MainTest extends AbstractTest {
    private static final String VALID_BASE_REPORT = getPath("InputBaseReportDifferences.xml");
    private static final String VALID_PATCH_REPORT = getPath("InputPatchReportDifferences.xml");

    @Test
    public void testHelp() throws Exception {
        Main.main("-h");

        Assert.assertEquals("patch-diff-report-tool execution started.\n"
                + "This program creates symmetric difference from two"
                + " checkstyle-result.xml reports\n" + "generated for checkstyle build.\n"
                + "Command line arguments:\n"
                + "\t--baseReportPath - path to the base checkstyle-result.xml,"
                + " obligatory argument;\n"
                + "\t--patchReportPath - path to the patch checkstyle-result.xml,"
                + " also obligatory argument;\n"
                + "\t--sourcePath - path to the data under check (optional, if absent"
                + " then file structure for cross reference files won't be relativized,"
                + " full paths will be used);\n"
                + "\t--output - path to store the resulting diff report (optional,"
                + " if absent then default path will be used:"
                + " ~/XMLDiffGen_report_yyyy.mm.dd_hh_mm_ss), remember, if this folder"
                + " exists its content will be purged;\n" + "\t-h - simply shows help message.\n"
                + "patch-diff-report-tool execution finished.\n", getSystemOut());
    }

    @Test
    public void test() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport", VALID_PATCH_REPORT_EMPTY,
                VALID_BASE_CONFIG, "-output", outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportEmpty.html"), outputDirectory);

        assertFileExists("maven-base.css doesn't exist", new File(outputDirectory,
                "css/maven-base.css"));
        assertFileExists("maven-theme.css doesn't exist", new File(outputDirectory,
                "css/maven-theme.css"));

        final File xrefDirectory = new File(outputDirectory, "xref");

        assertDirectoryExists("xref doesn't exist", xrefDirectory);

        final String[] xrefList = xrefDirectory.list();

        Assert.assertTrue("xref must be empty", xrefList == null || xrefList.length == 0);
    }

    @Test
    public void testEmptyWithConfig() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport", VALID_PATCH_REPORT_EMPTY,
                "-baseConfig", VALID_BASE_CONFIG, "-patchConfig", VALID_BASE_CONFIG, "-output",
                outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportEmptyWithConfig.html"), outputDirectory);
    }

    @Test
    public void testDifferences() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-baseReport", VALID_BASE_REPORT, "-patchReport", VALID_PATCH_REPORT,
                "-baseConfig", VALID_BASE_CONFIG, "-patchConfig", VALID_BASE_CONFIG, "-output",
                outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportDifferences.html"), outputDirectory);

        File xrefDirectory = new File(outputDirectory, "xref");

        assertDirectoryExists("xref doesn't exist", xrefDirectory);

        String[] xrefList = xrefDirectory.list();

        Assert.assertArrayEquals(new String[] {"src"}, xrefList);

        xrefDirectory = new File(outputDirectory, "xref/src/test/resources/run");

        assertDirectoryExists("xref doesn't exist", xrefDirectory);

        xrefList = xrefDirectory.list();

        Assert.assertEquals(8, xrefList.length);
    }

    @Test
    public void testDifferencesRefFiles() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-baseReport", VALID_BASE_REPORT, "-patchReport", VALID_PATCH_REPORT,
                "-baseConfig", VALID_BASE_CONFIG, "-patchConfig", VALID_BASE_CONFIG, "-output",
                outputDirectory.getAbsolutePath(), "-refFiles", "src/test/resources/run");

        assertReportOutput(getPath("ExpectedReportDifferencesRefFiles.html"), outputDirectory);

        final File xrefDirectory = new File(outputDirectory, "xref");

        assertDirectoryExists("xref doesn't exist", xrefDirectory);

        final String[] xrefList = xrefDirectory.list();

        Assert.assertEquals(8, xrefList.length);
    }

    @Test
    public void testDifferencesShortFilePaths() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-baseReport", VALID_BASE_REPORT, "-patchReport", VALID_PATCH_REPORT,
                "-baseConfig", VALID_BASE_CONFIG, "-patchConfig", VALID_BASE_CONFIG, "-output",
                outputDirectory.getAbsolutePath(), "-shortFilePaths");

        assertReportOutput(getPath("ExpectedReportDifferencesShortFilePaths.html"),
                outputDirectory);

        final File xrefDirectory = new File(outputDirectory, "xref");

        assertDirectoryExists("xref doesn't exist", xrefDirectory);

        final String[] xrefList = xrefDirectory.list();

        Assert.assertEquals(8, xrefList.length);
    }

    @Test
    public void testSeverities() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                getPath("InputPatchReportSeverities.xml"), "-output",
                outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportSeverities.html"), outputDirectory);
    }
}
