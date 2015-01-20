package com.askokov.rtsc.monitor;

import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import com.askokov.rtsc.common.Func;
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.common.StatHandler;
import com.askokov.rtsc.db.DBHelper;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class ProcessesMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ProcessesMonitor.class);

    public void scan(Context context, StatHandler handler, DBHelper dbHelper) {
        if (!handler.getApps().isEmpty()) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            boolean dateChanged = handler.setCurrent(new Date());
            if (dateChanged) {
                flushApps(handler, dbHelper);
            }

            scanRunningApps(am, handler);
        } else {
            logger.info("ProcessesMonitor: Observed applications are not set");
        }
    }

    private void scanRunningApps(ActivityManager am, StatHandler handler) {
        List<ActivityManager.RunningAppProcessInfo> listOfProcesses = am.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo process : listOfProcesses) {
            PInfo observed = getObserved(process.processName, handler.getApps());

            if (observed != null) {
                //обновить время работы приложения
                logger.info("ProcessesMonitor: correct time for <" + observed.getPackageName() + ">");
                Func.saveTime(observed);
            }
        }
    }

    private PInfo getObserved(String pname, List<PInfo> list) {
        PInfo result = null;

        for (PInfo info : list) {
            if (info.isChecked() && info.getPackageName().equals(pname)) {
                result = info;
                break;
            }
        }

        return result;
    }

    private void flushApps(StatHandler handler, DBHelper dbHelper) {
        logger.info("ProcessesMonitor: flush applications");

        dbHelper.saveList(handler.getApps());

        long startTime = System.currentTimeMillis();
        for (PInfo info : handler.getApps()) {
            logger.info("------ " + info.getDate() + ": " + info.getPackageName() + " - " + info.getFullTime());

            info.setFullTime(0);
            info.setStartTime(startTime);
        }
    }
}
