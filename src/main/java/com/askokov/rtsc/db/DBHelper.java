package com.askokov.rtsc.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class DBHelper extends SQLiteOpenHelper {
    private static final Logger logger = LoggerFactory.getLogger(DBHelper.class);
    private static final String DB_NAME = "store.db"; // имя БД
    private static final int DB_VERSION = 1;          // версия БД
    public SQLiteDatabase database;


    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        logger.info(" --- onCreate database --- ");

        // создаем таблицу должностей
        db.execSQL("CREATE TABLE STAT (" + "_id INTEGER PRIMARY KEY AUTOINCREMENT, date DATETIME NOT NULL, package TEXT NOT NULL, time REAL NOT NULL);");

        /*
        CREATE TABLE [table_scan] (
           [_id] INTEGER PRIMARY KEY AUTOINCREMENT,
           [NR_ID] INTEGER NOT NULL,
           [T_ID] INTEGER NOT NULL,
           [Color_ID] INTEGER NOT NULL,
           [R_ID] INTEGER NOT NULL,
           [Barcode] TEXT NOT NULL,
           [NumberSeat] INTEGER,
           [Date] DATETIME NOT NULL DEFAULT(DATETIME('now', 'localtime')),
           [Sum] REAL,
           [Deleted] INTEGER NOT NULL DEFAULT '0',
           [Status] INTEGER NOT NULL DEFAULT '0',
           [Export] INTEGER NOT NULL DEFAULT '0');

        String[] people_name = {"Иван", "Марья", "Петр", "Антон", "Даша",
            "Борис", "Костя", "Игорь"};
        int[] people_posid = {2, 3, 2, 2, 3, 1, 2, 4};

        // данные для таблицы должностей
        int[] position_id = {1, 2, 3, 4};
        String[] position_name = {"Директор", "Программер", "Бухгалтер",
            "Охранник"};
        int[] position_salary = {15000, 13000, 10000, 8000};

        ContentValues cv = new ContentValues();

        // создаем таблицу должностей
        db.execSQL("create table position (" + "id integer primary key,"
            + "name text, salary integer" + ");");

        // заполняем ее
        for (int i = 0; i < position_id.length; i++) {
            cv.clear();
            cv.put("id", position_id[i]);
            cv.put("name", position_name[i]);
            cv.put("salary", position_salary[i]);
            db.insert("position", null, cv);
        }

        // создаем таблицу людей
        db.execSQL("create table people ("
            + "id integer primary key autoincrement,"
            + "name text, posid integer);");

        // заполняем ее
        for (int i = 0; i < people_name.length; i++) {
            cv.clear();
            cv.put("name", people_name[i]);
            cv.put("posid", people_posid[i]);
            db.insert("people", null, cv);
        }
        */
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        logger.info(" --- onUpgrade database from " + oldVersion
            + " to " + newVersion + " version --- ");

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

    /*
    // запрос данных и вывод в лог
    public void writeStaff(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from people", null);
        logCursor(c, "Table people");
        c.close();

        c = db.rawQuery("select * from position", null);
        logCursor(c, "Table position");
        c.close();

        String sqlQuery = "select PL.name as Name, PS.name as Position, salary as Salary "
            + "from people as PL "
            + "inner join position as PS "
            + "on PL.posid = PS.id ";
        c = db.rawQuery(sqlQuery, null);
        logCursor(c, "inner join");
        c.close();
    }

    // вывод в лог данных из курсора
    public void logCursor(Cursor c, String title) {
        if (c != null) {
            if (c.moveToFirst()) {
                logger.info(title + ". " + c.getCount() + " rows");
                StringBuilder sb = new StringBuilder();
                do {
                    sb.setLength(0);
                    for (String cn : c.getColumnNames()) {
                        sb.append(cn + " = " + c.getString(c.getColumnIndex(cn)) + "; ");
                    }
                    logger.info(sb.toString());
                } while (c.moveToNext());
            }
        } else {
            logger.info(title + ". Cursor is null");
        }
    }
    */

    //Создаст базу, если она не создана
    public void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            this.getReadableDatabase();
        } else {
            logger.info("Database already exists");
        }
    }

    //Проверка существования базы данных
    private boolean checkDataBase() {
        SQLiteDatabase checkDb = null;
        try {
            String path = DB_NAME;
            checkDb = openDataBase(path, true);
        } catch (SQLException e) {
            logger.info("Error while checking db");
        }
        //Андроид не любит утечки ресурсов, все должно закрываться
        if (checkDb != null) {
            checkDb.close();
        }
        return checkDb != null;
    }

    public SQLiteDatabase openDataBase(String path, boolean readOnly) throws SQLException {
        return SQLiteDatabase.openDatabase(path, null,
            readOnly ? SQLiteDatabase.OPEN_READONLY : SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
            database = null;
        }
        super.close();
    }
}
