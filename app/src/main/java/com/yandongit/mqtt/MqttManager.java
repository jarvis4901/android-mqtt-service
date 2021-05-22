package com.yandongit.mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;

public class MqttManager implements MqttApi {

    private Context mContext;
    private MqttOption mCurrentApplyOption;  //当前引用的配置
    private MqttAndroidClient mMqttClient;  //Mqtt客户端
    private OnConnectionStatusListener mConnectionStatusListener;  //连接状态监听
    private boolean isConnect;    //是否已连接


    public interface OnConnectionStatusListener {  //连接状态监听
        void onConnectionLost(Throwable error); //连接丢失

        void onRetryConnection();   //正在重试连接
    }

    private void setOnConnectionStatusListener(OnConnectionStatusListener connectionStatusListener) {
        mConnectionStatusListener = connectionStatusListener;
    }

    private OnMessagePublishStatusListener mMessagePublishStatusListener;  // 消息发布状态监听器


    public interface OnMessagePublishStatusListener {  //消息发送状态监听
        /**
         * 发送消息完成
         *
         * @param message 原始消息
         */
        void onMessagePublishComplete(String message);
    }

    private void setOnMessagePublishStatusListener(OnMessagePublishStatusListener messagePublishStatusListener) {
        mMessagePublishStatusListener = messagePublishStatusListener;
    }


    public MqttManager(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public Observable<Boolean> connectServer(MqttOption option) {
        mCurrentApplyOption = option;
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) {
                try {
                    if (mContext == null) {
                        emitter.onNext(false);
                        return;
                    }
                    if (mMqttClient == null) {
                        mMqttClient = new MqttAndroidClient(mContext, option.getServerUrl(), option.getClientId(), new MemoryPersistence());
                    }
                    if (isConnect) {
                        emitter.onNext(true);
                        return;
                    }

                    if (!MqttNetUtil.hasNetWorkStatus(mContext, false)) {
                        emitter.onNext(false);
                        return;
                    }

                    //进行连接的配置
                    MqttConnectOptions connectOptions = new MqttConnectOptions();
                    //如果为false(flag=0)，Client断开连接后，Server应该保存Client的订阅信息
                    //如果为true(flag=1)，表示Server应该立刻丢弃任何会话状态信息
                    connectOptions.setCleanSession(true);
                    //设置用户名和密码
                    connectOptions.setUserName(option.getUsername());
                    connectOptions.setPassword(option.getPassWord().toCharArray());
                    //设置连接超时时间
                    connectOptions.setConnectionTimeout(option.getConnectionTimeout());
                    //设置心跳发送间隔时间，单位秒
                    connectOptions.setKeepAliveInterval(option.getKeepAliveInterval());
                    //设置遗嘱
                    connectOptions.setWill("android-mqtt-offline-topic", "android-mqtt-is_offline".getBytes(), MqttQos.QOS0.getCode(), false);

                    //开始连接
                    mMqttClient.connect(connectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            //连接成功
                            emitter.onNext(true);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            //连接失败
                            exception.printStackTrace();
                            emitter.onNext(false);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    emitter.onNext(false);
                }

            }
        }).flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(Boolean isSuccess) {
                try {
                    //连接成功，
                    if (isSuccess) {
                        //订阅主题
                        return subscribeTopic(mCurrentApplyOption.getTopics());
                    } else {
                        //连接失败，重试
                        return Observable.error(new MqttImproperCloseException());
                    }
                } catch (Exception e) {
                    //连接失败，重试
                    return Observable.error(new MqttImproperCloseException());
                }

            }
        }).doOnNext(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isSuccess) throws Throwable {
                isConnect = isSuccess;
            }
        }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Throwable {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Throwable {
                        mMqttClient = null;
                        //所有异常都走重试，延时指定秒值进行重试
                        return Observable.timer(mCurrentApplyOption.getRetryIntervalTime(), TimeUnit.SECONDS).doOnNext(new Consumer<Long>() {

                            @Override
                            public void accept(Long aLong) throws Throwable {
                                if (mConnectionStatusListener != null) {
                                    Log.i("尝试重连：","---------------");
                                    mConnectionStatusListener.onRetryConnection();
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    //断开连接
    @Override
    public Observable<Boolean> disconnectServer() {
        //已经断开连接了
        if (!isConnect) {
            return Observable.just(true);
        }
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> emitter) throws Throwable {
                try {
                    mMqttClient.disconnect(null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            mMqttClient = null;
                            emitter.onNext(true);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            exception.printStackTrace();
                            emitter.onNext(false);
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                    emitter.onNext(false);
                }
            }
        });
    }

    @Override
    public Observable<Boolean> restartConnectServer() {
        //先断开，再重新连接
        return disconnectServer().flatMap(new Function<Boolean, ObservableSource<Boolean>>() {
            @Override
            public ObservableSource<Boolean> apply(Boolean isSuccess) throws Exception {
                if (isSuccess) {
                    return connectServer(mCurrentApplyOption);
                }
                return Observable.just(false);
            }
        });
    }

    /**
     * 发布消息
     *
     * @param topic   主题
     * @param message 消息文本
     * @return
     */
    @Override
    public Observable<Boolean> publishMessage(String topic, String message) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                //连接未打开
                if (!isConnect) {
                    emitter.onNext(false);
                }
                try {
                    //发布消息
                    //目前消息等级用的qos2
                    mMqttClient.publish(topic, message.getBytes(), MqttQos.QOS2.getCode(), false);
                    emitter.onNext(true);
                } catch (MqttException e) {
                    e.printStackTrace();
                    emitter.onNext(false);
                }
            }
        });
    }

    /**
     * 订阅消息
     *
     * @return
     */
    @Override
    public Observable<MqttMessage> subscriberMessage() {
        return Observable.create(new ObservableOnSubscribe<MqttMessage>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<MqttMessage> emitter) throws Throwable {
                mMqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        //与服务器之间的连接失效
                        isConnect = false;
                        if (mConnectionStatusListener != null) {
                            mConnectionStatusListener.onConnectionLost(cause);
                        }
                        emitter.onError(cause);
                    }

                    @Override
                    public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) throws Exception {
                        //接收到消息
                        emitter.onNext(new MqttMessage(topic, message.toString()));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        //发送完成
                        if (mMessagePublishStatusListener != null) {
                            try {
                                String message = token.getMessage().toString();
                                mMessagePublishStatusListener.onMessagePublishComplete(message);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }).retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Exception {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Exception {
                        //延时重连
                        return Observable.timer(mCurrentApplyOption.getRetryIntervalTime(), TimeUnit.SECONDS).flatMap(new Function<Long, ObservableSource<?>>() {
                            @Override
                            public ObservableSource<?> apply(Long aLong) throws Exception {
                                return connectServer(mCurrentApplyOption);
                            }
                        });
                    }
                });
            }
        });
    }

    //订阅连接状态
    @Override
    public Observable<MqttConnectStatus> subscribeConnectionStatus() {
        return Observable.create(new ObservableOnSubscribe<MqttConnectStatus>() {
            @Override
            public void subscribe(ObservableEmitter<MqttConnectStatus> emitter) throws Exception {
                setOnConnectionStatusListener(new OnConnectionStatusListener() {
                    @Override
                    public void onConnectionLost(Throwable error) {
                        emitter.onNext(new MqttConnectStatus(true, error));
                    }

                    @Override
                    public void onRetryConnection() {
                        emitter.onNext(new MqttConnectStatus(true));
                    }
                });
            }
        });
    }

    //订阅消息发布状态
    @Override
    public Observable<MqttMessagePublishStatus> subscribeMessagePublishStatus() {
        return Observable.create(new ObservableOnSubscribe<MqttMessagePublishStatus>() {
            @Override
            public void subscribe(ObservableEmitter<MqttMessagePublishStatus> emitter) throws Exception {
                setOnMessagePublishStatusListener((message)
                        -> emitter.onNext(new MqttMessagePublishStatus(true, message)));
            }
        });
    }

    /**
     * 订阅主题，传入需要订阅的多个主题的数组
     *
     * @param topic 主题
     * @return
     */
    @Override
    public Observable<Boolean> subscribeTopic(String... topic) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                if (topic.length == 0) {
                    emitter.onNext(true);
                    return;
                }
                try {
                    int size = topic.length;
                    int[] qosArr = new int[size];
                    for (int i = 0; i < size; i++) {
                        qosArr[i] = MqttQos.QOS2.getCode();
                    }
                    mMqttClient.subscribe(topic, qosArr);
                    emitter.onNext(true);
                } catch (MqttException e) {
                    e.printStackTrace();
                    emitter.onNext(false);
                }
            }
        });
    }

    /**
     * 取消订阅主题，传入需要取消订阅的Topic数组
     *
     * @param topic 主题
     * @return
     */
    @Override
    public Observable<Boolean> unsubscribeTopic(String... topic) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                try {
                    mMqttClient.unsubscribe(topic);
                    emitter.onNext(true);
                } catch (MqttException e) {
                    e.printStackTrace();
                    emitter.onNext(false);
                }
            }
        });
    }
}
