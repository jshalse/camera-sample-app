package ai.fritz.camera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;


import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.core.FritzOnDeviceModel;

import ai.fritz.fritzvisionobjectmodel.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictorOptions;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;


public class MainActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);

    private AtomicBoolean computing = new AtomicBoolean(false);

    private FritzVisionObjectPredictor objectPredictor;
    private FritzVisionObjectResult objectResult;
    private FritzVisionImage fritzVisionImage;
    private CustomTFLiteClassifier classifier;
    private int imageRotation;
    private Intent customModelIntent;

    private static String speechSubscriptionKey = "75e2f2cf3bda44c0b7a43ea56ed89cb3";
    private static String serviceRegion = "westus";


    private int object = -1;
    private AlertDialog alert;
    private TextView label;
    private Boolean dup = false;
    private Boolean empty = true;
    private ArrayList<String> listOfObjects = new ArrayList<String>();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fritz
        Fritz.configure(this,"c8df3628771648f2960de5e3fca29053");
        label = (TextView)findViewById(R.id.textView);

        // STEP 1: Get the predictor and set the options.

        classifier = new CustomTFLiteClassifier(this);
        // ----------------------------------------------
        // END STEP 1

        //ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET}, requestCode);

    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_stylize;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    @Override
    public void onPreviewSizeChosen(final Size previewSize, final Size cameraViewSize, final int rotation) {

        imageRotation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);

        FritzVisionObjectPredictorOptions options = new FritzVisionObjectPredictorOptions.Builder()
                .confidenceThreshold(.65f).build();

        FritzOnDeviceModel onDeviceModel = new ObjectDetectionOnDeviceModel();
        objectPredictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel,options);

        // Callback draws a canvas on the OverlayView
        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        // STEP 4: Draw the prediction result
                            switch (object) {
                                case 0:
                                    label.setText("MacBook");
                                    break;
                                case 1:
                                    label.setText("Blender Bottle");
                                    break;
                                case 2:
                                    label.setText("Gloves");
                                    break;
                                case 3:
                                    label.setText("Lock");
                                    break;
                                case 4:
                                    label.setText("Remote Controller");
                                    break;
                                case 5:
                                    label.setText("Vigileo Monitor");
                                    break;
                            }

                            for(int x = 0; x < listOfObjects.size();x++){
                                if(listOfObjects.get(x).equals(label.getText())){
                                    dup = true;
                                }
                            }

                            if (!(label.getText().equals("TextView")) && dup == false && empty == true) {
                                listOfObjects.add(label.getText()+"");
                                empty = false;

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Is " + label.getText().toString() + " the correct object? Please say outloud yes or no")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // FIRE ZE MISSILES!
//                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://team34backend.azurewebsites.net/" + label.getText() + ".pdf"));
//                                            startActivity(browserIntent);
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User cancelled the dialog
                                            empty = true;
                                        }
                                    });

                            alert = builder.create();
                            alert.show();

                            listenToSpeech();

                            //alert.dismiss();

                        }

                        dup = false;
                            // alert.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

                            // finish();
                            // customModelIntent = new Intent(MainActivity.this, ChosenCustomModel.class);
                            // MainActivity.this.startActivity(customModelIntent);

                        }
                });
    }

    private void listenToSpeech() {
        TextView txt = (TextView) this.findViewById(R.id.showText);

        Log.d("tag","listen to speech");

        try {
            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
            assert(config != null);

            SpeechRecognizer reco = new SpeechRecognizer(config);
            assert(reco != null);

            Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            assert(task != null);

            // Note: this will block the UI thread
            SpeechRecognitionResult result = task.get();
            assert(result != null);
            Log.d("tag",result.toString() + " this is result");

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                String output_result = result.toString();

                String answer = "";
                for (int i = output_result.length()-4; i > 0; i--){
                    if(output_result.charAt(i) != '<'){
                        answer = output_result.charAt(i) + answer;
                    }else {
                        break;
                    }
                }
                Log.d("tag", answer);
                answer.toLowerCase();


                if (answer.contains("yes")){
                    // Add code to pull up corresponding instruction
                    alert.dismiss();
                    txt.setText("Pop up pdf!");

                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://team34backend.azurewebsites.net/" + label.getText() + ".pdf"));
                    //startActivity(browserIntent);

                }else if (answer.contains("no")) {
                    alert.dismiss();
                    // Go back and restart detecting objects
                }
            }
            else {
                txt.setText("Try again.");
            }
            Log.d("tag",txt.getText()+"");

            reco.close();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
        }
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = reader.acquireLatestImage();

        if (image == null) {
            return;
        }

        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }

        // STEP 2: Create the FritzVisionImage object from media.Image
        fritzVisionImage  = FritzVisionImage.fromMediaImage(image, imageRotation);

        // ------------------------------------------------------------------------
        // END STEP 2

        image.close();

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        // STEP 3: Run predict on the image
                        objectResult = objectPredictor.predict(fritzVisionImage);

                        object = classifier.classify(fritzVisionImage.getBitmap());


                        // Fire callback to change the OverlayView
                        requestRender();
                        computing.set(false);
                    }
                });
    }


}
