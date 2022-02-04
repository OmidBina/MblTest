package com.example.mbltestapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends SurfaceView implements Runnable  {

    private static final String TAG = "GameView";

    private Context context;

    private Thread thread;

    private boolean isPlaying;
    private int screenX, screenY;
    public static float screenRatioX, screenRatioY;
    private Paint paint;
    private Flight flight;
    private Background background1, background2;

    private Messenger mService = null;
    private Messenger mMessenger;

    private boolean mIsBound, isResting;

    private TextView rest_tv;

    private androidx.appcompat.app.AlertDialog alertDialog;

    private int setCount = 0;

    public GameView(Context context, int screenX, int screenY) {
        super(context);

        this.context = context;

        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 1920f / screenX;
        screenRatioY = 1080f / screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        flight = new Flight(screenY, getResources());

        background2.x = screenX;

        paint = new Paint();

        doBindService();
    }

    @Override
    public void run() {

        while (isPlaying) {

            update ();
            draw ();
            sleep ();

        }

    }

    private void update () {

        background1.x -= 10 * screenRatioX;
        background2.x -= 10 * screenRatioX;

        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }

        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }

        if (flight.isGoingUp) {
            flight.y -= 10 * screenRatioY;
        }else {
            flight.y += 10 * screenRatioY;
        }

        if (flight.y < 0) {
            flight.y = 0;
        }

        if (flight.y > screenY - flight.height)
            flight.y = screenY - flight.height;
    }

    private void draw() {

        if (getHolder().getSurface().isValid()){

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            getHolder().unlockCanvasAndPost(canvas);
        }

    }

    private void sleep () {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume () {

        isPlaying = true;
        thread = new Thread(this);
        thread.start();

        if (!mIsBound)
            doBindService();

    }

    public void pause () {

        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                //flight.isGoingUp = true;
                break;
            case MotionEvent.ACTION_UP:
                //flight.isGoingUp = false;
                break;

        }

        return true;
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0X12:

                    Bundle bundle = (Bundle) msg.obj;
                    double d = bundle.getDouble("controller_value");
                    d = Math.round(d * 10.0)/10.0;
                    //play_btn.setText(d + "");

                    if (!isResting) {
                        if (d > 1) {
                            flight.isGoingUp = true;
                        } else {
                            flight.isGoingUp = false;
                        }
                    }else {
                        flight.isGoingUp = false;
                    }

                    Log.e(TAG, "handleMessage: Receive data " + d);
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

            //Toast.makeText(context, "connected", Toast.LENGTH_SHORT).show();

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

            Toast.makeText(context, "disconnected",
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {

        context.bindService(new Intent(context,
                DeviceService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        //play_btn.setText("Binding.");

        get_generated_number();
    }

    void doUnbindService() {
        if (mIsBound) {

            context.unbindService(mConnection);
            mIsBound = false;
            //play_btn.setText("Unbinding.");
        }
    }

    private void get_generated_number() {

        setCount += 1;

        CountDownTimer countDownTimer = new CountDownTimer(60000, 100) {
            public void onTick(long millisUntilFinished) {

                try {
                    Message msg = Message.obtain();
                    msg.what = 0X12;
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    Log.e(TAG, "onTick: ", e);
                }
            }

            public void onFinish() {

                open_rest_dialog();
                rest_time();
            }
        };
        countDownTimer.start();
    }

    private void rest_time() {

        CountDownTimer restTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {

                long t = millisUntilFinished / 1000;

                if (t > 5){
                    rest_tv.setText("Rest!");
                }else if (t > 0){
                    rest_tv.setText(t + "");
                }else {
                    rest_tv.setText("Let's GO!");
                }

            }

            public void onFinish() {

                if (setCount < 3) {
                    alertDialog.dismiss();
                    get_generated_number();
                }

            }
        };
        restTimer.start();
    }

    private void open_rest_dialog () {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.rest_time_dialog, null);

        androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(context);
        alertBuilder.setView(v);
        alertDialog = alertBuilder.create();
        alertDialog.setCancelable(false);

        rest_tv = v.findViewById(R.id.rest_time_dialog_tv);

        alertDialog.show();
    }
}
