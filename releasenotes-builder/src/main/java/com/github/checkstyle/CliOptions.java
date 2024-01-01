///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code and other text files for adherence to a set of rules.
// Copyright (C) 2001-2024 the original author or authors.
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

package com.github.checkstyle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Verify;

/**
 * Helper structure class to clear show what command line arguments are required for NotesBuilder
 * to run.
 *
 * @author Andrei Selkin
 */
public final class CliOptions {

    /** Whether to display the help text. */
    private boolean help;
    /** Path to a local git repository. */
    private String localRepoPath;
    /** Path to remote GitHub repository. */
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
    /** Whether to generate a post for GitHub Page. */
    private boolean generateGitHub;

    /** File location for xdoc template. */
    private String xdocTemplate;
    /** File location for twitter template. */
    private String twitterTemplate;
    /** File location for GitHub page template. */
    private String gitHubTemplate;

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

    /** Validate that release version matches issues labels. */
    private boolean validateVersion;

    /** Default constructor. */
    private CliOptions() {
    }

    /**
     * Returns the path to a local git repository.
     *
     * @return the path to a local git repository
     */
    public String getLocalRepoPath() {
        return localRepoPath;
    }

    /**
     * Returns whether to display help.
     *
     * @return whether to display help.
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * Returns the path to remote GitHub repository.
     *
     * @return the path to remote GitHub repository
     */
    public String getRemoteRepoPath() {
        return remoteRepoPath;
    }

    /**
     * Returns the start reference.
     *
     * @return the start reference
     */
    public String getStartRef() {
        return startRef;
    }

    /**
     * Returns the end reference.
     *
     * @return the end reference
     */
    public String getEndRef() {
        return endRef;
    }

    /**
     * Returns the release number.
     *
     * @return the release number
     */
    public String getReleaseNumber() {
        return releaseNumber;
    }

    /**
     * Returns the auth token.
     *
     * @return the auth token
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Returns the output file location.
     *
     * @return the output file location
     */
    public String getOutputLocation() {
        return outputLocation;
    }

    /**
     * Returns whether to generate all posts.
     *
     * @return whether to generate all posts
     */
    public boolean isGenerateAll() {
        return generateAll;
    }

    /**
     * Returns whether to generate a post for xdoc.
     *
     * @return whether to generate a post for xdoc
     */
    public boolean isGenerateXdoc() {
        return generateXdoc;
    }

    /**
     * Returns whether to generate a post for Twitter.
     *
     * @return whether to generate a post for Twitter
     */
    public boolean isGenerateTw() {
        return generateTw;
    }

    /**
     * Returns whether to generate a post for GitHub page.
     *
     * @return whether to generate a post for GitHub page.
     */
    public boolean isGenerateGitHub() {
        return generateGitHub;
    }

    /**
     * Returns the file location for xdoc template.
     *
     * @return the file location for xdoc template
     */
    public String getXdocTemplate() {
        return xdocTemplate;
    }

    /**
     * Returns the file location for twitter template.
     *
     * @return the file location for twitter template
     */
    public String getTwitterTemplate() {
        return twitterTemplate;
    }

    /**
     * Returns the file location for GitHub Post template.
     *
     * @return the file location for GitHub Post template
     */
    public String getGitHubTemplate() {
        return gitHubTemplate;
    }

    /**
     * Returns whether to publish all social posts.
     *
     * @return whether to publish all social posts
     */
    public boolean isPublishAllSocial() {
        return publishAllSocial;
    }

    /**
     * Returns whether to publish on Twitter.
     *
     * @return whether to publish on Twitter
     */
    public boolean isPublishTwit() {
        return publishTwit;
    }

    /**
     * Returns the consumer key for Twitter.
     *
     * @return the consumer key for Twitter
     */
    public String getTwitterConsumerKey() {
        return twitterConsumerKey;
    }

    /**
     * Returns the twitter consumer secret.
     *
     * @return the twitter consumer secret
     */
    public String getTwitterConsumerSecret() {
        return twitterConsumerSecret;
    }

    /**
     * Returns the access token for Twitter.
     *
     * @return the access token for Twitter
     */
    public String getTwitterAccessToken() {
        return twitterAccessToken;
    }

    /**
     * Returns the access token secret for Twitter.
     *
     * @return the access token secret for Twitter
     */
    public String getTwitterAccessTokenSecret() {
        return twitterAccessTokenSecret;
    }

    /**
     * Returns whether to validate release version to issues labels.
     *
     * @return whether to validate release version.
     */
    public boolean isValidateVersion() {
        return validateVersion;
    }

    /**
     * Creates a new Builder instance.
     *
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
         * Specify flag to display help.
         *
         * @param inHelp flag to display help
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setHelp(boolean inHelp) {
            help = inHelp;
            return this;
        }

        /**
         * Specify Local repository path.
         *
         * @param path Local repository path
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setLocalRepoPath(String path) {
            localRepoPath = path;
            return this;
        }

        /**
         * Specify Remote repository path.
         *
         * @param path Remote repository path
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setRemoteRepoPath(String path) {
            remoteRepoPath = path;
            return this;
        }

        /**
         * Specify Start git reference.
         *
         * @param ref Start reference
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setStartRef(String ref) {
            startRef = ref;
            return this;
        }

        /**
         * Specify End git reference.
         *
         * @param ref End reference
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setEndRef(String ref) {
            endRef = ref;
            return this;
        }

        /**
         * Specify release number.
         *
         * @param number Release Number
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setReleaseNumber(String number) {
            releaseNumber = number;
            return this;
        }

        /**
         * Specify Auth Token.
         *
         * @param token Auth Token
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setAuthToken(String token) {
            authToken = token;
            return this;
        }

        /**
         * Specify Output location.
         *
         * @param outputLoc Output location
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setOutputLocation(String outputLoc) {
            outputLocation = outputLoc;
            return this;
        }

        /**
         * Specify flag to generate all.
         *
         * @param genAll flag to generate all
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setGenerateAll(boolean genAll) {
            generateAll = genAll;
            return this;
        }

        /**
         * Specify flag to generate xdoc.
         *
         * @param genXdoc flag to generate xdoc
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setGenerateXdoc(boolean genXdoc) {
            generateXdoc = genXdoc;
            return this;
        }

        /**
         * Spacify flag to generate twiter post.
         *
         * @param genTw flag to generate twitt
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setGenerateTw(boolean genTw) {
            generateTw = genTw;
            return this;
        }

        /**
         * Specify GitHub post template.
         *
         * @param genGitHub gitHub post
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setGenerateGitHub(boolean genGitHub) {
            generateGitHub = genGitHub;
            return this;
        }

        /**
         * Specify xdoc template.
         *
         * @param xdocTemp xdoc template
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setXdocTemplate(String xdocTemp) {
            xdocTemplate = xdocTemp;
            return this;
        }

        /**
         * Specify twitter template.
         *
         * @param twitterTemp twitter template
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setTwitterTemplate(String twitterTemp) {
            twitterTemplate = twitterTemp;
            return this;
        }

        /**
         * Specify GitHub Post template.
         *
         * @param gitHubPageTemp GitHub post template
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setGitHubTemplate(String gitHubPageTemp) {
            gitHubTemplate = gitHubPageTemp;
            return this;
        }

        /**
         * Spacify to do publish all social posts.
         *
         * @param pubAllSocial flag to generate all social posts
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setPublishAllSocial(boolean pubAllSocial) {
            publishAllSocial = pubAllSocial;
            return this;
        }

        /**
         * Specify to do publish only for twitter.
         *
         * @param publishTw flag to publish twitt
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setPublishTwit(boolean publishTw) {
            publishTwit = publishTw;
            return this;
        }

        /**
         * Specify Twitter consumer key.
         *
         * @param twConsKey twitter consumer key
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setTwitterConsumerKey(String twConsKey) {
            twitterConsumerKey = twConsKey;
            return this;
        }

        /**
         * Specify Twitter Consumer secret.
         *
         * @param twConsSecret twitter consumer secret
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setTwitterConsumerSecret(String twConsSecret) {
            twitterConsumerSecret = twConsSecret;
            return this;
        }

        /**
         * Specify Twitter Access Token.
         *
         * @param twAccessToken twitter access token
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setTwitterAccessToken(String twAccessToken) {
            twitterAccessToken = twAccessToken;
            return this;
        }

        /**
         * Specify Access Token Secret.
         *
         * @param twAccessTokenSecret twitter access token secret
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setTwitterAccessTokenSecret(String twAccessTokenSecret) {
            twitterAccessTokenSecret = twAccessTokenSecret;
            return this;
        }

        /**
         * Specify Twitter Properties.
         *
         * @param twProperties twitter properties
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setTwitterProperties(String twProperties) {
            twitterProperties = twProperties;
            return this;
        }

        /**
         * Specify whether to validate release version.
         *
         * @param validate flag to validate release version.
         * @return Builder Object
         * @noinspection ReturnOfInnerClass
         * @noinspectionreason ReturnOfInnerClass - builder is only used in enclosing class
         */
        public Builder setValidateVersion(boolean validate) {
            validateVersion = validate;
            return this;
        }

        /**
         * Verify options and set defaults.
         *
         * @return new CliOption instance
         */
        public CliOptions build() {
            if (endRef == null) {
                endRef = "HEAD";
            }
            if (outputLocation == null) {
                outputLocation = "";
            }
            else if (!outputLocation.endsWith(File.separator)) {
                outputLocation += File.separator;
            }

            if (!help) {
                Verify.verifyNotNull(localRepoPath,
                    "Path to a local git repository should not be null!");
                Verify.verifyNotNull(remoteRepoPath,
                        "Path to a remote github repository should not be null!");
                Verify.verifyNotNull(startRef, "Start reference should not be null!");
                Verify.verifyNotNull(releaseNumber, "Release number should not be null!");

                if (shouldLoadTwitterProperties()) {
                    Verify.verifyNotNull(twitterProperties,
                        "Properties file for Twitter is expected if some of the following options "
                            + "are not entered: twitterConsumerKey, twitterConsumerSecret, "
                            + "twitterAccessToken, twitterAccessTokenSecret.");
                    loadTwitterProperties();
                    Verify.verifyNotNull(twitterConsumerKey,
                            "Consumer key for Twitter is expected!");
                    Verify.verifyNotNull(twitterConsumerSecret,
                        "Consumer secret for Twitter is expected!");
                    Verify.verifyNotNull(twitterAccessToken,
                            "Access token for Twitter is expected!");
                    Verify.verifyNotNull(twitterAccessTokenSecret,
                        "Access token secret for Twitter is expected!");
                }
            }

            return getNewCliOptionsInstance();
        }

        /**
         * Whether Twitter properties should be loaded.
         *
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
         * Get new CliOptions instance.
         *
         * @return new CliOptions instance.
         */
        // -@cs[ExecutableStatementCount] long list of options being assigned to single instance
        private CliOptions getNewCliOptionsInstance() {
            final CliOptions cliOptions = new CliOptions();
            cliOptions.help = help;
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
            cliOptions.generateGitHub = generateGitHub;
            cliOptions.xdocTemplate = xdocTemplate;
            cliOptions.twitterTemplate = twitterTemplate;
            cliOptions.gitHubTemplate = gitHubTemplate;
            cliOptions.publishAllSocial = publishAllSocial;
            cliOptions.publishTwit = publishTwit;
            cliOptions.twitterConsumerKey = twitterConsumerKey;
            cliOptions.twitterConsumerSecret = twitterConsumerSecret;
            cliOptions.twitterAccessToken = twitterAccessToken;
            cliOptions.twitterAccessTokenSecret = twitterAccessTokenSecret;
            cliOptions.twitterProperties = twitterProperties;
            cliOptions.validateVersion = validateVersion;
            return cliOptions;
        }

    }
}
