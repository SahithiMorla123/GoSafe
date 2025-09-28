package com.example.gosafe;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnSOS, btnContacts, btnSensors;
    private EditText etTimerMinutes;
    private TextView tvCountdown;
    private Button btnStartTimer, btnCancelTimer;
    private Button btnFakeCall, btnDisguiseApp, btnRestoreApp;
    private FusedLocationProviderClient fusedLocationClient;
    private ArrayList<String> emergencyContacts = new ArrayList<>();
    private CountDownTimer countDownTimer;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> { /* ... */ });
    private final ActivityResultLauncher<Intent> contactPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> { if (result.getResultCode() == RESULT_OK && result.getData() != null) { ArrayList<String> selectedNumbers = result.getData().getStringArrayListExtra("selected_numbers"); if (selectedNumbers != null && !selectedNumbers.isEmpty()) { emergencyContacts.clear(); emergencyContacts.addAll(selectedNumbers); Toast.makeText(this, "Contacts saved: " + emergencyContacts.size(), Toast.LENGTH_SHORT).show(); } } });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This initialization order is now safe because all buttons exist in the XML
        btnSOS = findViewById(R.id.btnSOS);
        btnContacts = findViewById(R.id.btnContacts);
        btnSensors = findViewById(R.id.btnSensors);
        etTimerMinutes = findViewById(R.id.etTimerMinutes);
        tvCountdown = findViewById(R.id.tvCountdown);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnCancelTimer = findViewById(R.id.btnCancelTimer);
        btnFakeCall = findViewById(R.id.btnFakeCall);
        btnDisguiseApp = findViewById(R.id.btnDisguiseApp);
        btnRestoreApp = findViewById(R.id.btnRestoreApp);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // All listeners are set here
        btnSOS.setOnClickListener(v -> { if (hasPermissions()) sendSOS(); else requestPermissions(); });
        btnContacts.setOnClickListener(v -> { Intent intent = new Intent(this, ContactActivity.class); contactPickerLauncher.launch(intent); });
        btnSensors.setOnClickListener(v -> { Intent intent = new Intent(this, SensorActivity.class); startActivity(intent); });
        btnStartTimer.setOnClickListener(v -> startTimer());
        btnCancelTimer.setOnClickListener(v -> cancelTimer());
        btnFakeCall.setOnClickListener(v -> { Toast.makeText(this, "Fake call in 15s.", Toast.LENGTH_SHORT).show(); new Handler().postDelayed(() -> { Intent intent = new Intent(this, FakeCallActivity.class); startActivity(intent); }, 15000); });
        btnDisguiseApp.setOnClickListener(v -> disguiseApp());
        btnRestoreApp.setOnClickListener(v -> restoreAppIcon());
    }

    private void disguiseApp() {
        ComponentName defaultAlias = new ComponentName(this, "com.example.gosafe.GoSafeLauncher");
        ComponentName calculatorAlias = new ComponentName(this, "com.example.gosafe.CalculatorLauncher");
        getPackageManager().setComponentEnabledSetting(defaultAlias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        getPackageManager().setComponentEnabledSetting(calculatorAlias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(this, "App disguised. Relaunch from 'Calculator'.", Toast.LENGTH_LONG).show();
    }

    private void restoreAppIcon() {
        ComponentName defaultAlias = new ComponentName(this, "com.example.gosafe.GoSafeLauncher");
        ComponentName calculatorAlias = new ComponentName(this, "com.example.gosafe.CalculatorLauncher");
        getPackageManager().setComponentEnabledSetting(defaultAlias, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        getPackageManager().setComponentEnabledSetting(calculatorAlias, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(this, "App icon restored.", Toast.LENGTH_LONG).show();
    }

    // --- All other methods (sendSOS, etc.) ---
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); if (intent != null && intent.getBooleanExtra("TRIGGER_SOS", false)) { Toast.makeText(this, "SOS triggered by shake!", Toast.LENGTH_LONG).show(); new Handler().postDelayed(() -> { if (hasPermissions()) sendSOS(); else requestPermissions(); }, 500); } }
    private boolean hasPermissions() { return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED; }
    private void requestPermissions() { requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS}); }
    private void sendSOS() { if (emergencyContacts.isEmpty()) { Toast.makeText(this, "No contacts. Add contacts first.", Toast.LENGTH_LONG).show(); return; } if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) { return; } fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> { if (location != null) { String message = "EMERGENCY! Location: https://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude(); try { SmsManager smsManager = SmsManager.getDefault(); for (String number : emergencyContacts) { smsManager.sendTextMessage(number, null, message, null, null); } Toast.makeText(this, "SOS sent.", Toast.LENGTH_LONG).show(); } catch (Exception e) { Toast.makeText(this, "Failed to send SMS.", Toast.LENGTH_SHORT).show(); } } else { Toast.makeText(this, "Could not get location.", Toast.LENGTH_LONG).show(); } }); }
    private void startTimer() { String minutesStr = etTimerMinutes.getText().toString(); if (minutesStr.isEmpty()) { Toast.makeText(this, "Enter minutes.", Toast.LENGTH_SHORT).show(); return; } long minutes = Long.parseLong(minutesStr); countDownTimer = new CountDownTimer(minutes * 60 * 1000, 1000) { public void onTick(long millis) { tvCountdown.setText(String.format("%02d:%02d", (millis/1000)/60, (millis/1000)%60)); } public void onFinish() { sendSOS(); resetTimerUI(); }}.start(); btnStartTimer.setEnabled(false); btnCancelTimer.setEnabled(true); etTimerMinutes.setEnabled(false); }
    private void cancelTimer() { if (countDownTimer != null) { countDownTimer.cancel(); Toast.makeText(this, "Timer cancelled.", Toast.LENGTH_SHORT).show(); resetTimerUI(); } }
    private void resetTimerUI() { tvCountdown.setText("00:00"); btnStartTimer.setEnabled(true); btnCancelTimer.setEnabled(false); etTimerMinutes.setEnabled(true); etTimerMinutes.setText(""); }
}