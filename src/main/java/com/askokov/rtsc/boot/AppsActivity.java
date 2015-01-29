package com.askokov.rtsc.boot;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.askokov.rtsc.R;
import com.askokov.rtsc.common.Constant;
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class AppsActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(AppsActivity.class);

    private ResultReceiver getListReceiver = new GetListResultReceiver(null);
    private ResultReceiver updateListReceiver = new UpdateListResultReceiver(null);

    private BoxAdapter boxAdapter;
    private ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");

        setContentView(R.layout.apps);

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);

        //Intent intent = getIntent();

        getAppListRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("onResume");
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnClear:
                logger.info("onClick: CLEAR");
                performClear();

                break;

            case R.id.btnSave:
                logger.info("onClick: SAVE");
                performSave();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();

                break;
        }
    }

    @Override
    protected void onDestroy() {
        logger.info("onDestroy");

        super.onDestroy();
    }

    private void getAppListRequest() {
        Intent intent = new Intent(StatService.StatReceiver.ACTION);
        intent.putExtra(EXECUTE, GET_APP_LIST_FROM_SYSTEM);
        intent.putExtra(RECEIVER, getListReceiver);

        sendBroadcast(intent);

        logger.info("sendGetAppListRequest");
    }

    private void sendUpdateAppListRequest(List<PInfo> box) {
        Intent intent = new Intent(StatService.StatReceiver.ACTION);
        intent.putExtra(RECEIVER, updateListReceiver);
        intent.putExtra(EXECUTE, SAVE_APP_LIST_TO_SERVICE);
        intent.putExtra(PARCEL, new PInfoParcel(box));
        sendBroadcast(intent);

        logger.info("sendUpdateAppListRequest");
    }

    private void performClear() {
        boxAdapter.clearBox();
        boxAdapter.notifyDataSetChanged();
    }

    private void performSave() {
        List<PInfo> box = boxAdapter.getBox();
        // пишем в лог выделенные элементы
        logger.info("checked: ");

        for (PInfo info : box) {
            logger.info("---" + info.getPackageName());
        }

        sendUpdateAppListRequest(box);
    }

    class GetListResultReceiver extends ResultReceiver {

        public GetListResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == GET_APP_LIST_FROM_SYSTEM) {
                // создаем адаптер
                PInfoParcel parcel = (PInfoParcel) resultData.getSerializable(RESULT);
                boxAdapter = new BoxAdapter(AppsActivity.this, parcel.getList());
                // настраиваем список
                lvMain = (ListView) findViewById(R.id.lvMain);
                lvMain.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                lvMain.setAdapter(boxAdapter);
            }
        }
    }

    class UpdateListResultReceiver extends ResultReceiver {

        public UpdateListResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == SAVE_APP_LIST_TO_SERVICE) {
                String result = resultData.getString(RESULT);
                logger.info("onReceiveResult: result<" + result + ">");
            }
        }
    }
}
