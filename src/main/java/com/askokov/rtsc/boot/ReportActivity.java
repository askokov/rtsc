package com.askokov.rtsc.boot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.askokov.rtsc.R;
import com.askokov.rtsc.common.Configuration;
import com.askokov.rtsc.common.Constant;
import com.askokov.rtsc.common.ReportGenerator;
import com.askokov.rtsc.mail.SenderMailAsync;
import com.askokov.rtsc.parcel.PInfoParcel;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class ReportActivity extends Activity implements Constant, View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ReportActivity.class);

    private RadioGroup groupReport;
    private EditText editDate;

    private Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");

        setContentView(R.layout.config);

        Intent intent = getIntent();
        configuration = (Configuration) intent.getSerializableExtra(CONFIGURATION);

        groupReport = (RadioGroup) findViewById(R.id.groupReport);
        groupReport.setOnCheckedChangeListener(this);

        editDate = (EditText) findViewById(R.id.editDate);

        Button btnSendReport = (Button) findViewById(R.id.btnSendReport);
        btnSendReport.setOnClickListener(this);

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        GetConfigurationResultReceiver receiver = new GetConfigurationResultReceiver(null);

        Intent configIntent = new Intent(StatService.StatReceiver.ACTION);
        configIntent.putExtra(EXECUTE, GET_CONFIGURATION);
        configIntent.putExtra(RECEIVER, receiver);

        sendBroadcast(configIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("onResume");
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnSendReport:
                logger.info("onClick: Send report");

                StatReportReceiver receiver = new StatReportReceiver(null, this);

                Intent statIntent = new Intent(StatService.StatReceiver.ACTION);
                statIntent.putExtra(EXECUTE, GET_APP_LIST_FROM_SERVICE);
                statIntent.putExtra(RECEIVER, receiver);

                sendBroadcast(statIntent);

                break;


            case R.id.btnCancel:
                logger.info("onClick: Cancel");

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();

                break;

        }
    }

    @Override
    public void onCheckedChanged(final RadioGroup group, final int checkedId) {
        // checkedId is the RadioButton selected
        RadioButton rb = (RadioButton) findViewById(checkedId);
        editDate.setEnabled(rb.getId() == R.id.radioReportDate);

        logger.info("onCheckedChanged: Select radio<" + rb.getId() + ", " + rb.getText() + ">");
    }

    @Override
    protected void onDestroy() {
        logger.info("onDestroy");

        super.onDestroy();
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

            if (resultCode == GET_APP_LIST_FROM_SERVICE) {
                logger.info("StatReportReceiver: get statistic from service");

                PInfoParcel parcel = (PInfoParcel) resultData.getSerializable(RESULT);
                sendReportToEmail(parcel);

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
                    mailAsync.setUser(configuration.getMailUser());
                    mailAsync.setPassword(configuration.getMailPassword());
                    mailAsync.execute();

                    logger.info("StatReportReceiver: PDF file was sent to email");
                }
            } else {
                logger.info("StatReportReceiver: Statistic missed");
            }
        }
    }

    class GetConfigurationResultReceiver extends ResultReceiver {

        public GetConfigurationResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("GetConfigurationResultReceiver.onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == GET_CONFIGURATION) {
                Configuration configuration = (Configuration)resultData.getSerializable(RESULT);

                groupReport.check(configuration.getReportType().ordinal());
            }
        }
    }
}
