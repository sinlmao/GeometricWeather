package wangdaye.com.geometricweather.view.fragment;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.HefengResult;
import wangdaye.com.geometricweather.model.data.JuheResult;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.model.database.helper.DatabaseHelper;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.view.activity.MainActivity;
import wangdaye.com.geometricweather.view.dialog.ManageDialog;
import wangdaye.com.geometricweather.view.dialog.WeatherDialog;
import wangdaye.com.geometricweather.view.widget.swipeRefreshLayout.SwipeRefreshLayout;
import wangdaye.com.geometricweather.view.widget.weatherView.LifeInfoView;
import wangdaye.com.geometricweather.view.widget.weatherView.SkyView;
import wangdaye.com.geometricweather.view.widget.SwipeSwitchLayout;
import wangdaye.com.geometricweather.view.widget.weatherView.TrendView;
import wangdaye.com.geometricweather.view.widget.weatherView.WeekWeatherView;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.LocationUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;
import wangdaye.com.geometricweather.utils.WeatherUtils;

/**
 * Weather fragment.
 * */

public class WeatherFragment extends Fragment
        implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        NestedScrollView.OnScrollChangeListener, SwipeSwitchLayout.OnSwipeListener,
        WeekWeatherView.OnClickWeekContainerListener, WeatherUtils.OnRequestWeatherListener,
        LocationUtils.OnRequestLocationListener {
    // widget
    private Toolbar toolbar;
    private SkyView skyView;

    private SwipeSwitchLayout swipeSwitchLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NestedScrollView nestedScrollView;
    private LinearLayout weatherContainer;

    private TextView[] titleTexts;

    private TextView refreshTime;
    private TextView locationText;
    private ImageButton collectionIcon;

    private TextView overviewTitle;
    private WeekWeatherView weekWeatherView;
    private TrendView trendView;
    private TextView lifeInfoTitle;
    private LifeInfoView lifeInfoView;

    // data
    public Location location;
    public boolean collected;

    private int scrollDistance;
    private int totalDistance;

    private WeatherUtils weatherUtils;
    private LocationUtils locationUtils;

    // animation
    private AnimatorSet viewShowAnimator;

    /** <br> life cycle. */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        this.initData();
        this.initWidget(view);
        this.reset();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        weatherUtils.cancel();
        locationUtils.cancel();
    }

    /** <br> UI. */

    // init.

    private void initWidget(View view) {
        this.toolbar = (Toolbar) getActivity().findViewById(R.id.container_main_toolbar);
        this.skyView = (SkyView) view.findViewById(R.id.fragment_weather_skyView);
        this.initScrollViewPart(view);
    }

    private void initScrollViewPart(View view) {
        // get swipe switch layout.
        this.swipeSwitchLayout = (SwipeSwitchLayout) view.findViewById(R.id.fragment_weather_swipeSwitchLayout);
        swipeSwitchLayout.setOnSwipeListener(this);

        // get swipe refresh layout & set color.
        this.swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_weather_swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.lightPrimary_3),
                ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
        swipeRefreshLayout.setOnRefreshListener(this);

        // get nested scroll view & set listener.
        this.nestedScrollView = (NestedScrollView) view.findViewById(R.id.fragment_weather_scrollView);
        nestedScrollView.setOnScrollChangeListener(this);

        // get weather container.
        this.weatherContainer = (LinearLayout) view.findViewById(R.id.container_weather);
        viewShowAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.card_in);
        viewShowAnimator.setTarget(weatherContainer);

        // get touch layout, set height & get live texts.
        RelativeLayout touchLayout = (RelativeLayout) view.findViewById(R.id.container_weather_touchLayout);
        LinearLayout.LayoutParams touchParams = (LinearLayout.LayoutParams) touchLayout.getLayoutParams();
        touchParams.height = (int) (getResources().getDisplayMetrics().widthPixels / 6.8 * 4
                + DisplayUtils.dpToPx(getActivity(), 60)
                - DisplayUtils.dpToPx(getActivity(), 300 - 256));
        touchLayout.setLayoutParams(touchParams);
        touchLayout.setOnClickListener(this);

        this.titleTexts = new TextView[] {
                (TextView) view.findViewById(R.id.container_weather_aqi_text_live),
                (TextView) view.findViewById(R.id.container_weather_weather_text_live)};

        // weather card.
        this.initWeatherCard(view);

        //get life info view.
        this.lifeInfoView = (LifeInfoView) view.findViewById(R.id.container_weather_lifeInfoView);
    }

    private void initWeatherCard(View view) {
        this.refreshTime = (TextView) view.findViewById(R.id.container_weather_time_text_live);

        view.findViewById(R.id.container_weather_locationContainer).setOnClickListener(this);

        this.locationText = (TextView) view.findViewById(R.id.container_weather_location_text_live);

        this.collectionIcon = (ImageButton) view.findViewById(R.id.container_weather_location_collect_icon);
        collectionIcon.setOnClickListener(this);
        collectionIcon.setImageResource(collected ? R.drawable.ic_collected : R.drawable.ic_uncollected);

        this.overviewTitle = (TextView) view.findViewById(R.id.container_weather_overviewTitle);

        this.weekWeatherView = (WeekWeatherView) view.findViewById(R.id.container_weather_weekWeatherView);
        weekWeatherView.setOnClickWeekContainerListener(this);

        this.trendView = (TrendView) view.findViewById(R.id.container_weather_trendView);

        this.lifeInfoTitle = (TextView) view.findViewById(R.id.container_weather_lifeInfoTitle);
    }

    // reset.

    public void reset() {
        toolbar.setAlpha(1);
        skyView.reset();
        this.resetScrollViewPart();
    }

    private void resetScrollViewPart() {
        // set weather container gone.
        weatherContainer.setVisibility(View.GONE);
        // set swipe switch layout reset.
        swipeSwitchLayout.reset();
        swipeSwitchLayout.setEnabled(true);
        // set nested scroll view scroll to top.
        nestedScrollView.scrollTo(0, 0);
        scrollDistance = 0;
        // set swipe refresh layout refreshing.
        if (location.weather == null) {
            setRefreshing(true);
            onRefresh();
        } else {
            setRefreshing(false);
            buildUI();
        }
    }

    // build UI.

    private void setRefreshing(final boolean b) {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(b);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void buildUI() {
        Weather weather = location.weather;
        if (weather == null) {
            return;
        } else {
            TimeUtils.getInstance(getActivity()).getDayTime(getActivity(), weather, true);
        }

        DisplayUtils.setWindowTopColor(getActivity());
        ((MainActivity) getActivity()).initStatusBarColor(weather);
        ((MainActivity) getActivity()).initNavHeaderBackground();

        skyView.setWeather(weather);

        titleTexts[0].setText(weather.live.air);
        titleTexts[1].setText(weather.live.weather + " " + weather.live.temp + "℃");
        refreshTime.setText(weather.base.refreshTime);
        locationText.setText(weather.base.location);
        collectionIcon.setImageResource(collected ? R.drawable.ic_collected : R.drawable.ic_uncollected);

        if (TimeUtils.getInstance(getActivity()).isDay) {
            overviewTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightPrimary_3));
        } else {
            overviewTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
            lifeInfoTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.darkPrimary_1));
        }

        weekWeatherView.setData(weather);
        trendView.setData(location.weather, location.history);
        trendView.setState(TrendView.DAILY_STATE);
        lifeInfoView.setData(location.weather);

        weatherContainer.setVisibility(View.VISIBLE);
        viewShowAnimator.start();
    }

    /** <br> data. */

    // init.

    private void initData() {
        this.totalDistance = (int) DisplayUtils.dpToPx(getActivity(), 156);

        this.weatherUtils = new WeatherUtils();
        this.locationUtils = new LocationUtils(getActivity());
    }

    // interface.

    public void setLocation(Location l, boolean collected) {
        this.location = l;
        this.collected = collected;
    }

    public void requestLocation() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            locationUtils.requestLocation(this);
        } else {
            ((MainActivity) getActivity()).requestPermission(MainActivity.LOCATION_PERMISSIONS_REQUEST_CODE);
        }
    }

    public LocationUtils getLocationUtils() {
        return locationUtils;
    }

    /** <br> interface. */

    // on click listener.

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.container_weather_touchLayout:
                skyView.onClickSky();
                break;

            case R.id.container_weather_locationContainer:
                ManageDialog manageDialog = new ManageDialog();
                manageDialog.setOnLocationChangedListener(((MainActivity) getActivity()));
                manageDialog.show(getFragmentManager(), null);
                break;

            case R.id.container_weather_location_collect_icon:
                if (collected) {
                    if (((MainActivity) getActivity()).deleteLocation(location)) {
                        collected = false;
                        collectionIcon.setImageResource(R.drawable.ic_uncollected);
                    }
                } else {
                    collected = true;
                    ((MainActivity) getActivity()).addLocation(location);
                    collectionIcon.setImageResource(R.drawable.ic_collected);
                }
                break;
        }
    }

    // on refresh listener.

    @Override
    public void onRefresh() {
        locationUtils.cancel();
        weatherUtils.cancel();

        if (location.location.equals(getString(R.string.local))) {
            requestLocation();
        } else {
            weatherUtils.requestWeather(location.location, this);
        }
    }

    // on scroll changed listener.

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        scrollDistance += scrollY - oldScrollY;
        float alpha = (float) (1.0 - 1.0 * scrollDistance / totalDistance);
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 1) {
            alpha = 1;
        }
        toolbar.setAlpha(alpha);
        if (alpha == 0 && toolbar.getVisibility() == View.VISIBLE) {
            toolbar.setVisibility(View.GONE);
        } else if (alpha != 0 && toolbar.getVisibility() == View.GONE) {
            toolbar.setVisibility(View.VISIBLE);
        }
    }

    // on swipe listener(swipe switch layout).

    @Override
    public void swipeTakeEffect(int direction) {
        ((MainActivity) getActivity()).switchCity(location.location, direction);
    }

    // on click week listener.

    @Override
    public void onClickWeekContainer(int position) {
        WeatherDialog weatherDialog = new WeatherDialog();
        weatherDialog.setData(location.weather, position);
        weatherDialog.show(getFragmentManager(), null);
    }

    // on request location listener.

    @Override
    public void requestLocationSuccess(String locationName) {
        if (location.location.equals(getString(R.string.local))) {
            weatherUtils.requestWeather(locationName, this);
            location.realLocation = locationName;
            ((MainActivity) getActivity()).refreshLocation(location);
            DatabaseHelper.getInstance(getActivity()).insertLocation(location);
        }
    }

    @Override
    public void requestLocationFailed() {
        if (location.location.equals(getString(R.string.local))) {
            if (location.weather == null && !TextUtils.isEmpty(location.realLocation)) {
                location.weather = DatabaseHelper.getInstance(getActivity()).searchWeather(location);
                location.history = DatabaseHelper.getInstance(getActivity()).searchYesterdayHistory(location.weather);
                ((MainActivity) getActivity()).refreshLocation(location);
                buildUI();
            }
            LocationUtils.simpleLocationFailedFeedback(getActivity());
            setRefreshing(false);
        }
    }

    // on request weather listener.

    @Override
    public void requestJuheWeatherSuccess(JuheResult result, String locationName) {
        Weather weather = Weather.build(getActivity(), result);
        requestWeatherSuccess(weather, locationName);
    }

    @Override
    public void requestHefengWeatherSuccess(HefengResult result, String locationName) {
        Weather weather = Weather.build(getActivity(), result);
        requestWeatherSuccess(weather, locationName);
    }

    private void requestWeatherSuccess(Weather weather, String locationName) {
        if ((location.location.equals(getString(R.string.local)) && location.realLocation.equals(locationName))
                || location.location.equals(locationName)) {
            if (weather == null) {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));
                buildUI();
                setRefreshing(false);
            } else if (location.weather == null
                    || !location.weather.base.refreshTime.equals(weather.base.refreshTime)) {
                location.weather = weather;

                ((MainActivity) getActivity()).refreshWidgetView();
                ((MainActivity) getActivity()).sendNotification();

                location.history = DatabaseHelper.getInstance(getActivity()).searchYesterdayHistory(weather);
                ((MainActivity) getActivity()).refreshLocation(location);
                DatabaseHelper.getInstance(getActivity()).insertWeather(location);
                DatabaseHelper.getInstance(getActivity()).insertHistory(weather);

                buildUI();
                setRefreshing(false);
            }
        }
    }

    @Override
    public void requestWeatherFailed(String locationName) {
        if ((location.location.equals(getString(R.string.local)) && location.realLocation.equals(locationName))
                || location.location.equals(locationName)) {
            if (location.weather == null) {
                location.weather = DatabaseHelper.getInstance(getActivity()).searchWeather(location);

                if (location.weather != null) {
                    location.history = DatabaseHelper.getInstance(getActivity())
                            .searchYesterdayHistory(location.weather);
                }

                ((MainActivity) getActivity()).refreshLocation(location);
                SnackbarUtils.showSnackbar(getString(R.string.feedback_get_weather_failed));

                buildUI();
                setRefreshing(false);
            }
        }
    }
}