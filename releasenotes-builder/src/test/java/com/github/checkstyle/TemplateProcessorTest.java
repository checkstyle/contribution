package com.github.checkstyle;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.kohsuke.github.GHIssue;

public class TemplateProcessorTest {

    private static TemporaryFolder tmpDir;

    private static final String THYMELEAF_XDOC_TEMPLATE_FILE = "xdoc_thymeleaf.template";
    private static final String FREEMARKER_XDOC_TEMPLATE_FILE = "xdoc_freemarker.template";

    @BeforeClass
    public static void setup() throws IOException {
        tmpDir = new TemporaryFolder();
        tmpDir.create();
    }

    @Test
    public void testGenerateWithThymeleafOnlyBreakingCompatibilitySection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("breakingMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc1.txt");
        TemplateProcessor.generateWithThymeleaf(templateVariables, actualNotes.getAbsolutePath(),
            THYMELEAF_XDOC_TEMPLATE_FILE);

        final String expectedXdoc =
            getFileContents(getPath("correct_breaking_compatibility_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithThymeleafOnlyNewSection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("newMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc2.txt");
        TemplateProcessor.generateWithThymeleaf(templateVariables, actualNotes.getAbsolutePath(),
            THYMELEAF_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_new_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithThymeleafOnlyBugSection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("bugMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc3.txt");
        TemplateProcessor.generateWithThymeleaf(templateVariables, actualNotes.getAbsolutePath(),
            THYMELEAF_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_bug_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithThymeleafOnlyNotesSection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("notesMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc4.txt");
        TemplateProcessor.generateWithThymeleaf(templateVariables, actualNotes.getAbsolutePath(),
            THYMELEAF_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_notes_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithThymeleafAllSections() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("breakingMessages", getMockReleasenotesMessages());
        templateVariables.put("newMessages", getMockReleasenotesMessages());
        templateVariables.put("bugMessages", getMockReleasenotesMessages());
        templateVariables.put("notesMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc5.txt");
        TemplateProcessor.generateWithThymeleaf(templateVariables, actualNotes.getAbsolutePath(),
            THYMELEAF_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_all_sections.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithFreemarkerOnlyBreakingCompatibilitySection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("breakingMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc6.txt");
        TemplateProcessor.generateWithFreemarker(templateVariables, actualNotes.getAbsolutePath(),
            FREEMARKER_XDOC_TEMPLATE_FILE);

        final String expectedXdoc =
            getFileContents(getPath("correct_breaking_compatibility_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithFreemarkerOnlyNewSection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("newMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc7.txt");
        TemplateProcessor.generateWithFreemarker(templateVariables, actualNotes.getAbsolutePath(),
            FREEMARKER_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_new_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithFreemarkerOnlyBugSection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("bugMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc8.txt");
        TemplateProcessor.generateWithFreemarker(templateVariables, actualNotes.getAbsolutePath(),
            FREEMARKER_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_bug_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithFreemarkerOnlyNotesSection() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("notesMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc9.txt");
        TemplateProcessor.generateWithFreemarker(templateVariables, actualNotes.getAbsolutePath(),
            FREEMARKER_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_notes_section.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    @Test
    public void testGenerateWithFreemarkerAllSections() throws Exception{
        final Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("releaseNo", "1.0.0");
        templateVariables.put("breakingMessages", getMockReleasenotesMessages());
        templateVariables.put("newMessages", getMockReleasenotesMessages());
        templateVariables.put("bugMessages", getMockReleasenotesMessages());
        templateVariables.put("notesMessages", getMockReleasenotesMessages());

        final File actualNotes = tmpDir.newFile("xdoc10.txt");
        TemplateProcessor.generateWithFreemarker(templateVariables, actualNotes.getAbsolutePath(),
            FREEMARKER_XDOC_TEMPLATE_FILE);

        final String expectedXdoc = getFileContents(getPath("correct_all_sections.txt").toFile());
        final String actualXdoc = getFileContents(actualNotes);

        assertEquals(expectedXdoc, actualXdoc);
    }

    private static List<ReleaseNotesMessage> getMockReleasenotesMessages()
            throws NoSuchFieldException, IllegalAccessException {
        final List<ReleaseNotesMessage> messages = new ArrayList<>();

        final ReleaseNotesMessage msgWithoutIssueNo =
            new ReleaseNotesMessage("Mock issue title 1", "Author 1");
        messages.add(msgWithoutIssueNo);

        final ReleaseNotesMessage msgWithoutIssueNoWithMultipleAuthors =
            new ReleaseNotesMessage("Mock issue title 2", "Author 3, Author 4");
        messages.add(msgWithoutIssueNoWithMultipleAuthors);

        final GHIssue issue1 = getMockGithubIssue(123, "Mock issue title 3");
        final ReleaseNotesMessage msgWithIssueNo = new ReleaseNotesMessage(issue1, "Author 5");
        messages.add(msgWithIssueNo);

        final GHIssue issue2 = getMockGithubIssue(123, "Mock issue title 4");
        final ReleaseNotesMessage msgWithIssueNoWithMultipleAuthors =
            new ReleaseNotesMessage(issue2, "Author 6, Author 7");
        messages.add(msgWithIssueNoWithMultipleAuthors);

        return messages;
    }

    private static GHIssue getMockGithubIssue(int issueNo, String issueTitle)
        throws IllegalAccessException, NoSuchFieldException {
        final GHIssue issue = new GHIssue();

        final Field title = issue.getClass().getDeclaredField("title");
        title.setAccessible(true);
        title.set(issue, issueTitle);

        final Field number = issue.getClass().getDeclaredField("number");
        number.setAccessible(true);
        number.set(issue, issueNo);

        return issue;
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
        return Paths.get("src/test/resources/com/github/checkstyle/" + fileName)
            .toAbsolutePath();
    }

}
