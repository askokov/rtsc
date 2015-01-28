package com.askokov.rtsc.common;

import java.io.Serializable;

public class Configuration implements Serializable {
    private boolean addInstalled;
    private ReportType reportType;
    private MailType mailType;

    public boolean isAddInstalled() {
        return addInstalled;
    }

    public void setAddInstalled(final boolean addInstalled) {
        this.addInstalled = addInstalled;
    }

    public MailType getMailType() {
        return mailType;
    }

    public void setMailType(final MailType mailType) {
        this.mailType = mailType;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(final ReportType reportType) {
        this.reportType = reportType;
    }
}
