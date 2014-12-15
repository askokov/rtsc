package com.askokov.rtsc.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.askokov.rtsc.common.Func;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class PackageReceiver extends BroadcastReceiver {
    private static final Logger logger = LoggerFactory.getLogger(PackageReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        Func.printIntent("PackageReceiver.onReceive", intent);

        /*
        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
            printData(intent);

        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            printData(intent);

        } else if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
            printData(intent);
        }
        */
    }
}
