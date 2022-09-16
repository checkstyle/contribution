///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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
///////////////////////////////////////////////////////////////////////////////////////////////

package com.github.checkstyle.publishers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for RSS post publication.
 *
 * @author Vladislav Lisetskii
 */
public class SourceforgeRssPublisher {

    /** URL to send POST request to. */
    private static final String POST_URL =
            "https://sourceforge.net/rest/p/checkstyle/news";
    /** Template for the post. */
    private static final String POST_TEMPLATE =
            "access_token=%s&title=Checkstyle %s&text=%s&state=published";

    /** Patter for finding a number. */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    /** The name of the file to get post text from. */
    private final String postFilename;

    /** Token for posting. */
    private final String bearerToken;
    /** Release number. */
    private final String releaseNumber;

    /**
     * Default constructor.
     *
     * @param postFilename the name of the file to get post text from.
     * @param bearerToken token for posting.
     * @param releaseNumber release number.
     */
    public SourceforgeRssPublisher(String postFilename, String bearerToken, String releaseNumber) {
        this.postFilename = postFilename;
        this.bearerToken = bearerToken;
        this.releaseNumber = releaseNumber;
    }

    /**
     * Publish release notes.
     *
     * @throws IOException if problem with access to files appears.
     */
    public void publish() throws IOException {
        final int postsCountBefore = getPostsCount();

        final HttpURLConnection conn = (HttpURLConnection) new URL(POST_URL).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            final String postText = new String(Files.readAllBytes(Paths.get(postFilename)),
                StandardCharsets.UTF_8);
            os.write(String.format(POST_TEMPLATE, bearerToken, releaseNumber, postText)
                .getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        final int responseCode = conn.getResponseCode();
        conn.disconnect();

        // Sourceforge may not response correctly, so we also compare te number of posts in
        // RSS feed before and after publication in such cases to test whether it was successful
        if (responseCode != HttpURLConnection.HTTP_OK && getPostsCount() == postsCountBefore) {
            throw new IOException("Failed to post on RSS with HTTP error code : "
                    + responseCode);
        }
    }

    /**
     * Get number of posts in RSS feed.
     *
     * @return number of posts in RSS feed or -1 if number is not found in response.
     * @throws IOException in case of problems with connection or InputStream.
     */
    private static int getPostsCount() throws IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(POST_URL).openConnection();
        final Matcher matcher;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            matcher = NUMBER_PATTERN.matcher(br.readLine());
        }

        int count = -1;
        if (matcher.find()) {
            count = Integer.parseInt(matcher.group());
        }
        return count;
    }
}
