package com.askokov.mail;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class SenderMailAsync extends AsyncTask<Object, String, Boolean> {
    private static final Logger logger = LoggerFactory.getLogger(SenderMailAsync.class);

    private static final String SENDER = "camera@receiver.com";
    private static final String RECIPIENT = "skokov@inbox.ru";
    private static final String USER = "aliaksei.skokau@gmail.com";
    private static final String PASSWORD = "$qwertyu_1";

    private Context context;
    private String subject;
    private String body;
    private String filename;

    public SenderMailAsync(final Context context, final String subject, final String body, final String filename) {
        this.context = context;
        this.subject = subject;
        this.body = body;
        this.filename = filename;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        Toast.makeText(context, "Email was sent successfully.", Toast.LENGTH_LONG).show();
        logger.info("SenderMailAsync: Email was sent successfully.");
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        try {
            MailSenderClass mailSender = new MailSenderClass(USER, PASSWORD);

            mailSender.sendMail(subject, body, SENDER, RECIPIENT, filename);
        } catch (Exception e) {
            Toast.makeText(context, "Email was not sent.", Toast.LENGTH_SHORT).show();
            logger.info("SenderMailAsync: Email was not sent.");
        }

        return false;
    }
}
