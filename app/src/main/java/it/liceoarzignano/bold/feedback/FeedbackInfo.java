package it.liceoarzignano.bold.feedback;

import android.content.Context;
import android.os.Build;

import java.util.Arrays;
import java.util.Locale;

import it.liceoarzignano.bold.BuildConfig;
import it.liceoarzignano.bold.utils.PrefsUtils;

final class FeedbackInfo {

    private FeedbackInfo() {
    }

    private static String hardware() {
        String info = "\n\nHardware info\n-----\n " +
                "<table>\n " +
                "<tr><td>Brand</td><td>%1$s</td></tr>\n" +
                "<tr><td>Manufacturer</td><td>%2$s</td></tr>\n" +
                "<tr><td>Model</td><td>%3$s</td></tr>\n" +
                "<tr><td>Device</td><td>%4$s</td></tr>\n" +
                "<tr><td>Product</td><td>%5$s</td></tr>\n" +
                "<tr><td>Board</td><td>%6$s</td></tr>\n" +
                "<tr><td>ABI</td><td>%7$s</td></tr>\n" +
                "</table>\n\n";
        String[] abi = PrefsUtils.isNotLegacy() ? Build.SUPPORTED_ABIS :
                new String[] { Build.CPU_ABI, Build.CPU_ABI2 };

        return String.format(Locale.getDefault(), info, Build.BRAND, Build.MANUFACTURER,
                Build.MODEL, Build.DEVICE, Build.PRODUCT, Build.BOARD, Arrays.toString(abi));
    }

    private static String software() {
        String info = "Software info\n-----\n " +
                "<table>\n " +
                "<tr><td>Release</td><td>%1$s</td></tr>\n" +
                "<tr><td>SDK</td><td>%2$s</td></tr>\n" +
                "<tr><td>Security patch</td><td>%3$s</td></tr>\n" +
                "<tr><td>Fingerprint</td><td>%4$s</td></tr>\n" +
                "<tr><td>Display</td><td>%5$s</td></tr>\n" +
                "</table>\n\n";

        String patch = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                Build.VERSION.SECURITY_PATCH : "Unknown";

        return String.format(Locale.getDefault(), info, Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT, patch, Build.FINGERPRINT, Build.DISPLAY);
    }

    private static String app(Context ctx) {
        String info = "App info\n-----\n " +
                "<table>\n " +
                "<tr><td>Version</td><td>%1$s</td></tr>\n" +
                "<tr><td>Address</td><td>%2$s</td></tr>\n" +
                "<tr><td>Teacher</td><td>%3$s</td></tr>\n" +
                "<tr><td>Safe status</td><td>%4$s</td></tr>\n" +
                "<tr><td>Safe enabled</td><td>%5$s</td></tr>\n" +
                "</table>\n\n";

        return String.format(Locale.getDefault(), info, BuildConfig.VERSION_CODE,
                PrefsUtils.getAddress(ctx), PrefsUtils.isTeacher(ctx), PrefsUtils.hasSafe(ctx),
                PrefsUtils.hasPassedSafetyNetTest(ctx));
    }

    static String getInfo(Context context) {
        return hardware() + software() + app(context);
    }
}
