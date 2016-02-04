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
 * @author atta_troll
 *
 */
public class XrefGenerator {

    /**
     * Encoding used for input and output files.
     */
    public static final String ENCODING = "ISO-8859-1";

    /**
     * maven-jxr package manager.
     */
    private static PackageManager pacman;

    /**
     * maven-jxr XREF file generator.
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
     * @param relativizationPath1
     *        path to the sources, used to shorten paths.
     * @param destinationPath1
     *        destination folder for XREF files.
     * @param sitePath1
     *        path to the site.
     */
    public XrefGenerator(Path relativizationPath1,
            Path destinationPath1, Path sitePath1) {
        this.relativizationPath = relativizationPath1;
        this.destinationPath = destinationPath1;
        this.sitePath = sitePath1;
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
      File sourceFile = new File(name);
      Path dest = getDestinationPath(name);
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
