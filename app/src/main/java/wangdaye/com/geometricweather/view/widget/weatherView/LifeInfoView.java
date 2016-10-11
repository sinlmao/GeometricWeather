package wangdaye.com.geometricweather.view.widget.weatherView;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;

/**
 * Life info view.
 * */

public class LifeInfoView extends FrameLayout {
    // widget
    private TextView windTitle;
    private TextView windContent;
    private TextView pmTitle;
    private TextView pmContent;
    private TextView humidityTitle;
    private TextView humidityContent;
    private TextView uvTitle;
    private TextView uvContent;
    private TextView dressTitle;
    private TextView dressContent;
    private TextView coldTitle;
    private TextView coldContent;
    private TextView aqiTitle;
    private TextView aqiContent;
    private TextView washCarTitle;
    private TextView washCarContent;
    private TextView exerciseTitle;
    private TextView exerciseContent;

    /** <br> life cycle. */

    public LifeInfoView(Context context) {
        super(context);
        this.initialize();
    }

    public LifeInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public LifeInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LifeInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    @SuppressLint("InflateParams")
    private void initialize() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.container_life_info, null);
        addView(view);

        windTitle = (TextView) findViewById(R.id.container_details_wind_title);
        windContent = (TextView) findViewById(R.id.container_details_wind_content);
        pmTitle = (TextView) findViewById(R.id.container_details_pm_title);
        pmContent = (TextView) findViewById(R.id.container_details_pm_content);
        humidityTitle = (TextView) findViewById(R.id.container_details_humidity_title);
        humidityContent = (TextView) findViewById(R.id.container_details_humidity_content);
        uvTitle = (TextView) findViewById(R.id.container_details_uv_title);
        uvContent = (TextView) findViewById(R.id.container_details_uv_content);
        dressTitle = (TextView) findViewById(R.id.container_details_dress_title);
        dressContent = (TextView) findViewById(R.id.container_details_dress_content);
        coldTitle = (TextView) findViewById(R.id.container_details_cold_title);
        coldContent = (TextView) findViewById(R.id.container_details_cold_content);
        aqiTitle = (TextView) findViewById(R.id.container_details_aqi_title);
        aqiContent = (TextView) findViewById(R.id.container_details_aqi_content);
        washCarTitle = (TextView) findViewById(R.id.container_details_wash_car_title);
        washCarContent = (TextView) findViewById(R.id.container_details_wash_car_content);
        exerciseTitle = (TextView) findViewById(R.id.container_details_exercise_title);
        exerciseContent = (TextView) findViewById(R.id.container_details_exercise_content);
    }

    /** <br> data. */

    public void setData(Weather weather) {
        windTitle.setText(weather.life.winds[0]);
        windContent.setText(weather.life.winds[1]);
        pmTitle.setText(weather.life.pms[0]);
        pmContent.setText(weather.life.pms[1]);
        humidityTitle.setText(weather.life.hums[0]);
        humidityContent.setText(weather.life.hums[1]);
        uvTitle.setText(weather.life.uvs[0]);
        uvContent.setText(weather.life.uvs[1]);
        dressTitle.setText(weather.life.dresses[0]);
        dressContent.setText(weather.life.dresses[1]);
        coldTitle.setText(weather.life.colds[0]);
        coldContent.setText(weather.life.colds[1]);
        aqiTitle.setText(weather.life.airs[0]);
        aqiContent.setText(weather.life.airs[1]);
        washCarTitle.setText(weather.life.washCars[0]);
        washCarContent.setText(weather.life.washCars[1]);
        exerciseTitle.setText(weather.life.sports[0]);
        exerciseContent.setText(weather.life.sports[1]);
    }
}
