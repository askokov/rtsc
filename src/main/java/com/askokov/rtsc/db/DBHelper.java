package com.askokov.rtsc.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.askokov.rtsc.common.Configuration;
import com.askokov.rtsc.common.Func;
import com.askokov.rtsc.common.MailType;
import com.askokov.rtsc.common.PInfo;
import com.askokov.rtsc.common.ReportType;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class DBHelper extends SQLiteOpenHelper {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);

    private static final int DB_VERSION = 1;          // версия БД

    public DBHelper(Context context, String dbPath) {
        super(context, dbPath, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        logger.info("onCreate database");

        db.beginTransaction();
        try {
            db.execSQL("CREATE TABLE STAT (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT NOT NULL, package_name TEXT NOT NULL, version_name TEXT NOT NULL, date INTEGER NOT NULL, full_time INTEGER NOT NULL)");
            logger.info("Table <STAT> created");

            db.execSQL("CREATE TABLE CONF (" + "add_installed INTEGER NOT NULL, report_type TEXT NOT NULL, mail_type TEXT NOT NULL, mail_user TEXT, mail_password TEXT)");
            db.execSQL("INSERT INTO CONF (add_installed, report_type, mail_type) VALUES(0,'ALL','CLIENT')");
            logger.info("Table <CONF> created");

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        logger.info("onUpgrade database from " + oldVersion + " to " + newVersion + " version");

        /*
        if (oldVersion == 1 && newVersion == 2) {

            ContentValues cv = new ContentValues();

            // данные для таблицы должностей
            int[] position_id = {1, 2, 3, 4};
            String[] position_name = {"Директор", "Программер",
                "Бухгалтер", "Охранник"};
            int[] position_salary = {15000, 13000, 10000, 8000};

            db.beginTransaction();
            try {
                // создаем таблицу должностей
                db.execSQL("create table position ("
                    + "id integer primary key,"
                    + "name text, salary integer);");

                // заполняем ее
                for (int i = 0; i < position_id.length; i++) {
                    cv.clear();
                    cv.put("id", position_id[i]);
                    cv.put("name", position_name[i]);
                    cv.put("salary", position_salary[i]);
                    db.insert("position", null, cv);
                }

                db.execSQL("alter table people add column posid integer;");

                for (int i = 0; i < position_id.length; i++) {
                    cv.clear();
                    cv.put("posid", position_id[i]);
                    db.update("people", cv, "position = ?",
                        new String[]{position_name[i]});
                }

                db.execSQL("create temporary table people_tmp ("
                    + "id integer, name text, position text, posid integer);");

                db.execSQL("insert into people_tmp select id, name, position, posid from people;");
                db.execSQL("drop table people;");

                db.execSQL("create table people ("
                    + "id integer primary key autoincrement,"
                    + "name text, posid integer);");

                db.execSQL("insert into people select id, name, posid from people_tmp;");
                db.execSQL("drop table people_tmp;");

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
        */
    }


    public List<PInfo> loadApps(Date date) {
        List<PInfo> result = new ArrayList<PInfo>();
        long dateParam = Func.truncateDate(date).getTime();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from STAT where date=" + dateParam, null);

        if (c != null) {
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    logger.info("Table STAT has " + c.getCount() + " rows");

                    do {
                        PInfo info = new PInfo();

                        info.setChecked(true);

                        int id = c.getInt(c.getColumnIndex("_id"));
                        info.setId(id);
                        info.setLabel(c.getString(c.getColumnIndex("label")));
                        info.setPackageName(c.getString(c.getColumnIndex("package_name")));
                        info.setVersionName(c.getString(c.getColumnIndex("version_name")));

                        int d = c.getInt(c.getColumnIndex("date"));
                        info.setDate(new Date(d));

                        info.setFullTime(c.getLong(c.getColumnIndex("full_time")));

                        logger.info("loaded<" + info.getPackageName() + ">");
                    } while (c.moveToNext());
                }
            }
            c.close();
        } else {
            logger.info("Cursor is null");
        }

        db.close();
        logger.info("List loaded");

        return result;
    }

    public Configuration loadConfiguration() {
        Configuration configuration = new Configuration();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from CONF", null);

        if (c != null) {
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    int addInstalled = c.getInt(c.getColumnIndex("add_installed"));
                    configuration.setAddInstalled(addInstalled == 1);

                    String type = c.getString(c.getColumnIndex("report_type"));
                    configuration.setReportType(ReportType.valueOf(type));

                    type = c.getString(c.getColumnIndex("mail_type"));
                    configuration.setMailType(MailType.valueOf(type));

                    configuration.setMailUser(c.getString(c.getColumnIndex("mail_user")));
                    configuration.setMailPassword(c.getString(c.getColumnIndex("mail_password")));
                }
            }
            c.close();
        } else {
            logger.info("Cursor is null");
        }

        db.close();
        logger.info("Configuration loaded");

        return configuration;
    }

    public void saveApps(List<PInfo> list) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        int count = 0;
        for (PInfo info : list) {
            if (info.getFullTime() > 0) {
                count++;

                cv.clear();
                cv.put("label", info.getLabel());
                cv.put("package_name", info.getPackageName());
                cv.put("version_name", info.getVersionName());
                cv.put("date", info.getDate().getTime());
                cv.put("full_time", info.getFullTime());

                if (info.getId() != null) {
                    db.update("STAT", cv, null, null);
                } else {
                    db.insert("STAT", null, cv);
                }
            }
        }

        db.close();
        logger.info("List saved: size<" + count + ">");
    }

    public void saveConfiguration(Configuration configuration) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        cv.put("add_installed", configuration.isAddInstalled() ? 1 : 0);
        cv.put("report_type", configuration.getReportType().name());
        cv.put("mail_type", configuration.getMailType().name());
        cv.put("mail_user", configuration.getMailUser());
        cv.put("mail_password", configuration.getMailPassword());

        db.update("CONF", cv, null, null);

        db.close();
        logger.info("Configuration saved");
    }

    public void createDataBase() {
        //Создаст базу, если она не создана
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
        } else {
            logger.info("Database already exists");
        }
    }

    //Проверка существования базы данных
    private boolean checkDataBase() {
        //File dbFile = context.getDatabasePath(dbPath);
        //return dbFile.exists();
        return false;
        /*
        SQLiteDatabase checkDb = null;
        try {
            String path = DB_NAME;
            checkDb = openDataBase(path, true);
        } catch (SQLException e) {
            logger.info("Error while checking db", e);
        }
        //Андроид не любит утечки ресурсов, все должно закрываться
        if (checkDb != null) {
            checkDb.close();
        }
        return checkDb != null;
        */
    }

    /*
    private SQLiteDatabase openDataBase(String path, boolean readOnly) throws SQLException {
        logger.info("openDataBase: path<" + path + ">");
        return SQLiteDatabase.openDatabase(path, null,
            readOnly ? SQLiteDatabase.OPEN_READONLY : SQLiteDatabase.OPEN_READWRITE);
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        return openDataBase(DB_NAME, false);
    }
    */

    public synchronized void close(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
        super.close();
    }
}
