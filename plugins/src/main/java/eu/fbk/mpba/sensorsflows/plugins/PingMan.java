package eu.fbk.mpba.sensorsflows.plugins;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PingMan {
    public static boolean isHttpColonSlashSlashWwwDotEmpaticaDotComReachable(Context context) {
        return isUrlReachable(context, "http://www.empatica.com/");
    }

    public static boolean isUrlReachable(Context context, String url) {
        boolean ret;
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(url).openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(3000);
                urlc.connect();
                ret = (urlc.getResponseCode() > 99);
            } catch (IOException e) {
                Log.e("ALE PINGMAN", "Error checking internet connection");
                ret = false;
            }
        } else {
            Log.d("ALE PINGMAN", "No network available!");
            ret = false;
        }
        return ret;
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } else
            return false;
    }
}