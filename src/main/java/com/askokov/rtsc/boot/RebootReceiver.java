package com.askokov.rtsc.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.askokov.rtsc.common.Func;
import com.askokov.rtsc.common.StatHandler;
import com.askokov.rtsc.db.DBHelper;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class RebootReceiver extends BroadcastReceiver {
    private static final Logger logger = LoggerFactory.getLogger(RebootReceiver.class);

    private DBHelper dbHelper;
    private StatHandler statHandler;

    public RebootReceiver(final DBHelper dbHelper, final StatHandler statHandler) {
        this.dbHelper = dbHelper;
        this.statHandler = statHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info("RebootReceiver.onReceive");

        Func.printIntent("RebootReceiver.onReceive", intent);

        if (Intent.ACTION_REBOOT.equals(intent.getAction())) {

            dbHelper.saveApps(statHandler.getApps());
        } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {

            dbHelper.saveApps(statHandler.getApps());
        }
    }
}
