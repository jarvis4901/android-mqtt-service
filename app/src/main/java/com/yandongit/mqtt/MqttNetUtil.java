package com.yandongit.mqtt;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络连接工具类，用于断线重试时，避免断网了，不断重试调用MQTTClient的连接方法，不断报错，可能会大量耗电，是一种优化。
 */
public class MqttNetUtil {
    private MqttNetUtil() {
    }
    /**
     * 当前是否有网络状态
     *
     * @param context  上下文
     * @param needWifi 是否只有连接上wifi才算是连接上网络
     */
    public static boolean hasNetWorkStatus(Context context, boolean needWifi) {
        NetworkInfo info = getActiveNetwork(context);
        if (info == null) {
            return false;
        }
        if (!needWifi) {
            return info.isAvailable();
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return info.isAvailable();
        }
        return false;
    }

    /**
     * 获取活动网络连接信息
     *
     * @param context 上下文
     * @return NetworkInfo
     */
    public static NetworkInfo getActiveNetwork(Context context) {
        ConnectivityManager mConnMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (mConnMgr == null) {
            return null;
        }
        // 获取活动网络连接信息
        return mConnMgr.getActiveNetworkInfo();
    }
}
