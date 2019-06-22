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

package com.github.checkstyle.templates;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.globals.ReleaseNotesMessage;
import com.google.common.collect.Multimap;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Util class to generate release notes output file with FreeMarker template engines.
 * @author Andrei Selkin
 */
//-@cs[ClassDataAbstractionCoupling] No way to split this up right now.
public final class TemplateProcessor {

    /** Internal template name. */
    private static final String TEMPLATE_NAME = "template";

    /** Default constructor. */
    private TemplateProcessor() {
    }

    /**
     * Generates output file with release notes using FreeMarker.
     * @param variables the map which represents template variables.
     * @param outputFile output file.
     * @param templateFileName the optional file name of the template.
     * @param defaultResource the resource file name to use if no file name was given.
     * @throws IOException if I/O error occurs.
     * @throws TemplateException if an error occurs while generating freemarker template.
     */
    public static void generateWithFreemarker(Map<String, Object> variables, String outputFile,
            String templateFileName, String defaultResource) throws IOException, TemplateException {

        final Configuration configuration = new Configuration(Configuration.VERSION_2_3_22);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setLocale(Locale.US);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setNumberFormat("0.######");

        final StringTemplateLoader loader = new StringTemplateLoader();
        loader.putTemplate(TEMPLATE_NAME, loadTemplate(templateFileName, defaultResource));
        configuration.setTemplateLoader(loader);

        final Template template = configuration.getTemplate(TEMPLATE_NAME);
        try (Writer fileWriter = new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            template.process(variables, fileWriter);
        }
    }

    /**
     * Loads a template file to a string, otherwise a resource template if the file isn't supplied.
     * @param fileName The path of the optional file to load.
     * @param defaultResource The path of the resource to load if there is no file.
     * @return The contents of the template.
     * @throws FileNotFoundException if the supplied file can't be found.
     */
    private static String loadTemplate(String fileName, String defaultResource)
            throws FileNotFoundException {
        final InputStream inputStream;

        if (fileName == null) {
            inputStream = Template.class.getClassLoader().getResourceAsStream(defaultResource);

            if (inputStream == null) {
                throw new IllegalStateException("Failed to find resource: " + defaultResource);
            }
        }
        else {
            inputStream = new FileInputStream(fileName);
        }

        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().parallel().collect(Collectors.joining(System.lineSeparator()));
    }

    /**
     * Returns the map which represents template variables.
     * @param releaseNotes release notes map.
     * @param remoteRepoPath remote repository path.
     * @param releaseNumber release number.
     * @return the map which represents template variables.
     */
    public static Map<String, Object> getTemplateVariables(
            Multimap<String, ReleaseNotesMessage> releaseNotes, String remoteRepoPath,
            String releaseNumber) {

        final Map<String, Object> variables = new HashMap<>();
        variables.put("todaysDate",
                new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(LocalDate.now()));
        variables.put("remoteRepoPath", remoteRepoPath);
        variables.put("releaseNo", releaseNumber);
        variables.put("breakingMessages", releaseNotes.get(Constants.BREAKING_COMPATIBILITY_LABEL));

        final Collection<ReleaseNotesMessage> newMessages =
            releaseNotes.get(Constants.NEW_FEATURE_LABEL);
        newMessages.addAll(releaseNotes.get(Constants.NEW_MODULE_LABEL));
        variables.put("newMessages", newMessages);

        variables.put("bugMessages", releaseNotes.get(Constants.BUG_LABEL));
        variables.put("notesMessages", releaseNotes.get(Constants.MISCELLANEOUS_LABEL));

        return variables;
    }

}
