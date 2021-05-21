package com.yandongit.mqtt;

public class MqttMessage {
    /**
     * 订阅的主题
     */
    private String topic;
    /**
     * 接收到的消息
     */
    private String message;

    public MqttMessage(String topic, String message) {
        this.topic = topic;
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
