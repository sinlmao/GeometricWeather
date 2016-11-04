package wangdaye.com.geometricweather.model.service;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import wangdaye.com.geometricweather.model.api.JuheApi;
import wangdaye.com.geometricweather.model.data.JuheResult;
import wangdaye.com.geometricweather.utils.WeatherUtils;

/**
 * Juhe weather.
 * */

public class JuheWeather {
    // widget
    private Call call;

    /** <br> data. */

    public JuheWeather requestJuheWeather(final String name, String realName,
                                          final WeatherUtils.OnRequestWeatherListener l) {
        Call<JuheResult> getJuheWeather = buildApi().getJuheWeather(realName, JuheApi.APP_KEY);
        getJuheWeather.enqueue(new Callback<JuheResult>() {
            @Override
            public void onResponse(Call<JuheResult> call, Response<JuheResult> response) {
                if (l != null) {
                    if (response.isSuccessful() && response.body() != null) {
                        l.requestJuheWeatherSuccess(response.body(), name);
                    } else {
                        l.requestWeatherFailed(name);
                    }
                }
            }

            @Override
            public void onFailure(Call<JuheResult> call, Throwable t) {
                if (l != null) {
                    l.requestWeatherFailed(name);
                }
            }
        });
        call = getJuheWeather;
        return this;
    }

    public void cancel() {
        if (call != null) {
            call.cancel();
        }
    }

    public static JuheWeather getService() {
        return new JuheWeather();
    }

    private JuheApi buildApi() {
        return new Retrofit.Builder()
                .baseUrl(JuheApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((JuheApi.class));
    }
}
