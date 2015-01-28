package com.askokov.rtsc.boot;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.askokov.rtsc.R;
import com.askokov.rtsc.common.Configuration;
import com.askokov.rtsc.common.Constant;
import com.askokov.rtsc.common.Func;
import com.askokov.rtsc.common.ReportGenerator;
import com.askokov.rtsc.log.LogConfigurator;
import com.askokov.rtsc.mail.SenderMailAsync;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class MainActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private Button btnService;
    private Button btnAppList;
    private TextView txtService;
    private boolean serviceRunning;
    private Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogConfigurator.configure(this);

        logger.info("onCreate");

        setContentView(R.layout.main);

        btnAppList = (Button) findViewById(R.id.btnAppList);
        btnAppList.setOnClickListener(this);

        Button btnConfiguration = (Button) findViewById(R.id.btnConfiguration);
        btnConfiguration.setOnClickListener(this);

        Button btnGenerateReport = (Button) findViewById(R.id.btnGenerateReport);
        btnGenerateReport.setOnClickListener(this);

        btnService = (Button) findViewById(R.id.btnService);
        btnService.setOnClickListener(this);
        serviceRunning = false;

        if (isServiceRunning()) {
            serviceRunning = true;
            btnService.setText(R.string.txtServiceStop);

            logger.info("onCreate: service running");
        } else {
            btnAppList.setEnabled(false);
        }

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        configuration = Func.loadConfiguration(pref);

        Button btnStatFromMemory = (Button) findViewById(R.id.btnStatFromMemory);
        btnStatFromMemory.setOnClickListener(this);

        Button btnStatFromDatabase = (Button) findViewById(R.id.btnStatFromDatabase);
        btnStatFromDatabase.setOnClickListener(this);

        Button btnSaveStatToDatabase = (Button) findViewById(R.id.btnSaveStatToDatabase);
        btnSaveStatToDatabase.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("onResume");
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnAppList:
                logger.info("onClick: btnAppList");

                if (serviceRunning) {
                    logger.info("onClick: service running...");

                    Intent intent = new Intent(this, AppsActivity.class);
                    intent.putExtra(OBSERVE_INSTALLED, configuration.isAddInstalled());
                    startActivityForResult(intent, REQUEST_GET_APP_LIST);
                } else {
                    logger.info("onClick: service stopped...");
                }

                break;

            case R.id.btnConfiguration:
                logger.info("onClick: btnConfiguration");

                Intent intent = new Intent(this, AppsActivity.class);
                intent.putExtra(CONFIGURATION, configuration);
                startActivityForResult(intent, REQUEST_SAVE_CONFIGURATION);

                break;

            case R.id.btnGenerateReport:
                logger.info("onClick: btnGenerateReport");

                //statRequest();

                break;
            case R.id.btnService:
                logger.info("MainActivity.onClick: btnService");

                if (serviceRunning) {
                    Intent serviceIntent = new Intent(this, StatService.class);
                    stopService(serviceIntent);
                    serviceRunning = false;
                    btnAppList.setEnabled(false);

                    btnService.setText(R.string.txtServiceStart);

                    logger.info("onCreate: stop service");
                } else {
                    Intent serviceIntent = new Intent(this, StatService.class);
                    serviceIntent.putExtra(OBSERVE_INSTALLED, configuration.isAddInstalled());
                    startService(serviceIntent);
                    serviceRunning = true;
                    btnAppList.setEnabled(true);

                    btnService.setText(R.string.txtServiceStop);

                    logger.info("onCreate: start service");
                }

                break;

            case R.id.btnStatFromMemory:
                statMemoryRequest();

                break;

            case R.id.btnStatFromDatabase:
                statDatabaseRequest();

                break;

            case R.id.btnSaveStatToDatabase:
                saveStatRequest();

                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // запишем в лог значения requestCode и resultCode
        logger.info("onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);
        // если пришло ОК
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_APP_LIST:
                    boolean observeInstalled = data.getBooleanExtra(OBSERVE_INSTALLED, false);

                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        logger.info("onDestroy");

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        Func.saveConfiguration(pref, configuration);

        super.onDestroy();
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StatService.class.getName().equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void statMemoryRequest() {
        logger.info("Statistic from memory request start");
        StatReportReceiver receiver = new StatReportReceiver(null, this);

        Intent intent = new Intent(StatService.StatReceiver.ACTION);
        intent.putExtra(EXECUTE, REQUEST_GET_STAT_FROM_MEMORY);
        intent.putExtra(RECEIVER, receiver);

        sendBroadcast(intent);
        logger.info("Statistic from memory request finish");
    }

    private void statDatabaseRequest() {
        logger.info("Statistic from database request start");
        StatReportReceiver receiver = new StatReportReceiver(null, this);

        Intent intent = new Intent(StatService.StatReceiver.ACTION);
        intent.putExtra(EXECUTE, REQUEST_GET_STAT_FROM_DATABASE);
        intent.putExtra(RECEIVER, receiver);

        sendBroadcast(intent);
        logger.info("Statistic from database request finish");
    }

    private void saveStatRequest() {
        logger.info("Save statistic request start");
        StatReportReceiver receiver = new StatReportReceiver(null, this);

        Intent intent = new Intent(StatService.StatReceiver.ACTION);
        intent.putExtra(EXECUTE, REQUEST_SAVE_STAT_TO_DATABASE);
        intent.putExtra(RECEIVER, receiver);

        sendBroadcast(intent);
        logger.info("Save statistic request finish");
    }

    public void showInfo(int idx) {

        final Dialog infoDialog = new Dialog(this);
        infoDialog.setCancelable(false);

        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.info, null);
        infoDialog.setContentView(view);

        /*
        final TextView text = (TextView) view.findViewById(R.id.txtInfo);
        String[] infoArray = getResources().getStringArray(R.array.infoArray);
        text.setText(infoArray[idx]);
        */

        Button btnClose = (Button) view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                infoDialog.dismiss();
            }
        });

        infoDialog.show();
    }

    class StatReportReceiver extends ResultReceiver {
        private Context context;

        public StatReportReceiver(final Handler handler, final Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("StatReportReceiver.onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == REQUEST_GET_STAT_FROM_MEMORY) {
                logger.info("StatReportReceiver: get statistic from memory");

                PInfoParcel parcel = (PInfoParcel) resultData.getSerializable(RESULT);
                sendReportToEmail(parcel);

            } else if (resultCode == REQUEST_GET_STAT_FROM_DATABASE) {
                logger.info("StatReportReceiver: get statistic from database");

                PInfoParcel parcel = (PInfoParcel) resultData.getSerializable(RESULT);
                sendReportToEmail(parcel);

            } else if (resultCode == REQUEST_SAVE_STAT_TO_DATABASE) {
                String result = resultData.getString(RESULT);

                logger.info("StatReportReceiver: save statistic to database --> " + result);
            }

            logger.info("StatReportReceiver.onReceiveResult: finish");
        }

        private void sendReportToEmail(PInfoParcel parcel) {
            if (!parcel.getList().isEmpty()) {
                String file = null;
                try {
                    file = new ReportGenerator().createPDF("report.pdf", context, parcel.getList());
                } catch (Exception ex) {
                    logger.info("StatReportReceiver: createPDF exception", ex);
                }

                if (file != null) {
                    SenderMailAsync mailAsync = new SenderMailAsync(context, "Statistic report", "See attachment", file);
                    mailAsync.execute();

                    logger.info("StatReportReceiver: PDF file was sent to email");
                }
            } else {
                logger.info("StatReportReceiver: Statistic missed");
            }
        }
    }
}
