// IMessageInterface.aidl
package com.example.rxjavaservicesample;
import com.example.rxjavaservicesample.IMessageCallback;

interface IMessageInterface {
    long getCurrentTime();
    void start();
    void registerCallback(IMessageCallback callback);
    void unregisterCallback(IMessageCallback callback);
}