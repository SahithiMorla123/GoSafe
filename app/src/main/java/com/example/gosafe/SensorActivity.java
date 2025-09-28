package com.example.gosafe;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView tvAccelValue;
    private SensorGraphView sensorGraphView;

    private static final float SHAKE_THRESHOLD_G_FORCE = 2.5f;
    private static final int SHAKE_COOLDOWN_MS = 5000;
    private long lastShakeTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        tvAccelValue = findViewById(R.id.tvAccelValue);
        sensorGraphView = findViewById(R.id.sensorGraph);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        Log.d("SensorActivity_DEBUG", "Activity Created. Accelerometer is: " + (accelerometer != null ? "Ready" : "Not Found"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            Log.d("SensorActivity_DEBUG", "Sensor listener registered.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d("SensorActivity_DEBUG", "Sensor listener unregistered.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            double gForce = magnitude / SensorManager.GRAVITY_EARTH;

            tvAccelValue.setText(String.format("%.2f G", gForce));
            sensorGraphView.addDataPoint((float) gForce);

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastShakeTime) > SHAKE_COOLDOWN_MS) {
                if (gForce > SHAKE_THRESHOLD_G_FORCE) {
                    // *** CRITICAL LOG MESSAGE 1 ***
                    Log.d("SensorActivity_DEBUG", "SHAKE DETECTED! G-Force: " + gForce);
                    lastShakeTime = currentTime;
                    triggerSOSFromShake();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void triggerSOSFromShake() {

        Log.d("SensorActivity_DEBUG", "Sending TRIGGER_SOS command to MainActivity.");
        Toast.makeText(this, "Shake Detected! Triggering SOS...", Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("TRIGGER_SOS", true);
        startActivity(intent);
        finish();
    }
}