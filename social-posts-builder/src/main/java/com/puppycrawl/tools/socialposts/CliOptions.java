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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Helper structure.
 * @author Vladislav Lisetskii
 */
public final class CliOptions {

    /** Properties file location. */
    private String releaseNotes;
    /** Output file location. */
    private String outputLocation;
    /** Whether to generate all posts. */
    private boolean generateAll;
    /** Whether to publish all posts. */
    private boolean publishAll;

    /** Whether to generate a post for Twitter. */
    private boolean generateTw;
    /** Whether to publish a post on Twitter. */
    private boolean publishTw;
    /** Consumer key for Twitter. */
    private String consKeyTw;
    /** Consumer secret for Twitter. */
    private String consSecretTw;
    /** Access token for Twitter. */
    private String accessTokenTw;
    /** Access token secret for Twitter. */
    private String accessTokenSecretTw;
    /** Propeties for connection to Twitter. */
    private String propTw;

    /** Instead newBuilder() should be used. */
    private CliOptions() { }

    /**
     * Creates a new Builder instance.
     * @return new Builder instance.
     */
    public static Builder newBuilder() {
        return new CliOptions().new Builder();
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public boolean isGenerateAll() {
        return generateAll;
    }

    public boolean isPublishAll() {
        return publishAll;
    }

    public boolean isGenerateTw() {
        return generateTw;
    }

    public boolean isPublishTw() {
        return publishTw;
    }

    public String getConsKeyTw() {
        return consKeyTw;
    }

    public String getConsSecretTw() {
        return consSecretTw;
    }

    public String getAccessTokenTw() {
        return accessTokenTw;
    }

    public String getAccessTokenSecretTw() {
        return accessTokenSecretTw;
    }

    /** Class which implements Builder pattern for building CliOptions instance. */
    final class Builder {

        /** Default constructor. */
        private Builder() { }

        public Builder releaseNotes(String releaseNotes) {
            CliOptions.this.releaseNotes = releaseNotes;
            return this;
        }

        public Builder outputLocation(String outputLocation) {
            CliOptions.this.outputLocation = outputLocation;
            return this;
        }

        public Builder generateAll(boolean generateAll) {
            CliOptions.this.generateAll = generateAll;
            return this;
        }

        public Builder publishAll(boolean publishAll) {
            CliOptions.this.publishAll = publishAll;
            return this;
        }

        public Builder generateTw(boolean generateTw) {
            CliOptions.this.generateTw = generateTw;
            return this;
        }

        public Builder publishTw(boolean publishTw) {
            CliOptions.this.publishTw = publishTw;
            return this;
        }

        public Builder consKeyTw(String consKeyTw) {
            CliOptions.this.consKeyTw = consKeyTw;
            return this;
        }

        public Builder consSecretTw(String consSecretTw) {
            CliOptions.this.consSecretTw = consSecretTw;
            return this;
        }

        public Builder accessTokenTw(String accessTokenTw) {
            CliOptions.this.accessTokenTw = accessTokenTw;
            return this;
        }

        public Builder accessTokenSecretTw(String accessTokenSecretTw) {
            CliOptions.this.accessTokenSecretTw = accessTokenSecretTw;
            return this;
        }

        public Builder propTw(String propTw) {
            CliOptions.this.propTw = propTw;
            return this;
        }

        public CliOptions build() {
            if (outputLocation == null) {
                outputLocation = "";
            }
            if ((publishTw || publishAll) && propTw != null
                    && (consKeyTw == null || consSecretTw == null
                        || accessTokenTw == null || accessTokenSecretTw == null)) {
                loadTwitterProps();
            }
            return CliOptions.this;
        }

        private void loadTwitterProps() {
            try (InputStream propStream = new FileInputStream(propTw)) {
                final Properties props = new Properties();
                props.load(propStream);

                if (consKeyTw == null) {
                    consKeyTw = props.getProperty(Main.OPTION_CONS_KEY_TW);
                }
                if (consSecretTw == null) {
                    consSecretTw = props.getProperty(Main.OPTION_CONS_SECRET_TW);
                }
                if (accessTokenTw == null) {
                    accessTokenTw = props.getProperty(Main.OPTION_ACCESS_TOKEN_TW);
                }
                if (accessTokenSecretTw == null) {
                    accessTokenSecretTw = props.getProperty(Main.OPTION_ACCESS_TOKEN_SECRET_TW);
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
