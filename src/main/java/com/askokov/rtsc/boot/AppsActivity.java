package com.askokov.rtsc.boot;

import java.util.ArrayList;
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
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.parcel.Constant;
import com.askokov.rtsc.parcel.ListParcel;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class AppsActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(AppsActivity.class);

    private ResultReceiver bootReceiver = new GetListResultReceiver(null);
    private ResultReceiver updateReceiver = new UpdateListResultReceiver(null);

    private BoxAdapter boxAdapter;
    private ListView lvMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("AppsActivity.onCreate");

        setContentView(R.layout.apps);
        Button btnChecked = (Button) findViewById(R.id.btnSave);
        btnChecked.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("AppsActivity.onResume");
    }

    @Override
    public void onClick(final View v) {
        List<String> checked = new ArrayList<String>();

        List<PInfo> box = boxAdapter.getBox();
        // пишем в лог выделенные элементы
        logger.info("checked: ");

        for (PInfo info : box) {
            logger.info("---" + info.getPname());
            checked.add(info.getPname());
        }

        Intent intent = new Intent();
        //intent.putExtra("name", etName.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        logger.info("MainActivity.onDestroy");

        super.onDestroy();
    }

    private void getAppListRequest() {
        Intent intent = new Intent(StatService.SetupReceiver.ACTION);
        intent.putExtra(EXECUTE, REQUEST_GET_APP_LIST);
        intent.putExtra(RECEIVER, bootReceiver);

        sendBroadcast(intent);

        logger.info("MainActivity: sendGetAppListRequest");
    }

    private void sendUpdateAppListRequest(List<String> checked) {
        Intent intent = new Intent(StatService.SetupReceiver.ACTION);
        intent.putExtra(RECEIVER, updateReceiver);
        intent.putExtra(EXECUTE, REQUEST_UPDATE_APP_LIST);
        intent.putExtra(PARCEL, new ListParcel(checked));
        sendBroadcast(intent);

        logger.info("MainActivity: sendUpdateAppListRequest");
    }

    class GetListResultReceiver extends ResultReceiver {

        public GetListResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("GetListResultReceiver.onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == STATUS_FINISH) {
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
            logger.info("UpdateListResultReceiver.onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == STATUS_FINISH) {
                String result = resultData.getString(RESULT);
                logger.info("BootResultReceiver.onReceiveResult: result<" + result + ">");
            }
        }
    }
}
