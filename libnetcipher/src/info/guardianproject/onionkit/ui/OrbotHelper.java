
package info.guardianproject.onionkit.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import info.guardianproject.onionkit.R;

import java.util.List;

public class OrbotHelper {

    private final static int REQUEST_CODE_STATUS = 100;

    public final static String ORBOT_PACKAGE_NAME = "org.torproject.android";
    public final static String ORBOT_MARKET_URI = "market://details?id=" + ORBOT_PACKAGE_NAME;
    public final static String ORBOT_FDROID_URI = "https://f-droid.org/repository/browse/?fdid="
            + ORBOT_PACKAGE_NAME;
    public final static String ORBOT_PLAY_URI = "https://play.google.com/store/apps/details?id="
            + ORBOT_PACKAGE_NAME;

    public final static String TOR_BIN_PATH = "/data/data/" + ORBOT_PACKAGE_NAME + "/app_bin/tor";

    public final static String ACTION_START_TOR = "org.torproject.android.START_TOR";
    public final static String ACTION_REQUEST_HS = "org.torproject.android.REQUEST_HS_PORT";
    public final static int HS_REQUEST_CODE = 9999;

    private final static String FDROID_PACKAGE_NAME = "org.fdroid.fdroid";
    private final static String PLAY_PACKAGE_NAME = "com.android.vending";

    private Context mContext = null;

    public OrbotHelper(Context context)
    {
        mContext = context;
    }

    public static boolean isOrbotRunning() {
        int procId = TorServiceUtils.findProcessId(TOR_BIN_PATH);

        return (procId != -1);
    }

    public static boolean isOrbotInstalled(Context context) {
        return isAppInstalled(context, ORBOT_PACKAGE_NAME);
    }

    public boolean isOrbotInstalled() {
        return isAppInstalled(mContext, ORBOT_PACKAGE_NAME);
    }

    private static boolean isAppInstalled(Context context, String uri) {
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Ask the user whether to install Orbot or not. Check if installing from
     * F-Droid or Google Play, otherwise take the user to the Orbot download
     * page on f-droid.org.
     */
    public static void promptToInstall(final Activity activity) {
        String message = activity.getString(R.string.you_must_have_orbot) + "  ";
        activity.getString(R.string.get_orbot_from_google_play);

        final Intent intent = getOrbotInstallIntent(activity);
        if (intent.getPackage() == null) {
            message += activity.getString(R.string.download_orbot_from_fdroid);
        } else {
            message += activity.getString(R.string.get_orbot_from_fdroid);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.install_orbot_);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // nothing to do
            }
        });
        builder.show();
    }

    public static void requestOrbotStart(final Activity activity) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(R.string.start_orbot_);
        downloadDialog
                .setMessage(R.string.orbot_doesn_t_appear_to_be_running_would_you_like_to_start_it_up_and_connect_to_tor_);
        downloadDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                activity.startActivityForResult(getOrbotStartIntent(), 1);
            }
        });
        downloadDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        downloadDialog.show();

    }

    public static void requestHiddenServiceOnPort(Activity activity, int port) {
        Intent intent = new Intent(ACTION_REQUEST_HS);
        intent.setPackage(ORBOT_PACKAGE_NAME);
        intent.putExtra("hs_port", port);

        activity.startActivityForResult(intent, HS_REQUEST_CODE);
    }

    public static Intent getOrbotStartIntent() {
        Intent intent = new Intent(ACTION_START_TOR);
        intent.setPackage(ORBOT_PACKAGE_NAME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public static Intent getOrbotInstallIntent(Context context) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(ORBOT_MARKET_URI));

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resInfos = pm.queryIntentActivities(intent, 0);

        String foundPackageName = null;
        for (ResolveInfo r : resInfos) {
            Log.i("OrbotHelper", "market: " + r.activityInfo.packageName);
            if (TextUtils.equals(r.activityInfo.packageName, FDROID_PACKAGE_NAME)
                    || TextUtils.equals(r.activityInfo.packageName, PLAY_PACKAGE_NAME)) {
                foundPackageName = r.activityInfo.packageName;
                break;
            }
        }

        if (foundPackageName == null) {
            intent.setData(Uri.parse(ORBOT_FDROID_URI));
        } else {
            intent.setPackage(foundPackageName);
        }
        return intent;
    }
}
