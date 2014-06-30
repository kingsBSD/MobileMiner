// Licensed under the Apache License Version 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
package com.odo.kcl.mobileminer;

public class CountedCell {
    private int count;
    private String mcc,mnc,lac,cellid;

    public CountedCell(int c, String cc, String nc, String lc, String id) {
        count = c; mcc = cc; mnc = nc; lac = lc; cellid = id;
    }

    public Integer getCount() {return count;}
    public String getMcc() {return mcc;}
    public String getMnc() {return mnc;}
    public String getLac() {return lac;}
    public String getCellId() {return cellid;}

}
