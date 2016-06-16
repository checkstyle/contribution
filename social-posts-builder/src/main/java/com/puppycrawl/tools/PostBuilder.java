package com.puppycrawl.tools;

import org.jdom2.Element;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for social posts building.
 * @author Vladislav Lisetskii
 */
public class PostBuilder {

    /** A template for posts titles. */
    private static final String TITLE_TEMPLATE = "Checkstyle %s - " +
        "http://checkstyle.sourceforge.net/releasenotes.html#Release_%s";

    /** The name of the file to write a full post in. */
    private static final String FULL_POST_FILE_NAME = "fullPost.txt";

    /** The name of the file to write a short post in. */
    private static final String SHORT_POST_FILE_NAME = "shortPost.txt";

    /** The sections for posts. */
    private final List<PostSection> postSections;

    /** The title for posts. */
    private final String title;

    /** The title for posts. */
    private final CliOptions config;

    /** Prevent instantiating. */
    public PostBuilder(Element rootNode, CliOptions config) {
        this.config = config;
        title = getPostTitle(rootNode);
        postSections = getPostSections(rootNode);
    }

    /**
     * Get sections to write into social posts.
     * @param rootNode the root node of the DOM to get sections from.
     * @return a list of social post sections.
     */
    private static List<PostSection> getPostSections(Element rootNode) {
        final List<PostSection> result = new ArrayList<>();
        final List<Element> jdomElements = rootNode.getChildren();

        for (int i = 0; i < jdomElements.size(); i += 2) {
            final String sectionTitle = jdomElements.get(i).getText();
            final PostSection postSection = new PostSection(sectionTitle);

            final Element records = jdomElements.get(i + 1);
            for (Element record : records.getChildren()) {
                String recordText = record.getText();
                recordText = recordText.substring(0, recordText.lastIndexOf(". Author")).trim();
                postSection.addRecord(recordText);
            }
            result.add(postSection);
        }
        return result;
    }

    /**
     * Get a title for social posts.
     * @param rootNode the root node of the DOM to get the title from
     * @return a title for social posts.
     */
    private static String getPostTitle(Element rootNode) {
        final String rootNodeName = rootNode.getAttribute("name").getValue();
        final String releaseNumber = rootNodeName.substring(rootNodeName.indexOf(' ') + 1);
        return String.format(TITLE_TEMPLATE, releaseNumber, releaseNumber);
    }

    /** Write full post. */
    public void writeFullPost() {
        try (PrintWriter writer = new PrintWriter(
                new File(config.getOutputLocation(), FULL_POST_FILE_NAME), "UTF-8")) {
            writer.println(title);
            for (PostSection postSection : postSections) {
                final String postSectionTitle = postSection.getTitle();
                writer.println('\n' + postSectionTitle + '\n');
                for (String record : postSection.getRecords()) {
                    writer.println("  " + record);
                }
            }
        }
        catch (IOException ex) {
            System.err.format("IOException: %s%n", ex);
        }
    }

    /** Write short post. */
    public void writeShortPost() {
        try (PrintWriter writer = new PrintWriter(
                new File(config.getOutputLocation(), SHORT_POST_FILE_NAME), "UTF-8")) {
            writer.println(title);
            for (PostSection postSection : postSections) {
                String postSectionTitle = postSection.getTitle();
                if (!postSectionTitle.startsWith("Notes")) {
                    writer.println(postSectionTitle + ' ' + postSection.getRecords().size());
                }
            }
        }
        catch (IOException ex) {
            System.err.format("IOException: %s%n", ex);
        }
    }
}
