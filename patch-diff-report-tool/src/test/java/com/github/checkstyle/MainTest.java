///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
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
                + Main.MSG_HELP + "\n"
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
        assertFileExists("site.css doesn't exist", new File(outputDirectory,
            "css/site.css"));

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

    @Test
    public void testPatchOnly() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-patchReport", VALID_PATCH_REPORT, "-patchConfig", VALID_BASE_CONFIG, "-output",
                outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportPatchOnly.html"), outputDirectory);
    }

    @Test
    public void testMessages() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-patchReport", getPath("InputPatchReportMessages.xml"), "-output",
                outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportMessages.html"), outputDirectory);
    }

    @Test
    public void testNonCompilableFile() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-patchReport", getPath("InputPatchReportNonCompilable.xml"), "-output",
                outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportNonCompilable.html"), outputDirectory);

        final File xrefFile = new File(outputDirectory,
                "xref/src/test/resources/run/NonCompilable.java.html");

        assertFileExists("NonCompilable.java.html doesn't exist", xrefFile);

        Assert.assertEquals(getFileContents(new File(getPath("ExpectedXrefNonCompilable.html"))),
                getFileContents(xrefFile));
    }

    @Test
    public void testTextMode() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-compareMode", "text", "-baseReport", VALID_BASE_DIR, "-patchReport",
                VALID_PATCH_DIR, "-output", outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportTextMode.html"), outputDirectory);
    }

    @Test
    public void testConfigMessages() throws Exception {
        final File outputDirectory = folder.getRoot();

        Main.main("-patchReport", getPath("InputPatchReportEmpty.xml"), "-patchConfig",
                getPath("InputPatchConfigMessages.xml"), "-output",
                outputDirectory.getAbsolutePath());

        assertReportOutput(getPath("ExpectedReportConfigMessages.html"), outputDirectory);
    }

    @Test
    public void testConstructor() throws Exception {
        assertUtilsClassHasPrivateConstructor(Main.class);
    }
}
