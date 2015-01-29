package com.askokov.rtsc.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

public class Func {
    private static final Logger logger = LoggerFactory.getLogger(Func.class);

    private Func() {
    }

    public static List<PInfo> getInstalledApps(Context context) {
        return getInstalledApps(context, false); /* false = no system packages */
    }

    public static void printIntent(String where, Intent intent) {
        logger.info(where + ": Action<" + (intent != null ? intent.getAction() : "null") + ">");
        if (intent != null) {
            logger.info(where + ": intent=" + intent);

            boolean extraReplacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            logger.info(where + ": EXTRA_REPLACING=" + extraReplacing);

            boolean extraRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
            logger.info(where + ": EXTRA_DATA_REMOVED=" + extraRemoved);

            Uri data = intent.getData();
            logger.info(where + ": data=" + data);

            if (data != null) {
                String pkgName = data.getEncodedSchemeSpecificPart();
                logger.info(where + "Func: pkgName=" + pkgName);
            }
        }
    }

    public static Map<String, String> getContactByPhone(Context context, String sourcePhoneNumber) {
        logger.info("Func: retrieve data for number<" + sourcePhoneNumber + ">");

        Map<String, String> map = new HashMap<String, String>();

        String phoneNumber = PhoneNumberUtils.stripSeparators(sourcePhoneNumber);

        String[] projection = new String[]{ContactsContract.Data.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.DISPLAY_NAME};

        String selection = "PHONE_NUMBERS_EQUAL(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ",?) AND " + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'";
        String[] selectionArgs = new String[]{phoneNumber};

        Cursor cursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);

        /*
        String[] projection = new String[] {
                        ContactsContract.Contacts._ID,
                        ContactsContract.Data.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Email.DATA,
                        ContactsContract.CommonDataKinds.Phone.DATA, }

        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        projection, null, null, order);
                int idColumn = cursor.getColumnIndex(ContactsContract.Data._ID);
                int nameColumn = cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
                int emailColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                int phoneColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);



        Cursor data = context.getContentResolver().query(
        Data.CONTENT_URI, new String[] { Data._ID,Data.MIMETYPE,
        Email.ADDRESS, Photo.PHOTO},Data.CONTACT_ID
        + "=?" + " AND " + "(" +  Data.MIMETYPE + "='"
        + Photo.CONTENT_ITEM_TYPE + "' OR " + Data.MIMETYPE
        + "='" + Email.CONTENT_ITEM_TYPE +"')",
        new String[] {String.valueOf(contactId)}, null);



        private String getContactNameFromNumber(String number) {
                Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                        Uri.encode(number));

                Cursor cursor = context.getContentResolver().query(uri,
                        new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
                if (cursor.moveToFirst()) {
                    name = cursor.getString(cursor
                            .getColumnIndex(PhoneLookup.DISPLAY_NAME));
                }

                return name;
                // proceed as you need

            }
         */

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        //map.put(cursor.getColumnName(i), cursor.getString(i));

                        String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                        String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        map.put("id", id);
                        map.put("number", number);
                        map.put("displayName", displayName);
                    }
                }

                /*
                while (cursor.moveToNext()) {
                }
                */
            } else {
                logger.info("Func: cursor is EMPTY for number<" + sourcePhoneNumber + ">");
            }
            cursor.close();
        } else {
            logger.info("Func: cursor is NULL for number<" + sourcePhoneNumber + ">");
        }

        return map;
    }

    private static List<PInfo> getInstalledApps(Context context, boolean getSysPackages) {
        List<PInfo> res = new ArrayList<PInfo>();
        List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);

        for (PackageInfo p : packs) {
            if ((!getSysPackages) && (p.versionName == null)) {
                continue;
            }
            PInfo info = new PInfo();
            info.setLabel(p.applicationInfo.loadLabel(context.getPackageManager()).toString());
            info.setPackageName(p.packageName);
            info.setVersionName(p.versionName);

            res.add(info);
        }
        return res;
    }

    public static Date truncateDate(Date date) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static void saveTime(PInfo info) {
        //Сохранить время
        info.setFullTime(info.getFullTime() + (System.currentTimeMillis() - info.getStartTime()));
        info.setStartTime(System.currentTimeMillis());

        logger.info("Func: saveTime for info - " + info.prettyPrint());
    }

    public static void saveTime(Collection<PInfo> infos) {
        for (PInfo info : infos) {
            saveTime(info);
        }
    }

    public static boolean saveConfiguration(SharedPreferences pref, Configuration configuration) {
        logger.info("Save configuration");

        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean(Constant.ADD_INSTALLED, configuration.isAddInstalled());
        ed.putString(Constant.REPORT_TYPE, configuration.getReportType().name());
        ed.putString(Constant.MAIL_TYPE, configuration.getMailType().name());
        return ed.commit();
    }

    public static Configuration loadConfiguration(SharedPreferences pref) {
        logger.info("Load configuration");

        Configuration configuration = new Configuration();
        configuration.setAddInstalled(pref.getBoolean(Constant.ADD_INSTALLED, false));
        configuration.setReportType(ReportType.valueOf(pref.getString(Constant.REPORT_TYPE, ReportType.NOW.name())));
        configuration.setMailType(MailType.valueOf(pref.getString(Constant.MAIL_TYPE, MailType.CLIENT.name())));

        return configuration;
    }

    public static List<PInfo> merge(StatHandler handler, List<PInfo> input, boolean selected) {
        Set<PInfo> mergedSet = new TreeSet<PInfo>();
        Set<PInfo> memorySet = new HashSet<PInfo>(handler.getApps());
        Set<PInfo> inputSet = new HashSet<PInfo>(input);

        if (memorySet.isEmpty()) {
            mergedSet.addAll(input);
        } else {

            for (PInfo mem : memorySet) {
                if (input.contains(mem)) {
                    mergedSet.add(mem);
                } else {
                    handler.setFlush(true);
                    logger.info("merge: application<" + mem.getPackageName() + "> was deleted");
                }
            }

            for (PInfo in : inputSet) {
                if (!memorySet.contains(in)) {
                    in.setStartTime(System.currentTimeMillis());
                    mergedSet.add(in);

                    if (selected) {
                        handler.setFlush(true);
                    }
                }
            }
        }

        initInfo(mergedSet);

        return new ArrayList<PInfo>(mergedSet);
    }

    public static void initInfo(Collection<PInfo> infos) {
        //Выставить всем текущую дату
        Date date = Func.truncateDate(new Date());

        for (PInfo info : infos) {
            info.setChecked(true);
            info.setDate(date);
        }
    }

    public static void mergeIdentifiers(List<PInfo> memoryList, List<PInfo> dbList) {
        for (PInfo db : dbList) {
            PInfo mem = findByPackage(memoryList, db.getPackageName());

            if (mem != null) {
                mem.setId(db.getId());
            }
        }
    }

    public static PInfo findByPackage(List<PInfo> list, String pName) {
        PInfo result = null;
        for (PInfo info : list) {
            if (pName.equals(info.getPackageName())) {
                result = info;
                break;
            }
        }

        return result;
    }


}
