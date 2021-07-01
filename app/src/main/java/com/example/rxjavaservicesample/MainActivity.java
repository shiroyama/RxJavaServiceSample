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

    private MessageClient messageClient;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageClient = new MessageClient(this);

        Button buttonOneshot = (Button) findViewById(R.id.button_oneshot);
        buttonOneshot.setOnClickListener(v -> {
            Disposable disposable = messageClient.getCurrentTimeOneshot()
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
        Button buttonContinuous = (Button) findViewById(R.id.button_continuous);
        buttonContinuous.setOnClickListener(v -> {
            Disposable disposable = messageClient.getCurrentTimeContinuously()
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
        messageClient.destroy();
        compositeDisposable.dispose();
        super.onDestroy();
    }
}