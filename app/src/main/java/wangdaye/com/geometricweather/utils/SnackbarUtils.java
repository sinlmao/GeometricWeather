package wangdaye.com.geometricweather.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

import wangdaye.com.geometricweather.GeometricWeather;

/**
 * Notification utils.
 * */

public class SnackbarUtils {

    public static void showSnackbar(String txt) {
        Snackbar.make(
                GeometricWeather.getInstance().getTopActivity().getSnackbarContainer(),
                txt,
                Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(String txt, String action, View.OnClickListener l) {
        Snackbar.make(
                GeometricWeather.getInstance().getTopActivity().getSnackbarContainer(),
                txt,
                Snackbar.LENGTH_SHORT)
                .setAction(action, l)
                .show();
    }
}
