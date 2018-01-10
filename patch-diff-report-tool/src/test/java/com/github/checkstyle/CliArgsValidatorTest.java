////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2018 the original author or authors.
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
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("obligatory argument --patchReportPath not present, -h for help",
                    ex.getMessage());
        }
    }

    @Test
    public void testInvalidBaseReportPath() throws Exception {
        try {
            Main.main("-baseReport", "test", "-patchReport", VALID_BASE_REPORT_EMPTY);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Base XML Report file doesn't exist: test", ex.getMessage());
        }
    }

    @Test
    public void testInvalidPatchReportPath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport", "test");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Patch XML Report file doesn't exist: test", ex.getMessage());
        }
    }

    @Test
    public void testBatchPatchReportSamePath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_BASE_REPORT_EMPTY);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Both Base and Patch XML report files have the same path.",
                    ex.getMessage());
        }
    }

    @Test
    public void testInvalidOutputPath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-output", "pom.xml");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Output path is not a directory: pom.xml", ex.getMessage());
        }
    }

    @Test
    public void testInvalidRefFilesPath() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-refFiles", "pom.xml");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Ref Files path is not a directory: pom.xml", ex.getMessage());
        }
    }

    @Test
    public void testBaseConfigAlone() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-baseConfig", "test");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(
                    "Patch checkstyle configuration xml path is missing while base configuration"
                            + " path is present", ex.getMessage());
        }
    }

    @Test
    public void testPatchConfigAlone() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-patchConfig", VALID_BASE_CONFIG);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(
                    "Base checkstyle configuration xml path is missing while patch configuration"
                            + " path is present", ex.getMessage());
        }
    }

    @Test
    public void testInvalidBaseConfig() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-baseConfig", "test", "-patchConfig",
                    VALID_BASE_CONFIG);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Base checkstyle configuration xml file is missing: test",
                    ex.getMessage());
        }
    }

    @Test
    public void testInvalidPatchConfig() throws Exception {
        try {
            Main.main("-baseReport", VALID_BASE_REPORT_EMPTY, "-patchReport",
                    VALID_PATCH_REPORT_EMPTY, "-baseConfig", VALID_BASE_CONFIG, "-patchConfig",
                    "test");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Patch checkstyle configuration xml file is missing: test",
                    ex.getMessage());
        }
    }

    @Test
    public void testBaseConfigWithoutBaseReport() throws Exception {
        try {
            Main.main("-patchReport", VALID_PATCH_REPORT_EMPTY, "-baseConfig", VALID_BASE_CONFIG,
                    "-patchConfig", VALID_BASE_CONFIG);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals(
                    "Base checkstyle configuration xml path is missing while base configuration "
                            + "path is present.",
                    ex.getMessage());
        }
    }

    @Test
    public void testTextBaseConfigGiven() throws Exception {
        try {
            Main.main("-compareMode", "text", "-patchReport", VALID_PATCH_REPORT_EMPTY,
                    "-baseConfig", "test");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Checkstyle configuration xml paths do not need to be present for "
                    + "text mode.", ex.getMessage());
        }
    }

    @Test
    public void testTextPatchConfigGiven() throws Exception {
        try {
            Main.main("-compareMode", "text", "-patchReport", VALID_PATCH_REPORT_EMPTY,
                    "-patchConfig", "test");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Checkstyle configuration xml paths do not need to be present for "
                    + "text mode.", ex.getMessage());
        }
    }

    @Test
    public void testTextInvalidPatchReportPath() throws Exception {
        try {
            Main.main("-compareMode", "text", "-patchReport", "test");
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Patch Report directory doesn't exist: test", ex.getMessage());
        }
    }

    @Test
    public void testTextInvalidBaseReportPath() throws Exception {
        try {
            Main.main("-compareMode", "text", "-baseReport", "test", "-patchReport",
                    VALID_PATCH_DIR);
        }
        catch (IllegalArgumentException ex) {
            Assert.assertEquals("Base Report directory doesn't exist: test", ex.getMessage());
        }
    }
}
