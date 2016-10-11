package wangdaye.com.geometricweather.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;

/**
 * GeoActivity
 * */

public abstract class GeoActivity extends AppCompatActivity {
    // data
    private boolean started = false;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GeometricWeather.getInstance().addActivity(this);

        DisplayUtils.setWindowTopColor(this);
        DisplayUtils.setStatusBarTranslate(getWindow());
        DisplayUtils.setNavigationBarColor(this, TimeUtils.getInstance(this).isDay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GeometricWeather.getInstance().removeActivity();
    }

    public void setStarted() {
        started = true;
    }

    public boolean isStarted() {
        return started;
    }

    public abstract View getSnackbarContainer();
}
