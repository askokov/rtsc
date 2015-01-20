package com.askokov.rtsc.boot;

import java.util.List;

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
import android.widget.CheckBox;
import android.widget.TextView;
import com.askokov.rtsc.R;
import com.askokov.rtsc.common.Constant;
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.common.ReportGenerator;
import com.askokov.rtsc.log.LogConfigurator;
import com.askokov.rtsc.mail.SenderMailAsync;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class MainActivity extends Activity implements Constant, View.OnClickListener {

    private static final Logger logger = LoggerFactory.getLogger(MainActivity.class);
    private static final String SAVED_CHECK_BOX = "savedCheckBox";
    private static final String LABEL = "saveLabel";

    private CheckBox chb;
    private SharedPreferences pref;
    private Button btnService;
    private Button btnAppListSetup;
    private TextView txtService;
    private boolean serviceRinning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogConfigurator.configure(this);

        logger.info("onCreate");

        setContentView(R.layout.main);

        btnAppListSetup = (Button) findViewById(R.id.btnAppListSetup);
        btnAppListSetup.setOnClickListener(this);

        Button btnEmailSetup = (Button) findViewById(R.id.btnEmailSetup);
        btnEmailSetup.setOnClickListener(this);

        Button btnReportSetup = (Button) findViewById(R.id.btnReportSetup);
        btnReportSetup.setOnClickListener(this);

        Button btnGenerateReport = (Button) findViewById(R.id.btnGenerateReport);
        btnGenerateReport.setOnClickListener(this);

        btnService = (Button) findViewById(R.id.btnService);
        btnService.setOnClickListener(this);
        serviceRinning = false;

        if (isServiceRunning()) {
            btnService.setBackgroundResource(R.drawable.server_run_enabled);
            serviceRinning = true;
            txtService = (TextView) findViewById(R.id.txtService);
            txtService.setText(R.string.txtServiceStop);
            btnGenerateReport.setOnClickListener(this);

            logger.info("onCreate: service running");
        } else {
            btnAppListSetup.setEnabled(false);
        }

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
    }

    @Override
    protected void onResume() {
        logger.info("onResume");
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnAppListSetup:
                logger.info("onClick: btnAppListSetup");

                if (serviceRinning) {
                    logger.info("onClick: service running...");

                    Intent intent = new Intent(this, AppsActivity.class);
                    intent.putExtra(OBSERVE_INSTALLED, chb.isChecked());
                    startActivityForResult(intent, REQUEST_GET_APP_LIST);
                } else {
                    logger.info("onClick: service stopped...");
                }

                break;

            case R.id.btnEmailSetup:
                break;

            case R.id.btnReportSetup:
                break;

            case R.id.btnGenerateReport:
                logger.info("onClick: btnGenerateReport");

                List<PInfo> list = getStatListRequest();
                if (!list.isEmpty()) {
                    String file = null;
                    try {
                        file = new ReportGenerator().createPDF("report.pdf", this, list);
                    } catch (Exception ex) {
                        logger.info("onClick: createPDF exception", ex);
                    }

                    if (file != null) {
                        SenderMailAsync mailAsync = new SenderMailAsync(this, "Statistic report", "See attachment", file);
                        mailAsync.execute();

                        logger.info("PDF file was sent to email");
                    }
                } else {
                    logger.info("Statistic missed");
                }

                break;
            case R.id.btnService:
                logger.info("MainActivity.onClick: btnService");

                if (serviceRinning) {
                    Intent serviceIntent = new Intent(this, StatService.class);
                    stopService(serviceIntent);
                    serviceRinning = false;
                    btnAppListSetup.setEnabled(false);

                    btnService.setBackgroundResource(R.drawable.server_stop_enabled);
                    txtService = (TextView) findViewById(R.id.txtService);
                    txtService.setText(R.string.txtServiceStart);

                    logger.info("onCreate: stop service");
                } else {
                    Intent serviceIntent = new Intent(this, StatService.class);
                    serviceIntent.putExtra(OBSERVE_INSTALLED, chb.isChecked());
                    startService(serviceIntent);
                    serviceRinning = true;
                    btnAppListSetup.setEnabled(true);

                    btnService.setBackgroundResource(R.drawable.server_run_enabled);
                    txtService = (TextView) findViewById(R.id.txtService);
                    txtService.setText(R.string.txtServiceStop);

                    logger.info("onCreate: start service");
                }

                break;

            case R.id.btnHelp1:
                showInfo(0);

                break;

            case R.id.btnHelp2:
                showInfo(1);

                break;

            case R.id.btnHelp3:
                showInfo(2);

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
        logger.info("savePreferences: saved");

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
            logger.info("loadPreferences: loaded<" + ch + ">");
        }

        return label != null;
    }

    private List<PInfo> getStatListRequest() {
        GetStatReceiver receiver = new GetStatReceiver(null);

        Intent intent = new Intent(StatService.SetupReceiver.ACTION);
        intent.putExtra(EXECUTE, REQUEST_GET_STAT_LIST);
        intent.putExtra(RECEIVER, receiver);

        sendBroadcast(intent);

        logger.info("getStatListRequest");

        return receiver.getList();
    }

    public void showInfo(int idx) {

        final Dialog infoDialog = new Dialog(this);
        infoDialog.setCancelable(false);
        //final AlertDialog.Builder infoDialog = new AlertDialog.Builder(this);
        //infoDialog.setTitle(R.string.txtInfoHeader);

        infoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = getLayoutInflater().inflate(R.layout.info, null);
        infoDialog.setContentView(view);

        final TextView text = (TextView) view.findViewById(R.id.txtInfo);
        String[] infoArray = getResources().getStringArray(R.array.infoArray);
        text.setText(infoArray[idx]);

        Button btnClose = (Button) view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                infoDialog.dismiss();
                //infoDialog.cancel();
            }
        });

        /*
        ratingdialog.setPositiveButton("Готово",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    txtView.setText(String.valueOf(rating.getRating()));
                    dialog.dismiss();
                }
            })

            .setNegativeButton("Отмена",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        */

        //infoDialog.create();
        infoDialog.show();
    }

    class GetStatReceiver extends ResultReceiver {
        List<PInfo> list;

        public GetStatReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == STATUS_FINISH) {
                // создаем адаптер
                PInfoParcel parcel = (PInfoParcel) resultData.getSerializable(RESULT);
                list = parcel.getList();
            }
        }

        public List<PInfo> getList() {
            return list;
        }
    }
}
