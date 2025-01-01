///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2025 the original author or authors.
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

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Class for Twitter post publication.
 *
 * @author Vladislav Lisetskii
 */
public final class TwitterPublisher {

    /** Private constructor. */
    private TwitterPublisher() {
    }

    /**
     * Publish post.
     *
     * @param consumerKey consumer key.
     * @param consumerSecret consumer secret.
     * @param accessToken access token.
     * @param accessTokenSecret access token secret.
     * @param post the exact post to publish.
     * @throws TwitterException if an error occurs while publishing.
     */
    public static void publish(String consumerKey, String consumerSecret,
            String accessToken, String accessTokenSecret, String post) throws TwitterException {
        final Twitter twitter = Twitter.newBuilder().oAuthConsumer(consumerKey, consumerSecret)
            .oAuthAccessToken(accessToken, accessTokenSecret).build();
        twitter.v1().tweets().updateStatus(post);
    }
}
