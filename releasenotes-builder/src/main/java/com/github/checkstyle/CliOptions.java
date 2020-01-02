////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2020 the original author or authors.
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Verify;

/**
 * Helper structure class to clear show what command line arguments are required for NotesBuilder
 * to run.
 * @author Andrei Selkin
 */
public final class CliOptions {

    /** Path to a local git repository. */
    private String localRepoPath;
    /** Path to remote github repository. */
    private String remoteRepoPath;
    /** Start reference. */
    private String startRef;
    /** End reference. */
    private String endRef;
    /** Release number. */
    private String releaseNumber;
    /** Auth token. */
    private String authToken;

    /** Output file location. */
    private String outputLocation;
    /** Whether to generate all posts. */
    private boolean generateAll;
    /** Whether to generate a post for xdoc. */
    private boolean generateXdoc;
    /** Whether to generate a post for Twitter. */
    private boolean generateTw;
    /** Whether to generate a post for RSS. */
    private boolean generateRss;
    /** Whether to generate a post for Mailing List. */
    private boolean generateMlist;

    /** File location for xdoc template. */
    private String xdocTemplate;
    /** File location for twitter template. */
    private String twitterTemplate;
    /** File location for rss template. */
    private String rssTemplate;
    /** File location for mailing list template. */
    private String mlistTemplate;

    /** Whether to publish all social posts. */
    private boolean publishAllSocial;

    /** Whether to publish on Twitter. */
    private boolean publishTwit;
    /** Consumer key for Twitter. */
    private String twitterConsumerKey;
    /** Consumer secret for Twitter. */
    private String twitterConsumerSecret;
    /** Access token for Twitter. */
    private String twitterAccessToken;
    /** Access token secret for Twitter. */
    private String twitterAccessTokenSecret;
    /** Properties for connection to Twitter. */
    private String twitterProperties;

    /** Whether to publish xdoc. */
    private boolean publishXdoc;
    /** Whether to publish xdoc with push. */
    private boolean publishXdocWithPush;

    /** Whether to publish to mailing list. */
    private boolean publishMlist;
    /** Username to publish to mailing list. */
    private String mlistUsername;
    /** Password to publish to mailing list. */
    private String mlistPassword;
    /** Properties to publish to mailing list. */
    private String mlistProperties;

    /** Whether to publish to RSS. */
    private boolean publishSfRss;
    /** Bearer token for Sourceforge to publish to RSS. */
    private String sfRssBearerToken;
    /** Properties to publish to RSS. */
    private String sfRssProperties;

    /** Default constructor. */
    private CliOptions() {
    }

    public String getLocalRepoPath() {
        return localRepoPath;
    }

    public String getRemoteRepoPath() {
        return remoteRepoPath;
    }

    public String getStartRef() {
        return startRef;
    }

    public String getEndRef() {
        return endRef;
    }

    public String getReleaseNumber() {
        return releaseNumber;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public boolean isGenerateAll() {
        return generateAll;
    }

    public boolean isGenerateXdoc() {
        return generateXdoc;
    }

    public boolean isGenerateTw() {
        return generateTw;
    }

    public boolean isGenerateRss() {
        return generateRss;
    }

    public boolean isGenerateMlist() {
        return generateMlist;
    }

    public String getXdocTemplate() {
        return xdocTemplate;
    }

    public String getTwitterTemplate() {
        return twitterTemplate;
    }

    public String getRssTemplate() {
        return rssTemplate;
    }

    public String getMlistTemplate() {
        return mlistTemplate;
    }

    public boolean isPublishAllSocial() {
        return publishAllSocial;
    }

    public boolean isPublishTwit() {
        return publishTwit;
    }

    public String getTwitterConsumerKey() {
        return twitterConsumerKey;
    }

    public String getTwitterConsumerSecret() {
        return twitterConsumerSecret;
    }

    public String getTwitterAccessToken() {
        return twitterAccessToken;
    }

    public String getTwitterAccessTokenSecret() {
        return twitterAccessTokenSecret;
    }

    public boolean isPublishXdoc() {
        return publishXdoc;
    }

    public boolean isPublishXdocWithPush() {
        return publishXdocWithPush;
    }

    public boolean isPublishMlist() {
        return publishMlist;
    }

    public String getMlistUsername() {
        return mlistUsername;
    }

    public String getMlistPassword() {
        return mlistPassword;
    }

    public boolean isPublishSfRss() {
        return publishSfRss;
    }

    public String getSfRssBearerToken() {
        return sfRssBearerToken;
    }

    /**
     * Creates a new Builder instance.
     * @return new Builder instance.
     */
    public static Builder newBuilder() {
        return new CliOptions().new Builder();
    }

    /**
     * Class which implements Builder pattern for building CliOptions instance.
     */
    public final class Builder {

        /** Default constructor. */
        private Builder() {
        }

        /**
         * Specify Local repository path.
         * @param path Local repository path
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setLocalRepoPath(String path) {
            localRepoPath = path;
            return this;
        }

        /**
         * Specify Remote repository path.
         * @param path Remote repository path
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setRemoteRepoPath(String path) {
            remoteRepoPath = path;
            return this;
        }

        /**
         * Specify Start git reference.
         * @param ref Start reference
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setStartRef(String ref) {
            startRef = ref;
            return this;
        }

        /**
         * Specify End git reference.
         * @param ref End reference
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setEndRef(String ref) {
            endRef = ref;
            return this;
        }

        /**
         * Specify release number.
         * @param number Release Number
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setReleaseNumber(String number) {
            releaseNumber = number;
            return this;
        }

        /**
         * Specify Auth Token.
         * @param token Auth Token
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setAuthToken(String token) {
            authToken = token;
            return this;
        }

        /**
         * Specify Output location.
         * @param outputLoc Output location
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setOutputLocation(String outputLoc) {
            outputLocation = outputLoc;
            return this;
        }

        /**
         * Specify flag to generate all.
         * @param genAll flag to generate all
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setGenerateAll(boolean genAll) {
            generateAll = genAll;
            return this;
        }

        /**
         * Specify flag to generate xdoc.
         * @param genXdoc flag to generate xdoc
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setGenerateXdoc(boolean genXdoc) {
            generateXdoc = genXdoc;
            return this;
        }

        /**
         * Spacify flag to generate twiter post.
         * @param genTw flag to generate twitt
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setGenerateTw(boolean genTw) {
            generateTw = genTw;
            return this;
        }

        /**
         * Specify flag to generate RSS post.
         * @param genRss flag to generate RSS post
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setGenerateRss(boolean genRss) {
            generateRss = genRss;
            return this;
        }

        /**
         * Spacify flag to generate Mail-list post.
         * @param genMlist flag to generate mail-list post
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setGenerateMlist(boolean genMlist) {
            generateMlist = genMlist;
            return this;
        }

        /**
         * Specify xdoc template.
         * @param xdocTemp xdoc template
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setXdocTemplate(String xdocTemp) {
            xdocTemplate = xdocTemp;
            return this;
        }

        /**
         * Specify twitter template.
         * @param twitterTemp twitter template
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setTwitterTemplate(String twitterTemp) {
            twitterTemplate = twitterTemp;
            return this;
        }

        /**
         * Specify rss template.
         * @param rssTemp rss template
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setRssTemplate(String rssTemp) {
            rssTemplate = rssTemp;
            return this;
        }

        /**
         * Specify mailing list template.
         * @param mlistTemp mailing list template
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setMlistTemplate(String mlistTemp) {
            mlistTemplate = mlistTemp;
            return this;
        }

        /**
         * Spacify to do publish all social posts.
         * @param pubAllSocial flag to generate all social posts
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setPublishAllSocial(boolean pubAllSocial) {
            publishAllSocial = pubAllSocial;
            return this;
        }

        /**
         * Specify to do publish only for twitter.
         * @param publishTw flag to publish twitt
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setPublishTwit(boolean publishTw) {
            publishTwit = publishTw;
            return this;
        }

        /**
         * Specify Twitter consumer key.
         * @param twConsKey twitter consumer key
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setTwitterConsumerKey(String twConsKey) {
            twitterConsumerKey = twConsKey;
            return this;
        }

        /**
         * Specify Twitter Consumer secret.
         * @param twConsSecret twitter consumer secret
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setTwitterConsumerSecret(String twConsSecret) {
            twitterConsumerSecret = twConsSecret;
            return this;
        }

        /**
         * Specify Twitter Access Token.
         * @param twAccessToken twitter access token
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setTwitterAccessToken(String twAccessToken) {
            twitterAccessToken = twAccessToken;
            return this;
        }

        /**
         * Specify Access Token Secret.
         * @param twAccessTokenSecret twitter access token secret
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setTwitterAccessTokenSecret(String twAccessTokenSecret) {
            twitterAccessTokenSecret = twAccessTokenSecret;
            return this;
        }

        /**
         * Specify Twitter Properties.
         * @param twProperties twitter properties
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setTwitterProperties(String twProperties) {
            twitterProperties = twProperties;
            return this;
        }

        /**
         * Specify to publish Xdoc update.
         * @param pubXdoc flag to publish xdoc file update
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setPublishXdoc(boolean pubXdoc) {
            publishXdoc = pubXdoc;
            return this;
        }

        /**
         * Specify to publish xdoc update and do push to remote git.
         * @param pubXdocWithPush flag to publish xdoc and push to remote repo
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setPublishXdocWithPush(boolean pubXdocWithPush) {
            publishXdocWithPush = pubXdocWithPush;
            return this;
        }

        /**
         * Specify to do publication only for mailing list.
         * @param pubMlist flag to publish to mailing list
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setPublishMlist(boolean pubMlist) {
            publishMlist = pubMlist;
            return this;
        }

        /**
         * Specify username to publish to mailing list.
         * @param username mailing list username
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setMlistUsername(String username) {
            mlistUsername = username;
            return this;
        }

        /**
         * Specify password to publish to mailing list.
         * @param password mailing list password
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setMlistPassword(String password) {
            mlistPassword = password;
            return this;
        }

        /**
         * Specify mailing list properties.
         * @param mlistProps mailing list properties
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setMlistProperties(String mlistProps) {
            mlistProperties = mlistProps;
            return this;
        }

        /**
         * Specify to do publication only for RSS.
         * @param pubRss flag to publish to RSS
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setPublishSfRss(boolean pubRss) {
            publishSfRss = pubRss;
            return this;
        }

        /**
         * Specify mailing list properties.
         * @param token sourceforge bearer token
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setSfBearerToken(String token) {
            sfRssBearerToken = token;
            return this;
        }

        /**
         * Specify RSS properties.
         * @param rssProps mailing list properties
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         */
        public Builder setSfRssProperties(String rssProps) {
            sfRssProperties = rssProps;
            return this;
        }

        /**
         * Verify options and set defaults.
         * @return new CliOption instance
         */
        public CliOptions build() {
            if (endRef == null) {
                endRef = "HEAD";
            }
            if (outputLocation == null) {
                outputLocation = "";
            }
            Verify.verifyNotNull(localRepoPath,
                "Path to a local git repository should not be null!");
            Verify.verifyNotNull(remoteRepoPath,
                    "Path to a remote github repository should not be null!");
            Verify.verifyNotNull(startRef, "Start reference should not be null!");
            Verify.verifyNotNull(releaseNumber, "Release number should not be null!");

            if (shouldLoadTwitterProperties()) {
                Verify.verifyNotNull(twitterProperties, "Properties file for Twitter is expected"
                    + " if some of the following options are not entered: twitterConsumerKey, "
                    + "twitterConsumerSecret, twitterAccessToken, twitterAccessTokenSecret.");
                loadTwitterProperties();
                Verify.verifyNotNull(twitterConsumerKey, "Consumer key for Twitter is expected!");
                Verify.verifyNotNull(twitterConsumerSecret,
                    "Consumer secret for Twitter is expected!");
                Verify.verifyNotNull(twitterAccessToken, "Access token for Twitter is expected!");
                Verify.verifyNotNull(twitterAccessTokenSecret,
                    "Access token secret for Twitter is expected!");
            }

            if (shouldLoadMlistProperties()) {
                Verify.verifyNotNull(mlistProperties, "Properties file for mailing list is "
                    + "expected if some of the following options are not entered: mlistUsername, "
                    + "mlistPassword.");
                loadMlistProperties();
                Verify.verifyNotNull(mlistUsername, "Username for mailing list is expected!");
                Verify.verifyNotNull(mlistPassword, "Password for mailing list is expected!");
            }

            if ((publishAllSocial || publishSfRss) && sfRssBearerToken == null) {
                Verify.verifyNotNull(sfRssProperties, "Properties file for RSS is expected"
                        + " if some of the following options are not entered: sfRssBearerToken.");
                loadSfRssProperties();
                Verify.verifyNotNull(sfRssBearerToken, "sfRssBearerToken for RSS is expected!");
            }
            return getNewCliOptionsInstance();
        }

        /**
         * Whether Twitter properties should be loaded.
         * @return true, if Twitter properties should be loaded.
         */
        private boolean shouldLoadTwitterProperties() {
            return (publishAllSocial || publishTwit)
                    && (twitterConsumerKey == null || twitterConsumerSecret == null
                    || twitterAccessToken == null || twitterAccessTokenSecret == null);
        }

        /**
         * Load options for Twitter publication from properties if they were not set.
         *
         * @throws IllegalStateException when there is problem to load properties
         */
        private void loadTwitterProperties() {
            try (InputStream propStream = new FileInputStream(twitterProperties)) {
                final Properties props = new Properties();
                props.load(propStream);

                if (twitterConsumerKey == null) {
                    twitterConsumerKey =
                        props.getProperty(CliProcessor.OPTION_TWITTER_CONSUMER_KEY);
                }
                if (twitterConsumerSecret == null) {
                    twitterConsumerSecret =
                        props.getProperty(CliProcessor.OPTION_TWITTER_CONSUMER_SECRET);
                }
                if (twitterAccessToken == null) {
                    twitterAccessToken =
                        props.getProperty(CliProcessor.OPTION_TWITTER_ACCESS_TOKEN);
                }
                if (twitterAccessTokenSecret == null) {
                    twitterAccessTokenSecret =
                        props.getProperty(CliProcessor.OPTION_TWITTER_ACCESS_TOKEN_SECRET);
                }
            }
            catch (IOException ex) {
                throw new IllegalStateException("Twitter properties file has access problems"
                    + " (twitterProperties=" + twitterProperties + ')', ex);
            }
        }

        /**
         * Whether RSS properties should be loaded.
         * @return true, if RSS properties should be loaded.
         */
        private boolean shouldLoadMlistProperties() {
            return (publishAllSocial || publishMlist)
                    && (mlistUsername == null || mlistPassword == null);
        }

        /**
         * Load options for mailing list publication from properties if they were not set.
         *
         * @throws IllegalStateException when there is problem to load properties
         */
        private void loadMlistProperties() {
            try (InputStream propStream = new FileInputStream(mlistProperties)) {
                final Properties props = new Properties();
                props.load(propStream);

                if (mlistUsername == null) {
                    mlistUsername = props.getProperty(CliProcessor.OPTION_MLIST_USERNAME);
                }
                if (mlistPassword == null) {
                    mlistPassword = props.getProperty(CliProcessor.OPTION_MLIST_PASSWORD);
                }
            }
            catch (IOException ex) {
                throw new IllegalStateException("Mailing list properties file has access problems"
                    + " (mlistProperties=" + mlistProperties + ')', ex);
            }
        }

        /**
         * Load options for RSS publication from properties if they were not set.
         *
         * @throws IllegalStateException when there is problem to load properties
         */
        private void loadSfRssProperties() {
            try (InputStream propStream = new FileInputStream(sfRssProperties)) {
                final Properties props = new Properties();
                props.load(propStream);

                sfRssBearerToken = props.getProperty(CliProcessor.OPTION_SF_RSS_BEARER_TOKEN);
            }
            catch (IOException ex) {
                throw new IllegalStateException("RSS properties file has access problems"
                    + " (sfRssProperties=" + sfRssProperties + ')', ex);
            }
        }

        /**
         * Get new CliOptions instance.
         * @return new CliOptions instance.
         */
        // -@cs[ExecutableStatementCount] long list of options being assigned to single instance
        private CliOptions getNewCliOptionsInstance() {
            final CliOptions cliOptions = new CliOptions();
            cliOptions.localRepoPath = localRepoPath;
            cliOptions.remoteRepoPath = remoteRepoPath;
            cliOptions.startRef = startRef;
            cliOptions.endRef = endRef;
            cliOptions.releaseNumber = releaseNumber;
            cliOptions.outputLocation = outputLocation;
            cliOptions.authToken = authToken;
            cliOptions.generateAll = generateAll;
            cliOptions.generateXdoc = generateXdoc;
            cliOptions.generateTw = generateTw;
            cliOptions.generateRss = generateRss;
            cliOptions.generateMlist = generateMlist;
            cliOptions.xdocTemplate = xdocTemplate;
            cliOptions.twitterTemplate = twitterTemplate;
            cliOptions.rssTemplate = rssTemplate;
            cliOptions.mlistTemplate = mlistTemplate;
            cliOptions.publishAllSocial = publishAllSocial;
            cliOptions.publishTwit = publishTwit;
            cliOptions.twitterConsumerKey = twitterConsumerKey;
            cliOptions.twitterConsumerSecret = twitterConsumerSecret;
            cliOptions.twitterAccessToken = twitterAccessToken;
            cliOptions.twitterAccessTokenSecret = twitterAccessTokenSecret;
            cliOptions.twitterProperties = twitterProperties;
            cliOptions.publishXdoc = publishXdoc;
            cliOptions.publishXdocWithPush = publishXdocWithPush;
            cliOptions.publishMlist = publishMlist;
            cliOptions.mlistUsername = mlistUsername;
            cliOptions.mlistPassword = mlistPassword;
            cliOptions.mlistProperties = mlistProperties;
            cliOptions.publishSfRss = publishSfRss;
            cliOptions.sfRssBearerToken = sfRssBearerToken;
            cliOptions.sfRssProperties = sfRssProperties;
            return cliOptions;
        }

    }
}
