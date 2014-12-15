package com.askokov.rtsc.boot;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import com.askokov.rtsc.R;
import com.askokov.rtsc.log.LogConfigurator;
import com.askokov.rtsc.common.Constant;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class MainActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
    private static final String SAVED_CHECK_BOX = "savedCheckBox";
    private static final String LABEL = "saveLabel";

    private CheckBox chb;
    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogConfigurator.configure(this);

        logger.info("MainActivity.onCreate");

        setContentView(R.layout.main);

        Button btnAppListSetup = (Button) findViewById(R.id.btnAppListSetup);
        btnAppListSetup.setOnClickListener(this);

        Button btnEmailSetup = (Button) findViewById(R.id.btnEmailSetup);
        btnEmailSetup.setOnClickListener(this);

        Button btnReportSetup = (Button) findViewById(R.id.btnReportSetup);
        btnReportSetup.setOnClickListener(this);

        Button btnGenerateReport = (Button) findViewById(R.id.btnGenerateReport);
        btnGenerateReport.setOnClickListener(this);

        Button btnService = (Button) findViewById(R.id.btnService);
        btnService.setOnClickListener(this);

        chb = (CheckBox) findViewById(R.id.cbAddInstalled);
        if (!loadPreferences()) {
            chb.setChecked(false);
        }

        Button btnHelp1 = (Button) findViewById(R.id.btnHelp1);
        btnHelp1.setOnClickListener(this);

        Button btnHelp2 = (Button) findViewById(R.id.btnHelp2);
        btnHelp2.setOnClickListener(this);

        Button btnHelp3 = (Button) findViewById(R.id.btnHelp3);
        btnHelp3.setOnClickListener(this);

        //Start/Stop service button


        if (isServiceRunning()) {
            logger.info("MainActivity.onCreate: service running");
        } else {
            logger.info("MainActivity.onCreate: service missing");

            Intent intent = new Intent(this, StatService.class);
            intent.putExtra(OBSERVE_INSTALLED, chb.isChecked());
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("MainActivity.onResume");
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnAppListSetup:
                Intent intent = new Intent(this, AppsActivity.class);
                intent.putExtra(OBSERVE_INSTALLED, chb.isChecked());
                startActivityForResult(intent, REQUEST_GET_APP_LIST);

                break;
            case R.id.btnEmailSetup:
                break;
            case R.id.btnReportSetup:
                break;
            case R.id.btnGenerateReport:
                break;
            case R.id.btnService:
                break;

            case R.id.btnHelp1:
                break;
            case R.id.btnHelp2:
                break;
            case R.id.btnHelp3:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // запишем в лог значения requestCode и resultCode
        logger.info("MainActivity.onActivityResult: requestCode = " + requestCode + ", resultCode = " + resultCode);
        // если пришло ОК
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GET_APP_LIST:
                    //int align = data.getIntExtra("alignment", Gravity.LEFT);
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        logger.info("MainActivity.onDestroy");
        savePreferences();

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

    private boolean savePreferences() {
        logger.info("MainActivity.savePreferences: saved");

        pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean(SAVED_CHECK_BOX, chb.isChecked());
        ed.putString(LABEL, LABEL);
        return ed.commit();
    }

    private boolean loadPreferences() {
        pref = getPreferences(MODE_PRIVATE);
        String label = pref.getString(LABEL, null);

        if (label != null) {
            boolean ch = pref.getBoolean(SAVED_CHECK_BOX, false);
            chb.setChecked(ch);
            logger.info("MainActivity.loadPreferences: loaded<" + ch + ">");
        }

        return label != null;
    }
}
