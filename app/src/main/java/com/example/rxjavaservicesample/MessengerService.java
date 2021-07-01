package com.example.rxjavaservicesample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Remote Service that provides computation result
 */
public class MessengerService extends Service {
    private static final String TAG = MessengerService.class.getSimpleName();
    private static final long INTERVAL = 3000L;
    private final HandlerThread handlerThread = new HandlerThread(TAG);
    private Messenger serviceMessenger;

    @Override
    public void onCreate() {
        super.onCreate();
        handlerThread.start();
        ServiceHandler serviceHandler = new ServiceHandler(handlerThread.getLooper());
        serviceMessenger = new Messenger(serviceHandler);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        handlerThread.quitSafely();
        super.onDestroy();
    }

    /**
     * Handler in Service that provides computation results
     */
    private class ServiceHandler extends Handler {
        public ServiceHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "ServiceHandler#handleMessage: " + msg);

            if (msg.replyTo == null) {
                return;
            }
            switch (msg.what) {
                case MessageType.ONESHOT: {
                    try {
                        Message replyMessage = Message.obtain(null, MessageType.ONESHOT);
                        replyMessage.obj = String.valueOf(System.currentTimeMillis());
                        msg.replyTo.send(replyMessage);
                    } catch (RemoteException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    break;
                }
                case MessageType.CONTINUOUS: {
                    try {
                        Message replyMessage = Message.obtain(null, MessageType.CONTINUOUS);
                        replyMessage.obj = String.valueOf(System.currentTimeMillis());
                        msg.replyTo.send(replyMessage);

                        // Keep sending results
                        Thread.sleep(INTERVAL);
                        serviceMessenger.send(Message.obtain(msg));
                    } catch (RemoteException | InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    break;
                }
                default: {
                    Log.e(TAG, "That message type not supported: " + msg.what);
                }
            }
        }
    }
}