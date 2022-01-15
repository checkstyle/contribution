////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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

package com.github.checkstyle.internal;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class AbstractTest {
    protected static final String VALID_BASE_REPORT_EMPTY = getPath("InputBaseReportEmpty.xml");
    protected static final String VALID_PATCH_REPORT_EMPTY = getPath("InputPatchReportEmpty.xml");
    protected static final String VALID_BASE_CONFIG = getPath("InputBaseConfig.xml");

    protected static final String VALID_BASE_DIR = getPath("runText/base");
    protected static final String VALID_PATCH_DIR = getPath("runText/patch");

    protected static final ByteArrayOutputStream OUT_CONTENT = new ByteArrayOutputStream();

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(OUT_CONTENT));
    }

    @After
    public void cleanUpStreams() {
        System.setOut(null);
        OUT_CONTENT.reset();
    }

    protected static String getSystemOut() {
        return OUT_CONTENT.toString().replace("\r\n", "\n");
    }

    protected static String getPath(String path) {
        return "src/test/resources/" + path;
    }

    /**
     * Verifies that utils class has private constructor and invokes it to
     * satisfy code coverage.
     */
    protected static void assertUtilsClassHasPrivateConstructor(final Class<?> utilClass)
            throws ReflectiveOperationException {
        final Constructor<?> constructor = utilClass.getDeclaredConstructor();
        if (!Modifier.isPrivate(constructor.getModifiers())) {
            Assert.fail("Constructor is not private");
        }
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    protected static void assertReportOutput(String expectedOutputPath, File actualOutput)
            throws IOException {
        final File expectedOutputFile = new File(expectedOutputPath);

        assertFileExists("expected index.html doesn't exist", expectedOutputFile);

        Assert.assertTrue("actual output doesn't exist",
                actualOutput.exists() && actualOutput.isDirectory());

        final File actualOutputFile = new File(actualOutput, "index.html");

        assertFileExists("actual index.html doesn't exist", actualOutputFile);

        Assert.assertEquals(getFileContents(expectedOutputFile), getFileContents(actualOutputFile));
    }

    protected static void assertFileExists(String message, File actualFile) {
        Assert.assertTrue(message,
                actualFile.exists() && actualFile.isFile() && actualFile.canRead());
    }

    protected static void assertDirectoryExists(String message, File actualDirectory) {
        Assert.assertTrue(message, actualDirectory.exists() && actualDirectory.isDirectory()
                && actualDirectory.canRead());
    }

    protected static String getFileContents(File file) throws IOException {
        final StringBuilder result = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            do {
                line = br.readLine();

                if (line == null) {
                    break;
                }

                // fix for different OS runs as file path is printed in OS'
                // format. Windows prints with '\' and unix prints with '/'.
                if (line.contains("<a href = ") || (line.contains("<h3>"))) {
                    line = line.replace("\\", "/");
                }

                result.append(line);
                result.append('\n');
            } while (true);
        }

        return result.toString();
    }
}
