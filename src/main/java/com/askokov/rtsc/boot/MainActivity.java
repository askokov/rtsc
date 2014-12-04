package com.askokov.rtsc.boot;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.log.LogConfigurator;
import com.askokov.rtsc.parcel.Constant;
import com.askokov.rtsc.parcel.ListParcel;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class MainActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);

    private BoxAdapter boxAdapter;
    private ListView lvMain;
    private ResultReceiver bootReceiver = new BootResultReceiver(null);
    private ResultReceiver updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        Button btnChecked = (Button) findViewById(R.id.btnSave);
        btnChecked.setOnClickListener(this);

        LogConfigurator.configure(this);
        logger.info("MainActivity.onCreate");

        if (isServiceRunning()) {
            logger.info("MainActivity.onCreate: service running");
            getAppListRequest();
        } else {
            logger.info("MainActivity.onCreate: service missing");
            startServiceAndGetAppListRequest();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("MainActivity.onResume");
    }

    @Override
    public void onClick(final View v) {
        List<String> checked = new ArrayList<String>();

        List<PInfo> box = boxAdapter.getBox();
        // пишем в лог выделенные элементы
        logger.info("checked: ");

        for(PInfo info : box) {
            logger.info("---" + info.getPname());
            checked.add(info.getPname());
        }

        sendUpdateAppListRequest(checked);
    }

    @Override
    protected void onDestroy() {
        logger.info("MainActivity.onDestroy");

        super.onDestroy();
    }

    private void startServiceAndGetAppListRequest() {
        Intent intent = new Intent(this, StatService.class);
        intent.putExtra(RECEIVER, bootReceiver);
        startService(intent);
    }

    private void getAppListRequest() {
        Intent intent = new Intent(StatService.SetupReceiver.ACTION);

        intent.putExtra(EXECUTE, REQUEST_GET_APP_LIST);
        intent.putExtra(RECEIVER, bootReceiver);

        sendBroadcast(intent);

        logger.info("MainActivity: sendGetAppListRequest");
    }

    private void sendUpdateAppListRequest(List<String> checked) {
        updateReceiver = new UpdateResultReceiver(null);
        Intent intent = new Intent(StatService.SetupReceiver.ACTION);
        intent.putExtra(RECEIVER, updateReceiver);
        intent.putExtra(EXECUTE, REQUEST_UPDATE_APP_LIST);
        intent.putExtra(PARCEL, new ListParcel(checked));
        sendBroadcast(intent);

        logger.info("MainActivity: sendUpdateAppListRequest");
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

    class BootResultReceiver extends ResultReceiver {

        public BootResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("BootResultReceiver.onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == STATUS_FINISH) {
                // создаем адаптер
                PInfoParcel parcel = (PInfoParcel)resultData.getSerializable(RESULT);
                boxAdapter = new BoxAdapter(MainActivity.this, parcel.getList());
                // настраиваем список
                lvMain = (ListView) findViewById(R.id.lvMain);
                lvMain.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                lvMain.setAdapter(boxAdapter);
            }
        }
    }

    class UpdateResultReceiver extends ResultReceiver {

        public UpdateResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("BootResultReceiver.onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == STATUS_FINISH) {
                String result = resultData.getString(RESULT);
                logger.info("BootResultReceiver.onReceiveResult: result<" + result + ">");
            }
        }
    }
}
