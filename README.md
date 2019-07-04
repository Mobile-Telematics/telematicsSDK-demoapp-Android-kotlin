# Raxel demo app

This is an app that demostrate using of RaxelPulse framework. The app will track the person's driving behavior such as speeding, turning, braking and several other things.

# Installation
  - clone this repository to local folder
  - open project with Android Studio
  - replace stub with your device token:
    ```sh
    val api = TrackingApi.getInstance()
    api.initialize(this, settings)
    api.setDeviceID("YOUR TOKEN")
    ```
  - build project and run