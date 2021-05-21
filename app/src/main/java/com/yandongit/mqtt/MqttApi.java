package com.yandongit.mqtt;


import io.reactivex.rxjava3.core.Observable;

public interface MqttApi {

    //连接服务器
    Observable<Boolean> connectServer(MqttOption option);

    //断开连接
    Observable<Boolean> disconnectServer();

    //重启连接
    Observable<Boolean> restartConnectServer();

    /**
     * 发布消息
     * @param topic 主题
     * @param message 消息文本
     * @return
     */
    Observable<Boolean> publishMessage(String topic,String message);

    //监听消息
    Observable<MqttMessage> subscriberMessage();

    //订阅连接状态
    Observable<MqttConnectStatus> subscribeConnectionStatus();

    /**
     * 订阅消息发送状态
     */
    Observable<MqttMessagePublishStatus> subscribeMessagePublishStatus();

    /**
     * 动态订阅一个主题
     * @param topic 主题
     */
    Observable<Boolean> subscribeTopic(String... topic);

    /**
     * 动态取消订阅一个主题
     * @param topic 主题
     */
    Observable<Boolean> unsubscribeTopic(String... topic);
}
