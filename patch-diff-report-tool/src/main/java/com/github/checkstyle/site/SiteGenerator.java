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

package com.github.checkstyle.site;

import static com.github.checkstyle.Main.SITEPATH;
import static com.github.checkstyle.Main.XREF_FILEPATH;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.github.checkstyle.data.CheckstyleRecord;
import com.github.checkstyle.data.CliPaths;
import com.github.checkstyle.data.ParsedContent;
import com.github.checkstyle.data.Statistics;

/**
 * Generates site report using thymeleaf template engine.
 * Instead of single template 3 smaller ones are used with purpose
 * of avoiding creation of extra large Context instance.
 * @author atta_troll
 *
 */
public final class SiteGenerator {

    /**
     * Private ctor, please use generate method.
     */
    private SiteGenerator() {
    }

    /**
     * Generates site report using thymeleaf template engine.
     *
     * @param content
     *        container with parsed data.
     * @param statistics
     *        accumulator for statistical data.
     * @param paths
     *        cli paths.
     * @throws IOException
     *         on failure to write site to disc.
     */
    public static void generate(ParsedContent content, Statistics statistics,
            CliPaths paths) throws IOException {
        //setup thymeleaf engine
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/");
        templateResolver.setSuffix(".template");
        final TemplateEngine tplEngine = new TemplateEngine();
        tplEngine.setTemplateResolver(templateResolver);
        //setup xreference generator
        final XrefGenerator xrefGenerator = new XrefGenerator(paths.getSourcePath(),
            paths.getResultPath().resolve(XREF_FILEPATH), paths.getResultPath());
        //html generation
        final Path sitepath = paths.getResultPath().resolve(SITEPATH);
        try (FileWriter writer = new FileWriter(sitepath.toString())) {
            //write statistics
            generateHtmlHeadPart(tplEngine, writer, statistics, paths);
            //write parsed content
            final AnchorCounter anchorCounter = new AnchorCounter();
            final Iterator<Map.Entry<String, List<CheckstyleRecord>>> iter =
                    content.getRecords().entrySet().iterator();
            while (iter.hasNext()) {
                appendHtmlTable(iter, tplEngine, writer, xrefGenerator, anchorCounter);
            }
            //write final html chunk
            tplEngine.process("endPortion", new Context(), writer);
        }
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
     * @param paths
     *        cli paths.
     */
    private static void generateHtmlHeadPart(TemplateEngine tplEngine, FileWriter writer,
            Statistics statistics, CliPaths paths) {
        final Context context = new Context();
        context.setVariable("statistics", statistics);
        context.setVariable("paths", paths);
        tplEngine.process("headPortion", context, writer);
    }

    /**
     * Appends to the site a table with parsed data for a single file entry.
     *
     * @param iter
     *        iterator on the map containing file entries.
     * @param tplEngine
     *        thymeleaf template engine.
     * @param writer
     *        file writer.
     * @param xrefGenerator
     *        xreference generator.
     * @param anchorCounter
     *        anchor links provider.
     * @throws IOException
     *         on failure to write data on disk or generate xreference file.
     */
    private static void appendHtmlTable(Iterator<Map.Entry<String, List<CheckstyleRecord>>> iter,
            TemplateEngine tplEngine, FileWriter writer, XrefGenerator xrefGenerator,
            AnchorCounter anchorCounter) throws IOException {
        final Map.Entry<String, List<CheckstyleRecord>> entry = iter.next();
        final String filename = entry.getKey();
        final List<CheckstyleRecord> records = entry.getValue();
        final String xreference = xrefGenerator.generateXref(filename);
        final Context context = new Context();
        context.setVariable("filename", filename);
        context.setVariable("records", records);
        context.setVariable("xref", xreference);
        context.setVariable("anchor", anchorCounter);
        tplEngine.process("table", context, writer);
    }

}
