package com.yandongit.mqtt;

public enum MqttQos {
    /**
     * 最多一次，有可能重复或丢失
     */
    QOS0(0),
    /**
     * 至少一次，有可能重复
     */
    QOS1(1),
    /**
     * 只有一次，确保消息只到达一次（用于比较严格的计费系统）
     */
    QOS2(2);

    private int mCode;

    MqttQos(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}
