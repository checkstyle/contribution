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
    /** Output file name. */
    private String outputFile;
    /** Auth token. */
    private String authToken;

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

    public String getOutputFile() {
        return outputFile;
    }

    public String getAuthToken() {
        return authToken;
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

        /**
         * Specifies path to a local git repository for CliOptions object and returns new
         * builder object.
         * @param path path to a local git repository.
         * @return builder object.
         */
        public Builder localRepoPath(String path) {
            CliOptions.this.localRepoPath = path;
            return this;
        }

        /**
         * Specifies start reference for CliOptions object and returns new builder object.
         * @param ref start reference.
         * @return builder object.
         */
        public Builder startRef(String ref) {
            CliOptions.this.startRef = ref;
            return this;
        }

        /**
         * Specifies end reference for CliOptions object and returns new builder object.
         * @param ref end reference.
         * @return builder object.
         */
        public Builder endRef(String ref) {
            CliOptions.this.endRef = ref;
            return this;
        }

        /**
         * Specifies release number for CliOptions object and returns new builder object.
         * @param number release number.
         * @return builder object.
         */
        public Builder releaseNumber(String number) {
            CliOptions.this.releaseNumber = number;
            return this;
        }

        /**
         * Specifies output file name for CliOptions object and returns new builder object.
         * @param fileName output file name.
         * @return builder object.
         */
        public Builder outputFile(String fileName) {
            CliOptions.this.outputFile = fileName;
            return this;
        }

        /**
         * Specifies authentication for CliOptions object and returns new builder object.
         * @param token github private authentication token.
         * @return builder object.
         */
        public Builder authToken(String token) {
            CliOptions.this.authToken = token;
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
            if (CliOptions.this.outputFile == null) {
                CliOptions.this.outputFile = "releasenotes.xml";
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
            cliOptions.outputFile = CliOptions.this.outputFile;
            cliOptions.authToken = CliOptions.this.authToken;

            return cliOptions;
        }
    }
}
