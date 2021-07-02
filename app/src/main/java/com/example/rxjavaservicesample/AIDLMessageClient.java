package com.example.rxjavaservicesample;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Message client that binds to Remote Service through AIDL
 */
public class AIDLMessageClient {
    private static final String TAG = AIDLMessageClient.class.getSimpleName();
    private final Context context;

    private final ServiceConnection serviceConnection;
    private IMessageInterface service;
    private PublishSubject<String> subject;

    private final IMessageCallback.Stub callback = new IMessageCallback.Stub() {
        @Override
        public void onReceiveTime(long timeInMilliseconds) throws RemoteException {
            if (subject == null) {
                return;
            }
            subject.onNext(String.valueOf(timeInMilliseconds));
        }
    };

    public AIDLMessageClient(Context context) {
        this.context = context;
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                AIDLMessageClient.this.service = IMessageInterface.Stub.asInterface(service);
                try {
                    AIDLMessageClient.this.service.registerCallback(callback);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                try {
                    service.unregisterCallback(callback);
                } catch (RemoteException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                service = null;
            }
        };
        this.context.bindService(new Intent(this.context, AIDLMessengerService.class), serviceConnection, Service.BIND_AUTO_CREATE);
    }

    /**
     * Oneshot observable
     *
     * @return Observable<String>
     */
    public Observable<String> getCurrentTimeOneshot() {
        if (service == null) {
            return Observable.empty();
        }
        try {
            long currentTime = service.getCurrentTime();
            return Observable.just(String.valueOf(currentTime));
        } catch (RemoteException e) {
            return Observable.error(e);
        }
    }

    /**
     * Continuous observable
     *
     * @return Observable<String>
     */
    public Observable<String> getCurrentTimeContinuously() {
        subject = PublishSubject.create();
        try {
            service.start();
        } catch (RemoteException e) {
            subject.onError(e);
        }
        return subject;
    }

    /**
     * Make sure to call from Activity to unbind from the Service connection.
     */
    public void destroy() {
        context.unbindService(serviceConnection);
    }
}
