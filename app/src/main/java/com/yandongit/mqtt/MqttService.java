package com.yandongit.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MqttService extends Service {
    public static final String TAG = "MqttService";
    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connectMqttService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mDisposables.isDisposed()) {
            mDisposables.clear();
        }
    }

    /**
     * 连接mqtt服务器
     */
    private void connectMqttService() {
        String userId = "test";
        String[] topics = {"test"};
        String topic = "test";
        MqttProxy proxy = MqttProxy.getInstance();
        MqttOption options = new MqttOption.Builder()
                .setServerUrl("tcp://192.168.1.102:1883")
                .setUsername("test")
                .setPassWord("test")
                .setClientId(userId)
                .setTopics(topics)
                .build();

        //连接服务
        mDisposables.add(proxy.connectServer(options).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<Boolean>(
        ) {
            @Override
            public void onNext(@NonNull Boolean isSuccess) {
                Log.i(TAG, "Mqtt连接是否成功:" + isSuccess.toString());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.i(TAG, "Mqtt连接失败");
            }

            @Override
            public void onComplete() {

            }
        }));

        //订阅消息接收
        mDisposables.add(proxy.subscriberMessage().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<MqttMessage>() {
            @Override
            public void onNext(@NonNull MqttMessage msgData) {
                Log.i(TAG, "Mqtt接收到消息:" + msgData.getMessage());
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }));

        //订阅连接状态
        mDisposables.add(proxy.subscribeConnectionStatus().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<MqttConnectStatus>() {
            @Override
            public void onNext(@NonNull MqttConnectStatus mqttConnectStatus) {
                Log.i(TAG, "Mqtt连接状态:" + mqttConnectStatus.isLost());
                Log.i(TAG, "Mqtt重连状态:" + mqttConnectStatus.isRetry());
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }));

        //订阅消息发送状态
        mDisposables.add(proxy.subscribeMessagePublishStatus().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<MqttMessagePublishStatus>() {
            @Override
            public void onNext(@NonNull MqttMessagePublishStatus status) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        }));

        String msg = "测试消息";
        //订阅消息发送状态
        mDisposables.add(proxy.publishMessage(topic, msg).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableObserver<Boolean>() {
            @Override
            public void onNext(@NonNull Boolean isSuccess) {
                Log.i(TAG, "Mqtt消息发送是否成功:" + isSuccess.toString());
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.i(TAG, "Mqtt消息发送失败");
            }

            @Override
            public void onComplete() {

            }
        }));


    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
