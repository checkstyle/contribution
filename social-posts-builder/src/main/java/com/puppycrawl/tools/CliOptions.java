package com.puppycrawl.tools;

/**
 * Helper structure.
 * @author Vladislav Lisetskii
 */
public class CliOptions {

    /** Properties file location. */
    private String releaseNotes;

    /** Output file location. */
    private String outputLocation;

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }
}
