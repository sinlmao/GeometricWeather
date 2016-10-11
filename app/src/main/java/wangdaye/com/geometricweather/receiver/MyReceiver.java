package wangdaye.com.geometricweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.service.notification.NotificationService;
import wangdaye.com.geometricweather.service.notification.TimeService;
import wangdaye.com.geometricweather.service.notification.TodayForecastService;
import wangdaye.com.geometricweather.service.notification.TomorrowForecastService;

/**
 * My receiver.
 * */

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        switch (action) {
            case "android.intent.action.BOOT_COMPLETED":
                // power on the phone
                if (sharedPreferences.getBoolean(context.getString(R.string.key_notification), false)) {
                    Intent notificationService = new Intent(context, NotificationService.class);
                    context.startService(notificationService);
                }

                if (sharedPreferences.getBoolean(context.getString(R.string.key_forecast_today), false)
                        || sharedPreferences.getBoolean(context.getString(R.string.key_forecast_today), false)) {
                    Intent timerService = new Intent(context, TimeService.class);
                    context.startService(timerService);
                }
                break;

            case Intent.ACTION_TIME_TICK:
            case Intent.ACTION_TIME_CHANGED:
                this.sendForecast(context, sharedPreferences);
                break;
        }
    }

    private void sendForecast(Context context, SharedPreferences sharedPreferences) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (sharedPreferences.getBoolean(context.getString(R.string.key_forecast_today), false)) {
            String[] time = sharedPreferences.getString(context.getString(R.string.key_forecast_today_time), "07:00").split(":");
            if (hour == Integer.parseInt(time[0]) && minute == Integer.parseInt(time[1])) {
                Intent intentForecast = new Intent(context, TodayForecastService.class);
                context.startService(intentForecast);
            }
        }

        if (sharedPreferences.getBoolean(context.getString(R.string.key_forecast_tomorrow), false)) {
            String[] time = sharedPreferences.getString(context.getString(R.string.key_forecast_tomorrow_time), "21:00").split(":");
            if (hour == Integer.parseInt(time[0]) && minute == Integer.parseInt(time[1])) {
                Intent intentForecast = new Intent(context, TomorrowForecastService.class);
                context.startService(intentForecast);
            }
        }
    }
}
