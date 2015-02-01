package com.askokov.rtsc.common;

import java.io.Serializable;

public class Configuration implements Serializable {
    private boolean addInstalled;
    private ReportType reportType;
    private MailType mailType;
    private String mailUser;
    private String mailPassword;

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

    public String getMailPassword() {
        return mailPassword;
    }

    public void setMailPassword(final String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public String getMailUser() {
        return mailUser;
    }

    public void setMailUser(final String mailUser) {
        this.mailUser = mailUser;
    }

    @Override
    public String toString() {
        return "Configuration[addInstalled<" + addInstalled + ">; reportType<" + reportType.name() + ">, mailType<" + mailType.name() + ">, mailUser<" + mailUser +  ">, mailPassword<" + mailPassword + ">]";
    }
}
