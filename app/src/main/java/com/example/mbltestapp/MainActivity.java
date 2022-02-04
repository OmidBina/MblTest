package com.example.mbltestapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private Messenger mService = null;
    private Messenger mMessenger;

    private boolean mIsBound;

    private TextView play_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        elements();

        doBindService();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mIsBound)
            doBindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        doUnbindService();
    }

    private void elements() {

        play_btn = findViewById(R.id.main_play_btn);

        manage_clicks();
    }

    private void manage_clicks() {

        play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //startActivity(new Intent(MainActivity.this, GameActivity.class));

                get_generated_number();

                /*try {
                    Message msg = Message.obtain();
                    msg.what = 0X12;
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }*/

            }
        });

    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0X12:

                    Bundle bundle = (Bundle) msg.obj;
                    double d = bundle.getDouble("controller_value");
                    d = Math.round(d * 10.0)/10.0;
                    play_btn.setText(d + "");
                    Log.e(TAG, "handleMessage: Receive data " + d);
                    break;
                case 0X11:
                    Log.e(TAG, "handleMessage: start the game -> 0X11");
                    break;
                case DeviceService.MSG_SET_VALUE:
                    play_btn.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = new Messenger(service);
            mMessenger = new Messenger(new IncomingHandler());
            //play_btn.setText("Attached.");

            Toast.makeText(MainActivity.this, "connected",
                    Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // There is nothing special we need to do if the service
                // has crashed.
            }
        }

        public void onServiceDisconnected(ComponentName className) {

            mService = null;
            //play_btn.setText("Disconnected.");

            Toast.makeText(MainActivity.this, "disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {

        bindService(new Intent(MainActivity.this,
                DeviceService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        play_btn.setText("Binding.");
    }

    void doUnbindService() {
        if (mIsBound) {

            unbindService(mConnection);
            mIsBound = false;
            play_btn.setText("Unbinding.");
        }
    }

    private void get_generated_number() {

        new CountDownTimer(60000, 100) {
            public void onTick(long millisUntilFinished) {

                try {
                    Message msg = Message.obtain();
                    msg.what = 0X12;
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }

            }

            public void onFinish() {
                play_btn.setText("done!");
            }
        }.start();
    }
}

