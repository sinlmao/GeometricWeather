package wangdaye.com.geometricweather.service.notification;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.HefengResult;
import wangdaye.com.geometricweather.model.data.JuheResult;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.model.database.helper.DatabaseHelper;
import wangdaye.com.geometricweather.service.RecursionService;
import wangdaye.com.geometricweather.utils.LocationUtils;
import wangdaye.com.geometricweather.utils.WeatherUtils;
import wangdaye.com.geometricweather.utils.WidgetAndNotificationUtils;

/**
 * Notification service.
 * */

public class NotificationService extends RecursionService
        implements LocationUtils.OnRequestLocationListener, WeatherUtils.OnRequestWeatherListener {
    // data
    private boolean serviceSwitch;
    private boolean autoRefresh;
    private static final int REQUEST_CODE = 7;

    /** <br> life cycle. */

    @Override
    public void readSettings() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        serviceSwitch = sharedPreferences.getBoolean(getString(R.string.key_notification), false);
        autoRefresh = sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh), false);

        setLocation(DatabaseHelper.getInstance(this).readLocation().get(0));
    }

    @Override
    public void doRefresh() {
        if (serviceSwitch && autoRefresh) {
            requestData(this, this);
            setAlarmIntent(getClass(), REQUEST_CODE, false);
        } else {
            stopSelf(getStartId());
        }
    }

    /** <br> listener. */

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
        this.stopSelf(getStartId());
    }

    // request weather.

    @Override
    public void requestJuheWeatherSuccess(JuheResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        WidgetAndNotificationUtils.sendNotification(this, weather, true);
        getLocation().weather = weather;
        DatabaseHelper.getInstance(this).insertWeather(getLocation());
        DatabaseHelper.getInstance(this).insertHistory(weather);
        this.stopSelf(getStartId());
    }

    @Override
    public void requestHefengWeatherSuccess(HefengResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        WidgetAndNotificationUtils.sendNotification(this, weather, true);
        getLocation().weather = weather;
        DatabaseHelper.getInstance(this).insertWeather(getLocation());
        DatabaseHelper.getInstance(this).insertHistory(weather);
        this.stopSelf(getStartId());
    }

    @Override
    public void requestWeatherFailed(String locationName) {
        WidgetAndNotificationUtils.sendNotificationFailed(this);
        this.stopSelf(getStartId());
    }
}
