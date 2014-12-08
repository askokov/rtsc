package com.askokov.rtsc.boot;

import java.util.ArrayList;
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
import com.askokov.rtsc.monitor.ProcessesMonitor;
import com.askokov.rtsc.parcel.Constant;
import com.askokov.rtsc.parcel.ListParcel;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class StatService extends Service implements Constant {
    private static final Logger logger = LoggerFactory.getLogger(StatService.class);
    // restart service every 120 seconds
    private static final long REPEAT_TIME = 1000 * 60;

    private List<PInfo> infos = new ArrayList<PInfo>();
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

        if (intent != null) {
            ResultReceiver resultReceiver = (ResultReceiver)intent.getParcelableExtra(RECEIVER);

            if (resultReceiver != null) {
                infos.clear();
                infos.addAll(Func.getInstalledApps(this));

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(infos));
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
        for(PInfo info : infos) {
            info.setChecked(false);
        }

        for(String p : toUpdate) {
            for(PInfo info : infos) {
                if (p.equals(info.getPname())) {
                    info.setChecked(true);
                    logger.info("SetupReceiver.updateAppList: checked<" + p + ">");
                    break;
                }
            }
        }
    }

    class AlarmReceiver extends BroadcastReceiver {
        public static final String ACTION = "com.askokov.ALARM_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            logger.info("AlarmReceiver.onReceive(" + intent.getAction() + ")");

            synchronized (sync) {
                new ProcessesMonitor().scan(context, infos);
            }
        }
    }

    class SetupReceiver extends BroadcastReceiver implements Constant {
        public static final String ACTION = "com.askokov.SETUP_RECEIVER";

        @Override
        public void onReceive(Context context, Intent intent) {
            logger.info("SetupReceiver.onReceive(" + intent.getAction() + ")");

            int requestCode = intent.getIntExtra(EXECUTE, 0);
            logger.info("SetupReceiver.onReceive: requestCode<" + requestCode + ">");

            if (requestCode == REQUEST_GET_APP_LIST) {
                logger.info("SetupReceiver.onReceive: execute<get app list>");

                ResultReceiver resultReceiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER);

                infos.clear();
                infos.addAll(Func.getInstalledApps(context));

                Bundle bundle = new Bundle();
                bundle.putSerializable(RESULT, new PInfoParcel(infos));
                resultReceiver.send(STATUS_FINISH, bundle);

            } else if (requestCode == REQUEST_UPDATE_APP_LIST) {
                logger.info("SetupReceiver.onReceive: execute<update app list>");

                ResultReceiver resultReceiver = (ResultReceiver) intent.getParcelableExtra(RECEIVER);
                ListParcel parcel = (ListParcel) intent.getSerializableExtra(PARCEL);

                updateAppList(parcel.getList());

                Bundle bundle = new Bundle();
                bundle.putString(RESULT, "Update success");
                resultReceiver.send(STATUS_FINISH, bundle);
            }
        }
    }
}
