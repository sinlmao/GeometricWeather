package wangdaye.com.geometricweather.view.widget.weatherView;

import android.content.Context;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

import java.util.Calendar;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.utils.DisplayUtils;

/**
 * Weather icon control view.
 * */

public class WeatherIconControlView extends FrameLayout {
    // widget
    private OnWeatherIconChangingListener iconListener;
    private AnimListener animListener;

    // data
    private boolean rose = false;
    private int[] exchangeTimes = new int[] {6 * 60, 18 * 60};
    private int systemTime = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            + Calendar.getInstance().get(Calendar.MINUTE);

    private int cX;
    private int iconSize;
    private int radius;
    private int SHOW_MARGIN;

    private boolean dynamicPosition;

    /** <br> life cycle. */

    public WeatherIconControlView(Context context) {
        super(context);
        this.initialize();
    }

    public WeatherIconControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public WeatherIconControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WeatherIconControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        iconSize = (int) DisplayUtils.dpToPx(getContext(), 80 + 16 * 2);
        cX = (int) (-iconSize * 0.5);
        SHOW_MARGIN = iconSize;

        dynamicPosition = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(
                        getContext().getString(R.string.key_weather_icon_position),
                        false);
    }

    /** <br> measure. */

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                getChildAt(0).getMeasuredWidth(),
                getChildAt(0).getMeasuredHeight());

        radius = (int) (getMeasuredWidth() / 6.8 * 4.0);
    }

    /** <br> layout. */

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View child;

        child = getChildAt(0);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        child = getChildAt(1);
        child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());

        child = getChildAt(2);
        child.layout(
                getIconLeft(),
                getIconTop(),
                getIconLeft() + iconSize,
                getIconTop() + iconSize);
    }

    /** <br> UI. */

    public void showWeatherIcon(Weather weather) {
        if (weather != null) {
            exchangeTimes = new int[] {
                    60 * Integer.parseInt(weather.dailyList.get(0).exchangeTimes[0].split(":")[0])
                            + Integer.parseInt(weather.dailyList.get(0).exchangeTimes[0].split(":")[1]),
                    60 * Integer.parseInt(weather.dailyList.get(0).exchangeTimes[1].split(":")[0])
                            + Integer.parseInt(weather.dailyList.get(0).exchangeTimes[1].split(":")[1])};
        } else {
            exchangeTimes = new int[] {60 * 6, 60 * 18};
        }

        systemTime = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                + Calendar.getInstance().get(Calendar.MINUTE);

        if (rose) {
            animFall();
        } else {
            rose = true;
            animRise();
        }
    }

    /** <br> data. */

    private void calcCX(int startX, int endX, float time) {
        cX = (int) (startX + (endX - startX) * time);
    }

    private int getIconLeft() {
        return (int) (cX - iconSize * 0.5);
    }

    private int getIconTop() {
        return (int) (getIconCY() - iconSize * 0.5);
    }

    private int getIconCY() {
        return (int) (getMeasuredHeight()
                - Math.sqrt(Math.pow(radius, 2) - Math.pow(cX - getMeasuredWidth() / 2.0, 2)));
    }

    /** <br> animation. */

    private void animRise() {
        if (iconListener != null) {
            iconListener.OnWeatherIconChanging();
        }

        if (animListener != null) {
            animListener.canceled = true;
        }
        animListener =  new AnimListener(AnimListener.END_TYPE);

        AnimRise animation = new AnimRise();
        animation.setDuration(800);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(animListener);

        getChildAt(2).setVisibility(VISIBLE);
        clearAnimation();
        startAnimation(animation);
    }

    private void animFall() {
        if (animListener != null) {
            animListener.canceled = true;
        }
        animListener =  new AnimListener(AnimListener.CONTINUE_TYPE);

        AnimFall animation = new AnimFall();
        animation.setDuration(400);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setAnimationListener(animListener);

        clearAnimation();
        startAnimation(animation);
    }

    private class AnimRise extends Animation {
        // data
        private int startX;
        private int endX;

        AnimRise() {
            startX = (int) (getMeasuredWidth() / 2.0 - radius);
            if (dynamicPosition) {
                if (exchangeTimes[0] < systemTime && systemTime <= exchangeTimes[1]) {
                    endX = (int) (SHOW_MARGIN
                            + (getMeasuredWidth() - 2 * SHOW_MARGIN)
                            * (1.0 * (systemTime - exchangeTimes[0]) / (exchangeTimes[1] - exchangeTimes[0])));
                } else if (systemTime <= exchangeTimes[0]) {
                    endX = (int) (SHOW_MARGIN
                            + (getMeasuredWidth() - 2 * SHOW_MARGIN)
                            * (1.0 * (systemTime + 24 * 60 - exchangeTimes[1]) / (24 * 60 - exchangeTimes[1] + exchangeTimes[0])));
                } else {
                    endX = (int) (SHOW_MARGIN
                            + (getMeasuredWidth() - 2 * SHOW_MARGIN)
                            * (1.0 * (systemTime - exchangeTimes[1]) / (24 * 60 - exchangeTimes[1] + exchangeTimes[0])));
                }
            } else {
                endX = (int) (getMeasuredWidth() / 2.0);
            }
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            calcCX(startX, endX, interpolatedTime);
            requestLayout();
        }
    }

    private class AnimFall extends Animation {
        // data
        private int startX;
        private int endX;

        AnimFall() {
            startX = cX;
            endX = (int) (getMeasuredWidth() / 2.0 + radius);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            calcCX(startX, endX, interpolatedTime);
            requestLayout();
        }
    }

    private class AnimListener implements Animation.AnimationListener {
        // data
        private boolean canceled;
        private int type;

        static final int END_TYPE = 0;
        static final int CONTINUE_TYPE = 1;

        AnimListener(int type) {
            this.canceled = false;
            this.type = type;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            // do nothing.
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!canceled) {
                switch (type) {
                    case END_TYPE:
                        break;

                    case CONTINUE_TYPE:
                        getChildAt(2).setVisibility(GONE);
                        animRise();
                        break;
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // do nothing.
        }
    }

    /** <br> interface. */

    public interface OnWeatherIconChangingListener {
        void OnWeatherIconChanging();
    }

    public void setOnWeatherIconChangingListener(OnWeatherIconChangingListener l) {
        iconListener = l;
    }
}
