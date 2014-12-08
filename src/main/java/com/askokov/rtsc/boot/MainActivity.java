package com.askokov.rtsc.boot;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.askokov.rtsc.R;
import com.askokov.rtsc.parcel.Constant;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class MainActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.info("MainActivity.onCreate");

        setContentView(R.layout.main);

        Button btnSetup = (Button) findViewById(R.id.btnSetup);
        btnSetup.setOnClickListener(this);

        Button btnHelp1 = (Button) findViewById(R.id.btnHelp1);
        btnHelp1.setOnClickListener(this);

        Button btnHelp2 = (Button) findViewById(R.id.btnHelp2);
        btnHelp2.setOnClickListener(this);

        if (isServiceRunning()) {
            logger.info("MainActivity.onCreate: service running");
        } else {
            logger.info("MainActivity.onCreate: service missing");

            Intent intent = new Intent(this, StatService.class);
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
        Intent intent = new Intent(this, AppsActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onDestroy() {
        logger.info("MainActivity.onDestroy");

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
}
