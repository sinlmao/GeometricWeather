package wangdaye.com.geometricweather.utils;

import android.content.Context;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.model.service.BaiduLocation;

/**
 * Location utils.
 * */

public class LocationUtils {
    // data
    private LocationClient client;

    /** <br> life cycle. */

    public LocationUtils(Context c) {
        client = new LocationClient(c);
    }

    /** <br> data. */

    public void requestLocation(OnRequestLocationListener l) {
        BaiduLocation.requestLocation(client, new SimpleBDLocationListener(l));
    }

    public void cancel() {
        if (client != null) {
            client.stop();
        }
    }

    /** <br> utils. */

    public static void simpleLocationFailedFeedback(Context context) {
        SnackbarUtils.showSnackbar(context.getString(R.string.feedback__location_failed));
    }

    /** <br> interface */

    public interface OnRequestLocationListener {
        void requestLocationSuccess(String locationName);
        void requestLocationFailed();
    }

    private static class SimpleBDLocationListener implements BDLocationListener {
        // data
        private OnRequestLocationListener l;

        SimpleBDLocationListener(OnRequestLocationListener l) {
            this.l = l;
        }

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            switch (bdLocation.getLocType()) {
                case BDLocation.TypeGpsLocation: // GPS定位结果
                case BDLocation.TypeNetWorkLocation: // 网络定位结果
                case BDLocation.TypeOffLineLocation: // 离线定位
                    if (l != null) {
                        l.requestLocationSuccess(bdLocation.getCity().split("市")[0]);
                    }
                    break;
                default:
                    if (l != null) {
                        l.requestLocationFailed();
                    }
                    break;
            }
        }
    }
}
