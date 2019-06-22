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
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.github.GHIssue;

import com.github.checkstyle.CliOptions;
import com.github.checkstyle.CliOptions.Builder;
import com.github.checkstyle.MainProcess;
import com.github.checkstyle.globals.Constants;
import com.github.checkstyle.globals.ReleaseNotesMessage;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class TemplateProcessorTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException {
        temporaryFolder.delete();
        temporaryFolder.create();
    }

    @Test
    public void testGenerateOnlyBreakingCompatibility() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(createAllNotes(), null, null, null, null), createBaseCliOptions()
                .setGenerateAll(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("xdocBreakingCompatibility.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterGplusBreakingCompatibility.txt", MainProcess.TWITTER_FILENAME);
        assertFile("twitterGplusBreakingCompatibility.txt", MainProcess.GPLUS_FILENAME);
        assertFile("rssMlistBreakingCompatibility.txt", MainProcess.RSS_FILENAME);
        assertFile("rssMlistBreakingCompatibility.txt", MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyNewFeature() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(null, createAllNotes(), null, null, null), createBaseCliOptions()
                .setGenerateAll(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("xdocNew.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterGplusNew.txt", MainProcess.TWITTER_FILENAME);
        assertFile("twitterGplusNew.txt", MainProcess.GPLUS_FILENAME);
        assertFile("rssMlistNew.txt", MainProcess.RSS_FILENAME);
        assertFile("rssMlistNew.txt", MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyBug() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(null, null, createAllNotes(), null, null), createBaseCliOptions()
                .setGenerateAll(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("xdocBug.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterGplusBug.txt", MainProcess.TWITTER_FILENAME);
        assertFile("twitterGplusBug.txt", MainProcess.GPLUS_FILENAME);
        assertFile("rssMlistBug.txt", MainProcess.RSS_FILENAME);
        assertFile("rssMlistBug.txt", MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyMisc() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(null, null, null, createAllNotes(), null), createBaseCliOptions()
                .setGenerateAll(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("xdocMisc.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterGplusMisc.txt", MainProcess.TWITTER_FILENAME);
        assertFile("twitterGplusMisc.txt", MainProcess.GPLUS_FILENAME);
        assertFile("rssMlistMisc.txt", MainProcess.RSS_FILENAME);
        assertFile("rssMlistMisc.txt", MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyNewModule() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(null, null, null, null, createAllNotes()), createBaseCliOptions()
                .setGenerateAll(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("xdocNew.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterGplusNew.txt", MainProcess.TWITTER_FILENAME);
        assertFile("twitterGplusNew.txt", MainProcess.GPLUS_FILENAME);
        assertFile("rssMlistNew.txt", MainProcess.RSS_FILENAME);
        assertFile("rssMlistNew.txt", MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateAll() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(
                Collections.singletonList(createReleaseNotesMessage("Title 1", "Author 1")),
                Collections.singletonList(createReleaseNotesMessage(2, "Title 2", "Author 2")),
                Collections.singletonList(createReleaseNotesMessage("Title 3", "Author 3")),
                Collections.singletonList(createReleaseNotesMessage(4, "Title 4", "Author 4")),
                Collections.singletonList(createReleaseNotesMessage("Title 5", "Author 5"))
            ), createBaseCliOptions().setGenerateAll(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("xdocAll.txt", MainProcess.XDOC_FILENAME);
        assertFile("twitterGplusAll.txt", MainProcess.TWITTER_FILENAME);
        assertFile("twitterGplusAll.txt", MainProcess.GPLUS_FILENAME);
        assertFile("rssMlistAll.txt", MainProcess.RSS_FILENAME);
        assertFile("rssMlistAll.txt", MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyXdoc() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(createAllNotes(), null, null, null, null), createBaseCliOptions()
                .setGenerateXdoc(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("xdocBreakingCompatibility.txt", MainProcess.XDOC_FILENAME);
        assertFile(MainProcess.TWITTER_FILENAME);
        assertFile(MainProcess.GPLUS_FILENAME);
        assertFile(MainProcess.RSS_FILENAME);
        assertFile(MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyTwitter() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(createAllNotes(), null, null, null, null), createBaseCliOptions()
                .setGenerateTw(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile(MainProcess.XDOC_FILENAME);
        assertFile("twitterGplusBreakingCompatibility.txt", MainProcess.TWITTER_FILENAME);
        assertFile(MainProcess.GPLUS_FILENAME);
        assertFile(MainProcess.RSS_FILENAME);
        assertFile(MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyGplus() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(createAllNotes(), null, null, null, null), createBaseCliOptions()
                .setGenerateGplus(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile(MainProcess.XDOC_FILENAME);
        assertFile(MainProcess.TWITTER_FILENAME);
        assertFile("twitterGplusBreakingCompatibility.txt", MainProcess.GPLUS_FILENAME);
        assertFile(MainProcess.RSS_FILENAME);
        assertFile(MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyRss() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(createAllNotes(), null, null, null, null), createBaseCliOptions()
                .setGenerateRss(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile(MainProcess.XDOC_FILENAME);
        assertFile(MainProcess.TWITTER_FILENAME);
        assertFile(MainProcess.GPLUS_FILENAME);
        assertFile("rssMlistBreakingCompatibility.txt", MainProcess.RSS_FILENAME);
        assertFile(MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateOnlyMlist() throws Exception {
        final List<String> errors = MainProcess.run(
            createNotes(createAllNotes(), null, null, null, null), createBaseCliOptions()
                .setGenerateMlist(true).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile(MainProcess.XDOC_FILENAME);
        assertFile(MainProcess.TWITTER_FILENAME);
        assertFile(MainProcess.GPLUS_FILENAME);
        assertFile(MainProcess.RSS_FILENAME);
        assertFile("rssMlistBreakingCompatibility.txt", MainProcess.MLIST_FILENAME);
    }

    @Test
    public void testGenerateCustomTemplate() throws Exception {
        final File file = temporaryFolder.newFile("temp.template");
        FileUtils.writeStringToFile(file, "hello world");
        final String template = file.getAbsolutePath();

        final List<String> errors = MainProcess.run(
                createNotes(createAllNotes(), createAllNotes(), createAllNotes(), createAllNotes(),
                        createAllNotes()),
                createBaseCliOptions().setGenerateAll(true).setXdocTemplate(template)
                        .setTwitterTemplate(template).setGplusTemplate(template)
                        .setRssTemplate(template).setMlistTemplate(template).build());

        Assert.assertEquals("no errors", 0, errors.size());

        assertFile("customTemplate.txt", MainProcess.XDOC_FILENAME);
        assertFile("customTemplate.txt", MainProcess.TWITTER_FILENAME);
        assertFile("customTemplate.txt", MainProcess.GPLUS_FILENAME);
        assertFile("customTemplate.txt", MainProcess.RSS_FILENAME);
        assertFile("customTemplate.txt", MainProcess.MLIST_FILENAME);
    }

    private static List<ReleaseNotesMessage> createAllNotes() throws Exception {
        return Arrays.asList(
            createReleaseNotesMessage("Mock issue title 1", "Author 1"),
            createReleaseNotesMessage("Mock issue title 2", "Author 3, Author 4"),
            createReleaseNotesMessage(123, "Mock issue title 3", "Author 5"),
            createReleaseNotesMessage(123, "Mock issue title 4", "Author 6, Author 7"),
            createReleaseNotesMessage(123, "Mock issue title 5 ==> test", "Author 6, Author 7"),
            createReleaseNotesMessage("Mock issue title 6 L12345678901234567890123456789012345678"
                + "90123456789012345678901234567890oooooooooooooooooooooooooooooooooooooooooooooo"
                + "ooong'\"", "Author 1"));
    }

    private Builder createBaseCliOptions() {
        final Builder result = CliOptions.newBuilder();

        result.setOutputLocation(temporaryFolder.getRoot().getAbsolutePath() + File.separator);
        result.setRemoteRepoPath("checkstyle/checkstyle");
        result.setReleaseNumber("1.0.0");
        result.setLocalRepoPath("dummy");
        result.setStartRef("dummy");

        return result;
    }

    private static Multimap<String, ReleaseNotesMessage> createNotes(
            List<ReleaseNotesMessage> breakingMessages,
            List<ReleaseNotesMessage> newFeatureMessages, List<ReleaseNotesMessage> bugMessages,
            List<ReleaseNotesMessage> miscMessages, List<ReleaseNotesMessage> newModuleMessages) {
        final Multimap<String, ReleaseNotesMessage> notes = ArrayListMultimap.create();

        if (breakingMessages != null) {
            notes.putAll(Constants.BREAKING_COMPATIBILITY_LABEL, breakingMessages);
        }
        if (newFeatureMessages != null) {
            notes.putAll(Constants.NEW_FEATURE_LABEL, newFeatureMessages);
        }
        if (bugMessages != null) {
            notes.putAll(Constants.BUG_LABEL, bugMessages);
        }
        if (miscMessages != null) {
            notes.putAll(Constants.MISCELLANEOUS_LABEL, miscMessages);
        }
        if (newModuleMessages != null) {
            notes.putAll(Constants.NEW_MODULE_LABEL, newModuleMessages);
        }

        return notes;
    }

    private static ReleaseNotesMessage createReleaseNotesMessage(String title, String author) {
        return new ReleaseNotesMessage(title, author);
    }

    private static ReleaseNotesMessage createReleaseNotesMessage(int number, String title,
            String author) throws IllegalAccessException, NoSuchFieldException {
        final GHIssue issue = new GHIssue();

        final Field titleField = issue.getClass().getDeclaredField("title");
        titleField.setAccessible(true);
        titleField.set(issue, title);

        final Field numberField = issue.getClass().getDeclaredField("number");
        numberField.setAccessible(true);
        numberField.set(issue, number);

        return new ReleaseNotesMessage(issue, author);
    }

    private void assertFile(String expectedName, String actualName) throws IOException {
        final String expectedXdoc =
                getFileContents(getPath(expectedName).toFile());
        final String actualXdoc = getFileContents(new File(temporaryFolder.getRoot(), actualName))
                .replace(new SimpleDateFormat("dd.MM.yyyy", Locale.US).format(LocalDate.now()),
                        "XX.XX.XXXX");

        Assert.assertEquals(expectedXdoc, actualXdoc);
    }

    private void assertFile(String actualName) {
        Assert.assertFalse(new File(temporaryFolder.getRoot(), actualName).exists());
    }

    private static String getFileContents(File file) throws IOException {
        final StringBuilder result = new StringBuilder();

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

    private static Path getPath(String fileName) {
        return Paths.get("src/test/resources/com/github/checkstyle/" + fileName).toAbsolutePath();
    }

}
