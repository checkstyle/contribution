package com.github.checkstyle.parser;

/**
 * POJO holding all statistics gathered during parsing stage.
 * @author atta_troll
 */
public class StatisticsHolder {

    private int errorNumDiff = 0;
    private int warningNumDiff = 0;
    private int infoNumDiff = 0;
    private int totalNumDiff = 0;
    private int fileNumDiff = 0;

    private int errorNum1 = 0;
    private int errorNum2 = 0;
    private int warningNum1 = 0;
    private int warningNum2 = 0;
    private int infoNum1 = 0;
    private int infoNum2 = 0;
    private int totalNum1 = 0;
    private int totalNum2 = 0;

    private int fileNum1 = 0;
    private int fileNum2 = 0;

    public StatisticsHolder() {

    }

    public final int getErrorNumDiff() {
        return errorNumDiff;
    }

    public final void incrementErrorNumDiff() {
        this.errorNumDiff++;
    }

    public final int getWarningNumDiff() {
        return warningNumDiff;
    }

    public final void incrementWarningNumDiff() {
        this.warningNumDiff++;
    }

    public final int getInfoNumDiff() {
        return infoNumDiff;
    }

    public final void incrementInfoNumDiff() {
        this.infoNumDiff++;
    }

    public final int getTotalNumDiff() {
        return totalNumDiff;
    }

    public final void incrementTotalNumDiff() {
        this.totalNumDiff++;
    }

    public final int getFileNumDiff() {
        return fileNumDiff;
    }

    public final void setFileNumDiff(final int fileNumDiff1) {
        this.fileNumDiff = fileNumDiff1;
    }

    public final int getErrorNum1() {
        return errorNum1;
    }

    public final void incrementErrorNum1() {
        this.errorNum1++;
    }

    public final int getErrorNum2() {
        return errorNum2;
    }

    public final void incrementErrorNum2() {
        this.errorNum2++;
    }

    public final int getWarningNum1() {
        return warningNum1;
    }

    public final void incrementWarningNum1() {
        this.warningNum1++;
    }

    public final int getWarningNum2() {
        return warningNum2;
    }

    public final void incrementWarningNum2() {
        this.warningNum2++;
    }

    public final int getInfoNum1() {
        return infoNum1;
    }

    public final void incrementInfoNum1() {
        this.infoNum1++;
    }

    public final int getInfoNum2() {
        return infoNum2;
    }

    public final void incrementInfoNum2() {
        this.infoNum2++;
    }

    public final int getTotalNum1() {
        return totalNum1;
    }

    public final void incrementTotalNum1() {
        this.totalNum1++;
    }

    public final int getTotalNum2() {
        return totalNum2;
    }

    public final void incrementTotalNum2() {
        this.totalNum2++;
    }

    public final int getFileNum1() {
        return fileNum1;
    }

    public final void incrementFileNum1() {
        this.fileNum1++;
    }

    public final int getFileNum2() {
        return fileNum2;
    }

    public final void incrementFileNum2() {
        this.fileNum2++;
    }

}
