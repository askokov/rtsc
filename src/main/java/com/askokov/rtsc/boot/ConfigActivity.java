package com.askokov.rtsc.boot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.askokov.rtsc.R;
import com.askokov.rtsc.common.Configuration;
import com.askokov.rtsc.common.Constant;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class ConfigActivity extends Activity implements Constant, View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(ConfigActivity.class);

    private CheckBox cbAddInstalled;
    private RadioGroup groupMail;
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

        cbAddInstalled = (CheckBox) findViewById(R.id.cbAddInstalled);
        cbAddInstalled.setChecked(configuration.isAddInstalled());

        groupMail = (RadioGroup) findViewById(R.id.groupMail);
        groupMail.check(configuration.getMailType().ordinal());

        Button btnSaveConfig = (Button) findViewById(R.id.btnSaveConfig);
        btnSaveConfig.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        logger.info("onResume");
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btnSaveConfig:
                logger.info("onClick: Save configuration");
                performSaveConfiguration();

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

    private void performSaveConfiguration() {
    }

    class SaveConfigurationResultReceiver extends ResultReceiver {

        public SaveConfigurationResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == SAVE_CONFIGURATION) {
                String result = resultData.getString(RESULT);
                logger.info("onReceiveResult: result<" + result + ">");
            }
        }
    }
}
