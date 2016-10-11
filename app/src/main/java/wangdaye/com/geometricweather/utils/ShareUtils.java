package wangdaye.com.geometricweather.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;

/**
 * Share utils.
 * */

public class ShareUtils {

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public static void shareWeather(Context context, Weather weather) {
        if (weather == null) {
            SnackbarUtils.showSnackbar(context.getString(R.string.feedback_share_failed));
            return;
        }

        View view = LayoutInflater.from(context).inflate(R.layout.container_share_view, null);
        boolean isDay = TimeUtils.getInstance(context).getDayTime(context, weather, false).isDay;

        ImageView background = (ImageView) view.findViewById(R.id.container_share_view_background);
        ImageView iconNow = (ImageView) view.findViewById(R.id.container_share_view_flagIcon);
        TextView location = (TextView) view.findViewById(R.id.container_share_view_location);
        TextView weatherNow = (TextView) view.findViewById(R.id.container_share_view_weather);
        TextView tempNow = (TextView) view.findViewById(R.id.container_share_view_temp);
        TextView wind = (TextView) view.findViewById(R.id.container_share_view_wind);
        TextView air = (TextView) view.findViewById(R.id.container_share_view_aqi);
        TextView[] weeks = new TextView[] {
                (TextView) view.findViewById(R.id.container_share_view_week_1),
                (TextView) view.findViewById(R.id.container_share_view_week_2),
                (TextView) view.findViewById(R.id.container_share_view_week_3)
        };
        ImageView[] icons = new ImageView[] {
                (ImageView) view.findViewById(R.id.container_share_view_icon_1),
                (ImageView) view.findViewById(R.id.container_share_view_icon_2),
                (ImageView) view.findViewById(R.id.container_share_view_icon_3)
        };
        TextView[] temps = new TextView[] {
                (TextView) view.findViewById(R.id.container_share_view_temp_1),
                (TextView) view.findViewById(R.id.container_share_view_temp_2),
                (TextView) view.findViewById(R.id.container_share_view_temp_3)
        };

        Bitmap bg = BitmapFactory.decodeResource(
                context.getResources(),
                isDay ? R.drawable.nav_head_day : R.drawable.nav_head_night);
        background.setImageBitmap(bg);

        int[][] imageIds = new int[][] {
                WeatherUtils.getWeatherIcon(
                        WeatherUtils.getWeatherKind(weather.live.weather),
                        isDay),
                WeatherUtils.getWeatherIcon(
                        isDay
                                ?
                                WeatherUtils.getWeatherKind(weather.dailyList.get(0).weathers[0])
                                :
                                WeatherUtils.getWeatherKind(weather.dailyList.get(0).weathers[1]),
                        isDay),
                WeatherUtils.getWeatherIcon(
                        isDay
                                ?
                                WeatherUtils.getWeatherKind(weather.dailyList.get(1).weathers[0])
                                :
                                WeatherUtils.getWeatherKind(weather.dailyList.get(1).weathers[1]),
                        isDay),
                WeatherUtils.getWeatherIcon(
                        isDay
                                ?
                                WeatherUtils.getWeatherKind(weather.dailyList.get(2).weathers[0])
                                :
                                WeatherUtils.getWeatherKind(weather.dailyList.get(2).weathers[1]),
                        isDay),
        };
        for (int i = 0; i < imageIds.length; i ++) {
            if (imageIds[i][3] != 0) {
                if (i == 0) {
                    Bitmap ic = BitmapFactory.decodeResource(context.getResources(), imageIds[i][3]);
                    iconNow.setImageBitmap(ic);
                } else {
                    Bitmap ic = BitmapFactory.decodeResource(context.getResources(), imageIds[i][3]);
                    icons[i - 1].setImageBitmap(ic);
                }
            }
        }

        location.setText(weather.base.location);
        weatherNow.setText(weather.live.weather + " " + weather.live.temp + "℃");
        tempNow.setText(context.getString(R.string.temp) +  weather.dailyList.get(0).temps[1] + "/" + weather.dailyList.get(0).temps[0] + "°");
        wind.setText(weather.live.windDir +  weather.live.windLevel);
        air.setText(weather.live.air);
        for (int i = 0; i < weeks.length; i ++) {
            weeks[i].setText(weather.dailyList.get(i).week);
            temps[i].setText(weather.dailyList.get(i).temps[1] + "/" + weather.dailyList.get(i).temps[0] + "°");
        }

        view.setDrawingCacheEnabled(true);
        view.measure(
                View.MeasureSpec.makeMeasureSpec(
                        context.getResources().getDisplayMetrics().widthPixels,
                        View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(
                        (int) DisplayUtils.dpToPx(context, 260),
                        View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String oldUri = sharedPreferences.getString(context.getString(R.string.key_share_uri), "null");
        if (! oldUri.equals("null")) {
            deleteSharePicture(context, Uri.parse(oldUri));
        }

        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, null, null));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.key_share_uri), uri.toString());
        editor.apply();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        context.startActivity(
                Intent.createChooser(
                        shareIntent,
                        context.getResources().getText(R.string.action_share)));
    }

    private static void deleteSharePicture(Context context, Uri uri) {
        if (uri != null) {
            context.getContentResolver().delete(uri, null, null);
        }
    }
}
