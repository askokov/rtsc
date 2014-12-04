package com.askokov.rtsc.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import com.askokov.rtsc.log.LogConfigurator;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class OnBootReceiver extends BroadcastReceiver {
    private static final Logger logger = LoggerFactory.getLogger(OnBootReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        LogConfigurator.configure(context);
        logger.info("onReceive --> configure log");

        logger.info("OnBootReceiver.onReceive(" + (intent != null ? intent.getAction() : "null") + ")");

        // Start Service On Boot Start Up
        Intent service = new Intent(context, StatService.class);
        context.startService(service);
        logger.info("OnBootReceiver.onReceive --> start log service");
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
