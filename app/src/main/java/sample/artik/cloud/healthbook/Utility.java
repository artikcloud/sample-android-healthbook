package sample.artik.cloud.healthbook;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import cloud.artik.client.ApiException;

public class Utility {
    private static final String TAG = Utility.class.getSimpleName();
    private static final String ACCESS_TOKEN = "accessToken";

    public static String getAccessToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(ACCESS_TOKEN, null);
    }

    public static void setAccessToken(String accessToken, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(ACCESS_TOKEN, accessToken).commit();
    }

    public static String getKeyValue(String key, Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
    }

    public static void setKeyValue(String key, String value, Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit();
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    static String getMapField(Map<String, Object> data, String field) {
        return data.get(field).toString();
    }

    static String getMapSubField(Map<String, Object> data, String field, String subField) {
        Log.v(TAG, "getMapSubField: " + data + "\n, " + field + "\n, " + subField);
        Map<String, Object> subMap = (Map<String, Object>) data.get(field);
        return getMapField(subMap, subField);
    }

    static void setText(View view, int viewId, String text) {
        Log.v(TAG, "setText :" + viewId + ", " + text);
        TextView textView = (TextView) view.findViewById(viewId);
        textView.setText(text);
    }

    static void showError(final ApiException exc, final Activity activity) {
        Log.e(TAG, exc.getResponseBody(), exc);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CharSequence text = "Artik Cloud Error: " + exc.getResponseBody();
                int duration = Toast.LENGTH_LONG;

                Toast toast = Toast.makeText(activity.getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }

    static void showMessage(final String text, final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(activity.getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }
}