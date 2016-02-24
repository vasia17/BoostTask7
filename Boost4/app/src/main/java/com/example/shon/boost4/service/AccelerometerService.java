package com.example.shon.boost4.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.shon.boost4.MainActivity;
import com.example.shon.boost4.entity.Sample;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class AccelerometerService extends Service
        implements SensorEventListener {

    private long lastUpdate = 0;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private int mCounter = 0;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class AccelerometerBinder extends Binder {
        public AccelerometerService getService() {
            Log.d(MainActivity.MAIN_TAG, "AccelerometerBinder: getService");
            return AccelerometerService.this;
        }
    }

    // This is the object that receives interactions from clients.
    // See RemoteService for a more complete example.
    private final IBinder mBinder = new AccelerometerBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MainActivity.MAIN_TAG, "onStartCommand: Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(MainActivity.MAIN_TAG, "AccelerometerService: onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        Log.d(MainActivity.MAIN_TAG, "AccelerometerService: onCreate");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

        if(MainActivity.sSamplesRef != null) {
            MainActivity.sSamplesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mCounter = (int) dataSnapshot.getChildrenCount() + 1;
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        Log.d(MainActivity.MAIN_TAG, "AccelerometerService: onDestroy");
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(MainActivity.sSamplesRef != null){
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long curTime = System.currentTimeMillis();
                if ((curTime - lastUpdate) > MainActivity.TIME_INTERVAL) {
                    Log.d(MainActivity.MAIN_TAG, "AccelerometerService: onSensorChanged " +
                            String.valueOf(event.values[0]));

                    Sample s = new Sample("sample" + mCounter, event.values[0], event.values[1], event.values[2]);
                    MainActivity.sSamplesRef.push().setValue(s);

                    mCounter++;
                    lastUpdate = curTime;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}