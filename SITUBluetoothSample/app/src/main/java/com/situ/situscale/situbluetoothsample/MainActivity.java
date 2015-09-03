package com.situ.situscale.situbluetoothsample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.situ.situscale.bluetooth.SITUBluetoothEvent;
import com.situ.situscale.bluetooth.SITUBluetoothService;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private TextView mConnectionState;
    private TextView mWeight;

    private SITUBluetoothService mSITUBluetoothService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mSITUBluetoothService = ((SITUBluetoothService.LocalBinder) service).getService();
            if (!mSITUBluetoothService.initialize()) {
                Log.e(TAG, "Unable to initialize SITU Bluetooth Serivce");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSITUBluetoothService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        mConnectionState = (TextView) findViewById(R.id.status);
        mConnectionState.setTextColor(Color.RED);
        mWeight = (TextView) findViewById(R.id.weight);

        Intent situIntent = new Intent(this, SITUBluetoothService.class);
        bindService(situIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSITUBluetoothService != null) {
            mSITUBluetoothService.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSITUBluetoothService != null) {
            mSITUBluetoothService.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(mServiceConnection);
        if (mSITUBluetoothService != null) {
            mSITUBluetoothService = null;
        }

        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void onEvent(SITUBluetoothEvent event)
    {
        if (SITUBluetoothService.ACTION_SCALE_CONNECTED.equals(event.eventType)) {
            Log.d(TAG, "Scale connected.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConnectionState.setText("Connected");
                    mConnectionState.setTextColor(Color.rgb(0, 180, 120));
                }
            });
        }
        else if (SITUBluetoothService.ACTION_SCALE_DISCONNECTED.equals(event.eventType)) {
            Log.d(TAG, "Scale disconnected.");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConnectionState.setText("Disconnected");
                    mConnectionState.setTextColor(Color.RED);
                    mWeight.setText("N/A");
                }
            });
        }
        else if (SITUBluetoothService.ACTION_SCALE_DATA_AVAILABLE.equals(event.eventType)) {
            final Long grams = (Long) event.eventObject;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Log.i(TAG, "Weight: " + grams.toString());
                    mWeight.setText(grams.toString() + " g");
                }
            });
        }
        else if (SITUBluetoothService.ACTION_SCALE_UNSUPPORTED.equals(event.eventType)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConnectionState.setText("Unsupported");
                    mConnectionState.setTextColor(Color.rgb(255, 140, 0));
                    mWeight.setText("N/A");
                }
            });
        }
    }
}
