package wangdaye.com.geometricweather.utils;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.HefengResult;
import wangdaye.com.geometricweather.model.data.JuheResult;
import wangdaye.com.geometricweather.model.data.Location;
import wangdaye.com.geometricweather.model.service.HefengWeather;
import wangdaye.com.geometricweather.model.service.JuheWeather;

/**
 * Weather kind tools.
 * */

public class WeatherUtils {
    // widget
    private JuheWeather juheWeather;
    private HefengWeather hefengWeather;

    /** <br> life cycle. */

    public WeatherUtils() {
        juheWeather = null;
        hefengWeather = null;
    }

    /** <br> data. */

    public void requestWeather(String name, String realName, OnRequestWeatherListener l) {
        if (Location.isEngLocation(realName)) {
            hefengWeather = HefengWeather.getService()
                    .requestHefengWeather(name, realName, l);
        } else {
            juheWeather = JuheWeather.getService()
                    .requestJuheWeather(name, realName, l);
        }
    }

    public void cancel() {
        if (juheWeather != null) {
            juheWeather.cancel();
        }
        if (hefengWeather != null) {
            hefengWeather.cancel();
        }
    }

    /** <br> utils. */

    public static String getWeatherKind(String weatherInfo) {
        if (Location.isEngLocation(weatherInfo)) {
            return getEngWeatherKind(weatherInfo);
        }

        if(weatherInfo.contains("雨")) {
            if(weatherInfo.contains("雪")) {
                return "雨夹雪";
            } else if(weatherInfo.contains("雷")) {
                return "雷雨";
            } else {
                return "雨";
            }
        }
        if(weatherInfo.contains("雷")) {
            if (weatherInfo.contains("雨")) {
                return "雷雨";
            } else {
                return "雷";
            }
        }
        if (weatherInfo.contains("雪")) {
            if(weatherInfo.contains("雨")) {
                return "雨夹雪";
            } else {
                return "雪";
            }
        }
        if (weatherInfo.contains("雹")) {
            return "冰雹";
        }
        if (weatherInfo.contains("冰")) {
            return "冰雹";
        }
        if (weatherInfo.contains("冻")) {
            return "冰雹";
        }
        if (weatherInfo.contains("云")) {
            return "云";
        }
        if (weatherInfo.contains("阴")) {
            return "阴";
        }
        if (weatherInfo.contains("风")) {
            return "风";
        }
        if(weatherInfo.contains("沙")) {
            return "霾";
        }
        if(weatherInfo.contains("尘")) {
            return "霾";
        }
        if(weatherInfo.contains("雾")) {
            return "雾";
        }
        if(weatherInfo.contains("霾")) {
            return "霾";
        }
        if (weatherInfo.contains("晴")) {
            return "晴";
        }
        return "阴";
    }

    private static String getEngWeatherKind(String weatherInfo) {
        if (weatherInfo.contains("snow") || weatherInfo.contains("Snow")) {
            return "雪";
        }
        if(weatherInfo.contains("rain") || weatherInfo.contains("Rain")
                || weatherInfo.contains("drizzle") || weatherInfo.contains("Drizzle")) {
            return "雨";
        }
        if (weatherInfo.contains("thunderstorm") || weatherInfo.contains("Thunderstorm")) {
            return "雷雨";
        }
        if (weatherInfo.contains("thunder") || weatherInfo.contains("Thunder")) {
            return "雷";
        }
        if (weatherInfo.contains("storm") || weatherInfo.contains("Storm")) {
            return "雨";
        }
        if(weatherInfo.contains("sleet") || weatherInfo.contains("Sleet")) {
            return "雨夹雪";
        }
        if (weatherInfo.contains("hail") || weatherInfo.contains("Hail")) {
            return "冰雹";
        }
        if (weatherInfo.contains("frost") || weatherInfo.contains("Frost")) {
            return "冰雹";
        }
        if (weatherInfo.contains("冻")) {
            return "冰雹";
        }
        if (weatherInfo.contains("overcast") || weatherInfo.contains("Overcast")) {
            return "阴";
        }
        if (weatherInfo.contains("cloudy") || weatherInfo.contains("Cloudy")) {
            return "云";
        }
        if (weatherInfo.contains("typhoon") || weatherInfo.contains("Typhoon")
                || weatherInfo.contains("wind") || weatherInfo.contains("Wind")) {
            return "风";
        }
        if(weatherInfo.contains("sandstorm") || weatherInfo.contains("Sandstorm")
                || weatherInfo.contains("dust") || weatherInfo.contains("Dust")) {
            return "霾";
        }
        if(weatherInfo.contains("fog") || weatherInfo.contains("Fog")
                || weatherInfo.contains("mist") || weatherInfo.contains("Mist")) {
            return "雾";
        }
        if(weatherInfo.contains("haze") || weatherInfo.contains("Haze")) {
            return "霾";
        }
        if (weatherInfo.contains("sunny") || weatherInfo.contains("Sunny")
                || weatherInfo.contains("fair") || weatherInfo.contains("Fair")
                || weatherInfo.contains("clear") || weatherInfo.contains("Clear")) {
            return "晴";
        }
        return "阴";
    }

    public static int[] getWeatherIcon(String weatherKind, boolean isDay) {
        int[] imageId = new int[4];

        switch (weatherKind) {
            case "晴":
                if(isDay) {
                    imageId[0] = R.drawable.weather_sun_circle;
                    imageId[1] = R.drawable.weather_sun_shine;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_sun_day;
                } else {
                    imageId[0] = R.drawable.weather_sun_night;
                    imageId[1] = 0;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_sun_night;
                }
                break;
            case "云":
                if(isDay) {
                    imageId[0] = R.drawable.weather_cloud_right;
                    imageId[1] = R.drawable.weather_sun_circle;
                    imageId[2] = R.drawable.weather_sun_shine;
                    imageId[3] = R.drawable.weather_cloud_day;
                } else {
                    imageId[0] = R.drawable.weather_cloud_left;
                    imageId[1] = R.drawable.weather_moon;
                    imageId[2] = 0;
                    imageId[3] = R.drawable.weather_cloud_night;
                }
                break;
            case "阴":
                imageId[0] = R.drawable.weather_cloud_top;
                imageId[1] = R.drawable.weather_cloud_large;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_cloudy;
                break;
            case "雨":
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_rain_left;
                imageId[2] = R.drawable.weather_rain_right;
                imageId[3] = R.drawable.weather_rain;
                break;
            case "风":
                imageId[0] = R.drawable.weather_wind;
                imageId[1] = 0;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_wind;
                break;
            case "雪":
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_snow_left;
                imageId[2] = R.drawable.weather_snow_right;
                imageId[3] = R.drawable.weather_snow;
                break;
            case "雾":
                imageId[0] = R.drawable.weather_fog;
                imageId[1] = R.drawable.weather_fog;
                imageId[2] = R.drawable.weather_fog;
                imageId[3] = R.drawable.weather_fog;
                break;
            case "霾":
                imageId[0] = R.drawable.weather_haze_1;
                imageId[1] = R.drawable.weather_haze_2;
                imageId[2] = R.drawable.weather_haze_3;
                imageId[3] = R.drawable.weather_haze;
                break;
            case "雨夹雪":
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_snow_left;
                imageId[2] = R.drawable.weather_rain_right;
                imageId[3] = R.drawable.weather_sleet;
                break;
            case "雷雨":
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_single_thunder;
                imageId[2] = R.drawable.weather_rain_right;
                imageId[3] = R.drawable.weather_thunderstorm;
                break;
            case "雷":
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_single_thunder;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_thunder;
                break;
            case "冰雹":
                imageId[0] = R.drawable.weather_cloud_large;
                imageId[1] = R.drawable.weather_hail_left;
                imageId[2] = R.drawable.weather_hail_right;
                imageId[3] = R.drawable.weather_hail;
                break;
            default:
                imageId[0] = R.drawable.weather_cloud_top;
                imageId[1] = R.drawable.weather_cloud_large;
                imageId[2] = 0;
                imageId[3] = R.drawable.weather_cloudy;
                break;
        }
        return imageId;
    }

    public static int[] getAnimatorId(String weatherKind, boolean isDay) {
        int[] animatorId = new int[3];

        switch (weatherKind) {
            case "晴":
                if(isDay) {
                    animatorId[0] = R.animator.weather_sun_day_1;
                    animatorId[1] = R.animator.weather_sun_day_2;
                    animatorId[2] = 0;
                } else {
                    animatorId[0] = R.animator.weather_sun_night;
                    animatorId[1] = 0;
                    animatorId[2] = 0;
                }
                break;
            case "云":
                if(isDay) {
                    animatorId[0] = R.animator.weather_cloud_day_1;
                    animatorId[1] = R.animator.weather_cloud_day_2;
                    animatorId[2] = R.animator.weather_cloud_day_3;
                } else {
                    animatorId[0] = R.animator.weather_cloud_night_1;
                    animatorId[1] = R.animator.weather_cloud_night_2;
                    animatorId[2] = 0;
                }
                break;
            case "阴":
                animatorId[0] = R.animator.weather_cloudy_1;
                animatorId[1] = R.animator.weather_cloudy_2;
                animatorId[2] = 0;
                break;
            case "雨":
                animatorId[0] = R.animator.weather_rain_1;
                animatorId[1] = R.animator.weather_rain_2;
                animatorId[2] = R.animator.weather_rain_3;
                break;
            case "风":
                animatorId[0] = R.animator.weather_wind;
                animatorId[1] = 0;
                animatorId[2] = 0;
                break;
            case "雪":
                animatorId[0] = R.animator.weather_snow_1;
                animatorId[1] = R.animator.weather_snow_2;
                animatorId[2] = R.animator.weather_snow_3;
                break;
            case "雾":
                animatorId[0] = R.animator.weather_fog_1;
                animatorId[1] = R.animator.weather_fog_2;
                animatorId[2] = R.animator.weather_fog_3;
                break;
            case "霾":
                animatorId[0] = R.animator.weather_haze_1;
                animatorId[1] = R.animator.weather_haze_2;
                animatorId[2] = R.animator.weather_haze_3;
                break;
            case "雨夹雪":
                animatorId[0] = R.animator.weather_sleet_1;
                animatorId[1] = R.animator.weather_sleet_2;
                animatorId[2] = R.animator.weather_sleet_3;
                break;
            case "雷雨":
                animatorId[0] = R.animator.weather_thunderstorm_1;
                animatorId[1] = R.animator.weather_thunderstorm_2;
                animatorId[2] = R.animator.weather_thunderstorm_3;
                break;
            case "雷":
                animatorId[0] = R.animator.weather_thunder_1;
                animatorId[1] = R.animator.weather_thunder_2;
                animatorId[2] = R.animator.weather_thunder_2;
                break;
            case "冰雹":
                animatorId[0] = R.animator.weather_hail_1;
                animatorId[1] = R.animator.weather_hail_2;
                animatorId[2] = R.animator.weather_hail_3;
                break;
            default:
                animatorId[0] = R.animator.weather_cloudy_1;
                animatorId[1] = R.animator.weather_cloudy_2;
                animatorId[2] = 0;
                break;
        }
        return animatorId;
    }

    static int getMiniWeatherIcon(String weatherInfo, boolean isDay) {
        int imageId;
        switch (weatherInfo) {
            case "晴":
                if(isDay) {
                    imageId = R.drawable.weather_sun_day_mini;
                } else {
                    imageId = R.drawable.weather_sun_night_mini;
                }
                break;
            case "云":
                if(isDay) {
                    imageId = R.drawable.weather_cloud_day_mini;
                } else {
                    imageId = R.drawable.weather_cloud_mini;
                }
                break;
            case "雨":
                imageId = R.drawable.weather_rain_mini;
                break;
            case "风":
                imageId = R.drawable.weather_wind_mini;
                break;
            case "雪":
                imageId = R.drawable.weather_snow_mini;
                break;
            case "雾":
                imageId = R.drawable.weather_fog_mini;
                break;
            case "霾":
                imageId = R.drawable.weather_haze_mini;
                break;
            case "雨夹雪":
                imageId = R.drawable.weather_sleet_mini;
                break;
            case "雷雨":
                imageId = R.drawable.weather_thunder_mini;
                break;
            case "雷":
                imageId = R.drawable.weather_thunder_mini;
                break;
            case "冰雹":
                imageId = R.drawable.weather_hail_mini;
                break;
            case "阴":
            default:
                imageId = R.drawable.weather_cloud_mini;
                break;
        }
        return imageId;
    }

    /** <br> listener. */

    public interface OnRequestWeatherListener {
        void requestJuheWeatherSuccess(JuheResult result, String name);
        void requestHefengWeatherSuccess(HefengResult result, String name);
        void requestWeatherFailed(String name);
    }
}
