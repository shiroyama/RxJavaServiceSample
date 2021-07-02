package com.example.rxjavaservicesample;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

public class AIDLMessengerService extends Service {
    private static final String TAG = AIDLMessengerService.class.getSimpleName();
    private static final long INTERVAL = 3000L;

    private final RemoteCallbackList<IMessageCallback> callbackList = new RemoteCallbackList<>();
    private final Handler handler = new Handler();

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int i = callbackList.beginBroadcast();
            while (i > 0) {
                i--;
                try {
                    callbackList.getBroadcastItem(i).onReceiveTime(getCurrentTime());
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
            callbackList.finishBroadcast();
            handler.postDelayed(runnable, INTERVAL);
        }
    };

    private final IMessageInterface.Stub binder = new IMessageInterface.Stub() {
        @Override
        public long getCurrentTime() throws RemoteException {
            return AIDLMessengerService.this.getCurrentTime();
        }

        @Override
        public void start() throws RemoteException {
            Log.d(TAG, "start");
            runnable.run();
        }

        @Override
        public void registerCallback(IMessageCallback callback) throws RemoteException {
            if (callback != null) {
                callbackList.register(callback);
            }
        }

        @Override
        public void unregisterCallback(IMessageCallback callback) throws RemoteException {
            if (callback != null) {
                callbackList.unregister(callback);
            }
        }
    };

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        callbackList.kill();
        super.onDestroy();
    }
}
