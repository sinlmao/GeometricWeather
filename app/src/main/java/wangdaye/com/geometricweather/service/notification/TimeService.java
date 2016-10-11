package wangdaye.com.geometricweather.service.notification;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import wangdaye.com.geometricweather.receiver.MyReceiver;

/**
 * Time listener.
 * */

public class TimeService extends Service {
    // widget
    private MyReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        if (this.receiver == null) {
            this.receiver = new MyReceiver();
            registerReceiver(receiver, intentFilter);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.receiver != null) {
            unregisterReceiver(receiver);
        }
    }
}
