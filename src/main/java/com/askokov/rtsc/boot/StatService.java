package com.askokov.rtsc.boot;

import java.io.File;
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

    private void clearAppList() {
        for (PInfo info : statHandler.getApps()) {
            Func.saveTime(info);
            info.setChecked(false);
        }
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

        /*
        Список приложений:
        1. Получить полный список приложений из системы (с мержем списка в сервисе, если он установлен)
        2. Получить список отслеживаемых приложений из данного сервиса
        3. Получить сохраненный список отслеживаемых приложений из базы
        4. Сохранить список отслеживаемых приложений в базе
        5. Сохранить список отслеживаемых приложений в сервисе

        Настройки:
        1. Получить настройки из сервиса
           - если настроек в сервисе нет, попробовать подгрузить их из базы и перед возвращением сохранить в сервисе
           - если настроек в базе нет, вернуть пустые настройки, но в сервисе не сохранять
        2. Сохранить настройки в сервисе, с одновременным сохранением в базе

        int GET_APP_LIST_FROM_SYSTEM = 101;
        int GET_APP_LIST_FROM_SERVICE = 102;
        int GET_APP_LIST_FROM_DATABASE = 103;
        int SAVE_APP_LIST_TO_SERVICE = 104;
        int SAVE_APP_LIST_TO_DATABASE = 105;

        int GET_CONFIGURATION = 201;
        int SAVE_CONFIGURATION = 202;

         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Func.printIntent("SetupReceiver.onReceive", intent);

            int requestCode = intent.getIntExtra(EXECUTE, 0);
            logger.info("onReceive: requestCode<" + requestCode + ">");

            if (requestCode == GET_APP_LIST_FROM_SYSTEM) {
                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                List<PInfo> installed = Func.getInstalledApps(context);
                List<PInfo> merged = Func.merge(statHandler, installed, false);

                if (statHandler.isFlush()) {
                    Func.saveTime(statHandler.getApps());
                    dbHelper.saveList(statHandler.getApps());

                    List<PInfo> saved = dbHelper.loadList(new Date());
                    Func.mergeIdentifiers(merged, saved);
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(merged));
                resultReceiver.send(requestCode, bundle);

            } else if (requestCode == GET_APP_LIST_FROM_SERVICE) {
                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(statHandler.getApps()));
                resultReceiver.send(requestCode, bundle);

            } else if (requestCode == GET_APP_LIST_FROM_DATABASE) {
                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                List<PInfo> saved = dbHelper.loadList(new Date());
                statHandler = new StatHandler(saved);

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(statHandler.getApps()));
                resultReceiver.send(requestCode, bundle);

            } else if (requestCode == SAVE_APP_LIST_TO_SERVICE) {
                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);
                PInfoParcel parcel = (PInfoParcel) intent.getSerializableExtra(PARCEL);

                List<PInfo> selected = parcel.getList();
                List<PInfo> merged = Func.merge(statHandler, selected, true);

                if (statHandler.isFlush()) {
                    Func.saveTime(statHandler.getApps());
                    dbHelper.saveList(statHandler.getApps());

                    List<PInfo> saved = dbHelper.loadList(new Date());
                    Func.mergeIdentifiers(merged, saved);
                }

                statHandler = new StatHandler(merged);

                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Success");
                resultReceiver.send(requestCode, bundle);
            } else if (requestCode == SAVE_APP_LIST_TO_DATABASE) {
                ResultReceiver resultReceiver = intent.getParcelableExtra(RECEIVER);

                Func.saveTime(statHandler.getApps());
                dbHelper.saveList(statHandler.getApps());

                List<PInfo> saved = dbHelper.loadList(new Date());
                statHandler = new StatHandler(saved);

                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Success");
                resultReceiver.send(requestCode, bundle);
            }
        }
    }
}
