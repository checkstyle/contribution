////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
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

import java.util.Arrays;

/**
 * Container for constants.
 * @author Andrei Selkin
 */
public final class Constants {

    /** Bug label name. */
    public static final String BUG_LABEL = "bug";
    /** New feature label name. */
    public static final String NEW_LABEL = "new feature";
    /** Miscellaneous label name. */
    public static final String MISCELLANEOUS_LABEL = "miscellaneous";
    /** Breaking backward compatibility label name. */
    public static final String BREAKING_COMPATIBILITY_LABEL = "breaking compatibility";

    /**
     * The array which represents the issue labels for release notes.
     */
    public static final String[] ISSUE_LABELS;

    static {
        ISSUE_LABELS = new String[] {
            BREAKING_COMPATIBILITY_LABEL, NEW_LABEL, BUG_LABEL, MISCELLANEOUS_LABEL,
        };
        Arrays.sort(ISSUE_LABELS);
    }

    /** Default constructor. */
    private Constants() { }
}
