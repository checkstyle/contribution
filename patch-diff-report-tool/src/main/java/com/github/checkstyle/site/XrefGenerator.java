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

package com.github.checkstyle.site;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

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
     * Maven-jxr package manager.
     */
    private static PackageManager pacman;

    /**
     * Maven-jxr XREF file generator.
     */
    private static JavaCodeTransform codeTransform;

    static {
        pacman = new PackageManager(new JxrDummyLog(),
                new FileManager());
        codeTransform = new JavaCodeTransform(pacman);
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
     * Generates XREF file from source file.
     *
     * @param name
     *        path to the source file.
     * @return relative path to the resulting file.
     * @throws IOException
     *         on maven-jxr internal failure.
     */
    public final String generateXref(String name) throws IOException {
        final File sourceFile = new File(name);
        final Path dest = getDestinationPath(name);
        codeTransform.transform(sourceFile.getAbsolutePath(),
                dest.toString(), Locale.ENGLISH,
                ENCODING, ENCODING, "", "", "");
        return sitePath.relativize(dest).toString();
    }

    /**
     * Generates full path to the destination of XREF file.
     *
     * @param name
     *        java source file path.
     * @return full path to the destination of XREF file.
     */
    private Path getDestinationPath(String name) {
        final String newName = name + ".html";
        final Path sourcePath = Paths.get(newName);
        final Path destPath;
        if (relativizationPath == null) {
            destPath = destinationPath
            .resolve(sourcePath.subpath(0, sourcePath.getNameCount()));
        }
        else {
            destPath = destinationPath
            .resolve(relativizationPath.relativize(sourcePath));
        }
        return destPath;
    }
}
