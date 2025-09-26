package com.example.gosafe; // Or your package name, e.g., com.example.gosafe

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private Button btnSOS, btnContacts, btnSensors;

    // Location client to get GPS coordinates
    private FusedLocationProviderClient fusedLocationClient;

    // This list will hold the phone numbers of emergency contacts
    private ArrayList<String> emergencyContacts = new ArrayList<>();

    // Modern way to handle asking for multiple permissions (Location, SMS)
    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }

                if (allGranted) {
                    // If permissions were granted, try sending the SOS again
                    sendSOS();
                } else {
                    // If permissions were denied, show a message to the user
                    Toast.makeText(this, "SOS requires Location and SMS permissions to function.", Toast.LENGTH_LONG).show();
                }
            });

    // Modern way to get the result back from ContactActivity
    private final ActivityResultLauncher<Intent> contactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // Get the list of selected numbers returned from ContactActivity
                    ArrayList<String> selectedNumbers = result.getData().getStringArrayListExtra("selected_numbers");
                    if (selectedNumbers != null && !selectedNumbers.isEmpty()) {
                        emergencyContacts.clear(); // Clear the old list
                        emergencyContacts.addAll(selectedNumbers); // Add the new ones
                        Toast.makeText(this, emergencyContacts.size() + " contacts saved.", Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the variables to the buttons in the layout file
        btnSOS = findViewById(R.id.btnSOS);
        btnContacts = findViewById(R.id.btnContacts);
        btnSensors = findViewById(R.id.btnSensors);

        // Initialize the location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // --- Set up what happens when each button is clicked ---

        btnSOS.setOnClickListener(v -> {
            // Check if we already have the necessary permissions
            if (hasPermissions()) {
                sendSOS(); // If yes, send the SOS
            } else {
                requestPermissions(); // If no, ask for them
            }
        });

        btnContacts.setOnClickListener(v -> {
            // Create an intent to open the ContactActivity
            Intent intent = new Intent(MainActivity.this, ContactActivity.class);
            contactPickerLauncher.launch(intent); // Launch it and wait for a result
        });

        btnSensors.setOnClickListener(v -> {
            // Create an intent to open the SensorActivity
            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Checks if both Location and SMS permissions have been granted by the user.
     */
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Launches the system dialog to ask the user for permissions.
     */
    private void requestPermissions() {
        requestPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS
        });
    }

    /**
     * The main SOS function. It gets the location and sends the SMS messages.
     */
    private void sendSOS() {
        // First, check if any contacts have been added.
        if (emergencyContacts.isEmpty()) {
            Toast.makeText(this, "Please add emergency contacts first.", Toast.LENGTH_LONG).show();
            return;
        }

        // We must check for permissions here again, as the system requires it.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission is required to send SOS.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the last known location from the phone's GPS
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // We found the location
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        String message = "EMERGENCY! I need help. My current location is: " +
                                "https://maps.google.com/?q=" + latitude + "," + longitude;

                        // Send the SMS to every contact in our list
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            for (String number : emergencyContacts) {
                                smsManager.sendTextMessage(number, null, message, null, null);
                            }
                            Toast.makeText(this, "SOS message sent to " + emergencyContacts.size() + " contacts.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to send SMS. Check permissions.", Toast.LENGTH_SHORT).show();
                            Log.e("SOS_ERROR", "SMS sending failed", e);
                        }
                    } else {
                        // We could not get the location
                        Toast.makeText(this, "Could not get your location. Please enable GPS and try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}