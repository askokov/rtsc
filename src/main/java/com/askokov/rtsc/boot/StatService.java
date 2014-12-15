package com.askokov.rtsc.boot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import com.askokov.rtsc.common.Func;
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.common.StatHandler;
import com.askokov.rtsc.monitor.ProcessesMonitor;
import com.askokov.rtsc.common.Constant;
import com.askokov.rtsc.parcel.ListParcel;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class StatService extends Service implements Constant {
    private static final Logger logger = LoggerFactory.getLogger(StatService.class);
    // restart service every 120 seconds
    private static final long REPEAT_TIME = 1000 * 120;

    private StatHandler statHandler = new StatHandler();
    private final Object sync = new Object();

    private SetupReceiver setupReceiver;
    private AlarmReceiver alarmReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        logger.info("StatService.onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("StatService.onStartCommand(" + (intent != null ? intent.getAction() : "null") + ")");

        setupReceiver = new SetupReceiver();
        IntentFilter setupFilter = new IntentFilter();
        setupFilter.addAction(SetupReceiver.ACTION);
        registerReceiver(setupReceiver, setupFilter);

        alarmReceiver = new AlarmReceiver();
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction(AlarmReceiver.ACTION);
        registerReceiver(alarmReceiver, alarmFilter);

        startAlarm(this);

        logger.info("StatService: register receivers");

        //Load applications list from DB

        if (intent != null) {
            ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

            if (resultReceiver != null) {
                boolean observeInstalled = intent.getBooleanExtra(OBSERVE_INSTALLED, false);
                mergeInfo(statHandler.getApps(), retrieveInstalledApplications(), observeInstalled, true);

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(statHandler.getApps()));
                resultReceiver.send(STATUS_FINISH, bundle);

                logger.info("StatService: result receiver send");
            } else {
                logger.info("StatService: missing result receiver");
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        logger.info("StatService.onDestroy");

        unregisterReceiver(setupReceiver);
        unregisterReceiver(alarmReceiver);

        stopAlarm(this);

        logger.info("StatService: unregister receivers");

        super.onDestroy();
    }

    private void startAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmReceiver.ACTION);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long alarmTime = System.currentTimeMillis() + REPEAT_TIME;
        am.cancel(pending);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, REPEAT_TIME, pending);

        logger.info("StatService: startAlarm");
    }

    private void stopAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmReceiver.ACTION);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pending);

        logger.info("StatService: stopAlarm");
    }

    private void updateAppList(List<String> toUpdate) {
        clearAppList();

        for(String p : toUpdate) {
            PInfo info = findInfoByPackage(statHandler.getApps(), p);

            if (info != null) {
                info.setChecked(true);
                logger.info("SetupReceiver.updateAppList: checked<" + p + ">");
            }
        }
    }

    private void clearAppList() {
        for(PInfo info : statHandler.getApps()) {
            info.setChecked(false);
        }
    }

    private PInfo findInfoByPackage(List<PInfo> inMemory, String pName) {
        PInfo result = null;
        for(PInfo info : inMemory) {
            if (pName.equals(info.getPname())) {
                result = info;
                break;
            }
        }

        return result;
    }

    private List<PInfo> retrieveInstalledApplications() {
        List<PInfo> apps = Func.getInstalledApps(this);
        Collections.sort(apps, new PInfoComparator());

        return apps;
    }

    private void mergeInfo(List<PInfo> inMemory, List<PInfo> inDevice, boolean observeInstalled, boolean onStart) {
        List<PInfo> result = new ArrayList<PInfo>();
        boolean empty = inMemory.isEmpty();

        if (!empty) {
            for (PInfo mem : inMemory) {
                PInfo dev = findInfoByPackage(inDevice, mem.getPname());

                if (dev == null) {
                    logger.info("StatService.mergeInfo: application was deleted - " + mem.getPname());

                    if (!onStart) {
                        //close statistic for it
                    }
                }
            }
        }

        for(PInfo dev : inDevice) {
            PInfo mem = findInfoByPackage(inMemory, dev.getPname());

            if (mem == null) {
                logger.info("StatService.mergeInfo: found new application - " + dev.getPname());

                if (observeInstalled && !empty) {
                    dev.setChecked(true);
                }
            } else {
                dev.setChecked(mem.isChecked());
            }
            result.add(dev);
        }

        inMemory.clear();
        inMemory.addAll(result);
    }

    class AlarmReceiver extends BroadcastReceiver {
        public static final String ACTION = "com.askokov.ALARM_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            Func.printIntent("AlarmReceiver.onReceive", intent);

            synchronized (sync) {
                new ProcessesMonitor().scan(context, statHandler.getApps());
            }
        }
    }

    class SetupReceiver extends BroadcastReceiver {
        public static final String ACTION = "com.askokov.SETUP_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            Func.printIntent("SetupReceiver.onReceive", intent);

            int requestCode = intent.getIntExtra(EXECUTE, 0);
            logger.info("SetupReceiver.onReceive: requestCode<" + requestCode + ">");

            if (requestCode == REQUEST_GET_APP_LIST) {
                logger.info("SetupReceiver.onReceive: execute<get app list>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);
                boolean observeInstalled = intent.getBooleanExtra(OBSERVE_INSTALLED, false);

                mergeInfo(statHandler.getApps(), retrieveInstalledApplications(), observeInstalled, false);

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(statHandler.getApps()));
                resultReceiver.send(STATUS_FINISH, bundle);

            } else if (requestCode == REQUEST_UPDATE_APP_LIST) {
                logger.info("SetupReceiver.onReceive: execute<update app list>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);
                ListParcel parcel = (ListParcel) intent.getSerializableExtra(PARCEL);

                updateAppList(parcel.getList());

                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Update success");
                resultReceiver.send(STATUS_FINISH, bundle);
            } else if (requestCode == REQUEST_CLEAR_APP_LIST) {
                logger.info("SetupReceiver.onReceive: execute<clear app list>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                clearAppList();

                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Update success");
                resultReceiver.send(STATUS_FINISH, bundle);
            }
        }
    }

    class PInfoComparator implements Comparator<PInfo> {
        @Override
        public int compare(final PInfo lhs, final PInfo rhs) {
            return lhs.getPname().compareTo(rhs.getPname());
        }
    }
}
