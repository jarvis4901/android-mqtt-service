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

    public boolean isLost() {
        return isLost;
    }

    public void setLost(boolean lost) {
        isLost = lost;
    }

    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean retry) {
        isRetry = retry;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
