package wangdaye.com.geometricweather.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

/**
 * Swipe switch layout.
 * */

public class SwipeSwitchLayout extends FrameLayout {
    // widget
    private View target;
    private OnSwipeListener listener;

    // data
    private float swipeDistance;
    private float swipeTrigger;
    private float oldX, deltaX;
    private float initialX, initialY;
    private int touchSlop;
    private boolean animating = false;

    private int swipeState;
    private static final int NOT_SWIPING = 0;
    private static final int SWITCH_STATE = 1;
    private static final int FILTER_STATE = 2;

    public static final int DIRECTION_LEFT = -1;
    public static final int DIRECTION_RIGHT = 1;

    /** <br> life cycle. */

    public SwipeSwitchLayout(Context context) {
        super(context);
        this.initialize();
    }

    public SwipeSwitchLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initialize();
    }

    public SwipeSwitchLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwipeSwitchLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.initialize();
    }

    private void initialize() {
        this.swipeDistance = 0;
        this.swipeTrigger = (int) (getContext().getResources().getDisplayMetrics().widthPixels / 2.5);
        this.touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        this.swipeState = NOT_SWIPING;
    }

    /** <br> layout. */

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.getTarget();

        float realDistance = swipeDistance;
        if (realDistance > swipeTrigger) {
            realDistance = swipeTrigger;
        } else if (realDistance < -swipeTrigger) {
            realDistance = -swipeTrigger;
        }
        target.layout(
                (int) realDistance,
                0,
                (int) (target.getMeasuredWidth() + realDistance),
                target.getMeasuredHeight());
        target.setAlpha(1 - Math.abs(realDistance) / swipeTrigger);
    }

    public void reset() {
        swipeState =NOT_SWIPING;
        animating = false;
        swipeDistance = 0;
        this.requestLayout();
    }

    private void getTarget() {
        if (target == null) {
            target = getChildAt(0);
        }
    }

    /** <br> touch. */

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        if (!isEnabled() || animating) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                initialY = ev.getY();
                oldX = ev.getX();
                deltaX = 0;
                swipeDistance = 0;
                swipeState = NOT_SWIPING;
                return true;

            case MotionEvent.ACTION_MOVE:
                deltaX = Math.abs(ev.getX() - oldX);
                oldX = ev.getX();
                if (Math.abs(ev.getX() - initialX) < touchSlop) {
                    return false;
                }
                if (swipeState == NOT_SWIPING) {
                    if (Math.abs(ev.getX() - initialX) > Math.abs(ev.getY() - initialY)) {
                        swipeState = SWITCH_STATE;
                    } else {
                        swipeState = FILTER_STATE;
                    }
                }
                if (swipeState == SWITCH_STATE) {
                    swipeDistance = ev.getX() - initialX;
                    requestLayout();
                    return false;
                } else {
                    swipeDistance = 0;
                    return true;
                }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (swipeState == SWITCH_STATE) {
                    swipeState = NOT_SWIPING;
                    animating = true;

                    if (Math.abs(swipeDistance) > Math.abs(swipeTrigger)
                            || deltaX > swipeTrigger / 25.0) {
                        if (listener != null) {
                            listener.swipeTakeEffect(ev.getX() < initialX ?
                                    DIRECTION_LEFT
                                    :
                                    DIRECTION_RIGHT);
                        }
                    } else {
                        animating = true;
                        resetAnimation.reset();
                        resetAnimation.setDuration(300);
                        resetAnimation.setInterpolator(new DecelerateInterpolator());
                        startAnimation(resetAnimation);
                    }
                    return false;
                } else {
                    swipeState = NOT_SWIPING;
                    return true;
                }
        }
        return true;
    }

    /** <br> animate. */

    private Animation resetAnimation = new Animation() {

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            if (interpolatedTime < 1) {
                swipeDistance *= (1 - interpolatedTime);
            } else {
                swipeDistance = 0;
                animating = false;
            }
            requestLayout();
        }
    };

    /** <br> interface. */

    public interface OnSwipeListener {
        void swipeTakeEffect(int direction);
    }

    public void setOnSwipeListener(OnSwipeListener l) {
        this.listener = l;
    }
}
