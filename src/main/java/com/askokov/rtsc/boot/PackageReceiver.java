package com.askokov.rtsc.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.askokov.rtsc.common.Func;
import com.askokov.rtsc.common.StatHandler;
import com.askokov.rtsc.db.DBHelper;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class PackageReceiver extends BroadcastReceiver {
    private static final Logger logger = LoggerFactory.getLogger(PackageReceiver.class);

    private DBHelper dbHelper;
    private StatHandler statHandler;

    public PackageReceiver(final DBHelper dbHelper, final StatHandler statHandler) {
        this.dbHelper = dbHelper;
        this.statHandler = statHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Func.printIntent("PackageReceiver.onReceive", intent);

        if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
        } else if (Intent.ACTION_PACKAGE_FIRST_LAUNCH.equals(intent.getAction())) {
        } else if (Intent.ACTION_PACKAGE_CHANGED.equals(intent.getAction())) {
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(intent.getAction())) {
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
        } else if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
        } else if (Intent.ACTION_REBOOT.equals(intent.getAction())) {

            dbHelper.saveList(statHandler.getApps());
        } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {

            dbHelper.saveList(statHandler.getApps());
        }
    }
}
