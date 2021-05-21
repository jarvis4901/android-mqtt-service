package com.yandongit.mqtt;

public class MqttMessagePublishStatus {
    /**
     * 是否发送完毕
     */
    private boolean isComplete;
    /**
     * 发送的消息内容
     */
    private String message;

    public MqttMessagePublishStatus(boolean isComplete, String message) {
        this.isComplete = isComplete;
        this.message = message;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
