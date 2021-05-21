package com.yandongit.mqtt;

import android.content.Context;

import io.reactivex.rxjava3.core.Observable;

public class MqttProxy implements MqttApi {

    private final MqttApi mImpl;

    private MqttProxy() {
        mImpl = new MqttManager();
    }

    private static final class SingleHolder {
        private static final MqttProxy INSTANCE = new MqttProxy();
    }

    public static MqttProxy getInstance() {
        return SingleHolder.INSTANCE;
    }


    @Override
    public Observable<Boolean> connectServer(MqttOption option) {
        return mImpl.connectServer(option);
    }

    @Override
    public Observable<Boolean> disconnectServer() {
        return mImpl.disconnectServer();
    }

    @Override
    public Observable<Boolean> restartConnectServer() {
        return mImpl.restartConnectServer();
    }

    @Override
    public Observable<Boolean> publishMessage(String topic, String message) {
        return mImpl.publishMessage(topic, message);
    }

    @Override
    public Observable<MqttMessage> subscriberMessage() {
        return mImpl.subscriberMessage();
    }

    @Override
    public Observable<MqttConnectStatus> subscribeConnectionStatus() {
        return mImpl.subscribeConnectionStatus();
    }

    @Override
    public Observable<MqttMessagePublishStatus> subscribeMessagePublishStatus() {
        return mImpl.subscribeMessagePublishStatus();
    }

    @Override
    public Observable<Boolean> subscribeTopic(String... topic) {
        return mImpl.subscribeTopic(topic);
    }

    @Override
    public Observable<Boolean> unsubscribeTopic(String... topic) {
        return mImpl.unsubscribeTopic();
    }
}
