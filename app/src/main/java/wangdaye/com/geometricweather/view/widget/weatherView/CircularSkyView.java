package wangdaye.com.geometricweather.view.widget.weatherView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.util.Calendar;

import wangdaye.com.geometricweather.model.data.Weather;
import wangdaye.com.geometricweather.utils.DisplayUtils;
import wangdaye.com.geometricweather.utils.TimeUtils;

/**
 * Show the sky.
 * */

public class CircularSkyView extends View {
    // widget
    private Paint paint;

    // data
    private int[] exchangeTimes = new int[] {60 * 6, 60 * 18};
    private int systemTime = 60 * Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            + Calendar.getInstance().get(Calendar.MINUTE);
    private float mixRatio = 0.5F;

    private float unitRadius;
    private float[] radius;
    private float cX, cY;
    private boolean isDay;
    private float animTime;

    private final int ANIM_LENGTH = 1500;

    private int state = NORMAL_STATE;
    public static final int NORMAL_STATE = 0;
    public static final int SHOWING_STATE = 1;
    public static final int TOUCHING_STATE = 2;
    public static final int HIDING_STATE = -1;

    private SkyAnimListener listener;

    /** <br> life cycle. */

    public CircularSkyView(Context context) {
        super(context);
        this.initialize();
    }

    public CircularSkyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public CircularSkyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircularSkyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        this.paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        this.radius = new float[4];
        this.isDay = TimeUtils.getInstance(getContext()).isDay;
    }

    /** <br> UI. */

    public void showCircle(boolean isDay) {
        if (this.isDay != isDay) {
            this.isDay = isDay;
            animHide();
        } else if (state != SHOWING_STATE) {
            animShow();
        }
    }

    public void touchCircle() {
        animTouch();
    }

    /** <br> measure */

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = (int) (width / 6.8 * 4 + DisplayUtils.dpToPx(getContext(), 40 + 16));
        setMeasuredDimension(width, height);

        unitRadius = (float) (width / 6.8);
        for (int i = 0; i < radius.length; i ++) {
            radius[i] = unitRadius * (i + 1);
        }
        cX = (float) (getMeasuredWidth() / 2.0);
        cY = getMeasuredHeight();
    }

    /** <br> draw. */

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawFourthFloor(canvas);
        drawThirdFloor(canvas);
        drawSecondFloor(canvas);
        drawFirstFloor(canvas);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(getColor(5));
    }

    private void drawFourthFloor(Canvas canvas) {
        paint.setColor(getColor(4));
        canvas.drawCircle(cX, cY, radius[3], paint);
    }

    private void drawThirdFloor(Canvas canvas) {
        paint.setColor(getColor(3));
        canvas.drawCircle(cX, cY, radius[2], paint);
    }

    private void drawSecondFloor(Canvas canvas) {
        paint.setColor(getColor(2));
        canvas.drawCircle(cX, cY, radius[1], paint);
    }

    private void drawFirstFloor(Canvas canvas) {
        paint.setColor(getColor(1));
        canvas.drawCircle(cX, cY, radius[0], paint);
    }

    private int getColor(int floor) {
        if (exchangeTimes[0] == exchangeTimes[1]) {
            return Color.TRANSPARENT;
        }
        switch (floor) {
            case 1:
                return Color.rgb(
                        (int) (207 * mixRatio + 63 * (1 - mixRatio)),
                        (int) (235 * mixRatio + 67 * (1 - mixRatio)),
                        (int) (240 * mixRatio + 95 * (1 - mixRatio)));

            case 2:
                return Color.rgb(
                        (int) (182 * mixRatio + 52 * (1 - mixRatio)),
                        (int) (227 * mixRatio + 56 * (1 - mixRatio)),
                        (int) (231 * mixRatio + 81 * (1 - mixRatio)));

            case 3:
                return Color.rgb(
                        (int) (150 * mixRatio + 44 * (1 - mixRatio)),
                        (int) (214 * mixRatio + 47 * (1 - mixRatio)),
                        (int) (219 * mixRatio + 67 * (1 - mixRatio)));

            case 4:
                return Color.rgb(
                        (int) (122 * mixRatio + 32 * (1 - mixRatio)),
                        (int) (199 * mixRatio + 34 * (1 - mixRatio)),
                        (int) (211 * mixRatio + 47 * (1 - mixRatio)));

            default:
                switch (state) {
                    case SHOWING_STATE:
                        return Color.argb(
                                (int) (255 * animTime),
                                (int) (117 * mixRatio + 26 * (1 - mixRatio)),
                                (int) (190 * mixRatio + 27 * (1 - mixRatio)),
                                (int) (203 * mixRatio + 34 * (1 - mixRatio)));

                    case HIDING_STATE:
                        return Color.argb(
                                (int) (255 * (1 - animTime)),
                                (int) (117 * mixRatio + 26 * (1 - mixRatio)),
                                (int) (190 * mixRatio + 27 * (1 - mixRatio)),
                                (int) (203 * mixRatio + 34 * (1 - mixRatio)));

                    default:
                        return Color.rgb(
                                (int) (117 * mixRatio + 26 * (1 - mixRatio)),
                                (int) (190 * mixRatio + 27 * (1 - mixRatio)),
                                (int) (203 * mixRatio + 34 * (1 - mixRatio)));
                }
        }
    }

    /** <br> data. */

    public void setExchangeTime(Weather weather) {
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
    }

    private void calcRadius() {
        switch (state) {
            case NORMAL_STATE:
                radius = new float[] {unitRadius, unitRadius * 2, unitRadius * 3, unitRadius * 4};
                break;

            case SHOWING_STATE:
                radius = new float[] {
                        (float) Math.min(unitRadius / 0.2 * animTime, unitRadius),
                        (float) Math.min(unitRadius / 0.2 * animTime, unitRadius * 2),
                        (float) Math.min(unitRadius / 0.2 * animTime, unitRadius * 3),
                        (float) Math.min(unitRadius / 0.2 * animTime, unitRadius * 4)};
                break;

            case HIDING_STATE:
                radius = new float[] {
                        unitRadius * (1 - animTime),
                        unitRadius * (1 - animTime) * 2,
                        unitRadius * (1 - animTime) * 3,
                        unitRadius * (1 - animTime) * 4};
                break;

            case TOUCHING_STATE:
                float partTime = (float) (1.0 / 6.0);
                if (isDay) {
                    if (animTime < partTime) {
                        radius = new float[] {
                                (float) (unitRadius * (1 + 0.15 * (animTime / partTime))),
                                unitRadius * 2,
                                unitRadius * 3,
                                unitRadius * 4};
                    } else if (animTime < partTime * 2) {
                        radius = new float[] {
                                (float) (unitRadius * (1.15 - 0.25 * ((animTime - partTime) / partTime))),
                                (float) (unitRadius * (2 + 0.2 * ((animTime - partTime) / partTime))),
                                unitRadius * 3,
                                unitRadius * 4};
                    } else if (animTime < partTime * 3) {
                        radius = new float[] {
                                (float) (unitRadius * (0.9 + 0.1 * ((animTime - 2 * partTime) / partTime))),
                                (float) (unitRadius * (2.2 - 0.3 * ((animTime - 2 * partTime) / partTime))),
                                (float) (unitRadius * (3 + 0.25 * ((animTime - 2 * partTime) / partTime))),
                                unitRadius * 4};
                    } else if (animTime < partTime * 4) {
                        radius = new float[] {
                                unitRadius,
                                (float) (unitRadius * (1.9 + 0.1 * ((animTime - 3 * partTime) / partTime))),
                                (float) (unitRadius * (3.25 - 0.35 * ((animTime - 3 * partTime) / partTime))),
                                (float) (unitRadius * (4 + 0.3 * ((animTime - 3 * partTime) / partTime)))};
                    } else if (animTime < partTime * 5) {
                        radius = new float[] {
                                unitRadius,
                                unitRadius * 2,
                                (float) (unitRadius * (2.9 + 0.1 * ((animTime - 4 * partTime) / partTime))),
                                (float) (unitRadius * (4.3 - 0.4 * ((animTime - 4 * partTime) / partTime)))};
                    } else {
                        radius = new float[] {
                                unitRadius,
                                unitRadius * 2,
                                unitRadius * 3,
                                (float) (unitRadius * (3.9 + 0.1 * ((animTime - 5 * partTime) / partTime)))};
                    }
                } else {
                    if (animTime < partTime) {
                        radius = new float[] {
                                (float) (unitRadius * (1 + 0.15 * (animTime / partTime))),
                                unitRadius * 2,
                                unitRadius * 3,
                                unitRadius * 4};
                    } else if (animTime < partTime * 2) {
                        radius = new float[] {
                                (float) (unitRadius * (1.15 - 0.2 * ((animTime - partTime) / partTime))),
                                (float) (unitRadius * (2 + 0.12 * ((animTime - partTime) / partTime))),
                                unitRadius * 3,
                                unitRadius * 4};
                    } else if (animTime < partTime * 3) {
                        radius = new float[] {
                                (float) (unitRadius * (0.95 + 0.05 * ((animTime - 2 * partTime) / partTime))),
                                (float) (unitRadius * (2.12 - 0.17 * ((animTime - 2 * partTime) / partTime))),
                                (float) (unitRadius * (3 + 0.09 * ((animTime - 2 * partTime) / partTime))),
                                unitRadius * 4};
                    } else if (animTime < partTime * 4) {
                        radius = new float[] {
                                unitRadius,
                                (float) (unitRadius * (1.95 + 0.05 * ((animTime - 3 * partTime) / partTime))),
                                (float) (unitRadius * (3.09 - 0.14 * ((animTime - 3 * partTime) / partTime))),
                                (float) (unitRadius * (4 + 0.06 * ((animTime - 3 * partTime) / partTime)))};
                    } else if (animTime < partTime * 5) {
                        radius = new float[] {
                                unitRadius,
                                unitRadius * 2,
                                (float) (unitRadius * (2.95 + 0.05 * ((animTime - 4 * partTime) / partTime))),
                                (float) (unitRadius * (4.06 - 0.11 * ((animTime - 4 * partTime) / partTime)))};
                    } else {
                        radius = new float[] {
                                unitRadius,
                                unitRadius * 2,
                                unitRadius * 3,
                                (float) (unitRadius * (3.95 + 0.05 * ((animTime - 5 * partTime) / partTime)))};
                    }
                }
                break;
        }
    }

    private void calcMixRatio() {
        mixRatio = getMixRatio(systemTime, exchangeTimes, isDay);
    }

    public static float getMixRatio(int systemTime, int[] exchangeTimes, boolean isDay) {
        if (exchangeTimes[0] == exchangeTimes[1]) {
            return isDay ? 0.5F : 0.5F;
        }

        int midday = (int) ((exchangeTimes[0] + exchangeTimes[1]) / 2.0);

        int time = systemTime;
        if (isDay) {
            if (systemTime <= exchangeTimes[0] || systemTime > exchangeTimes[1]) {
                time = (time + 12 * 60) % (24 * 60);
            }
        } else {
            if (exchangeTimes[0] < systemTime && systemTime <= exchangeTimes[1]) {
                time = (time + 12 * 60) % (24 * 60);
            }
        }

        float mixRatio;

        if (exchangeTimes[0] < time && time <= exchangeTimes[1]) {
            mixRatio = (float) (0.5 * (1 + (1 - Math.abs(time - midday) / (1.0 * (exchangeTimes[1] - exchangeTimes[0])))));
            // mixRatio = 0.5 * (1 + |time - midday| / (ss - sr))
        } else {
            int calcTime = (time + 12 * 60) % (24 * 60);
            mixRatio = (float) (0.5 * (1 - (1 - Math.abs(calcTime - midday) / (1.0 * (24 * 60 - exchangeTimes[1] + exchangeTimes[0])))));
            // |time - midnight| = |(time + 12h) % 24h - midday|
            // calcTime = (time + 12h) % 24h
            // mixRatio = 0.5 * (1 - |calcTime - midday| / (24h - (ss - sr)))
        }

        if (mixRatio > 0.5) {
            mixRatio = (float) Math.pow(mixRatio, 1.0 / 2.0);
        } else if (mixRatio < 0.5) {
            mixRatio = (float) Math.pow(mixRatio, 3.0 / 2.0);
        }

        return mixRatio;
    }

    /** <br> anim. */

    // methods.

    private void animShow() {
        if (listener != null) {
            listener.cancel();
        }
        listener = new SkyAnimListener(SkyAnimListener.TYPE_END);

        animShow.setDuration(ANIM_LENGTH);
        animShow.setAnimationListener(listener);

        calcMixRatio();
        clearAnimation();
        startAnimation(animShow);
    }

    private void animHide() {
        if (listener != null) {
            listener.cancel();
        }
        listener = new SkyAnimListener(SkyAnimListener.TYPE_CONTAINER);

        animHide.setDuration((long) (ANIM_LENGTH / 3.0));
        animHide.setAnimationListener(listener);

        clearAnimation();
        startAnimation(animHide);
    }

    private void animTouch() {
        if (listener != null) {
            listener.cancel();
        }
        listener = new SkyAnimListener(SkyAnimListener.TYPE_END);

        animTouch.setDuration(ANIM_LENGTH);
        animTouch.setAnimationListener(listener);

        clearAnimation();
        startAnimation(animTouch);
    }

    // animations.

    private Animation animShow = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            state = SHOWING_STATE;
            animTime = interpolatedTime;
            calcRadius();
            invalidate();
        }
    };

    private Animation animHide = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            state = HIDING_STATE;
            animTime = interpolatedTime;
            calcRadius();
            invalidate();
        }
    };

    private Animation animTouch = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            state = TOUCHING_STATE;
            animTime = interpolatedTime;
            calcRadius();
            invalidate();
        }
    };

    // listeners.

    private class SkyAnimListener implements Animation.AnimationListener {
        // data
        private boolean canceled;

        private int type;
        static final int TYPE_END = 0;
        static final int TYPE_CONTAINER = 1;

        // life cycle.

        SkyAnimListener(int type) {
            this.canceled = false;
            this.type = type;
        }

        // data

        public void cancel() {
            canceled = false;
        }

        // interface.

        @Override
        public void onAnimationStart(Animation animation) {
            // do nothing.
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!canceled) {
                switch (type) {
                    case TYPE_END:
                        state = NORMAL_STATE;
                        calcRadius();
                        invalidate();
                        break;

                    case TYPE_CONTAINER:
                        animShow();
                        break;
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
            // do nothing.
        }
    }
}