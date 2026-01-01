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

package com.github.checkstyle.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class AbstractPathTestSupport {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        temporaryFolder.delete();
        temporaryFolder.create();
    }

    /**
     * Retrieves the temporary folder.
     *
     * @return The temporary folder.
     */
    protected File getTempFolder() {
        return temporaryFolder.getRoot();
    }

    /**
     * Returns canonical path for the file with the given file name.
     * The path is formed base on the root location.
     * This implementation uses 'src/test/resources/'
     * as a root location.
     *
     * @param fileName file name.
     * @return canonical path for the file name.
     * @throws IOException if I/O exception occurs while forming the path.
     */
    protected static String getPath(String fileName) throws IOException {
        return new File("src/test/resources/com/github/checkstyle/" + fileName).getCanonicalPath();
    }

    /**
     * Asserts file equals the expected result.
     *
     * @param expectedName Expected file path to read.
     * @param actualName Actual file path to read.
     * @throws IOException if there is an error reading the file.
     */
    protected void assertFile(String expectedName, String actualName) throws IOException {
        final String expected =
                getFileContents(new File(getPath(expectedName)));
        final String actual = getFileContents(new File(getTempFolder(), actualName))
                .replace(LocalDate.now()
                                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US)),
                        "XX.XX.XXXX");

        Assert.assertEquals("expected " + actualName, expected, actual);
    }

    /**
     * Asserts file does not exist.
     *
     * @param actualName Actual file path to check.
     */
    protected void assertFile(String actualName) {
        Assert.assertFalse("file does not exist",
            new File(temporaryFolder.getRoot(), actualName).exists());
    }

    protected static String getFileContents(File file) throws IOException {
        final StringBuilder result = new StringBuilder(256);

        try (BufferedReader br = Files.newBufferedReader(file.toPath())) {
            do {
                final String line = br.readLine();

                if (line == null) {
                    break;
                }

                result.append(line);
                result.append('\n');
            } while (true);
        }

        return result.toString();
    }

}
