package wangdaye.com.geometricweather.receiver.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import wangdaye.com.geometricweather.service.widget.WidgetClockDayCenterService;

/**
 * Widget clock day center provider.
 * */

public class WidgetClockDayCenterProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        context.startService(new Intent(context, WidgetClockDayCenterService.class));
    }
}
