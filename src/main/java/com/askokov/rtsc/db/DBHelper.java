package com.askokov.rtsc.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.askokov.rtsc.common.PInfo;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class DBHelper extends SQLiteOpenHelper {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);

    private static final int DB_VERSION = 1;          // версия БД
    private Context context;

    public DBHelper(Context context, String dbPath) {
        super(context, dbPath, null, DB_VERSION);
        this.context = context;


        /*
public File getAlbumStorageDir(String albumName) {
    // Get the directory for the user's public pictures directory.
    File file = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), albumName);
    if (!file.mkdirs()) {
        Log.e(LOG_TAG, "Directory not created");
    }
    return file;
}
         */
    }

    public void onCreate(SQLiteDatabase db) {
        logger.info("onCreate database");

        // создаем таблицу должностей
        db.execSQL("CREATE TABLE STAT (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT NOT NULL, package_name TEXT NOT NULL, version_name TEXT NOT NULL, date INTEGER NOT NULL, full_time INTEGER NOT NULL)");
        logger.info("Table created");
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


    public List<PInfo> loadList(Date date) {
        List<PInfo> result = new ArrayList<PInfo>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from STAT", null);

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

    private String generateInClause(int count) {
        String result = "(";

        for(int i = 0; i < count; i++) {
            result += "?,";
        }

        result = result.substring(0, result.length());
        result += ")";

        return result;
    }


    public void saveList(List<PInfo> list) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();

        //database.execSQL("CREATE TABLE STAT (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, label TEXT NOT NULL,
        // package_name TEXT NOT NULL, version_name TEXT NOT NULL, date INTEGER NULL, full_time INTEGER NOT NULL)");
        // заполняем ее
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
