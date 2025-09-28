# GoSafe - Proactive Android Safety Application

GoSafe is a comprehensive personal safety application for Android, built from the ground up to provide users with both reactive and proactive tools to enhance their security in various situations. The app was developed in a one-day sprint, focusing on core safety features and a clean, modern user interface.

## 🌟 Core Features

This app is more than just an SOS button. It includes a suite of intelligent features designed to prevent dangerous situations and provide peace of mind.

*   **Immediate SOS:** A prominent button to instantly send an SMS with the user's live GPS coordinates to pre-selected emergency contacts.
*   **Shake & Sensor (Shake-to-SOS):** In an emergency where the user cannot unlock their phone, a firm shake of the device will automatically trigger the SOS alert.
*   **Smart Check-In Timer:** A proactive safety feature where a user can set a timer for an activity (e.g., walking home). If they do not mark themselves as "safe" before the timer expires, an SOS is automatically sent.
*   **Fake Call Request:** Allows the user to trigger a simulated incoming phone call after a 15-second delay, providing a believable excuse to leave an uncomfortable or unsafe situation.
*   **Emergency Contact Management:** An easy-to-use interface to add, view, and delete emergency contacts, which are saved securely on the device.
*   **App Disguise (Stealth Mode):** For user privacy and to prevent an aggressor from easily identifying the app, the user can change the app's home screen icon and name to that of a generic "Calculator" app.
*   **Live Sensor Data:** A dedicated screen that visualizes live data from the phone's accelerometer, including a real-ti

## 🛠️ Technology & Implementation

*   **Language:** Java
*   **IDE:** Android Studio
*   **Core Components:**
    *   **UI:** Modern dark theme with `ConstraintLayout` for responsive and flexible screen designs. Custom styles and drawable resources for a professional look and feel.
    *   **Activities:** Uses multiple activities (`MainActivity`, `ContactActivity`, `SensorActivity`, etc.) for screen management.
    *   **Sensors:** Implements the `SensorManager` to access the `TYPE_ACCELEROMETER` for shake detection and live data visualization.
    *   **Location:** Utilizes `FusedLocationProviderClient` from Google Play Services for efficient and accurate GPS location retrieval.
    *   **Data Persistence:** Uses `SharedPreferences` to securely store the user's list of emergency contacts.
    *   **Dynamic UI:** The "App Disguise" feature is implemented using `<activity-alias>` in the `AndroidManifest.xml`, programmatically enabling and disabling components with the `PackageManager`.

## 🚀 How to Run the Project

1.  Clone the repository to your local machine.
2.  Open the project in the latest version of Android Studio.
3.  Let Gradle sync all the required dependencies.
4.  Connect a physical Android device or set up an emulator.
5.  Run the app.

