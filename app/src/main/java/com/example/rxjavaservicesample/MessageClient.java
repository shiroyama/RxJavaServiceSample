package com.example.rxjavaservicesample;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Message client that binds to Remote Service
 */
public class MessageClient {
    private static final String TAG = MessageClient.class.getSimpleName();
    private final Context context;

    private Messenger remoteMessenger;
    private final Messenger clientMessenger;
    private final ServiceConnection serviceConnection;
    private PublishSubject<String> subject;

    public MessageClient(Context context) {
        this.context = context.getApplicationContext();

        clientMessenger = new Messenger(new ClientHandler());
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                remoteMessenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                remoteMessenger = null;
            }
        };

        this.context.bindService(new Intent(context, MessengerService.class), serviceConnection, Service.BIND_AUTO_CREATE);
    }

    /**
     * Wraps Service messaging with RxJava object.
     *
     * @param what message type
     * @return communication result string as Observable<String>
     */
    private Observable<String> getCurrentTime(int what) {
        subject = PublishSubject.create();
        if (remoteMessenger == null) {
            return Observable.empty();
        }
        try {
            Message msg = Message.obtain(null, what);
            msg.replyTo = clientMessenger;
            remoteMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, e.getMessage(), e);
            subject.onError(e);
        }
        return subject;
    }

    /**
     * Oneshot observable
     *
     * @return Observable<String>
     */
    public Observable<String> getCurrentTimeOneshot() {
        return getCurrentTime(MessageType.ONESHOT);
    }

    /**
     * Continuous observable
     *
     * @return Observable<String>
     */
    public Observable<String> getCurrentTimeContinuously() {
        return getCurrentTime(MessageType.CONTINUOUS);
    }

    /**
     * Make sure to call from Activity to unbind from the Service connection.
     */
    public void destroy() {
        context.unbindService(serviceConnection);
    }

    private class ClientHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "ClientHandler#handleMessage: " + msg);
            String reply = (String) msg.obj;
            switch (msg.what) {
                case MessageType.ONESHOT: {
                    subject.onNext(reply);
                    subject.onComplete();
                    break;
                }
                case MessageType.CONTINUOUS: {
                    subject.onNext(reply);
                    break;
                }
                default: {
                    subject.onError(new RuntimeException("No such message."));
                }
            }
        }
    }
}
