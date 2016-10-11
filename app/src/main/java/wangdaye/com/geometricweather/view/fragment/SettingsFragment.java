package wangdaye.com.geometricweather.view.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.service.notification.NotificationService;
import wangdaye.com.geometricweather.service.notification.TimeService;
import wangdaye.com.geometricweather.utils.SnackbarUtils;
import wangdaye.com.geometricweather.utils.ValueUtils;
import wangdaye.com.geometricweather.view.dialog.TimeSetterDialog;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.WidgetAndNotificationUtils;

/**
 * Settings fragment.
 * */

public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, TimeSetterDialog.OnTimeChangedListener {

    /** <br> life cycle. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.perference);

        initUIPart();
        initForecastPart();
        initWidgetPart();
        initNotificationPart();
    }

    /** <br> UI. */

    private void initUIPart() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(false);
        } else {
            findPreference(getString(R.string.key_navigationBar_color)).setEnabled(true);
        }
    }

    private void initForecastPart() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // set today forecast time & todayForecastType.
        Preference todayForecastTime = findPreference(getString(R.string.key_forecast_today_time));
        todayForecastTime.setSummary(
                sharedPreferences.getString(getString(R.string.key_forecast_today_time), "07:00"));

        ListPreference todayForecastType = (ListPreference) findPreference(getString(R.string.key_forecast_today_type));
        todayForecastType.setSummary(
                ValueUtils.getForecastType(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_forecast_today_type), "simple_forecast")));
        todayForecastType.setOnPreferenceChangeListener(this);

        // set tomorrow forecast time & tomorrowForecastType.
        Preference tomorrowForecastTime = findPreference(getString(R.string.key_forecast_tomorrow_time));
        tomorrowForecastTime.setSummary(
                sharedPreferences.getString(getString(R.string.key_forecast_tomorrow_time), "21:00"));

        ListPreference tomorrowForecastType = (ListPreference) findPreference(getString(R.string.key_forecast_today_type));
        tomorrowForecastType.setSummary(
                ValueUtils.getForecastType(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_forecast_tomorrow_type), "simple_forecast")));
        tomorrowForecastType.setOnPreferenceChangeListener(this);

        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false)) {
            // open today forecast.
            // start service.
            Intent intent = new Intent(getActivity(), TimeService.class);
            getActivity().startService(intent);
            // set item enable.
            todayForecastTime.setEnabled(true);
            tomorrowForecastType.setEnabled(true);
        } else {
            // set item enable.
            todayForecastTime.setEnabled(false);
            tomorrowForecastType.setEnabled(false);
        }

        if (sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false)) {
            // open tomorrow forecast.
            Intent intent = new Intent(getActivity(), TimeService.class);
            getActivity().startService(intent);
            tomorrowForecastTime.setEnabled(true);
            tomorrowForecastType.setEnabled(true);
        } else {
            tomorrowForecastTime.setEnabled(false);
            tomorrowForecastType.setEnabled(false);
        }

        if (!sharedPreferences.getBoolean(getString(R.string.key_forecast_today), false)
                && !sharedPreferences.getBoolean(getString(R.string.key_forecast_tomorrow), false)) {
            // close forecast.
            Intent intent = new Intent(getActivity(), TimeService.class);
            getActivity().stopService(intent);
        }
    }

    private void initWidgetPart() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference widgetRefreshTime = (ListPreference) findPreference(getString(R.string.key_widget_refresh_time));
        widgetRefreshTime.setSummary(
                ValueUtils.getRefreshTime(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_widget_refresh_time), "2hours")));
        widgetRefreshTime.setOnPreferenceChangeListener(this);
    }

    private void initNotificationPart() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // notification text color.
        ListPreference notificationTextColor = (ListPreference) findPreference(getString(R.string.key_notification_text_color));
        notificationTextColor.setSummary(
                ValueUtils.getNotificationTextColor(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_notification_text_color), "grey")));
        notificationTextColor.setOnPreferenceChangeListener(this);

        // notification background.
        CheckBoxPreference notificationBackground = (CheckBoxPreference) findPreference(getString(R.string.key_notification_background));

        // notification can be cleared.
        CheckBoxPreference notificationClearFlag = (CheckBoxPreference) findPreference(getString(R.string.key_notification_can_be_cleared));

        // notification hide icon.
        CheckBoxPreference notificationIconBehavior = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_icon));

        // notification hide in lock screen.
        CheckBoxPreference notificationHideBehavior = (CheckBoxPreference) findPreference(getString(R.string.key_notification_hide_in_lockScreen));

        // notification auto refresh.
        CheckBoxPreference notificationAutoRefresh = (CheckBoxPreference) findPreference(getString(R.string.key_notification_auto_refresh));

        // notification refresh time.
        ListPreference notificationRefreshTime = (ListPreference) findPreference(getString(R.string.key_notification_refresh_time));
        notificationRefreshTime.setSummary(
                ValueUtils.getRefreshTime(
                        getActivity(),
                        sharedPreferences.getString(getString(R.string.key_notification_refresh_time), "2hours")));
        notificationRefreshTime.setOnPreferenceChangeListener(this);

        CheckBoxPreference notificationSoundSwitch = (CheckBoxPreference) findPreference(getString(R.string.key_notification_sound));

        CheckBoxPreference notificationShockSwitch = (CheckBoxPreference) findPreference(getString(R.string.key_notification_shock));

        if(sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
            // open notification.
            notificationTextColor.setEnabled(true);
            notificationBackground.setEnabled(true);
            notificationClearFlag.setEnabled(true);
            notificationIconBehavior.setEnabled(true);
            notificationHideBehavior.setEnabled(true);
            notificationAutoRefresh.setEnabled(true);
            if(sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh), false)) {
                // open auto refresh.
                notificationRefreshTime.setEnabled(true);
                notificationSoundSwitch.setEnabled(true);
                notificationShockSwitch.setEnabled(true);
            } else {
                notificationRefreshTime.setEnabled(false);
                notificationSoundSwitch.setEnabled(false);
                notificationShockSwitch.setEnabled(false);
            }
        } else {
            // close notification.
            notificationTextColor.setEnabled(false);
            notificationBackground.setEnabled(false);
            notificationClearFlag.setEnabled(false);
            notificationIconBehavior.setEnabled(false);
            notificationHideBehavior.setEnabled(false);
            notificationAutoRefresh.setEnabled(false);
            notificationRefreshTime.setEnabled(false);
            notificationSoundSwitch.setEnabled(false);
            notificationShockSwitch.setEnabled(false);
        }
    }

    /** <br> data. */

    private void startNotificationService() {
        Intent intent = new Intent(getActivity(), NotificationService.class);
        getActivity().startService(intent);
    }

    private void stopNotificationService() {
        Intent intent = new Intent(getActivity(), NotificationService.class);
        getActivity().stopService(intent);
    }

    /** interface. */

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_weather_icon_position))) {
            SnackbarUtils.showSnackbar(getString(R.string.feedback_restart));
        } else if (preference.getKey().equals(getString(R.string.key_navigationBar_color))) {
            // navigation bar color.
            DisplayUtils.setNavigationBarColor(getActivity(), true);
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today))) {
            // forecast today.
            initForecastPart();
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_forecast_today_time))) {
            // set today forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(true);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow))) {
            // timing forecast tomorrow.
            initForecastPart();
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_forecast_tomorrow_time))) {
            // set tomorrow forecast time.
            TimeSetterDialog dialog = new TimeSetterDialog();
            dialog.setModel(false);
            dialog.setOnTimeChangedListener(this);
            dialog.show(getFragmentManager(), null);
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification))) {
            // notification switch.
            initNotificationPart();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (sharedPreferences.getBoolean(getString(R.string.key_notification), false)) {
                // open notification.
                if (sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh), false)) {
                    // open auto refresh.
                    startNotificationService();
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
                } else {
                    // close auto refresh.
                    SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
                }
            } else {
                // close notification.
                stopNotificationService();
                NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(WidgetAndNotificationUtils.NOTIFICATION_ID);
                notificationManager.cancel(WidgetAndNotificationUtils.FORECAST_ID);
            }
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_text_color))) {
            // notification text color.
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_background))) {
            // notification background.
            SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_can_be_cleared))) {
            // notification clear flag.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh), false)) {
                SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_after_back));
            }
            return true;
        } else if (preference.getKey().equals(getString(R.string.key_notification_auto_refresh))) {
            // notification auto refresh.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if(sharedPreferences.getBoolean(getString(R.string.key_notification_auto_refresh), false)) {
                startNotificationService();
                SnackbarUtils.showSnackbar(getString(R.string.feedback_refresh_notification_now));
            } else {
                stopNotificationService();
            }
            this.initNotificationPart();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        initForecastPart();
        initWidgetPart();
        initNotificationPart();
        return true;
    }

    @Override
    public void timeChanged() {
        this.initForecastPart();
    }
}