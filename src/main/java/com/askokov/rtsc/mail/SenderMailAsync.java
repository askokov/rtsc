package com.askokov.rtsc.mail;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class SenderMailAsync extends AsyncTask<Object, String, Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(SenderMailAsync.class);

    private static final String SENDER = "sender@rtsc.com";
    private static final String RECIPIENT = "recipient@rtsc.com";
    private static final String USER = "aliaksei.skokau@gmail.com";
    private static final String PASSWORD = "$qwertyu_1";

    private Context context;
    private String subject;
    private String body;
    private String filename;
    private String user = USER;
    private String password = PASSWORD;

    public SenderMailAsync(Context context, String subject, String body, String filename) {
        this.context = context;
        this.subject = subject;
        this.body = body;
        this.filename = filename;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        Toast.makeText(context, "Email was sent successfully.", Toast.LENGTH_LONG).show();
        logger.info("SenderMailAsync: Email was sent successfully.");
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        try {
            MailSender mailSender = new MailSender(USER, PASSWORD);

            mailSender.sendMail(subject, body, SENDER, RECIPIENT, filename);
        } catch (Exception e) {
            Toast.makeText(context, "Email was not sent.", Toast.LENGTH_SHORT).show();
            logger.info("SenderMailAsync: Email was not sent.");
        }

        return false;
    }
}
