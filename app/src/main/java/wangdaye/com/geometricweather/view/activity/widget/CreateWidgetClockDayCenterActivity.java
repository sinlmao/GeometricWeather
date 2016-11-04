package wangdaye.com.geometricweather.view.activity.widget;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.model.data.HefengResult;
import wangdaye.com.geometricweather.model.data.JuheResult;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.model.database.helper.DatabaseHelper;
import wangdaye.com.geometricweather.service.widget.WidgetClockDayCenterService;
import wangdaye.com.geometricweather.utils.LocationUtils;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.WeatherUtils;
import wangdaye.com.geometricweather.utils.WidgetAndNotificationUtils;
import wangdaye.com.geometricweather.view.activity.GeoActivity;

/**
 * Create widget clock day center activity.
 * */

public class CreateWidgetClockDayCenterActivity extends GeoActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener,
        WeatherUtils.OnRequestWeatherListener, LocationUtils.OnRequestLocationListener {
    // widget
    private ImageView widgetCard;
    private ImageView widgetIcon;
    private TextClock widgetClock;
    private TextView widgetWeather;
    private TextView widgetTemp;
    private TextView widgetRefreshTime;

    private CoordinatorLayout container;

    private Switch showCardSwitch;
    private Switch blackTextSwitch;
    private Switch hideRefreshTimeSwitch;

    // data
    private Location location;
    private List<String> nameList;

    private WeatherUtils weatherUtils;
    private LocationUtils locationUtils;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_widget_clock_day_center);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();

            if (location.name.equals(getString(R.string.local))) {
                locationUtils.requestLocation(this, this);
            } else {
                weatherUtils.requestWeather(location.name, location.name, this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        weatherUtils.cancel();
        locationUtils.cancel();
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    /** <br> UI. */

    private void initWidget() {
        this.widgetCard = (ImageView) findViewById(R.id.widget_clock_day_center_card);
        widgetCard.setVisibility(View.GONE);

        this.widgetIcon = (ImageView) findViewById(R.id.widget_clock_day_center_icon);
        this.widgetClock = (TextClock) findViewById(R.id.widget_clock_day_center_clock);
        this.widgetWeather = (TextView) findViewById(R.id.widget_clock_day_center_weather);
        this.widgetTemp = (TextView) findViewById(R.id.widget_clock_day_center_temp);
        this.widgetRefreshTime = (TextView) findViewById(R.id.widget_clock_day_center_refreshTime);

        ImageView wallpaper = (ImageView) findViewById(R.id.activity_create_widget_clock_day_center_wall);
        wallpaper.setImageDrawable(WallpaperManager.getInstance(this).getDrawable());

        this.container = (CoordinatorLayout) findViewById(R.id.activity_create_widget_clock_day_center_container);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_text, nameList);
        adapter.setDropDownViewResource(R.layout.spinner_text);
        Spinner locationSpinner = (Spinner) findViewById(R.id.activity_create_widget_clock_day_center_spinner);
        locationSpinner.setAdapter(adapter);
        locationSpinner.setOnItemSelectedListener(this);

        this.showCardSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_center_showCardSwitch);
        showCardSwitch.setOnCheckedChangeListener(new ShowCardSwitchCheckListener());

        this.hideRefreshTimeSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_center_hideRefreshTimeSwitch);
        hideRefreshTimeSwitch.setOnCheckedChangeListener(new HideRefreshTimeSwitchCheckListener());

        this.blackTextSwitch = (Switch) findViewById(R.id.activity_create_widget_clock_day_center_blackTextSwitch);
        blackTextSwitch.setOnCheckedChangeListener(new BlackTextSwitchCheckListener());

        Button doneButton = (Button) findViewById(R.id.activity_create_widget_clock_day_center_doneButton);
        doneButton.setOnClickListener(this);
    }

    @SuppressLint("SetTextI18n")
    private void refreshWidgetView() {
        if (location.weather == null) {
            return;
        }

        Weather weather = location.weather;

        int[] imageId = WeatherUtils.getWeatherIcon(
                WeatherUtils.getWeatherKind(weather.live.weather),
                TimeUtils.getInstance(this).getDayTime(this, location.weather, false).isDay);
        widgetIcon.setImageResource(imageId[3]);

        String[] texts = WidgetAndNotificationUtils.buildWidgetDayStyleText(weather);
        widgetWeather.setText(texts[0]);
        widgetTemp.setText(texts[1]);
        widgetRefreshTime.setText(weather.base.location + "." + weather.base.refreshTime);
    }

    /** <br> data. */

    private void initData() {
        this.nameList = new ArrayList<>();
        List<Location> locationList = DatabaseHelper.getInstance(this).readLocation();
        for (Location l : locationList) {
            nameList.add(l.name);
        }
        this.location = new Location(nameList.get(0), null);

        this.weatherUtils = new WeatherUtils();
        this.locationUtils = new LocationUtils(this);
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_create_widget_clock_day_center_doneButton:
                SharedPreferences.Editor editor = getSharedPreferences(
                        getString(R.string.sp_widget_clock_day_center_setting),
                        MODE_PRIVATE)
                        .edit();
                editor.putString(getString(R.string.key_location), location.name);
                editor.putBoolean(getString(R.string.key_show_card), showCardSwitch.isChecked());
                editor.putBoolean(getString(R.string.key_hide_refresh_time), hideRefreshTimeSwitch.isChecked());
                editor.putBoolean(getString(R.string.key_black_text), blackTextSwitch.isChecked());
                editor.apply();

                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                int appWidgetId = 0;
                if (extras != null) {
                    appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);

                Intent service = new Intent(this, WidgetClockDayCenterService.class);
                startService(service);
                finish();
                break;
        }
    }

    // on select changed listener(spinner).

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        location = new Location(parent.getItemAtPosition(position).toString(), null);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        location = new Location(parent.getItemAtPosition(0).toString(), null);
    }

    // on check changed listener(switch).

    private class ShowCardSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetCard.setVisibility(View.VISIBLE);
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextDark));
                widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextDark));
                widgetTemp.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextDark));
            } else {
                widgetCard.setVisibility(View.GONE);
                if (!blackTextSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextLight));
                    widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextLight));
                    widgetTemp.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextLight));
                }
            }
        }
    }

    private class HideRefreshTimeSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            widgetRefreshTime.setVisibility(hideRefreshTimeSwitch.isChecked() ? View.GONE : View.VISIBLE);
        }
    }

    private class BlackTextSwitchCheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextDark));
                widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextDark));
                widgetTemp.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextDark));
            } else {
                if (!showCardSwitch.isChecked()) {
                    widgetClock.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextLight));
                    widgetWeather.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextLight));
                    widgetTemp.setTextColor(ContextCompat.getColor(CreateWidgetClockDayCenterActivity.this, R.color.colorTextLight));
                }
            }
        }
    }

    // on request name listener.

    @Override
    public void requestLocationSuccess(String locationName) {
        location.realName = locationName;
        weatherUtils.requestWeather(location.name, location.realName, this);
        DatabaseHelper.getInstance(this).insertLocation(location);
    }

    @Override
    public void requestLocationFailed() {
        LocationUtils.simpleLocationFailedFeedback(this);
    }

    // on request weather listener.

    @Override
    public void requestJuheWeatherSuccess(JuheResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        location.weather = weather;
        refreshWidgetView();
        DatabaseHelper.getInstance(this).insertWeather(location);
        DatabaseHelper.getInstance(this).insertHistory(weather);
    }

    @Override
    public void requestHefengWeatherSuccess(HefengResult result, String locationName) {
        Weather weather = Weather.build(this, result);

        location.weather = weather;
        refreshWidgetView();
        DatabaseHelper.getInstance(this).insertWeather(location);
        DatabaseHelper.getInstance(this).insertHistory(weather);
    }

    @Override
    public void requestWeatherFailed(String location) {
        this.location.weather = DatabaseHelper.getInstance(this).searchWeather(this.location);
        refreshWidgetView();
        SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
    }
}