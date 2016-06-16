package com.puppycrawl.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a section of a social post.
 * @author Vladislav Lisetskii
 */
public class PostSection {

    /** The title of the section. */
    private final String title;

    /** The records of the section. */
    private final List<String> records;

    public PostSection(String title) {
        this.title = title;
        records = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public List<String> getRecords() {
        return Collections.unmodifiableList(records);
    }

    /**
     * Add a record to the section.
     * @param record the record to add.
     */
    public void addRecord(String record) {
        records.add(record);
    }
}
