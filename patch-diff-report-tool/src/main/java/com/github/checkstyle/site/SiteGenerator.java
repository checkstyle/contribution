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

package com.github.checkstyle.site;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.github.checkstyle.Main;
import com.github.checkstyle.data.CheckstyleRecord;
import com.github.checkstyle.data.CliPaths;
import com.github.checkstyle.data.DiffReport;
import com.github.checkstyle.data.MergedConfigurationModule;
import com.github.checkstyle.data.Statistics;

/**
 * Generates site report using thymeleaf template engine. Instead of single
 * template 3 smaller ones are used with purpose of avoiding creation of extra
 * large Context instance.
 * @author attatrol
 */
public final class SiteGenerator {

    /**
     * Name for the site file.
     */
    public static final Path SITEPATH = Paths.get("index.html");

    /**
     * Pattern for a common file name beginning.
     */
    private static final Pattern COMMON_FILENAME_BEGINNING = Pattern.compile("src/main/java/.*");

    /**
     * Common filename beginning length.
     */
    private static final int COMMON_FILENAME_BEGINNING_LENGTH = 14;

    /**
     * Private ctor, please use generate method.
     */
    private SiteGenerator() {
    }

    /**
     * Generates site report using thymeleaf template engine.
     *
     * @param diffReport
     *        container with parsed data.
     * @param diffConfiguration
     *        merged configurations from both reports.
     * @param paths
     *        CLI paths.
     * @throws IOException
     *         on failure to write site to disc.
     */
    public static void generate(DiffReport diffReport, MergedConfigurationModule diffConfiguration,
            CliPaths paths) throws IOException {
        // setup thymeleaf engine
        final TemplateEngine tplEngine = getTemplateEngine();
        // setup xreference generator
        final XrefGenerator xrefGenerator = new XrefGenerator(paths.getRefFilesPath(),
                paths.getOutputPath().resolve(Main.XREF_FILEPATH), paths.getOutputPath());
        // html generation
        final Path sitepath = paths.getOutputPath().resolve(SITEPATH);
        final FileWriter writer = new FileWriter(sitepath.toString());
        try {
            // write statistics
            generateHeader(tplEngine, writer, diffReport.getStatistics(), diffConfiguration);
            // write parsed content
            generateBody(tplEngine, writer, diffReport, paths, xrefGenerator);
            // write html footer
            tplEngine.process("footer", new Context(), writer);
        }
        finally {
            writer.close();
        }
    }

    /**
     * Creates thymeleaf template engine.
     *
     * @return template engine.
     */
    private static TemplateEngine getTemplateEngine() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".template");
        final TemplateEngine tplEngine = new TemplateEngine();
        tplEngine.setTemplateResolver(templateResolver);
        return tplEngine;
    }

    /**
     * Creates beginning part of resulting site.
     *
     * @param tplEngine
     *        thymeleaf template engine.
     * @param writer
     *        file writer.
     * @param statistics
     *        container for statistics.
     * @param diffConfiguration
     *        merged configurations from both reports.
     */
    private static void generateHeader(TemplateEngine tplEngine, FileWriter writer,
            Statistics statistics, MergedConfigurationModule diffConfiguration) {
        final Context context = new Context();
        context.setVariable("statistics", statistics);
        context.setVariable("config", diffConfiguration);
        tplEngine.process("header", context, writer);
    }

    /**
     * Creates main part of resulting site.
     *
     * @param tplEngine
     *        thymeleaf template engine.
     * @param writer
     *        file writer.
     * @param diffReport
     *        difference between two checkstyle reports.
     * @param paths
     *        CLI paths.
     * @param xrefGenerator
     *        xReference generator.
     */
    private static void generateBody(TemplateEngine tplEngine, FileWriter writer,
            DiffReport diffReport, CliPaths paths, XrefGenerator xrefGenerator) {
        final AnchorCounter anchorCounter = new AnchorCounter();

        final Path refFilesPath = paths.getRefFilesPath();
        for (Map.Entry<String, List<CheckstyleRecord>> entry : diffReport.getRecords().entrySet()) {
            final List<CheckstyleRecord> records = entry.getValue();
            String filename = entry.getKey();

            xrefGenerator.reset();

            for (CheckstyleRecord record : records) {
                final String xreference = xrefGenerator.generateXref(record.getXref(),
                            paths.isShortFilePaths());
                record.setXref(xreference);
            }

            if (refFilesPath != null) {
                try {
                    filename = refFilesPath.relativize(Paths.get(filename)).toString();
                }
                catch (IllegalArgumentException ignore) {
                    // use original file name
                }
            }
            generateContent(tplEngine, writer, records, shortenFilename(filename),
                    anchorCounter);
        }
    }

    /**
     * Appends to the site a table with parsed data for a single file entry.
     *
     * @param tplEngine
     *        thymeleaf template engine.
     * @param writer
     *        file writer.
     * @param records
     *        checkstyle records for a single file.
     * @param filename
     *        current file name from checkstyle reports.
     * @param anchorCounter
     *        anchor links provider.
     */
    private static void generateContent(TemplateEngine tplEngine, FileWriter writer,
            List<CheckstyleRecord> records, String filename,
            AnchorCounter anchorCounter) {
        final Context context = new Context();
        context.setVariable("filename", filename);
        context.setVariable("records", records);
        context.setVariable("anchor", anchorCounter);
        tplEngine.process("content", context, writer);
    }

    /**
     * Removes "src/main/java/" from filename beginning.
     *
     * @param filename
     *        file name.
     * @return shortened
     *         file name.
     */
    private static String shortenFilename(String filename) {
        final String shortenedFilename;
        if (COMMON_FILENAME_BEGINNING.matcher(filename).matches()) {
            shortenedFilename = filename.substring(COMMON_FILENAME_BEGINNING_LENGTH,
                    filename.length());
        }
        else {
            shortenedFilename = filename;
        }
        return shortenedFilename;
    }

}
