package in.trujobs.dev.trudroid.Util;

import android.util.Log;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;

public class Tlog {
    private Tlog() {
    }

    public static final String LOG_TAG = "Trudroid";

    public static boolean isLoggable(int level) {
        return Log.isLoggable(LOG_TAG, level);
    }

    public static void v(String format, Object... args) {
        Log.v(LOG_TAG, buildLogMsg(format, args));
    }

    public static void d(Throwable tr, String format, Object... args) {
        Log.d(LOG_TAG, buildLogMsg(format, args), tr);
    }

    public static void d(String format, Object... args) {
        Log.d(LOG_TAG, buildLogMsg(format, args));
    }

    public static void i(String format, Object... args) {
        Log.i(LOG_TAG, buildLogMsg(format, args));
    }

    public static void w(Throwable tr, String format, Object... args) {
        Log.w(LOG_TAG, buildLogMsg(format, args), tr);
    }

    public static void w(String format, Object... args) {
        Log.w(LOG_TAG, buildLogMsg(format, args));
    }

    public static void e(Throwable tr, String format, Object... args) {
        Log.e(LOG_TAG, buildLogMsg(format, args), tr);
    }

    public static void e(String format, Object... args) {
        Log.e(LOG_TAG, buildLogMsg(format, args));
    }

    public static void wtf(Throwable tr, String format, Object... args) {
        Log.wtf(LOG_TAG, buildLogMsg(format, args), tr);
    }

    public static void wtf(String format, Object... args) {
        Log.wtf(LOG_TAG, buildLogMsg(format, args));
    }

    private static String buildLogMsg(String formatString, Object... args) {
        String message;
        try {
            message = (args == null || args.length == 0) ? formatString
                    : String.format(Locale.US, formatString, args);
        } catch (IllegalFormatException e) {
            String formattedArgs = Arrays.toString(args);
            e(LOG_TAG, e, "message: \"%s\" arguments: %s", formatString, formattedArgs);
            message = formatString + " " + formattedArgs;
        }
        StackTraceElement[] trace = new Throwable().fillInStackTrace().getStackTrace();

        String caller = trace[1].getClassName() + "." + trace[1].getMethodName();
        return String.format(Locale.US, "[%d] %s: %s",
                Thread.currentThread().getId(), caller, message);
    }
}
