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

package com.github.checkstyle;

import com.google.common.base.Verify;

/**
 * Helper structure class to clear show what command line arguments are required for NotesBuilder
 * to run.
 * @author Andrei Selkin
 */
public final class CliOptions {

    /** Path to a local git repository. */
    private String localRepoPath;
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
    /** Whether to generate a post for Google Plus. */
    private boolean generateGplus;
    /** Whether to generate a post for RSS. */
    private boolean generateRss;
    /** Whether to generate a post for Sourceforge. */
    private boolean generateSf;
    /** Whether to generate a post for Mailing List. */
    private boolean generateMlist;

    /** Default constructor. */
    private CliOptions() { }

    public String getLocalRepoPath() {
        return localRepoPath;
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

    public boolean isGenerateGplus() {
        return generateGplus;
    }

    public boolean isGenerateRss() {
        return generateRss;
    }

    public boolean isGenerateSf() {
        return generateSf;
    }

    public boolean isGenerateMlist() {
        return generateMlist;
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
    final class Builder {

        /** Default constructor. */
        private Builder() { }

        public Builder localRepoPath(String path) {
            CliOptions.this.localRepoPath = path;
            return this;
        }

        public Builder startRef(String ref) {
            CliOptions.this.startRef = ref;
            return this;
        }

        public Builder endRef(String ref) {
            CliOptions.this.endRef = ref;
            return this;
        }

        public Builder releaseNumber(String number) {
            CliOptions.this.releaseNumber = number;
            return this;
        }

        public Builder authToken(String token) {
            CliOptions.this.authToken = token;
            return this;
        }

        public Builder outputLocation(String outputLoc) {
            CliOptions.this.outputLocation = outputLoc;
            return this;
        }

        public Builder generateAll(boolean genAll) {
            CliOptions.this.generateAll = genAll;
            return this;
        }

        public Builder generateXdoc(boolean genXdoc) {
            CliOptions.this.generateXdoc = genXdoc;
            return this;
        }

        public Builder generateTw(boolean genTw) {
            CliOptions.this.generateTw = genTw;
            return this;
        }

        public Builder generateGplus(boolean genGplus) {
            CliOptions.this.generateGplus = genGplus;
            return this;
        }

        public Builder generateRss(boolean genRss) {
            CliOptions.this.generateRss = genRss;
            return this;
        }

        public Builder generateSf(boolean genSf) {
            CliOptions.this.generateSf = genSf;
            return this;
        }

        public Builder generateMlist(boolean genMlist) {
            CliOptions.this.generateMlist = genMlist;
            return this;
        }

        /**
         * Returns new CliOption instance.
         * @return new CliOption instance
         */
        public CliOptions build() {
            if (CliOptions.this.endRef == null) {
                CliOptions.this.endRef = "HEAD";
            }
            if (CliOptions.this.outputLocation == null) {
                CliOptions.this.outputLocation = "";
            }
            Verify.verifyNotNull(localRepoPath,
                "Path to a local git repository should not be null!");
            Verify.verifyNotNull(startRef, "Start reference should not be null!");
            Verify.verifyNotNull(releaseNumber, "Release number should not be null!");

            final CliOptions cliOptions = new CliOptions();
            cliOptions.localRepoPath = CliOptions.this.localRepoPath;
            cliOptions.startRef = CliOptions.this.startRef;
            cliOptions.endRef = CliOptions.this.endRef;
            cliOptions.releaseNumber = CliOptions.this.releaseNumber;
            cliOptions.outputLocation = CliOptions.this.outputLocation;
            cliOptions.authToken = CliOptions.this.authToken;
            cliOptions.generateAll = CliOptions.this.generateAll;
            cliOptions.generateXdoc = CliOptions.this.generateXdoc;
            cliOptions.generateTw = CliOptions.this.generateTw;
            cliOptions.generateGplus = CliOptions.this.generateGplus;
            cliOptions.generateRss = CliOptions.this.generateRss;
            cliOptions.generateSf = CliOptions.this.generateSf;
            cliOptions.generateMlist = CliOptions.this.generateMlist;

            return cliOptions;
        }
    }
}
