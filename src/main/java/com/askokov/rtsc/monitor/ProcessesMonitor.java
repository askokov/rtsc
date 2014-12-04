package com.askokov.rtsc.monitor;

import java.util.List;

import android.app.ActivityManager;
import android.content.Context;
import com.askokov.rtsc.common.PInfo;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class ProcessesMonitor {
    private static final Logger logger = LoggerFactory.getLogger(ProcessesMonitor.class);

    public void scan(Context context, List<PInfo> infos) {
        if (!infos.isEmpty()) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

            scanRunningApps(am, infos);
        } else {
            logger.info("Observed applications are not set");
        }
    }

    private void scanRunningApps(ActivityManager am, List<PInfo> infos) {
        List<ActivityManager.RunningAppProcessInfo> listOfProcesses = am.getRunningAppProcesses();

        //int i = 0;
        for (ActivityManager.RunningAppProcessInfo process : listOfProcesses) {
            //logger.info("--- running application[" + i + "]: processName=" + process.processName);
            if (isObserved(process.processName, infos)) {
                logger.info("------ process <" + process.processName + "> is observed");
            }
            //i++;
        }
    }

    private boolean isObserved(String pname, List<PInfo> infos) {
        boolean result = false;

        for (PInfo info : infos) {
            if (info.isChecked() && info.getPname().equals(pname)) {
                result = true;
                break;
            }
        }

        return result;
    }
}
