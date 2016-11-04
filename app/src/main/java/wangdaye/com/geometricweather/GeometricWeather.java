package wangdaye.com.geometricweather;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.view.activity.GeoActivity;

/**
 * Geometric weather.
 * */

public class GeometricWeather extends Application {
    // data
    private List<GeoActivity> activityList;

    /** <br> data. */

    public void addActivity(GeoActivity a) {
        activityList.add(a);
    }

    public void removeActivity() {
        activityList.remove(activityList.size() - 1);
    }

    public GeoActivity getTopActivity() {
        if (activityList.size() == 0) {
            return null;
        }
        return activityList.get(activityList.size() - 1);
    }

    /** <br> life cycle. */

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    private void initialize() {
        instance = this;
        activityList = new ArrayList<>();
    }

    /** <br> singleton. */

    private static GeometricWeather instance;

    public static GeometricWeather getInstance() {
        return instance;
    }
}
