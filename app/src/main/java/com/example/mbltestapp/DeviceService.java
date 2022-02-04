package com.example.mbltestapp;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Random;

public class DeviceService extends Service {

    private static final String TAG = "DeviceService";

    static final int MSG_SET_VALUE = 111;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private boolean isGeneratingOn = true;
    private double randomNumber;

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    Log.e(TAG, "handleMessage: Registered");
                    //start_running_timer(msg);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            startRandomNumberGenerator();
                        }
                    }).start();
                    break;
                case 0X12:
                    Bundle bundle = new Bundle();
                    bundle.putDouble("controller_value", randomNumber);
                    Message me = Message.obtain();
                    me.obj = bundle;
                    me.what = 0X12;
                    try{
                        msg.replyTo.send(me);
                    }catch (RemoteException e){
                        Log.e(TAG, "send_random_number: ", e);
                    }

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void start_running_timer(Message msg) {

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Log.e(TAG, "send_running_msg: ");

                Message m = Message.obtain();
                m.what = 0X11;
                try{
                    msg.replyTo.send(m);
                }catch (RemoteException e){
                    Log.e(TAG, "send_random_number: ", e);
                }
            }
        }.start();
    }

    private void send_running_msg() {


    }

    private double generate_random_number() {

        final int min = 0;
        final int max = 20;
        final int random = new Random().nextInt((max - min) + 1) + min;

        return random * 0.1;

        /*Bundle bundle = null;
        bundle.putDouble("controller_value", random_number);
        Message msg = Message.obtain();
        msg.obj = bundle;
        msg.what = 0X12;
        try{
            mMessenger.send(msg);
        }catch (RemoteException e){
            Log.e(TAG, "send_random_number: ", e);
        }*/
    }

    private void send_rest_msg() {

        Message msg = Message.obtain();
        msg.what = 0X13;
        try{
            mMessenger.send(msg);
        }catch (RemoteException e){
            Log.e(TAG, "send_random_number: ", e);
        }
    }

    private void send_stopped_msg() {

        Message msg = Message.obtain();
        msg.what = 0X14;
        try{
            mMessenger.send(msg);
        }catch (RemoteException e){
            Log.e(TAG, "send_random_number: ", e);
        }
    }

    private void send_failed_msg() {

        Bundle bundle = null;
        bundle.putInt("error_code", 1111);
        bundle.putString("reason", "nothing!");
        Message msg = Message.obtain();
        msg.obj = bundle;
        msg.what = 0X15;
        try{
            mMessenger.send(msg);
        }catch (RemoteException e){
            Log.e(TAG, "send_random_number: ", e);
        }
    }

    private void startRandomNumberGenerator(){
        while (isGeneratingOn){
            try{
                Thread.sleep(40);
                if(isGeneratingOn){
                    randomNumber = generate_random_number();
                    Log.i(TAG,"Random Number: " + randomNumber);
                }
            }catch (InterruptedException e){
                Log.i(TAG,"Thread Interrupted");
            }
        }
    }

    public double getRandomNumber(){
        return randomNumber;
    }
}
