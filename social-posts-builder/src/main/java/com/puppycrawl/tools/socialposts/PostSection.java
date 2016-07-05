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

    /**
     * @param title the section title.
     */
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
