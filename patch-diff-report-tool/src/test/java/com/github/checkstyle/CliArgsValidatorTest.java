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

import org.junit.Assert;
import org.junit.Test;

import com.github.checkstyle.internal.AbstractTest;

public class CliArgsValidatorTest extends AbstractTest {
    @Test
    public void testConstructor() throws Exception {
        assertUtilsClassHasPrivateConstructor(CliArgsValidator.class);
    }

    @Test
    public void testNoPatchReportPath() throws Exception {
        try {
            Main.main();
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("obligatory argument --patchReportPath not present, -h for help",
                    exc.getMessage());
        }
    }

    @Test
    public void testInvalidBaseReportPath() throws Exception {
        try {
            Main.main("-baseReport", "test", "-patchReport", VALID_BASE_REPORT_EMPTY);
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Base XML Report file doesn't exist: test", exc.getMessage());
        }
    }

    @Test
    public void testInvalidPatchReportPath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport", "test");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Patch XML Report file doesn't exist: test", exc.getMessage());
        }
    }

    @Test
    public void testBatchPatchReportSamePath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_BASE_REPORT_EMPTY);
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Both Base and Patch XML report files have the same path.",
                    exc.getMessage());
        }
    }

    @Test
    public void testInvalidOutputPath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-output", "pom.xml");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Output path is not a directory: pom.xml", exc.getMessage());
        }
    }

    @Test
    public void testInvalidRefFilesPath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-refFiles", "pom.xml");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Ref Files path is not a directory: pom.xml", exc.getMessage());
        }
    }

    @Test
    public void testBaseConfigAlone() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-baseConfig", "test");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals(
                    "Patch checkstyle configuration xml path is missing while base configuration"
                            + " path is present", exc.getMessage());
        }
    }

    @Test
    public void testPatchConfigAlone() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-patchConfig", VALID_BASE_CONFIG);
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals(
                    "Base checkstyle configuration xml path is missing while patch configuration"
                            + " path is present", exc.getMessage());
        }
    }

    @Test
    public void testInvalidBaseConfig() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-baseConfig", "test", "-patchConfig",
                    VALID_BASE_CONFIG);
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Base checkstyle configuration xml file is missing: test",
                    exc.getMessage());
        }
    }

    @Test
    public void testInvalidPatchConfig() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-baseConfig", VALID_BASE_CONFIG, "-patchConfig",
                    "test");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Patch checkstyle configuration xml file is missing: test",
                    exc.getMessage());
        }
    }

    @Test
    public void testBaseConfigWithoutBaseReport() throws Exception {
        try {
            Main.main("-patchReport", VALID_PATCH_REPORT_EMPTY, "-baseConfig", VALID_BASE_CONFIG,
                    "-patchConfig", VALID_BASE_CONFIG);
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals(
                    "Base checkstyle configuration xml path is missing while base configuration "
                            + "path is present.",
                    exc.getMessage());
        }
    }

    @Test
    public void testTextBaseConfigGiven() throws Exception {
        try {
            Main.main("-compareMode", "text", "-patchReport", VALID_PATCH_REPORT_EMPTY,
                    "-baseConfig", "test");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Checkstyle configuration xml paths do not need to be present for "
                    + "text mode.", exc.getMessage());
        }
    }

    @Test
    public void testTextPatchConfigGiven() throws Exception {
        try {
            Main.main("-compareMode", "text", "-patchReport", VALID_PATCH_REPORT_EMPTY,
                    "-patchConfig", "test");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Checkstyle configuration xml paths do not need to be present for "
                    + "text mode.", exc.getMessage());
        }
    }

    @Test
    public void testTextInvalidPatchReportPath() throws Exception {
        try {
            Main.main("-compareMode", "text", "-patchReport", "test");
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Patch Report directory doesn't exist: test", exc.getMessage());
        }
    }

    @Test
    public void testTextInvalidBaseReportPath() throws Exception {
        try {
            Main.main("-compareMode", "text", "-baseReport", "test", "-patchReport",
                    VALID_PATCH_DIR);
        }
        catch (IllegalArgumentException exc) {
            Assert.assertEquals("Base Report directory doesn't exist: test", exc.getMessage());
        }
    }
}
