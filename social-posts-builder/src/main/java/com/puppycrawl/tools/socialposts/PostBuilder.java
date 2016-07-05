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

package com.puppycrawl.tools.socialposts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Class for social posts building.
 * @author Vladislav Lisetskii
 */
public class PostBuilder {

    /** A template for posts titles. */
    private static final String TITLE_TEMPLATE = "Checkstyle %s - "
        + "http://checkstyle.sourceforge.net/releasenotes.html#Release_%s";

    /** The name of the file to write a Twitter post in. */
    private static final String TWITTER_POST_FILE_NAME = "twitter_post.txt";

    /** The name of the file to write a Google Plus post in. */
    private static final String GPLUS_POST_FILE_NAME = "google_plus_post.txt";

    /** The name of the file to write a sourceforge post in. */
    private static final String SOURCEFORGE_POST_FILE_NAME = "sourceforge_post.txt";

    /** The name of the file to write a mailing list post in. */
    private static final String MAILING_POST_FILE_NAME = "mailing_list_post.txt";

    /** The name of the file to write an RSS post in. */
    private static final String RSS_POST_FILE_NAME = "rss_post.txt";

    /** The sections for posts. */
    private final List<PostSection> postSections;

    /** The title for posts. */
    private final String title;

    /** The title for posts. */
    private final CliOptions config;

    /** Full post. */
    private String fullPost;

    /** Short post. */
    private String shortPost;

    /**
     * @param rootNode DOM tree root node of release notes.
     * @param config command line options.
     */
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

    /**
     * Generate posts and write them to files.
     * @throws IOException if there is a problem with files access.
     */
    public void generatePosts() throws IOException {
        if (config.isGenerateAll()) {
            writeTwitterPost();
            writeGplusPost();
            writeSourceforgePost();
            writeMListPost();
            writeRssPost();
        }
        else {
            if (config.isGenerateTw()) {
                writeTwitterPost();
            }
        }
    }

    /**
     * Publish posts.
     */
    public void publishPosts() {
        if (config.isPublishAll()) {
            publishOnTwitter();
        }
        else {
            if (config.isPublishTw()) {
                publishOnTwitter();
            }
        }
    }

    /**
     * Generate a full post.
     * @return a full post.
     */
    private String generateFullPost() {
        if (fullPost == null) {
            final StringBuilder fpBuilder = new StringBuilder();
            fpBuilder.append(title).append('\n');

            for (PostSection postSection : postSections) {
                final String postSectionTitle = postSection.getTitle();
                fpBuilder.append('\n').append(postSectionTitle).append('\n').append('\n');

                for (String record : postSection.getRecords()) {
                    fpBuilder.append(' ').append(' ').append(record).append('\n');
                }
            }
            fullPost = fpBuilder.toString();
        }
        return fullPost;
    }

    /**
     * Generate a short post.
     * @return a short post.
     */
    private String generateShortPost() {
        if (shortPost == null) {
            final StringBuilder spBuilder = new StringBuilder();
            spBuilder.append(title).append('\n').append('\n');

            for (PostSection postSection : postSections) {
                final String postSectionTitle = postSection.getTitle();
                if (!postSectionTitle.startsWith("Notes")) {
                    spBuilder.append(postSectionTitle).append(' ')
                            .append(postSection.getRecords().size()).append('\n');
                }
            }
            shortPost = spBuilder.toString();
        }
        return shortPost;
    }

    /**
     * Write a post to a file.
     * @param post the post to write.
     * @param fileName the name of the file.
     * @throws IOException if there is a problem with files access.
     */
    private void writePost(String post, String fileName) throws IOException {
        Files.write(Paths.get(config.getOutputLocation() + fileName), post.getBytes("UTF-8"));
    }

    /**
     * Write a Twitter post to a file.
     * @throws IOException if there is a problem with files access.
     */
    private void writeTwitterPost() throws IOException {
        writePost(generateTwitterPost(), TWITTER_POST_FILE_NAME);
    }

    /**
     * Generate a Twitter post.
     * @return a Twitter post.
     */
    private String generateTwitterPost() {
        return generateShortPost();
    }

    /**
     * Write a Google Plus post to a file.
     * @throws IOException if there is a problem with files access.
     */
    private void writeGplusPost() throws IOException {
        writePost(generateGplusPost(), GPLUS_POST_FILE_NAME);
    }

    /**
     * Generate a Google Plus post.
     * @return a Google Plus post.
     */
    private String generateGplusPost() {
        return generateShortPost();
    }

    /**
     * Write a Sourceforge post to a file.
     * @throws IOException if there is a problem with files access.
     */
    private void writeSourceforgePost() throws IOException {
        writePost(generateSourceforgePost(), SOURCEFORGE_POST_FILE_NAME);
    }

    /**
     * Generate a Sourceforge post.
     * @return a Sourceforge post.
     */
    private String generateSourceforgePost() {
        return generateFullPost();
    }

    /**
     * Write a mailing list post to a file.
     * @throws IOException if there is a problem with files access.
     */
    private void writeMListPost() throws IOException {
        writePost(generateMListPost(), MAILING_POST_FILE_NAME);
    }

    /**
     * Generate a mailing list post.
     * @return a mailing list post.
     */
    private String generateMListPost() {
        return generateFullPost();
    }

    /**
     * Write a mailing list post to a file.
     * @throws IOException if there is a problem with files access.
     */
    private void writeRssPost() throws IOException {
        writePost(generateRssPost(), RSS_POST_FILE_NAME);
    }

    /**
     * Generate an RSS post.
     * @return an RSS post.
     */
    private String generateRssPost() {
        return generateFullPost();
    }

    /** Publish post on Twitter. */
    private void publishOnTwitter() {
        try {
            final Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(config.getConsKeyTw(), config.getConsSecretTw());
            final AccessToken accessToken = new AccessToken(config.getAccessTokenTw(),
                    config.getAccessTokenSecretTw());
            twitter.setOAuthAccessToken(accessToken);
            twitter.updateStatus(generateTwitterPost());
        }
        catch (TwitterException tex) {
            System.err.printf("Cannot publish on Twitter: %s", tex.getMessage());
        }
    }
}
