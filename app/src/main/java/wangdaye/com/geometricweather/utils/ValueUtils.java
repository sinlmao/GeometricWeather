package wangdaye.com.geometricweather.utils;

import android.content.Context;

import wangdaye.com.geometricweather.R;

/**
 * Value utils.
 * */

public class ValueUtils {

    public static String getForecastType(Context c, String value) {
        switch (value) {
            case "simple_forecast":
                return c.getResources().getStringArray(R.array.forecast_types)[0];

            default:
                return c.getResources().getStringArray(R.array.forecast_types)[1];
        }
    }

    public static String getRefreshTime(Context c, String value) {
        switch (value) {
            case "1hour":
                return c.getResources().getStringArray(R.array.refresh_times)[0];

            case "2hours":
                return c.getResources().getStringArray(R.array.refresh_times)[1];

            case "3hours":
                return c.getResources().getStringArray(R.array.refresh_times)[2];

            default:
                return c.getResources().getStringArray(R.array.refresh_times)[3];
        }
    }

    public static String getNotificationTextColor(Context c, String value) {
        switch (value) {
            case "dark":
                return c.getResources().getStringArray(R.array.notification_text_colors)[0];

            case "grey":
                return c.getResources().getStringArray(R.array.notification_text_colors)[1];

            default:
                return c.getResources().getStringArray(R.array.notification_text_colors)[2];
        }
    }
}
