package com.askokov.rtsc.common;

public interface Constant {

    int REQUEST_GET_APP_LIST = 100;
    int REQUEST_UPDATE_APP_LIST = 101;
    int REQUEST_GET_STAT_FROM_MEMORY = 103;
    int REQUEST_GET_STAT_FROM_DATABASE = 104;
    int REQUEST_SAVE_STAT_TO_DATABASE = 105;
    int REQUEST_SAVE_CONFIGURATION = 106;
    int REQUEST_GET_CONFIGURATION = 107;

    String EXECUTE = "execute";
    String RESULT = "result";
    String PARCEL = "parcel";
    String RECEIVER = "receiver";
    String OBSERVE_INSTALLED = "observeInstalled";

    String ADD_INSTALLED = "ADD_INSTALLED";
    String REPORT_TYPE = "REPORT_TYPE";
    String MAIL_TYPE = "MAIL_TYPE";
    String CONFIGURATION = "CONFIGURATION";
}
