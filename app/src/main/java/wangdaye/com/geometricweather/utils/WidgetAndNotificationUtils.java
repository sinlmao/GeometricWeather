package wangdaye.com.geometricweather.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.view.activity.MainActivity;

/**
 * Widget and notification utils.
 * */

public class WidgetAndNotificationUtils {
    // data
    public static final int NOTIFICATION_ID = 7;
    public static final int FORECAST_ID = 9;

    /** <br> time. */

    public static int getWidgetRefreshHours(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String hourText = sharedPreferences.getString(
                context.getString(R.string.key_widget_refresh_time),
                "2hours");
        return WidgetAndNotificationUtils.HoursTextToHours(hourText);
    }

    public static int getNotificationRefreshHours(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String hourText = sharedPreferences.getString(
                context.getString(R.string.key_notification_refresh_time),
                "2hours");
        return WidgetAndNotificationUtils.HoursTextToHours(hourText);
    }

    private static int HoursTextToHours(String key) {
        switch (key) {
            case "1hour":
                return 1;

            case "2hours":
                return 2;

            case "3hours":
                return 3;

            case "4hours":
                return 4;

            default:
                return 2;
        }
    }

    /** <br> feedback */

    public static void refreshWidgetFailed(Context c) {
        Toast.makeText(c,
                c.getString(R.string.feedback_refresh_widget_failed),
                Toast.LENGTH_SHORT).show();
    }

    public static void sendNotificationFailed(Context c) {
        Toast.makeText(c,
                c.getString(R.string.feedback_send_notification_failed),
                Toast.LENGTH_SHORT).show();
    }

    /** <br> builder. */

    public static void sendNotification(Context context, Weather weather, boolean isService) {
        if (weather == null) {
            return;
        }

        // get sp & weather.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // get time & background color.
        boolean isDay = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDay;
        boolean backgroundColor = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_background),
                false);

        // get text color.
        String textColor = sharedPreferences.getString(
                context.getString(R.string.key_notification_text_color),
                "grey");
        int mainColor;
        int subColor;
        switch (textColor) {
            case "dark":
                mainColor = ContextCompat.getColor(context, R.color.colorTextDark);
                subColor = ContextCompat.getColor(context, R.color.colorTextDark2nd);
                break;
            case "grey":
                mainColor = ContextCompat.getColor(context, R.color.colorTextGrey);
                subColor = ContextCompat.getColor(context, R.color.colorTextGrey2nd);
                break;
            case "light":
            default:
                mainColor = ContextCompat.getColor(context, R.color.colorTextLight);
                subColor = ContextCompat.getColor(context, R.color.colorTextLight2nd);
                break;
        }

        // get manager & builder.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // set notification level.
        if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_icon), false)) {
            builder.setPriority(NotificationCompat.PRIORITY_MIN);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        // set notification visibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_in_lockScreen), false)) {
                builder.setVisibility(Notification.VISIBILITY_SECRET);
            } else {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
        }

        // set small icon.
        builder.setSmallIcon(
                WeatherUtils.getMiniWeatherIcon(
                        WeatherUtils.getWeatherKind(weather.live.weather),
                        isDay));

        // build base view.
        RemoteViews base = new RemoteViews(context.getPackageName(), R.layout.notification_base);
        int[] imageId = WeatherUtils.getWeatherIcon( // get icon resource id.
                WeatherUtils.getWeatherKind(weather.live.weather),
                isDay);
        base.setImageViewResource( // set icon.
                R.id.notification_base_icon,
                imageId[3]);
        base.setTextViewText( // set title.
                R.id.notification_base_title,
                weather.live.weather + " " + weather.live.temp + "℃");
        base.setTextViewText( // set content.
                R.id.notification_base_content,
                weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        base.setTextViewText( // set time.
                R.id.notification_base_time, weather.base.location + "." + weather.base.refreshTime);
        if (backgroundColor) { // set background.
            base.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
        } else {
            base.setViewVisibility(R.id.notification_base_background, View.GONE);
        }
         // set text color.
        base.setTextColor(R.id.notification_base_title, mainColor);
        base.setTextColor(R.id.notification_base_content, subColor);
        base.setTextColor(R.id.notification_base_time, subColor);
        builder.setContent(base); // commit.
        // set click intent.
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // build big view.
        RemoteViews big = new RemoteViews(context.getPackageName(), R.layout.notification_big);
        // today
        imageId = WeatherUtils.getWeatherIcon( // get icon resource id.
                WeatherUtils.getWeatherKind(weather.live.weather),
                isDay);
        big.setImageViewResource( // set icon.
                R.id.notification_base_icon,
                imageId[3]);
        big.setTextViewText( // set title.
                R.id.notification_base_title,
                weather.live.weather + " " + weather.live.temp + "℃");
        big.setTextViewText( // set content.
                R.id.notification_base_content,
                weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        big.setTextViewText( // set time.
                R.id.notification_base_time, weather.base.location + "." + weather.base.refreshTime);
        big.setViewVisibility(R.id.notification_base_background, View.GONE);
        // 1
        big.setTextViewText( // set week 1.
                R.id.notification_big_week_1,
                context.getString(R.string.today));
        big.setTextViewText( // set temps 1.
                R.id.notification_big_temp_1,
                weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        imageId = WeatherUtils.getWeatherIcon( // get icon 1 resource id.
                isDay ?
                        WeatherUtils.getWeatherKind(weather.dailyList.get(0).weathers[0])
                        :
                        WeatherUtils.getWeatherKind(weather.dailyList.get(0).weathers[1]),
                isDay);
        big.setImageViewResource( // set icon 1.
                R.id.notification_big_icon_1,
                imageId[3]);
        // 2
        big.setTextViewText( // set week 2.
                R.id.notification_big_week_2,
                weather.dailyList.get(1).week);
        big.setTextViewText( // set temps 2.
                R.id.notification_big_temp_2,
                weather.dailyList.get(1).temps[1] + "/" + weather.dailyList.get(1).temps[0] + "°");
        imageId = WeatherUtils.getWeatherIcon( // get icon 1 resource id.
                isDay ?
                        WeatherUtils.getWeatherKind(weather.dailyList.get(1).weathers[0])
                        :
                        WeatherUtils.getWeatherKind(weather.dailyList.get(1).weathers[1]),
                isDay);
        big.setImageViewResource( // set icon 2.
                R.id.notification_big_icon_2,
                imageId[3]);
        // 3
        big.setTextViewText( // set week 3.
                R.id.notification_big_week_3,
                weather.dailyList.get(2).week);
        big.setTextViewText( // set temps 3.
                R.id.notification_big_temp_3,
                weather.dailyList.get(2).temps[1] + "/" + weather.dailyList.get(2).temps[0] + "°");
        imageId = WeatherUtils.getWeatherIcon( // get icon 3 resource id.
                isDay ?
                        WeatherUtils.getWeatherKind(weather.dailyList.get(2).weathers[0])
                        :
                        WeatherUtils.getWeatherKind(weather.dailyList.get(2).weathers[1]),
                isDay);
        big.setImageViewResource( // set icon 3.
                R.id.notification_big_icon_3,
                imageId[3]);
        // 4
        big.setTextViewText( // set week 4.
                R.id.notification_big_week_4,
                weather.dailyList.get(3).week);
        big.setTextViewText( // set temps 4.
                R.id.notification_big_temp_4,
                weather.dailyList.get(3).temps[1] + "/" + weather.dailyList.get(3).temps[0] + "°");
        imageId = WeatherUtils.getWeatherIcon( // get icon 4 resource id.
                isDay ?
                        WeatherUtils.getWeatherKind(weather.dailyList.get(3).weathers[0])
                        :
                        WeatherUtils.getWeatherKind(weather.dailyList.get(3).weathers[1]),
                isDay);
        big.setImageViewResource( // set icon 4.
                R.id.notification_big_icon_4,
                imageId[3]);
        // 5
        big.setTextViewText( // set week 5.
                R.id.notification_big_week_5,
                weather.dailyList.get(4).week);
        big.setTextViewText( // set temps 5.
                R.id.notification_big_temp_5,
                weather.dailyList.get(4).temps[1] + "/" + weather.dailyList.get(4).temps[0] + "°");
        imageId = WeatherUtils.getWeatherIcon( // get icon 5 resource id.
                isDay ?
                        WeatherUtils.getWeatherKind(weather.dailyList.get(4).weathers[0])
                        :
                        WeatherUtils.getWeatherKind(weather.dailyList.get(4).weathers[1]),
                isDay);
        big.setImageViewResource( // set icon 5.
                R.id.notification_big_icon_5,
                imageId[3]);
        // set text color.
        big.setTextColor(R.id.notification_base_title, mainColor);
        big.setTextColor(R.id.notification_base_content, subColor);
        big.setTextColor(R.id.notification_base_time, subColor);
        big.setTextColor(R.id.notification_big_week_1, subColor);
        big.setTextColor(R.id.notification_big_week_2, subColor);
        big.setTextColor(R.id.notification_big_week_3, subColor);
        big.setTextColor(R.id.notification_big_week_4, subColor);
        big.setTextColor(R.id.notification_big_week_5, subColor);
        big.setTextColor(R.id.notification_big_temp_1, subColor);
        big.setTextColor(R.id.notification_big_temp_2, subColor);
        big.setTextColor(R.id.notification_big_temp_3, subColor);
        big.setTextColor(R.id.notification_big_temp_4, subColor);
        big.setTextColor(R.id.notification_big_temp_5, subColor);
        // set background.
        big.setViewVisibility(R.id.notification_base_background, View.GONE);
        if (backgroundColor) {
            big.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
            big.setViewVisibility(R.id.notification_big_background, View.VISIBLE);
        } else {
            big.setViewVisibility(R.id.notification_base_background, View.GONE);
            big.setViewVisibility(R.id.notification_big_background, View.GONE);
        }

        // set big view.
        builder.setCustomBigContentView(big);

        // get notification.
        Notification notification = builder.build();

        // sound and shock
        if (isService) {
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_sound), false)) {
                notification.defaults |= Notification.DEFAULT_SOUND;
            }
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_shock), false)) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }
        }

        // set clear flag
        if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_can_be_cleared), false)) {
            // the notification can be cleared
            notification.flags = Notification.FLAG_AUTO_CANCEL;
        } else {
            // the notification can not be cleared
            notification.flags = Notification.FLAG_ONGOING_EVENT;
        }

        // commit.
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    public static void sendForecast(Context context, Weather weather, boolean isToday) {
        // get sp & weather.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // get time & background color.
        boolean isDay = !isToday || TimeUtils.getInstance(context).getDayTime(context, weather, false).isDay;
        boolean backgroundColor = sharedPreferences.getBoolean(
                context.getString(R.string.key_notification_background),
                false);

        // get text color.
        String textColor = sharedPreferences.getString(
                context.getString(R.string.key_notification_text_color),
                "grey");
        int mainColor;
        int subColor;
        switch (textColor) {
            case "dark":
                mainColor = ContextCompat.getColor(context, R.color.colorTextDark);
                subColor = ContextCompat.getColor(context, R.color.colorTextDark2nd);
                break;

            case "grey":
                mainColor = ContextCompat.getColor(context, R.color.colorTextGrey);
                subColor = ContextCompat.getColor(context, R.color.colorTextGrey2nd);
                break;

            case "light":
            default:
                mainColor = ContextCompat.getColor(context, R.color.colorTextLight);
                subColor = ContextCompat.getColor(context, R.color.colorTextLight2nd);
                break;
        }

        // get manager & builder.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // set notification level.
        if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_icon), false)) {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        // set notification visibility.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedPreferences.getBoolean(context.getString(R.string.key_notification_hide_in_lockScreen), false)) {
                builder.setVisibility(Notification.VISIBILITY_SECRET);
            } else {
                builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            }
        }

        // set small icon.
        builder.setSmallIcon(
                WeatherUtils.getMiniWeatherIcon(
                        WeatherUtils.getWeatherKind(weather.live.weather),
                        isDay));

        // set view
        RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.notification_base);
        int[] imageId = WeatherUtils.getWeatherIcon( // get icon resource id.
                isDay ?
                        WeatherUtils.getWeatherKind(weather.dailyList.get(1).weathers[0])
                        :
                        WeatherUtils.getWeatherKind(weather.dailyList.get(1).weathers[1]),
                isDay);
        view.setImageViewResource( // set icon.
                R.id.notification_base_icon,
                imageId[3]);
        // build title.
        String title = isToday ?
                context.getString(R.string.today) + weather.base.location + " : "
                        + (isDay ? weather.dailyList.get(0).weathers[0] : weather.dailyList.get(0).weathers[1])
                        + " " + weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°"
                :
                context.getString(R.string.tomorrow) + weather.base.location + " : "
                        + weather.dailyList.get(1).weathers[0]
                        + " " + weather.dailyList.get(1).temps[1] + "/" + weather.dailyList.get(1).temps[0] + "°";
        view.setTextViewText( // set title.
                R.id.notification_base_title, title);
        // build content.
        String content = isToday ?
                context.getString(R.string.live) + " : "
                        + weather.live.weather + " " + weather.live.temp + "℃"
                :
                context.getString(R.string.today) + " : "
                        + weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°";
        switch (isToday ?
                        (isDay ?
                                WeatherUtils.getWeatherKind(weather.dailyList.get(0).weathers[0])
                                :
                                WeatherUtils.getWeatherKind(weather.dailyList.get(0).weathers[1]))
                        :
                        WeatherUtils.getWeatherKind(weather.dailyList.get(1).weathers[0])) {
            case "晴":
                content = isToday ?
                        content + " , 今日晴朗。" : content + " , 明日晴朗。";
                break;

            case "云":
                content = isToday?
                        content + " , 今日多云。" : content + " , 明日多云。";
                break;

            case "雨":
                content = content + " , 请备好雨伞。";
                break;

            case "风":
                content = content + " , 请做好防风措施。";
                break;

            case "雪":
                content = content + " , 请注意保暖防滑。";
                break;

            case "雾":
                content = content + " , 请注意慢行。";
                break;

            case "霾":
                content = content + " , 请注意呼吸道健康。";
                break;

            case "雨夹雪":
                content = content + " , 请备雨伞并注意保暖。";
                break;

            case "雷雨":
                content = content + " , 请注意雷雨。";
                break;

            case "雷":
                content = content + " , 请注意雷电。";
                break;

            case "冰雹":
                content = content + " , 请注意躲避。";
                break;

            case "阴":
            default:
                content = isToday ?
                        content + " , 今日天气阴沉。" : content + " , 明日天气阴沉。";
                break;
        }
        view.setTextViewText( // set content.
                R.id.notification_base_content,
                content);
        view.setTextViewText( // set time.
                R.id.notification_base_time,
                weather.base.location + "." + weather.base.refreshTime);
        if (backgroundColor) { // set background.
            view.setViewVisibility(R.id.notification_base_background, View.VISIBLE);
        } else {
            view.setViewVisibility(R.id.notification_base_background, View.GONE);
        }
        // set text color.
        view.setTextColor(R.id.notification_base_title, mainColor);
        view.setTextColor(R.id.notification_base_content, subColor);
        view.setTextColor(R.id.notification_base_time, subColor);
        builder.setContent(view);
        // set click intent.
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        // set sound & vibrate.
        Notification notification = builder.build();
        if(sharedPreferences.getBoolean(context.getString(R.string.key_notification_sound), false)) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
        if(sharedPreferences.getBoolean(context.getString(R.string.key_notification_shock), false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        // set clean flag.
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        // commit.
        notificationManager.notify(FORECAST_ID, notification);
    }

    public static String[] buildWidgetDayStyleText(Weather weather) {
        String[] texts = new String[] {
                weather.live.weather,
                weather.live.temp + "℃",
                weather.dailyList.get(0).temps[0] + "°",
                weather.dailyList.get(0).temps[1] + "°"};

        TextPaint paint = new TextPaint();
        float[] widths = new float[4];
        for (int i = 0; i < widths.length; i ++) {
            widths[i] = paint.measureText(texts[i]);
        }

        float maxiWidth = widths[0];
        for (float w : widths) {
            if (w > maxiWidth) {
                maxiWidth = w;
            }
        }

        while (true) {
            int flag = 0;

            for (int i = 0; i < 2; i ++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = texts[i] + " ";
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flag ++;
                }
            }
            for (int i = 2; i < 4; i ++) {
                if (widths[i] < maxiWidth) {
                    texts[i] = " " + texts[i];
                    widths[i] = paint.measureText(texts[i]);
                } else {
                    flag ++;
                }
            }

            if (flag == 4) {
                break;
            }
        }

        return new String[] {
                texts[0] + "\n" + texts[1],
                texts[2] + "\n" + texts[3]};
    }
}
