////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2022 the original author or authors.
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

package com.github.checkstyle.globals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Represents the result of release notes generation.
 *
 * @author Andrei Selkin
 */
public final class Result {

    /** Error messages. */
    private final List<String> errorMessages;
    /** Warning messages. */
    private final List<String> warningMessages;

    /**
     *  Release notes which are represented as a map.
     *  Key - section name. Value - release notes message.
     */
    private final Multimap<String, ReleaseNotesMessage> releaseNotes;

    /** Default constructor. */
    public Result() {
        errorMessages = new ArrayList<>();
        warningMessages = new ArrayList<>();
        releaseNotes = ArrayListMultimap.create();
    }

    /**
     * Puts release notes message into release notes map.
     *
     * @param label section label.
     * @param message release notes message.
     */
    public void putReleaseNotesMessage(String label, ReleaseNotesMessage message) {
        if (!releaseNotes.containsValue(message)) {
            releaseNotes.put(label, message);
        }
    }

    /**
     * Returns the list of error messages.
     *
     * @return the list of error messages
     */
    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    /**
     * Returns the list of warning messages.
     *
     * @return the list of warning messages
     */
    public List<String> getWarningMessages() {
        return Collections.unmodifiableList(warningMessages);
    }

    /**
     * Returns the release notes as a map.
     *
     * @return the rRelease notes as a map
     */
    public Multimap<String, ReleaseNotesMessage> getReleaseNotes() {
        return ArrayListMultimap.create(releaseNotes);
    }

    /**
     * Adds error message into result.
     *
     * @param message error message.
     */
    public void addError(String message) {
        errorMessages.add(message);
    }

    /**
     * Adds warning message into result.
     *
     * @param message warning message.
     */
    public void addWarning(String message) {
        warningMessages.add(message);
    }

    /**
     * Checks whether result has errors.
     *
     * @return true if result has errors.
     */
    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    /**
     * Checks whether result has warnings.
     *
     * @return true if result has warnings.
     */
    public boolean hasWarnings() {
        return !warningMessages.isEmpty();
    }
}
