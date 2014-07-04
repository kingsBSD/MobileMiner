// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer.cell;

public class CountedCell {
    private int count;
    private String mcc, mnc, lac, cellid;

    public CountedCell(int count, String mcc, String mnc, String lac, String cellid) {
        this.count = count;
        this.mcc = mcc;
        this.mnc = mnc;
        this.lac = lac;
        this.cellid = cellid;
    }

    public Integer getCount() {
        return this.count;
    }

    public String getMcc() {
        return this.mcc;
    }

    public String getMnc() {
        return this.mnc;
    }

    public String getLac() {
        return this.lac;
    }

    public String getCellId() {
        return this.cellid;
    }

}
