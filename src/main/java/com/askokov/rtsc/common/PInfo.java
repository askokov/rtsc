package com.askokov.rtsc.common;

import java.io.Serializable;
import java.util.Date;

public class PInfo implements Serializable {
    private Integer id;
    private String label = "";
    private String packageName = "";
    private String versionName = "";
    private boolean checked;
    private Date date;
    private long fullTime;
    private long startTime;

    public String prettyPrint() {
        return label + "-->" + packageName + "-->" + versionName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
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

    public long getFullTime() {
        return fullTime;
    }

    public void setFullTime(final long fullTime) {
        this.fullTime = fullTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(final long startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PInfo info = (PInfo) o;

        return packageName.equals(info.packageName);
    }

    @Override
    public int hashCode() {
        return packageName.hashCode();
    }
}
