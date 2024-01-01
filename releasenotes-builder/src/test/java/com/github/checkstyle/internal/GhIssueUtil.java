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

package com.github.checkstyle.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHLabel;

public final class GhIssueUtil {
    private GhIssueUtil() {
    }

    public static GHIssue create(int ghNumber, GHIssueState ghState, String ghTitle,
            String... labels) {
        final List<GHLabel> ghLabels = new ArrayList<>();

        for (String label : labels) {
            final GHLabel mockGhLabel = mock(GHLabel.class);
            when(mockGhLabel.getName()).thenReturn(label);

            ghLabels.add(mockGhLabel);
        }

        return new GHIssue() {
            @Override
            public int getNumber() {
                return ghNumber;
            }

            @Override
            public GHIssueState getState() {
                return ghState;
            }

            @Override
            public String getTitle() {
                return ghTitle;
            }

            @Override
            public Collection<GHLabel> getLabels() {
                return ghLabels;
            }
        };
    }
}
