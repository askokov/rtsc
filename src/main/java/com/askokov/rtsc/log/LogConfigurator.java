package com.askokov.rtsc.log;

import android.content.Context;
import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.appender.LogCatAppender;
import com.google.code.microlog4android.config.PropertyConfigurator;
import com.google.code.microlog4android.format.PatternFormatter;

public class LogConfigurator {

    private LogConfigurator() {
    }

    /**
     * Configure microlog
     */
    public static void configure(Context context) {
        PropertyConfigurator.getConfigurator(context).configure();

        PatternFormatter formatter = new PatternFormatter();
        formatter.setPattern("[%d{DATE}]:[%P] [%c] - %m %T");

        FileAppender fileAppender = new FileAppender();
        fileAppender.setFileName("rtsc_log.txt");
        fileAppender.setFormatter(formatter);
        fileAppender.setAppend(true);
        LoggerFactory.getLogger().addAppender(fileAppender);

        /*
        LogCatAppender logCatAppender = new LogCatAppender();
        logCatAppender.setFormatter(formatter);
        LoggerFactory.getLogger().addAppender(logCatAppender);
        */
        LoggerFactory.getLogger().setLevel(Level.INFO);
    }
}
