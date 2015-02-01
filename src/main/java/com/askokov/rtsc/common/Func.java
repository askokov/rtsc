package com.askokov.rtsc.common;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
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

    public static boolean savePreferences(SharedPreferences pref, Configuration configuration) {
        logger.info("Save configuration");

        SharedPreferences.Editor ed = pref.edit();
        ed.putBoolean(Constant.ADD_INSTALLED, configuration.isAddInstalled());
        ed.putString(Constant.REPORT_TYPE, configuration.getReportType().name());
        ed.putString(Constant.MAIL_TYPE, configuration.getMailType().name());
        return ed.commit();
    }

    public static Configuration loadPreferences(SharedPreferences pref) {
        logger.info("Load configuration");

        Configuration configuration = new Configuration();
        configuration.setAddInstalled(pref.getBoolean(Constant.ADD_INSTALLED, false));
        configuration.setReportType(ReportType.valueOf(pref.getString(Constant.REPORT_TYPE, ReportType.NOW.name())));
        configuration.setMailType(MailType.valueOf(pref.getString(Constant.MAIL_TYPE, MailType.CLIENT.name())));

        return configuration;
    }

    public static List<PInfo> merge(StatHandler handler, List<PInfo> input, boolean saveList) {
        Set<PInfo> mergedSet = new TreeSet<PInfo>(new PInfoComparator());
        Set<PInfo> memorySet = new HashSet<PInfo>(handler.getApps());
        Set<PInfo> inputSet = new HashSet<PInfo>(input);

        if (memorySet.isEmpty()) {
            mergedSet.addAll(input);
            initInfo(mergedSet);
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

                    if (saveList) {
                        handler.setFlush(true);
                    }
                }
            }

            initInfo(mergedSet);
        }

        return new ArrayList<PInfo>(mergedSet);
    }

    public static void initInfo(Collection<PInfo> infos) {
        //Выставить всем текущую дату
        Date date = Func.truncateDate(new Date());

        for (PInfo info : infos) {
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

    public static void printInfo(Collection<PInfo> infos) {
        for (PInfo info : infos) {
            logger.info(info.prettyPrint());
        }
    }

    public static class PInfoComparator implements Comparator<PInfo> {

        @Override
        public int compare(final PInfo lhs, final PInfo rhs) {
            return lhs.getPackageName().compareTo(rhs.getPackageName());
        }
    }
}
