package com.yandongit.mqtt;

/**
 * 自定义异常类，用于RxJava的Retry操作符时，手动抛出异常，让retry操作符生效，重走连接逻辑
 */
public class MqttImproperCloseException extends Exception {
    private static final long serialVersionUID = -4030414538155742302L;

    MqttImproperCloseException() {
    }

    public MqttImproperCloseException(String message) {
        super(message);
    }
}
