// IMessageCallback.aidl
package com.example.rxjavaservicesample;

interface IMessageCallback {
    void onReceiveTime(long timeInMilliseconds);
}