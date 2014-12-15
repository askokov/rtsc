package com.askokov.rtsc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
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
        logger.info("ContactUtil: retrieve data for number<" + sourcePhoneNumber + ">");

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
                logger.info("ContactUtil: cursor is EMPTY for number<" + sourcePhoneNumber + ">");
            }
            cursor.close();
        } else {
            logger.info("ContactUtil: cursor is NULL for number<" + sourcePhoneNumber + ">");
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
            info.setAppname(p.applicationInfo.loadLabel(context.getPackageManager()).toString());
            info.setPname(p.packageName);
            info.setVersionName(p.versionName);

            res.add(info);
        }
        return res;
    }
}
