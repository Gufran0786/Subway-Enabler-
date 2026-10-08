package com.gufran.subwayenabler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileOps {

    // Game ka target folder — Subway Surfers profile
    public static final String GAME_DIR =
        "/data/data/com.kiloo.subwaysurf/files/profile";

    // Base folder SD card pe
    public static final String BASE_DIR = "/sdcard/SubwayEnabler";

    // Subfolders
    public static final String UNLOCKED_DIR = BASE_DIR + "/unlocked";
    public static final String ORIGINAL_DIR = BASE_DIR + "/original";
    public static final String BACKUP_DIR   = BASE_DIR + "/backup";

    /** Root shell command chalao. Exit code 0 = success. */
    public static boolean root(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Root check: file ya folder exist karta hai. */
    public static boolean rootExists(String path) {
        try {
            Process p = Runtime.getRuntime().exec(
                new String[]{"su", "-c", "test -e " + path + " && echo Y"});
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            String line = br.readLine();
            p.waitFor();
            return "Y".equals(line);
        } catch (Exception e) {
            return false;
        }
    }

    /** SD card pe base folders banao. */
    public static void ensureBaseDirs() {
        root("mkdir -p " + UNLOCKED_DIR);
        root("mkdir -p " + ORIGINAL_DIR);
        root("mkdir -p " + BACKUP_DIR);
    }

    /**
     * Pehli baar app khulte hi game ka current profile ORIGINAL_DIR me copy karo.
     * Agar pehle se backup hai, skip karo.
     */
    public static boolean backupOriginalOnce() {
        // Agar original folder me index file hai, matlab backup pehle ho chuka
        if (rootExists(ORIGINAL_DIR + "/index")) return true;

        // Purana wala clear karo, naya backup
        root("rm -rf " + ORIGINAL_DIR);
        root("mkdir -p " + ORIGINAL_DIR);

        boolean ok = root("cp -r " + GAME_DIR + "/. " + ORIGINAL_DIR);
        return ok;
    }

    /** File enable karo — unlocked se game folder me copy. */
    public static boolean enableFile(String fileName) {
        String src = UNLOCKED_DIR + "/" + fileName;
        String dst = GAME_DIR + "/" + fileName;

        // Source file exist karta hai?
        if (!rootExists(src)) return false;

        // Game band karo pehle
        root("am force-stop com.kiloo.subwaysurf");

        // Copy + permission
        boolean ok = root("cp " + src + " " + dst);
        root("chmod 660 " + dst);
        return ok;
    }

    /** File disable karo — original se game folder me copy. */
    public static boolean disableFile(String fileName) {
        String src = ORIGINAL_DIR + "/" + fileName;
        String dst = GAME_DIR + "/" + fileName;

        if (!rootExists(src)) return false;

        root("am force-stop com.kiloo.subwaysurf");

        boolean ok = root("cp " + src + " " + dst);
        root("chmod 660 " + dst);
        return ok;
    }

    /** Proxy ON: poora profile folder unlocked se replace. */
    public static boolean enableProxy() {
        if (!rootExists(UNLOCKED_DIR)) return false;

        root("am force-stop com.kiloo.subwaysurf");
        root("rm -rf " + GAME_DIR);
        root("mkdir -p " + GAME_DIR);

        boolean ok = root("cp -r " + UNLOCKED_DIR + "/. " + GAME_DIR);
        root("chmod -R 771 " + GAME_DIR);
        return ok;
    }

    /** Proxy OFF: poora profile folder original se restore. */
    public static boolean disableProxy() {
        if (!rootExists(ORIGINAL_DIR)) return false;

        root("am force-stop com.kiloo.subwaysurf");
        root("rm -rf " + GAME_DIR);
        root("mkdir -p " + GAME_DIR);

        boolean ok = root("cp -r " + ORIGINAL_DIR + "/. " + GAME_DIR);
        root("chmod -R 771 " + GAME_DIR);
        return ok;
    }

    /** Manual backup — timestamp ke saath. */
    public static String manualBackup() {
        String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(new Date());
        String dst = BACKUP_DIR + "/backup_" + ts;

        boolean ok = root("cp -r " + GAME_DIR + " " + dst);
        return ok ? "backup_" + ts : null;
    }

    /** Latest backup ko restore karo. */
    public static boolean restoreLatest() {
        try {
            // Newest backup folder ka naam lo
            Process p = Runtime.getRuntime().exec(
                new String[]{"su", "-c", "ls -1t " + BACKUP_DIR});
            BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream()));
            String latest = br.readLine();
            p.waitFor();

            if (latest == null || latest.isEmpty()) return false;

            String src = BACKUP_DIR + "/" + latest;

            root("am force-stop com.kiloo.subwaysurf");
            root("rm -rf " + GAME_DIR);
            root("cp -r " + src + " " + GAME_DIR);
            root("chmod -R 771 " + GAME_DIR);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
  }
