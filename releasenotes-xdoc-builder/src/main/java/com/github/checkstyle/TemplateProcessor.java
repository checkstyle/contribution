////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import com.google.common.collect.Multimap;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Util class to generate release notes output file with FreeMarker or Thymeleaf template engines.
 * @author Andrei Selkin
 */
public final class TemplateProcessor {

    /** Path to the template folder. */
    private static final String TEMPLATE_FOLDER_PATH = "com/github/checkstyle/templates/";
    /** FreeMarker template file name. */
    private static final String FREEMARKER_TEMPLATE_FILE = "freemarker.template";
    /** Thymeleaf template file name. */
    private static final String THYMELEAF_TEMPLATE_FILE = "thymeleaf.template";

    /** Default constructor. */
    private TemplateProcessor() { }

    /**
     * Generates output file with release notes using Thymeleaf.
     * @param releaseNotes release notes map.
     * @param releaseNumber current release number.
     * @param outputFile output file.
     * @throws IOException if I/O error occurs.
     */
    public static void generateWithThymeleaf(Multimap<String, ReleaseNotesMessage> releaseNotes,
        String releaseNumber, String outputFile) throws IOException {

        final TemplateEngine templateEngine = new TemplateEngine();
        final TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix(TEMPLATE_FOLDER_PATH);
        templateEngine.addTemplateResolver(templateResolver);

        final IContext ctx = new Context();
        final Map<String, Object> variables = ctx.getVariables();
        fillTemplateVariables(variables, releaseNotes, releaseNumber);

        final String result = templateEngine.process(THYMELEAF_TEMPLATE_FILE, ctx);
        try (Writer fileWriter = new FileWriter(outputFile)) {
            fileWriter.write("    ");
            fileWriter.write(result);
        }
    }

    /**
     * Generates output file with release notes using FreeMarker.
     * @param releaseNotes release notes map.
     * @param releaseNumber current release number.
     * @param outputFile output file.
     * @throws IOException if I/O error occurs.
     * @throws TemplateException if an error occurs while generating freemarker template.
     */
    public static void generateWithFreemarker(Multimap<String, ReleaseNotesMessage> releaseNotes,
        String releaseNumber, String outputFile) throws IOException, TemplateException {

        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setNumberFormat("0.######");
        configuration.setClassForTemplateLoading(NotesBuilder.class, "/" + TEMPLATE_FOLDER_PATH);

        final Map<String, Object> variables = new HashMap<>();
        fillTemplateVariables(variables, releaseNotes, releaseNumber);

        final Template template = configuration.getTemplate(FREEMARKER_TEMPLATE_FILE);
        try (Writer fileWriter = new FileWriter(outputFile)) {
            template.process(variables, fileWriter);
        }
    }

    /**
     * Fills template variables.
     * @param variables variables.
     * @param releaseNotes release notes map.
     * @param releaseNumber release number.
     */
    private static void fillTemplateVariables(Map<String, Object> variables,
        Multimap<String, ReleaseNotesMessage> releaseNotes, String releaseNumber) {

        variables.put("releaseNo", releaseNumber);
        variables.put("breakingMessages", releaseNotes.get(Constants.BREAKING_COMPATIBILITY_LABEL));
        variables.put("newMessages", releaseNotes.get(Constants.NEW_LABEL));
        variables.put("bugMessages", releaseNotes.get(Constants.BUG_LABEL));
        variables.put("notesMessages", releaseNotes.get(Constants.MISCELLANEOUS_LABEL));
    }

}
