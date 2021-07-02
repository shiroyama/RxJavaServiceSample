package com.example.rxjavaservicesample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Entry point of App
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MessageClient messageClientMessenger;
    private AIDLMessageClient messageClientAIDL;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageClientMessenger = new MessageClient(this);
        messageClientAIDL = new AIDLMessageClient(this);

        Button buttonOneshotMessenger = (Button) findViewById(R.id.button_oneshot_messenger);
        buttonOneshotMessenger.setOnClickListener(v -> {
            Disposable disposable = messageClientMessenger.getCurrentTimeOneshot()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            text -> {
                                Log.d(TAG, "onNext: " + text);
                                toast("Oneshot: " + text);
                            },
                            error -> Log.e(TAG, "onError: " + error.getMessage(), error),
                            () -> Log.d(TAG, "onComplete")
                    );
            compositeDisposable.add(disposable);
        });

        Button buttonContinuousMessenger = (Button) findViewById(R.id.button_continuous_messenger);
        buttonContinuousMessenger.setOnClickListener(v -> {
            Disposable disposable = messageClientMessenger.getCurrentTimeContinuously()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            text -> {
                                Log.d(TAG, "onNext: " + text);
                                toast("Continuous: " + text);
                            },
                            error -> Log.e(TAG, "onError: " + error.getMessage(), error),
                            () -> Log.d(TAG, "onComplete")
                    );
            compositeDisposable.add(disposable);
        });

        Button buttonOneshotAIDL = (Button) findViewById(R.id.button_oneshot_aidl);
        buttonOneshotAIDL.setOnClickListener(v -> {
            Disposable disposable = messageClientAIDL.getCurrentTimeOneshot()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            text -> {
                                Log.d(TAG, "onNext: " + text);
                                toast("Oneshot: " + text);
                            },
                            error -> Log.e(TAG, "onError: " + error.getMessage(), error),
                            () -> Log.d(TAG, "onComplete")
                    );
            compositeDisposable.add(disposable);
        });

        Button buttonContinuousAIDL = (Button) findViewById(R.id.button_continuous_aidl);
        buttonContinuousAIDL.setOnClickListener(v -> {
            Disposable disposable = messageClientAIDL.getCurrentTimeContinuously()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            text -> {
                                Log.d(TAG, "onNext: " + text);
                                toast("Continuous: " + text);
                            },
                            error -> Log.e(TAG, "onError: " + error.getMessage(), error),
                            () -> Log.d(TAG, "onComplete")
                    );
            compositeDisposable.add(disposable);
        });
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        messageClientMessenger.destroy();
        messageClientAIDL.destroy();
        compositeDisposable.dispose();
        super.onDestroy();
    }
}