package wangdaye.com.geometricweather.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;

/**
 * Time utils.
 * */

public class TimeUtils {
    // data
    public boolean isDay;

    /** <br> data. */

    private TimeUtils(Context context) {
        getLastDayTime(context);
    }

    public TimeUtils getDayTime(Context context, Weather weather, boolean writeToPreference) {
        int time = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                + Calendar.getInstance().get(Calendar.MINUTE);

        if (weather != null) {
            int sr = 60 * Integer.parseInt(weather.dailyList.get(0).exchangeTimes[0].split(":")[0])
                    + Integer.parseInt(weather.dailyList.get(0).exchangeTimes[0].split(":")[1]);
            int ss = 60 * Integer.parseInt(weather.dailyList.get(0).exchangeTimes[1].split(":")[0])
                    + Integer.parseInt(weather.dailyList.get(0).exchangeTimes[1].split(":")[1]);

            isDay = sr < time && time <= ss;
        } else {
            int sr = 60 * 6;
            int ss = 60 * 18;

            isDay = sr < time && time <= ss;
        }

        if (writeToPreference) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean(context.getString(R.string.key_isDay), isDay);
            editor.apply();
        }

        return this;
    }

    private TimeUtils getLastDayTime(Context context) {
        isDay = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(
                        context.getString(R.string.key_isDay),
                        true);
        return this;
    }

    /** <br> singleton. */

    private static TimeUtils instance;

    public static synchronized TimeUtils getInstance(Context context) {
        synchronized (TimeUtils.class) {
            if (instance == null) {
                instance = new TimeUtils(context);
            }
        }
        return instance;
    }
}
