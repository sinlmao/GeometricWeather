package wangdaye.com.geometricweather.service.notification;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import wangdaye.com.geometricweather.model.data.HefengResult;
import wangdaye.com.geometricweather.model.data.JuheResult;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.model.database.helper.DatabaseHelper;
import wangdaye.com.geometricweather.utils.LocationUtils;
import wangdaye.com.geometricweather.utils.WeatherUtils;
import wangdaye.com.geometricweather.utils.WidgetAndNotificationUtils;

/**
 * Send tomorrow weather forecast.
 * */

public class TomorrowForecastService extends Service
        implements LocationUtils.OnRequestLocationListener, WeatherUtils.OnRequestWeatherListener {
    // widget
    private WeatherUtils weatherUtils;
    private LocationUtils locationUtils;

    // data
    private int startId;
    private Location location;
    //private final int REQUEST_CODE = 9;

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

        this.location = DatabaseHelper.getInstance(this).readLocation().get(0);

        if (location.location.equals(getString(R.string.local))) {
            locationUtils.requestLocation(this);
        } else {
            weatherUtils.requestWeather(location.location, this);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        weatherUtils.cancel();
        locationUtils.cancel();
    }

    /** <br> interface. */

    // request location.

    @Override
    public void requestLocationSuccess(String locationName) {
        weatherUtils.requestWeather(locationName, this);
        location.realLocation = locationName;
        DatabaseHelper.getInstance(this).insertLocation(location);
    }

    @Override
    public void requestLocationFailed() {
        LocationUtils.simpleLocationFailedFeedback(this);
        this.stopSelf(startId);
    }

    // request weather.

    @Override
    public void requestJuheWeatherSuccess(JuheResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String forecastType = sharedPreferences.getString(
                getString(R.string.key_forecast_tomorrow_type),
                "simple_forecast");
        if (forecastType.equals("simple_forecast")) {
            WidgetAndNotificationUtils.sendForecast(this, weather, false);
        } else {
            WidgetAndNotificationUtils.sendNotification(this, weather, true);
        }

        location.weather = weather;
        DatabaseHelper.getInstance(this).insertWeather(location);
        DatabaseHelper.getInstance(this).insertHistory(weather);
        this.stopSelf(startId);
    }

    @Override
    public void requestHefengWeatherSuccess(HefengResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String forecastType = sharedPreferences.getString(
                getString(R.string.key_forecast_tomorrow_type),
                "simple_forecast");
        if (forecastType.equals("simple_forecast")) {
            WidgetAndNotificationUtils.sendForecast(this, weather, false);
        } else {
            WidgetAndNotificationUtils.sendNotification(this, weather, true);
        }

        location.weather = weather;
        DatabaseHelper.getInstance(this).insertWeather(location);
        DatabaseHelper.getInstance(this).insertHistory(weather);
        this.stopSelf(startId);
    }

    @Override
    public void requestWeatherFailed(String locationName) {
        WidgetAndNotificationUtils.sendNotificationFailed(this);
        this.stopSelf(startId);
    }
}
