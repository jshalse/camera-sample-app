# Sample Camera App
A simple camera app for Android that allows you to run ML predictions on the video input.

Get Started:
=============

1. Setup your Fritz account (https://fritz.ai/).
2. Fork/Clone this repo and run it in Android Studios.
3. Edit the AndroidManifest.xml andd add your API key. This is in the Fritz webapp under Project Settings > App > Show API Key.
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="ai.fritz.camera">
    <application ...>
      <meta-data
                  android:name="fritz_api_key"
                  android:value="your app's api key" />
    </application>
</manifest>
...
```
4.Follow the documentation or a tutorial to add in specific Fritz feature or add your own custom model.

Object Detection: https://docs.fritz.ai/features/object-detection/about.html
Image Labeling: https://docs.fritz.ai/features/image-labeling/about.html
Style Transefer: https://docs.fritz.ai/features/style-transfer/about.html

Tutorials:
============
Style Transfer: https://heartbeat.fritz.ai/real-time-style-transfer-for-android-6a9d238dfdb5
