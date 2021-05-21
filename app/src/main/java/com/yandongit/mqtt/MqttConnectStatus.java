package com.yandongit.mqtt;

public class MqttConnectStatus {

    /**
     * 是否丢失连接
     */
    private boolean isLost;
    /**
     * 是否在重新连接
     */
    private boolean isRetry;
    /**
     * 丢失连接时的异常
     */
    private Throwable error;

    public MqttConnectStatus(boolean isLost, Throwable error) {
        this.isLost = isLost;
        this.error = error;
    }

    public MqttConnectStatus(boolean isRetry) {
        this.isRetry = isRetry;
    }

    //省略get、set方法
}
