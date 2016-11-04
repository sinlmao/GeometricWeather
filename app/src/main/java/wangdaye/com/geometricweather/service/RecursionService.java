package wangdaye.com.geometricweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.utils.LocationUtils;
import wangdaye.com.geometricweather.utils.WeatherUtils;
import wangdaye.com.geometricweather.utils.WidgetAndNotificationUtils;

/**
 * Recursion service.
 * */

public abstract class RecursionService extends Service {
    // widget
    public WeatherUtils weatherUtils;
    public LocationUtils locationUtils;

    // data
    private int startId;
    private Location location;

    /** <br> life cycle. */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public  int onStartCommand(Intent intent, int flags, int startId) {
        this.startId = startId;
        this.weatherUtils = new WeatherUtils();
        this.locationUtils = new LocationUtils(this);

        readSettings();
        doRefresh();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        weatherUtils.cancel();
        locationUtils.cancel();
    }

    /** <br> option. */

    public abstract void readSettings();

    public abstract void doRefresh();

    public void requestData(WeatherUtils.OnRequestWeatherListener weatherListener,
                             LocationUtils.OnRequestLocationListener locationListener) {
        if(location.name.equals(getString(R.string.local))) {
            locationUtils.requestLocation(this, locationListener);
        } else {
            weatherUtils.requestWeather(location.name, location.name, weatherListener);
        }
    }

    /** <br> alarm mission. */

    public void setAlarmIntent(Class<?> cls, int REQUEST_CODE, boolean widget) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent target = new Intent(getBaseContext(), cls);
        PendingIntent pendingIntent = PendingIntent.getService(
                getBaseContext(),
                REQUEST_CODE,
                target,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int duration = 1000 * 60 * 60 * (
                widget ?
                        WidgetAndNotificationUtils.getWidgetRefreshHours(this)
                        :
                        WidgetAndNotificationUtils.getNotificationRefreshHours(this));
        alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + duration,
                pendingIntent);
    }

    /** <br> getters & setters. */

    public int getStartId() {
        return startId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location l) {
        this.location = l;
    }
}
