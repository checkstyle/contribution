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

package com.github.checkstyle.publishers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Class for Twitter post publication.
 * @author Vladislav Lisetskii
 */
public class TwitterPublisher {

    /** The name of the file to get post text from. */
    private final String postFilename;

    /** Consumer key. */
    private final String consumerKey;
    /** Consumer secret. */
    private final String consumerSecret;
    /** Access token. */
    private final String accessToken;
    /** Access token secret. */
    private final String accessTokenSecret;

    /**
     * Default constructor.
     * @param postFilename the name of the file to get post text from.
     * @param consumerKey consumer key.
     * @param consumerSecret consumer secret.
     * @param accessToken access token.
     * @param accessTokenSecret access token secret.
     */
    public TwitterPublisher(String postFilename, String consumerKey, String consumerSecret,
        String accessToken, String accessTokenSecret) {
        this.postFilename = postFilename;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.accessTokenSecret = accessTokenSecret;
    }

    /**
     * Publish post.
     * @throws TwitterException if an error occurs while publishing.
     * @throws IOException if there are problems with reading file with the post text.
     */
    public void publish() throws TwitterException, IOException {
        final Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
        final AccessToken token = new AccessToken(accessToken, accessTokenSecret);
        twitter.setOAuthAccessToken(token);
        final String post = new String(Files.readAllBytes(Paths.get(postFilename)),
            StandardCharsets.UTF_8);
        twitter.updateStatus(post);
    }
}
