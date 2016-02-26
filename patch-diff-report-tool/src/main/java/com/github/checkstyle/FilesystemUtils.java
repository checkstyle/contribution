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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class used for common manipulations with files.
 *
 * @author atta_troll
 *
 */
public final class FilesystemUtils {

    /**
     * Buffer size for buffered stream.
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * Private ctor.
     */
    private FilesystemUtils() {

    }

    /**
     * Deletes the file, if it is a directory, deletes its content recursively.
     *
     * @param root
     *        path to the file.
     * @throws IOException
     *         thrown on filesystem error.
     */
    public static void delete(Path root)
            throws IOException {
        if (Files.isDirectory(root)) {
            final DirectoryStream<Path> subPaths =
                Files.newDirectoryStream(root);
            for (Path path : subPaths) {
                delete(path);
            }
            subPaths.close();
            Files.delete(root);
        }
        else {
            Files.delete(root);
        }
    }

    /**
     * Creates new directory or overwrites existing one.
     *
     * @param path
     *        path to the directory.
     * @throws IOException
     *         thrown on filesystem error.
     */
    public static void createOverwriteDirectory(Path path)
            throws IOException {
        if (Files.exists(path)) {
            delete(path);
        }
        Files.createDirectory(path);
    }

    /**
     * Safely copies files and directories with its content.
     *
     * @param source
     *        path to the existing file.
     * @param destination
     *        path to the desired location.
     * @throws IllegalArgumentException
     *         thrown on failure to resolve filename conflict.
     * @throws IOException
     *         thrown on filesystem error.
     */
    public static void copy(Path source, Path destination)
            throws IllegalArgumentException,
            IOException {
        if (Files.isDirectory(source)) {
            if (Files.exists(destination)) {
                if (!Files.isDirectory(destination)) {
                    throw new IllegalArgumentException(String.format(
                            "fail to copy %s to %s "
                                    + "because former is not a directory",
                            source.toString(),
                            destination.toString()));
                }
            }
            else {
                Files.createDirectory(destination);
            }
            final DirectoryStream<Path> subPaths =
                Files.newDirectoryStream(source);
            for (Path path : subPaths) {
                final Path relativePath = source.relativize(path);
                copy(path, destination.resolve(relativePath));
            }
            subPaths.close();

        }
        else {
            Files.copy(source, destination);
        }
    }

    /**
     * Safely copies file, creates destination folder if needed.
     *
     * @param source
     *        path to the file
     * @param destination
     *        path to file's destination.
     * @throws IOException
     *         thrown on filesystem error.
     */
    public static void copyFile(Path source, Path destination)
            throws IOException {
        final Path destFolders = destination.getParent();
        if (Files.notExists(destFolders)) {
            Files.createDirectories(destFolders);
        }
        Files.copy(source, destination);
    }

    /**
     * Exports a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName
     *        ie.: "/SmartLibrary.dll".
     * @param destination
     *        the desired path of the resource.
     * @throws IOException
     *         thrown on filesystem error.
     */
    public static void exportResource(String resourceName,
            Path destination) throws IOException {
        try (InputStream in = FilesystemUtils.class
                .getResourceAsStream(resourceName);
                OutputStream out = Files.newOutputStream(destination)) {
            int readBytes;
            final byte[] buffer = new byte[BUFFER_SIZE];
            while ((readBytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, BUFFER_SIZE);
            }
        }
    }

}
