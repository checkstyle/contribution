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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

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

    /** Default constructor. */
    private TemplateProcessor() { }

    /**
     * Generates output file with release notes using Thymeleaf.
     * @param variables the map which represents template variables.
     * @param outputFile output file.
     * @param templateFilename template name.
     * @throws IOException if I/O error occurs.
     */
    public static void generateWithThymeleaf(Map<String, Object> variables, String outputFile,
            String templateFilename) throws IOException {

        final TemplateEngine engine = new TemplateEngine();
        final AbstractConfigurableTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(TEMPLATE_FOLDER_PATH);
        engine.setTemplateResolver(resolver);

        final IContext ctx = new Context(Locale.US, variables);

        try (Writer fileWriter = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            engine.process(templateFilename, ctx, fileWriter);
        }
    }

    /**
     * Generates output file with release notes using FreeMarker.
     * @param variables the map which represents template variables.
     * @param outputFile output file.
     * @param templateFilename template name.
     * @throws IOException if I/O error occurs.
     * @throws TemplateException if an error occurs while generating freemarker template.
     */
    public static void generateWithFreemarker(Map<String, Object> variables, String outputFile,
            String templateFilename) throws IOException, TemplateException {

        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setNumberFormat("0.######");
        configuration.setClassForTemplateLoading(Main.class, "/" + TEMPLATE_FOLDER_PATH);

        final Template template = configuration.getTemplate(templateFilename);
        try (Writer fileWriter = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            template.process(variables, fileWriter);
        }
    }

    /**
     * Returns the map which represents template variables.
     * @param releaseNotes release notes map.
     * @param releaseNumber release number.
     * @return the map which represents template variables.
     */
    public static Map<String, Object> getTemplateVariables(
        Multimap<String, ReleaseNotesMessage> releaseNotes, String releaseNumber) {

        final Map<String, Object> variables = new HashMap<>();
        variables.put("releaseNo", releaseNumber);
        variables.put("breakingMessages", releaseNotes.get(Constants.BREAKING_COMPATIBILITY_LABEL));

        final Collection<ReleaseNotesMessage> newMessages =
            releaseNotes.get(Constants.NEW_FEATURE_LABEL);
        newMessages.addAll(releaseNotes.get(Constants.MEW_MODULE_LABEL));
        variables.put("newMessages", newMessages);

        variables.put("bugMessages", releaseNotes.get(Constants.BUG_LABEL));
        variables.put("notesMessages", releaseNotes.get(Constants.MISCELLANEOUS_LABEL));

        return variables;
    }

}
