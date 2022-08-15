package de.ceskilia.schoolbot.util;

import java.util.Calendar;

public final class SystemInfo {

    private SystemInfo() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

    public static String getVendorName() {
        return System.getProperty("java.vendor");
    }

    public static String getVendorURL() {
        return System.getProperty("java.vendor.url");
    }

    public static String getOperatingSystemName() {
        return System.getProperty("os.name");
    }
    
    public static int currentYearsPrefix() {
        return Calendar.getInstance().get(Calendar.YEAR) / 100;
    }

}
