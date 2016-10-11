package wangdaye.com.geometricweather.view.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.model.database.helper.DatabaseHelper;
import wangdaye.com.geometricweather.service.widget.WidgetClockDayService;
import wangdaye.com.geometricweather.service.widget.WidgetClockDayCenterService;
import wangdaye.com.geometricweather.service.widget.WidgetClockDayWeekService;
import wangdaye.com.geometricweather.service.widget.WidgetDayService;
import wangdaye.com.geometricweather.service.widget.WidgetDayWeekService;
import wangdaye.com.geometricweather.service.widget.WidgetWeekService;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.view.dialog.ManageDialog;
import wangdaye.com.geometricweather.view.fragment.WeatherFragment;
import wangdaye.com.geometricweather.view.widget.StatusBarView;
import wangdaye.com.geometricweather.view.widget.SwipeSwitchLayout;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.SafeHandler;
import wangdaye.com.geometricweather.utils.ShareUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.WidgetAndNotificationUtils;
import wangdaye.com.geometricweather.view.widget.weatherView.CircularSkyView;

/**
 * Main activity.
 * */

public class MainActivity extends GeoActivity
        implements NavigationView.OnNavigationItemSelectedListener, ManageDialog.OnLocationChangedListener,
        SafeHandler.HandlerContainer {
    // widget
    private SafeHandler<MainActivity> handler;

    private CoordinatorLayout container;
    private StatusBarView statusBar;
    private ImageView navBackground;
    private WeatherFragment weatherFragment;

    // data
    private List<Location> locationList;

    public static final int LOCATION_PERMISSIONS_REQUEST_CODE = 1;
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;

    private final int SETTINGS_ACTIVITY = 1;

    /** <br> life cycle. */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isStarted()) {
            setStarted();
            initData();
            initWidget();
        }
        if (weatherFragment == null) {
            weatherFragment = new WeatherFragment();
            weatherFragment.setLocation(locationList.get(0), true);
            changeFragment(weatherFragment);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public View getSnackbarContainer() {
        return container;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_drawerLayout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SETTINGS_ACTIVITY:
                DisplayUtils.setNavigationBarColor(this, TimeUtils.getInstance(this).isDay);
                sendNotification();
                break;
        }
    }

    /** <br> UI. */

    private void initWidget() {
        this.handler = new SafeHandler<>(this);

        this.container = (CoordinatorLayout) findViewById(R.id.container_main_container);

        this.statusBar = (StatusBarView) findViewById(R.id.container_main_statusBar);
        this.initStatusBarColor(null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.container_main_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer, toolbar,
                R.string.action_open_drawer, R.string.action_close_drawer);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.activity_nav);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);

        View navHeader = navigationView.getHeaderView(0);

        ImageView headerIcon = (ImageView) navHeader.findViewById(R.id.container_nav_header_icon);
        Glide.with(this)
                .load(R.drawable.ic_launcher)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(headerIcon);

        this.navBackground = (ImageView) navHeader.findViewById(R.id.container_nav_header_background);
        this.initNavHeaderBackground();
    }

    public void initNavHeaderBackground() {
        boolean isDay = TimeUtils.getInstance(this).isDay;
        int navBackId = isDay ? R.drawable.nav_head_day : R.drawable.nav_head_night;
        Glide.with(this).
                load(navBackId).
                into(navBackground);
    }

    public void initStatusBarColor(Weather weather) {
        int[] exchangeTimes;
        int systemTime;

        if (weather == null) {
            exchangeTimes = new int[] {60 * 6, 60 * 18};
        } else {
            exchangeTimes = new int[] {
                    60 * Integer.parseInt(weather.dailyList.get(0).exchangeTimes[0].split(":")[0])
                            + Integer.parseInt(weather.dailyList.get(0).exchangeTimes[0].split(":")[1]),
                    60 * Integer.parseInt(weather.dailyList.get(0).exchangeTimes[1].split(":")[0])
                            + Integer.parseInt(weather.dailyList.get(0).exchangeTimes[1].split(":")[1])};
        }
        systemTime = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                + Calendar.getInstance().get(Calendar.MINUTE);

        float mixRatio = CircularSkyView.getMixRatio(
                systemTime,
                exchangeTimes,
                TimeUtils.getInstance(this).isDay);

        statusBar.setBackgroundColor(Color.rgb(
                (int) (117 * mixRatio + 26 * (1 - mixRatio)),
                (int) (190 * mixRatio + 27 * (1 - mixRatio)),
                (int) (203 * mixRatio + 34 * (1 - mixRatio))));
    }

    private void changeFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.container_main_fragment, fragment);
        fragmentTransaction.commit();
    }

    /** <br> data. */

    private void initData() {
        this.locationList = DatabaseHelper.getInstance(this).readLocation();
    }

    public void switchCity(String name, int swipeDir) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).location.equals(name)) {
                int position = swipeDir == SwipeSwitchLayout.DIRECTION_LEFT ?
                        i + 1 : i - 1;
                if (position < 0) {
                    position = locationList.size() - 1;
                } else if (position > locationList.size() - 1) {
                    position = 0;
                }
                weatherFragment.setLocation(locationList.get(position), true);
                weatherFragment.reset();
                return;
            }
        }
        weatherFragment.setLocation(locationList.get(0), true);
        weatherFragment.reset();
    }

    public void addLocation(Location location) {
        DatabaseHelper.getInstance(this).insertLocation(location);
        locationList.add(location);
    }

    public boolean deleteLocation(Location location) {
        if (locationList.size() <= 1) {
            SnackbarUtils.showSnackbar(getString(R.string.feedback_location_list_cannot_be_null));
            return false;
        } else {
            DatabaseHelper.getInstance(this).deleteLocation(location);
            for (int i = 0; i < locationList.size(); i ++) {
                if (locationList.get(i).location.equals(location.location)) {
                    locationList.remove(i);
                    break;
                }
            }
            return true;
        }
    }

    public void refreshLocation(Location location) {
        for (int i = 0; i < locationList.size(); i ++) {
            if (locationList.get(i).location.equals(location.location)) {
                locationList.remove(i);
                locationList.add(i, location);
                break;
            }
        }
    }

    /** <br> permission. */

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission(int permissionCode) {
        switch (permissionCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.INSTALL_LOCATION_PROVIDER) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[] {android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSIONS_REQUEST_CODE);
                } else if (weatherFragment != null) {
                    weatherFragment.getLocationUtils().requestLocation(weatherFragment);
                }
                break;

            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                    break;
                } else {
                    ShareUtils.shareWeather(this, weatherFragment.location.weather);
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permission, @NonNull int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permission, grantResult);
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST_CODE:
                if (weatherFragment != null && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    weatherFragment.getLocationUtils().requestLocation(weatherFragment);
                } else {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_location_permission_failed));
                }
                break;

            case WRITE_EXTERNAL_STORAGE_REQUEST_CODE:
                if (weatherFragment != null && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                    ShareUtils.shareWeather(this, weatherFragment.location.weather);
                } else {
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_request_write_permission_failed));
                }
                break;
        }
    }

    /** <br> widget & notification. */

    // widget.

    public void refreshWidgetView() {
        SharedPreferences sharedPreferences;
        String locationName;

        // day
        sharedPreferences = getSharedPreferences(
                getString(R.string.sp_widget_day_setting),
                Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(
                getString(R.string.key_location),
                getString(R.string.local));
        if (weatherFragment.location.location.equals(locationName)) {
            WidgetDayService.refreshWidgetView(this, weatherFragment.location.weather);
        }

        // week
        sharedPreferences = getSharedPreferences(
                getString(R.string.sp_widget_week_setting),
                Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(
                getString(R.string.key_location),
                getString(R.string.local));
        if (weatherFragment.location.location.equals(locationName)) {
            WidgetWeekService.refreshWidgetView(this, weatherFragment.location.weather);
        }

        // day week
        sharedPreferences = getSharedPreferences(
                getString(R.string.sp_widget_day_week_setting),
                Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(
                getString(R.string.key_location),
                getString(R.string.local));
        if (weatherFragment.location.location.equals(locationName)) {
            WidgetDayWeekService.refreshWidgetView(this, weatherFragment.location.weather);
        }

        // clock day
        sharedPreferences = getSharedPreferences(
                getString(R.string.sp_widget_clock_day_setting),
                Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(
                getString(R.string.key_location),
                getString(R.string.local));
        if (weatherFragment.location.location.equals(locationName)) {
            WidgetClockDayService.refreshWidgetView(this, weatherFragment.location.weather);
        }

        // clock day center
        sharedPreferences = getSharedPreferences(
                getString(R.string.sp_widget_clock_day_center_setting),
                Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(
                getString(R.string.key_location),
                getString(R.string.local));
        if (weatherFragment.location.location.equals(locationName)) {
            WidgetClockDayCenterService.refreshWidgetView(this, weatherFragment.location.weather);
        }

        // clock day week
        sharedPreferences = getSharedPreferences(
                getString(R.string.sp_widget_clock_day_week_setting),
                Context.MODE_PRIVATE);
        locationName = sharedPreferences.getString(
                getString(R.string.key_location),
                getString(R.string.local));
        if (weatherFragment.location.location.equals(locationName)) {
            WidgetClockDayWeekService.refreshWidgetView(this, weatherFragment.location.weather);
        }
    }

    public void sendNotification() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.getBoolean(
                getString(R.string.key_notification),
                false)) {
            WidgetAndNotificationUtils.sendNotification(this,
                    weatherFragment.location.weather,
                    false);
        }
    }

    /** <br> interface. */

    // menu.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    ShareUtils.shareWeather(this, weatherFragment.location.weather);
                } else {
                    requestPermission(WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // drawer.

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_manage:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = R.id.action_manage;
                        handler.sendMessage(msg);
                    }
                }, 400);
                break;

            case R.id.action_settings:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = R.id.action_settings;
                        handler.sendMessage(msg);
                    }
                }, 400);
                break;

            case R.id.action_about:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        msg.what = R.id.action_about;
                        handler.sendMessage(msg);
                    }
                }, 400);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_drawerLayout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    // on location changed listener.

    @Override
    public void selectLocation(String result) {
        Location location = null;
        boolean collected = false;
        for (Location l : locationList) {
            if (l.location.equals(result)) {
                location = l;
                collected = true;
                break;
            }
        }
        if (location == null) {
            location = new Location(result, null);
        }
        if (weatherFragment != null) {
            weatherFragment.setLocation(location, collected);
            weatherFragment.reset();
        }
    }

    @Override
    public void changeLocationList(List<String> nameList) {
        List<Location> newList = new ArrayList<>();
        for (int i = 0; i < nameList.size(); i ++) {
            for (Location l : locationList) {
                if (l.location.equals(nameList.get(i))) {
                    newList.add(l);
                    break;
                }
            }
            if (newList.size() - 1 < i) {
                newList.add(new Location(nameList.get(i), null));
            }
        }

        DatabaseHelper.getInstance(this).clearLocation();
        DatabaseHelper.getInstance(this).writeLocation(newList);
    }

    // handler.

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.action_manage:
                ManageDialog manageDialog = new ManageDialog();
                manageDialog.setOnLocationChangedListener(this);
                manageDialog.show(getFragmentManager(), null);
                break;

            case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivityForResult(intentSettings, SETTINGS_ACTIVITY);
                break;

            case R.id.action_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                break;
        }
    }
}
