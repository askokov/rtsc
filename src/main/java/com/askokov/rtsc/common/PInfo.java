package com.askokov.rtsc.common;

import java.io.Serializable;
import java.util.Date;

public class PInfo implements Serializable {
    private String appname = "";
    private String pname = "";
    private String versionName = "";
    private boolean checked;
    private Date date;
    private Float time;

    public String prettyPrint() {
        return appname + "-->" + pname + "-->" + versionName;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(final String appname) {
        this.appname = appname;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(final String pname) {
        this.pname = pname;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(final boolean checked) {
        this.checked = checked;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public Float getTime() {
        return time;
    }

    public void setTime(final Float time) {
        this.time = time;
    }
}
