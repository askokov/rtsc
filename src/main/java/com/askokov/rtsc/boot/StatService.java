package com.askokov.rtsc.boot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.ResultReceiver;
import com.askokov.rtsc.common.Constant;
import com.askokov.rtsc.common.Func;
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.common.StatHandler;
import com.askokov.rtsc.db.DBHelper;
import com.askokov.rtsc.monitor.ProcessesMonitor;
import com.askokov.rtsc.parcel.ListParcel;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class StatService extends Service implements Constant {
    private static final Logger logger = LoggerFactory.getLogger(StatService.class);
    // restart service every 120 seconds
    private static final long REPEAT_TIME = 1000 * 60;
    private static final String DB_NAME = "/store.db"; // имя БД

    private StatHandler statHandler;
    private final Object sync = new Object();

    private StatReceiver statReceiver;
    private AlarmReceiver alarmReceiver;
    private PackageReceiver packageReceiver;
    private RebootReceiver rebootReceiver;
    private DBHelper dbHelper;

    @Override
    public void onCreate() {
        super.onCreate();

        logger.info("onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.info("onStartCommand(" + (intent != null ? intent.getAction() : "null") + ")");

        File path = new File(Environment.getExternalStorageDirectory(), getPackageName());
        if (!path.exists()) {
            path.mkdir();
        }

        String dbPath = path.getPath() + DB_NAME;
        logger.info("DB File: " + dbPath);

        //Load applications list from DB
        dbHelper = new DBHelper(this, dbPath);
        //dbHelper.createDataBase();

        List<PInfo> list = dbHelper.loadList(Func.truncateDate(new Date()));
        statHandler = new StatHandler(list);

        statReceiver = new StatReceiver();
        IntentFilter setupFilter = new IntentFilter();
        setupFilter.addAction(StatReceiver.ACTION);
        registerReceiver(statReceiver, setupFilter);

        alarmReceiver = new AlarmReceiver();
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction(AlarmReceiver.ACTION);
        registerReceiver(alarmReceiver, alarmFilter);

        packageReceiver = new PackageReceiver(dbHelper, statHandler);
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.setPriority(999);

        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_FIRST_LAUNCH);
        packageFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        registerReceiver(packageReceiver, packageFilter);

        rebootReceiver = new RebootReceiver(dbHelper, statHandler);
        IntentFilter rebootFilter = new IntentFilter();

        rebootFilter.addAction(Intent.ACTION_REBOOT);
        rebootFilter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(rebootReceiver, rebootFilter);

        startAlarm(this);

        logger.info("Register receivers");
        /*
        if (intent != null) {
            ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

            if (resultReceiver != null) {
                boolean observeInstalled = intent.getBooleanExtra(OBSERVE_INSTALLED, false);
                mergeInfo(statHandler.getApps(), retrieveInstalledApplications(), observeInstalled, true);

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(statHandler.getApps()));
                resultReceiver.send(STATUS_FINISH, bundle);

                logger.info("Result receiver send");
            } else {
                logger.info("Missing result receiver");
            }
        }
        */

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        logger.info("onDestroy");

        unregisterReceiver(statReceiver);
        statReceiver = null;
        unregisterReceiver(alarmReceiver);
        alarmReceiver = null;
        unregisterReceiver(packageReceiver);
        packageReceiver = null;
        unregisterReceiver(rebootReceiver);
        rebootReceiver = null;
        logger.info("Unregister receivers");

        stopAlarm(this);

        dbHelper.saveList(statHandler.getApps());
        dbHelper = null;
        logger.info("Save list");

        super.onDestroy();
    }

    private void startAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmReceiver.ACTION);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long alarmTime = System.currentTimeMillis() + REPEAT_TIME;
        am.cancel(pending);

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime, REPEAT_TIME, pending);

        logger.info("Start alarm");
    }

    private void stopAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmReceiver.ACTION);
        PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(pending);

        logger.info("Stop alarm");
    }

    private void updateAppList(List<String> toUpdate) {
        clearAppList();

        for (String p : toUpdate) {
            PInfo info = findInfoByPackage(statHandler.getApps(), p);

            if (info != null) {
                //Func.saveTime(info);

                info.setChecked(true);
                //info.setStartTime(System.currentTimeMillis());

                logger.info("--- updateAppList: checked<" + p + ">");
            }
        }
    }

    private void clearAppList() {
        for (PInfo info : statHandler.getApps()) {
            Func.saveTime(info);
            info.setChecked(false);
        }
    }

    private PInfo findInfoByPackage(List<PInfo> inMemory, String pName) {
        PInfo result = null;
        for (PInfo info : inMemory) {
            if (pName.equals(info.getPackageName())) {
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
        logger.info("Before merge");
        logger.info("--- in memory size<" + inMemory.size() + ">");
        logger.info("--- in device size<" + inDevice.size() + ">");

        List<PInfo> result = new ArrayList<PInfo>();
        boolean empty = inMemory.isEmpty();

        if (!empty) {
            for (PInfo mem : inMemory) {
                PInfo dev = findInfoByPackage(inDevice, mem.getPackageName());

                if (dev == null) {
                    if (!onStart) {
                        Func.saveTime(mem, true);
                        mem.setStopMonitoring(true);
                        logger.info("mergeInfo: application was deleted - " + mem.getPackageName());
                    }
                }
            }
        }

        Date date = Func.truncateDate(new Date());
        for (PInfo dev : inDevice) {
            PInfo mem = findInfoByPackage(inMemory, dev.getPackageName());

            if (mem == null) {
                //Устанавливаемые приложения надо отслеживать через событие!!!!!
                if (observeInstalled && !empty) {
                    dev.setChecked(true);
                    logger.info("mergeInfo: found new application - " + dev.getPackageName());
                }
            } else {
                dev.setChecked(mem.isChecked());
            }

            //надо подумать, как не устанавливать дату
            dev.setDate(date);
            dev.setFullTime(0);
            dev.setStartTime(System.currentTimeMillis());
            result.add(dev);
        }

        inMemory.clear();
        inMemory.addAll(result);

        logger.info("After merge");
        logger.info("--- in memory size<" + inMemory.size() + ">");
        logger.info("--- in device size<" + inDevice.size() + ">");
    }

    class AlarmReceiver extends BroadcastReceiver {
        public static final String ACTION = "com.askokov.ALARM_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            Func.printIntent("AlarmReceiver.onReceive", intent);

            synchronized (sync) {
                new ProcessesMonitor().scan(context, statHandler, dbHelper);
            }
        }
    }

    class StatReceiver extends BroadcastReceiver {
        public static final String ACTION = "com.askokov.SETUP_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            Func.printIntent("SetupReceiver.onReceive", intent);

            int requestCode = intent.getIntExtra(EXECUTE, 0);
            logger.info("onReceive: requestCode<" + requestCode + ">");

            if (requestCode == REQUEST_GET_APP_LIST) {
                logger.info("onReceive: execute<get application list>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);
                boolean observeInstalled = intent.getBooleanExtra(OBSERVE_INSTALLED, false);

                mergeInfo(statHandler.getApps(), retrieveInstalledApplications(), observeInstalled, false);

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(statHandler.getApps()));
                resultReceiver.send(requestCode, bundle);

            } else if (requestCode == REQUEST_UPDATE_APP_LIST) {
                logger.info("onReceive: execute<update application list>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);
                ListParcel parcel = (ListParcel) intent.getSerializableExtra(PARCEL);

                updateAppList(parcel.getList());

                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Update success");
                resultReceiver.send(requestCode, bundle);
            } else if (requestCode == REQUEST_GET_STAT_FROM_MEMORY) {
                logger.info("onReceive: execute<get statistic from memory>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(getActiveApps(statHandler.getApps())));
                resultReceiver.send(requestCode, bundle);
            } else if (requestCode == REQUEST_GET_STAT_FROM_DATABASE) {
                logger.info("onReceive: execute<get statistic from database>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                List<PInfo> fromDB = dbHelper.loadList(Func.truncateDate(new Date()));
                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(fromDB));
                resultReceiver.send(requestCode, bundle);
            } else if (requestCode == REQUEST_SAVE_STAT_TO_DATABASE) {
                logger.info("onReceive: execute<save statistic to database>");

                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                dbHelper.saveList(statHandler.getApps());
                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Save statistic success");
                resultReceiver.send(requestCode, bundle);
            }
        }

        private List<PInfo> getActiveApps(List<PInfo> list) {
            List<PInfo> result = new ArrayList<PInfo>();

            for(PInfo info : list) {
                if (info.getFullTime() > 0) {
                    result.add(info);
                }
            }

            return result;
        }
    }

    class PInfoComparator implements Comparator<PInfo> {
        @Override
        public int compare(final PInfo lhs, final PInfo rhs) {
            return lhs.getPackageName().compareTo(rhs.getPackageName());
        }
    }
}
