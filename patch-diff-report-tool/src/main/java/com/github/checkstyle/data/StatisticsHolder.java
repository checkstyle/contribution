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

/**
 * POJO holding all statistics gathered during parsing stage.
 * @author atta_troll
 */
public class StatisticsHolder {

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
     * Number of error rows in source 1.
     */
    private int errorNum1;

    /**
     * Number of warning rows in source 1.
     */
    private int warningNum1;

    /**
     * Number of info rows in source 1.
     */
    private int infoNum1;

    /**
     * Number of files in source 1.
     */
    private int fileNum1;

    /**
     * Number of error rows in source 2.
     */
    private int errorNum2;

    /**
     * Number of warning rows in source 2.
     */
    private int warningNum2;

    /**
     * Number of info rows in source 2.
     */
    private int infoNum2;

    /**
     * Number of files in source 2.
     */
    private int fileNum2;

    /**
     * Default ctor.
     */
    public StatisticsHolder() {

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

    public final int getErrorNum1() {
        return errorNum1;
    }

    public final int getWarningNum1() {
        return warningNum1;
    }

    public final int getInfoNum1() {
        return infoNum1;
    }

    public final int getTotalNum1() {
        return errorNum1 + warningNum1 + infoNum1;
    }

    public final int getFileNum1() {
        return fileNum1;
    }

    public final int getErrorNum2() {
        return errorNum2;
    }

    public final int getWarningNum2() {
        return warningNum2;
    }

    public final int getInfoNum2() {
        return infoNum2;
    }

    public final int getTotalNum2() {
        return errorNum2 + warningNum2 + infoNum2;
    }

    public final int getFileNum2() {
        return fileNum2;
    }

    /**
     * Registers single error row from difference.
     */
    public final void registerSingleErrorDiff() {
        this.errorNumDiff++;
    }

    /**
     * Registers single warning row from difference.
     */
    public final void registerSingleWarningDiff() {
        this.warningNumDiff++;
    }

    /**
     * Registers single info row from difference.
     */
    public final void registerSingleInfoDiff() {
        this.infoNumDiff++;
    }

    /**
     * Setter for number of files in difference.
     * @param fileNumDiff1 number of files in difference.
     */
    public final void setFileNumDiff(final int fileNumDiff1) {
        this.fileNumDiff = fileNumDiff1;
    }

    /**
     * Registers single error row from source 1.
     */
    public final void registerSingleError1() {
        this.errorNum1++;
    }

    /**
     * Registers single warning row from source 1.
     */
    public final void registerSingleWarning1() {
        this.warningNum1++;
    }

    /**
     * Registers single info row from source 1.
     */
    public final void registerSingleInfo1() {
        this.infoNum1++;
    }

    /**
     * Registers single file from source 1.
     */
    public final void registerSingleFile1() {
        this.fileNum1++;
    }

    /**
     * Registers single error row from source 2.
     */
    public final void registerSingleError2() {
        this.errorNum2++;
    }

    /**
     * Registers single warning row from source 2.
     */
    public final void registerSingleWarning2() {
        this.warningNum2++;
    }

    /**
     * Registers single info row from source 2.
     */
    public final void registerSingleInfo2() {
        this.infoNum2++;
    }

    /**
     * Registers single file from source 2.
     */
    public final void registerSingleFile2() {
        this.fileNum2++;
    }

}
