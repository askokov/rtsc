package com.askokov.rtsc.common;

public interface Constant {

    int GET_APP_LIST_FROM_SYSTEM = 101;
    int GET_APP_LIST_FROM_SERVICE = 102;
    int GET_APP_LIST_FROM_DATABASE = 103;
    int SAVE_APP_LIST_TO_SERVICE = 104;
    int SAVE_APP_LIST_TO_DATABASE = 105;

    int GET_CONFIGURATION = 201;
    int SAVE_CONFIGURATION = 202;

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
