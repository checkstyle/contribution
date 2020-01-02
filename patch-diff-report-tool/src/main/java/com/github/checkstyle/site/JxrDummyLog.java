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

package com.github.checkstyle.site;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.jxr.log.Log;

/**
 * Simple log used by maven-jxr PackageManager.
 *
 * @author attatrol
 *
 */
public class JxrDummyLog implements Log {

    /**
     * Container for logs.
     */
    private static List<String> logs = new ArrayList<>();

    @Override
    public void debug(String arg0) {
        logs.add("Debug: " + arg0);
    }

    @Override
    public void error(String arg0) {
        logs.add("Error: " + arg0);
    }

    @Override
    public void info(String arg0) {
        logs.add("Info: " + arg0);
    }

    @Override
    public void warn(String arg0) {
        logs.add("Warning: " + arg0);
    }

    public static List<String> getLogs() {
        return logs;
    }

}
