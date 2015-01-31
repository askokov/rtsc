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
    private EditText editUser;
    private EditText editPassword;

    private GetConfigurationResultReceiver getConfigurationReceiver;
    private SaveConfigurationResultReceiver saveConfigurationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        logger.info("onCreate");

        setContentView(R.layout.config);

        cbAddInstalled = (CheckBox) findViewById(R.id.cbAddInstalled);

        groupMail = (RadioGroup) findViewById(R.id.groupMail);
        groupMail.setOnCheckedChangeListener(this);

        editUser = (EditText) findViewById(R.id.editUser);
        editPassword = (EditText) findViewById(R.id.editPassword);

        Button btnSaveConfig = (Button) findViewById(R.id.btnSaveConfig);
        btnSaveConfig.setOnClickListener(this);

        getConfigurationReceiver = new GetConfigurationResultReceiver(null);

        Intent configIntent = new Intent(StatService.StatReceiver.ACTION);
        configIntent.putExtra(EXECUTE, GET_CONFIGURATION);
        configIntent.putExtra(RECEIVER, getConfigurationReceiver);

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
        if (rb.getId() == R.id.radioMailGmail) {
            editUser.setEnabled(true);
            editPassword.setEnabled(true);
        } else {
            editUser.setEnabled(false);
            editPassword.setEnabled(false);
        }

        logger.info("onCheckedChanged: Select radio<" + rb.getId() + ", " + rb.getText() + ">");
    }

    @Override
    protected void onDestroy() {
        logger.info("onDestroy");

        super.onDestroy();
    }

    private void performSaveConfiguration() {
        saveConfigurationReceiver = new SaveConfigurationResultReceiver(null);

        Intent configIntent = new Intent(StatService.StatReceiver.ACTION);
        configIntent.putExtra(EXECUTE, SAVE_CONFIGURATION);
        configIntent.putExtra(RECEIVER, saveConfigurationReceiver);

        sendBroadcast(configIntent);
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

                cbAddInstalled.setChecked(configuration.isAddInstalled());

                groupMail.check(configuration.getMailType().ordinal());

                editUser.setText(configuration.getMailUser());
                editPassword.setText(configuration.getMailPassword());
                //editUser.setText("Google is your friend.", TextView.BufferType.EDITABLE);
            }
        }
    }

    class SaveConfigurationResultReceiver extends ResultReceiver {

        public SaveConfigurationResultReceiver(final Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            logger.info("SaveConfigurationResultReceiver.onReceiveResult: resultCode<" + resultCode + ">");

            if (resultCode == SAVE_CONFIGURATION) {
                String result = resultData.getString(RESULT);
                logger.info("onReceiveResult: result<" + result + ">");
            }
        }
    }
}
