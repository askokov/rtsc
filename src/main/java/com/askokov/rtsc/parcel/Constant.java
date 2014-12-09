package com.askokov.rtsc.parcel;

import android.app.PendingIntent;

public interface Constant {

    int STATUS_START = 1;
    int STATUS_FINISH = 2;
    int STATUS_FAIL = 3;

    int REQUEST_GET_APP_LIST = 100;
    int REQUEST_UPDATE_APP_LIST = 101;
    int REQUEST_CLEAR_APP_LIST = 102;

    String EXECUTE = "execute";
    String RESULT = "result";
    String PENDING_INTENT = PendingIntent.class.getCanonicalName();
    String PARCEL = "parcel";
    String RECEIVER = "receiver";
    String OBSERVE_INSTALLED = "observeInstalled";
}
