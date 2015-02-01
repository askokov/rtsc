package com.askokov.rtsc.boot;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.askokov.rtsc.R;
import com.askokov.rtsc.common.Constant;
import com.askokov.rtsc.log.LogConfigurator;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class MainActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private Button btnService;
    private Button btnAppList;
    private boolean serviceRunning;
    //private Configuration configuration;

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

            logger.info("onCreate: service is running...");
        } else {
            btnAppList.setText(R.string.txtAppList_disabled);
            btnAppList.setEnabled(false);

            logger.info("onCreate: service is stopped");
        }

        //SharedPreferences pref = getPreferences(MODE_PRIVATE);

        Button btnStatFromMemory = (Button) findViewById(R.id.btnStatFromService);
        btnStatFromMemory.setOnClickListener(this);
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
                    logger.info("onClick: service is running...");

                    Intent intent = new Intent(this, AppsActivity.class);
                    startActivityForResult(intent, GET_APP_LIST_FROM_SYSTEM);
                } else {
                    logger.info("onClick: service is stopped");
                }

                break;

            case R.id.btnConfiguration:
                logger.info("onClick: btnConfiguration");

                Intent configIntent = new Intent(this, ConfigActivity.class);
                startActivityForResult(configIntent, GET_CONFIGURATION);

                break;

            case R.id.btnGenerateReport:
                logger.info("onClick: btnGenerateReport");

                Intent reportIntent = new Intent(this, ReportActivity.class);
                startActivityForResult(reportIntent, GET_CONFIGURATION);

                break;
            case R.id.btnService:
                logger.info("MainActivity.onClick: btnService");

                if (serviceRunning) {
                    Intent serviceIntent = new Intent(this, StatService.class);
                    stopService(serviceIntent);

                    serviceRunning = false;
                    btnAppList.setText(R.string.txtAppList_disabled);
                    btnAppList.setEnabled(false);

                    btnService.setText(R.string.txtServiceStart);

                    logger.info("onCreate: stop service");
                } else {
                    Intent serviceIntent = new Intent(this, StatService.class);
                    startService(serviceIntent);

                    serviceRunning = true;
                    btnAppList.setText(R.string.txtAppList);
                    btnAppList.setEnabled(true);

                    btnService.setText(R.string.txtServiceStop);

                    logger.info("onCreate: start service");
                }

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
                case GET_APP_LIST_FROM_SYSTEM:
                    logger.info("onActivityResult: return from GET_APP_LIST_FROM_SYSTEM");
                    break;

                case GET_CONFIGURATION:
                    logger.info("onActivityResult: return from GET_CONFIGURATION");
                    break;

            }
        }
    }

    @Override
    protected void onDestroy() {
        logger.info("onDestroy");

        //SharedPreferences pref = getPreferences(MODE_PRIVATE);

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

    /*
    public void showInfo(int idx) {

        final Dialog infoDialog = new Dialog(this);
        infoDialog.setCancelable(false);

        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.info, null);
        infoDialog.setContentView(view);

        Button btnClose = (Button) view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                infoDialog.dismiss();
            }
        });

        infoDialog.show();
    }
    */
}
