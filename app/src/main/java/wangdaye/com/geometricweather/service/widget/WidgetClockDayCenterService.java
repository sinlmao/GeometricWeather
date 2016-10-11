package wangdaye.com.geometricweather.service.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.AlarmClock;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import wangdaye.com.geometricweather.model.data.HefengResult;
import wangdaye.com.geometricweather.model.data.JuheResult;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.model.database.helper.DatabaseHelper;
import wangdaye.com.geometricweather.service.RecursionService;
import wangdaye.com.geometricweather.view.activity.MainActivity;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.receiver.widget.WidgetClockDayCenterProvider;
import wangdaye.com.geometricweather.utils.LocationUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.WeatherUtils;
import wangdaye.com.geometricweather.utils.WidgetAndNotificationUtils;

/**
 * Widget clock day center service.
 * */


public class WidgetClockDayCenterService extends RecursionService
        implements LocationUtils.OnRequestLocationListener, WeatherUtils.OnRequestWeatherListener {
    // data
    private static final int REQUEST_CODE = 1;

    /** <br> life cycle. */

    @Override
    public void readSettings() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.sp_widget_clock_day_center_setting), Context.MODE_PRIVATE);
        setLocation(
                new Location(
                        sharedPreferences.getString(
                                getString(R.string.key_location),
                                getString(R.string.local)),
                        null));
        Location location = DatabaseHelper.getInstance(this).searchLocation(getLocation());
        if (location != null) {
            setLocation(location);
        }
    }

    @Override
    public void doRefresh() {
        int[] widgetIds = AppWidgetManager.getInstance(this)
                .getAppWidgetIds(new ComponentName(this, WidgetClockDayCenterProvider.class));
        if (widgetIds != null && widgetIds.length != 0) {
            requestData(this, this);
            setAlarmIntent(getClass(), REQUEST_CODE, true);
        } else {
            stopSelf(getStartId());
        }
    }

    /** <br> widget. */

    public static void refreshWidgetView(Context context, Weather weather) {
        if (weather == null) {
            return;
        }

        // get settings & time.
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.sp_widget_clock_day_center_setting),
                Context.MODE_PRIVATE);
        boolean showCard = sharedPreferences.getBoolean(context.getString(R.string.key_show_card), false);
        boolean blackText = sharedPreferences.getBoolean(context.getString(R.string.key_black_text), false);
        boolean hideRefreshTime = sharedPreferences.getBoolean(context.getString(R.string.key_hide_refresh_time), false);
        boolean isDay = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDay;

        // get text color.
        int textColor;
        if (blackText || showCard) {
            textColor = ContextCompat.getColor(context, R.color.colorTextDark);
        } else {
            textColor = ContextCompat.getColor(context, R.color.colorTextLight);
        }

        // get remote views.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_clock_day_center);

        // build view.
        int[] imageId = WeatherUtils.getWeatherIcon( // get icon resource id.
                WeatherUtils.getWeatherKind(weather.live.weather),
                isDay);
        views.setImageViewResource( // set icon.
                R.id.widget_clock_day_center_icon,
                imageId[3]);
        // build weather & temps text.
        String[] texts = WidgetAndNotificationUtils.buildWidgetDayStyleText(weather);
        views.setTextViewText( // set weather.
                R.id.widget_clock_day_center_weather,
                texts[0]);
        views.setTextViewText( // set temps.
                R.id.widget_clock_day_center_temp,
                texts[1]);
        views.setTextViewText( // set time.
                R.id.widget_clock_day_center_refreshTime,
                weather.base.location + "." + weather.base.refreshTime);
        // set text color.
        views.setTextColor(R.id.widget_clock_day_center_clock, textColor);
        views.setTextColor(R.id.widget_clock_day_center_weather, textColor);
        views.setTextColor(R.id.widget_clock_day_center_temp, textColor);
        views.setTextColor(R.id.widget_clock_day_center_refreshTime, textColor);
        // set card visibility.
        views.setViewVisibility(R.id.widget_clock_day_center_card, showCard ? View.VISIBLE : View.GONE);
        // set refresh time visibility.
        views.setViewVisibility(R.id.widget_clock_day_center_refreshTime, hideRefreshTime ? View.GONE : View.VISIBLE);
        // set clock intent.
        Intent intentClock = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
        PendingIntent pendingIntentClock = PendingIntent.getActivity(context, 0, intentClock, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_center_clockButton, pendingIntentClock);
        // set weather intent.
        Intent intentWeather = new Intent(context, MainActivity.class);
        PendingIntent pendingIntentWeather = PendingIntent.getActivity(context, 0, intentWeather, 0);
        views.setOnClickPendingIntent(R.id.widget_clock_day_center_weatherButton, pendingIntentWeather);

        // commit.
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.updateAppWidget(
                new ComponentName(context, WidgetClockDayCenterProvider.class),
                views);
    }

    /** <br> interface. */

    // request location.

    @Override
    public void requestLocationSuccess(String locationName) {
        weatherUtils.requestWeather(locationName, this);
        getLocation().realLocation = locationName;
        DatabaseHelper.getInstance(this).insertLocation(getLocation());
    }

    @Override
    public void requestLocationFailed() {
        LocationUtils.simpleLocationFailedFeedback(this);
        refreshWidgetView(
                this,
                DatabaseHelper.getInstance(this).searchWeather(getLocation()));
        this.stopSelf(getStartId());
    }

    // request weather.

    @Override
    public void requestJuheWeatherSuccess(JuheResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        refreshWidgetView(this, weather);
        getLocation().weather = weather;
        DatabaseHelper.getInstance(this).insertWeather(getLocation());
        DatabaseHelper.getInstance(this).insertHistory(weather);
        this.stopSelf(getStartId());
    }

    @Override
    public void requestHefengWeatherSuccess(HefengResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        refreshWidgetView(this, weather);
        getLocation().weather = weather;
        DatabaseHelper.getInstance(this).insertWeather(getLocation());
        DatabaseHelper.getInstance(this).insertHistory(weather);
        this.stopSelf(getStartId());
    }

    @Override
    public void requestWeatherFailed(String locationName) {
        WidgetAndNotificationUtils.refreshWidgetFailed(this);
        refreshWidgetView(
                this,
                DatabaseHelper.getInstance(this).searchWeather(getLocation()));
        this.stopSelf(getStartId());
    }
}