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

package com.github.checkstyle.data;

import static com.github.checkstyle.Main.BASE_REPORT_INDEX;
import static com.github.checkstyle.Main.PATCH_REPORT_INDEX;

/**
 * POJO holding all statistics gathered during parsing stage.
 * @author atta_troll
 */
public class Statistics {

    /**
     * Number of error rows in difference.
     */
    private int errorNumDiff;

    /**
     * Number of warning rows in difference.
     */
    private int warningNumDiff;

    /**
     * Number of info rows in difference.
     */
    private int infoNumDiff;

    /**
     * Number of files in difference.
     */
    private int fileNumDiff;

    /**
     * Number of error rows in the base source.
     */
    private int errorNumBase;

    /**
     * Number of warning rows in the base source.
     */
    private int warningNumBase;

    /**
     * Number of info rows in the base source.
     */
    private int infoNumBase;

    /**
     * Number of files in the base source.
     */
    private int fileNumBase;

    /**
     * Number of error rows in the patch source.
     */
    private int errorNumPatch;

    /**
     * Number of warning rows in the patch source.
     */
    private int warningNumPatch;

    /**
     * Number of info rows in the patch source.
     */
    private int infoNumPatch;

    /**
     * Number of files in the patch source.
     */
    private int fileNumPatch;

    /**
     * Default ctor.
     */
    public Statistics() {

    }

    public final int getErrorNumDiff() {
        return errorNumDiff;
    }

    public final int getWarningNumDiff() {
        return warningNumDiff;
    }

    public final int getInfoNumDiff() {
        return infoNumDiff;
    }

    public final int getTotalNumDiff() {
        return errorNumDiff + warningNumDiff + infoNumDiff;
    }

    public final int getFileNumDiff() {
        return fileNumDiff;
    }

    public final int getErrorNumBase() {
        return errorNumBase;
    }

    public final int getWarningNumBase() {
        return warningNumBase;
    }

    public final int getInfoNumBase() {
        return infoNumBase;
    }

    public final int getTotalNumBase() {
        return errorNumBase + warningNumBase + infoNumBase;
    }

    public final int getFileNumBase() {
        return fileNumBase;
    }

    public final int getErrorNumPatch() {
        return errorNumPatch;
    }

    public final int getWarningNumPatch() {
        return warningNumPatch;
    }

    public final int getInfoNumPatch() {
        return infoNumPatch;
    }

    public final int getTotalNumPatch() {
        return errorNumPatch + warningNumPatch + infoNumPatch;
    }

    public final int getFileNumPatch() {
        return fileNumPatch;
    }

    /**
     * Setter for number of files in difference.
     * @param fileNumDiff1 number of files in difference.
     */
    public final void setFileNumDiff(final int fileNumDiff1) {
        this.fileNumDiff = fileNumDiff1;
    }

    /**
     * Registers single error row from indexed source.
     * @param index index of the source.
     */
    public final void registerSingleError(int index) {
        if (index == BASE_REPORT_INDEX) {
            this.errorNumBase++;
        }
        else if (index == PATCH_REPORT_INDEX) {
            this.errorNumPatch++;
        }
        else {
            this.errorNumDiff++;
        }
    }

    /**
     * Registers single warning row from an indexed source.
     * @param index index of the source.
     */
    public final void registerSingleWarning(int index) {
        if (index == BASE_REPORT_INDEX) {
            this.warningNumBase++;
        }
        else if (index == PATCH_REPORT_INDEX) {
            this.warningNumPatch++;
        }
        else {
            this.warningNumDiff++;
        }
    }

    /**
     * Registers single info row from source 1.
     * @param index index of the source.
     */
    public final void registerSingleInfo(int index) {
        if (index == BASE_REPORT_INDEX) {
            this.infoNumBase++;
        }
        else if (index == PATCH_REPORT_INDEX) {
            this.infoNumPatch++;
        }
        else {
            this.infoNumDiff++;
        }
    }

    /**
     * Registers single file from source 1.
     * @param index index of the source.
     */
    public final void registerSingleFile(int index) {
        this.fileNumBase++;
        if (index == BASE_REPORT_INDEX) {
            this.fileNumBase++;
        }
        else if (index == PATCH_REPORT_INDEX) {
            this.fileNumPatch++;
        }
    }

}
