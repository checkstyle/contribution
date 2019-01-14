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

package com.github.checkstyle.site;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.jxr.JavaCodeTransform;
import org.apache.maven.jxr.pacman.FileManager;
import org.apache.maven.jxr.pacman.PackageManager;

/**
 * Constructor for cross reference HTMLs
 * from java source files. Wrapper around
 * maven-jxr functional.
 *
 * @author attatrol
 *
 */
class XrefGenerator {

    /**
     * Encoding used for input and output files.
     */
    public static final String ENCODING = "UTF-8";

    /**
     * File extension used for reports.
     */
    private static final String FILE_EXTENSION = ".html";

    /**
     * List of file maps for {@code shortFileNames} option in
     * {@link #getDestinationPath(String, boolean)}.
     */
    private static final Map<String, Path> SIMPLE_FILE_NAME_MAP = new HashMap<>();

    /**
     * File counter only to be used with {@code shortFileNames} option in
     * {@link #getDestinationPath(String, boolean)}.
     */
    private static int simpleFileNameCounter;

    /**
     * Maven-jxr package manager.
     */
    private static PackageManager pacman;

    /**
     * Maven-jxr XREF file generator.
     */
    private static JavaCodeTransform codeTransform;

    /**
     * Text XREF file generator.
     */
    private static TextTransform textTransform;

    static {
        pacman = new PackageManager(new JxrDummyLog(),
                new FileManager());
        codeTransform = new JavaCodeTransform(pacman);
        textTransform = new TextTransform();
    }

    /**
     * Path to the sources, used to shorten paths.
     */
    private Path relativizationPath;

    /**
     * Destination folder for XREF files.
     */
    private Path destinationPath;

    /**
     * Path to the site.
     */
    private Path sitePath;

    /**
     * The only constructor.
     *
     * @param relativizationPath
     *        path to the sources, used to shorten paths.
     * @param destinationPath
     *        destination folder for XREF files.
     * @param sitePath
     *        path to the site.
     */
    XrefGenerator(Path relativizationPath,
            Path destinationPath, Path sitePath) {
        this.relativizationPath = relativizationPath;
        this.destinationPath = destinationPath;
        this.sitePath = sitePath;
    }

    /**
     * Resets contents of the class.
     */
    public void reset() {
        SIMPLE_FILE_NAME_MAP.clear();
    }

    /**
     * Generates XREF file from source file.
     *
     * @param name
     *        path to the source file.
     * @param shortFilePaths
     *           {@code true} if only short file names should be used with no path.
     * @return relative path to the resulting file.
     */
    public final String generateXref(String name, boolean shortFilePaths) {
        final File sourceFile = new File(name);
        final Path dest = getDestinationPath(name, shortFilePaths);
        String result;
        if (!dest.toFile().exists()) {
            try {
                codeTransform.transform(sourceFile.getAbsolutePath(),
                    dest.toString(), Locale.ENGLISH,
                    ENCODING, ENCODING, null, "", "");
            }
            // -@cs[IllegalCatch] We need to catch all exceptions from JXR
            catch (Exception ex) {
                try {
                    textTransform.transform(sourceFile.getAbsolutePath(),
                        dest.toString(), Locale.ENGLISH,
                        ENCODING, ENCODING);
                }
                catch (IOException ignore) {
                    result = null;
                }
            }
        }
        result = sitePath.relativize(dest).toString();
        return result;
    }

    /**
     * Generates full path to the destination of XREF file.
     *
     * @param name
     *        java source file path.
     * @param shortFilePaths
     *           {@code true} if only short file names should be used with no path.
     * @return full path to the destination of XREF file.
     */
    private Path getDestinationPath(String name, boolean shortFilePaths) {
        Path destPath;

        if (shortFilePaths) {
            destPath = SIMPLE_FILE_NAME_MAP.get(name);

            if (destPath == null) {
                simpleFileNameCounter++;
                destPath = Paths.get(destinationPath + "/File" + simpleFileNameCounter
                        + FILE_EXTENSION);

                SIMPLE_FILE_NAME_MAP.put(name, destPath);
            }
        }
        else {
            final String newName = name + FILE_EXTENSION;
            final Path sourcePath = Paths.get(newName);
            if (relativizationPath == null) {
                destPath = destinationPath
                    .resolve(sourcePath.subpath(0, sourcePath.getNameCount()));
            }
            else {
                destPath = destinationPath
                    .resolve(relativizationPath.relativize(sourcePath));
            }
        }
        return destPath;
    }
}
