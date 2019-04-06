package ai.fritz.camera;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.core.FritzOnDeviceModel;

import ai.fritz.fritzvisionobjectmodel.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionOrientation;
import ai.fritz.vision.objectdetection.FritzVisionObjectPredictor;
import ai.fritz.vision.objectdetection.FritzVisionObjectResult;


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

    private final int REQ_CODE = 100;


    private int object = -1;
    private AlertDialog alert;
    private TextView label;
    private String prevObj = "";
    private String prevprevObj = "";

    private enum Object{BlenderBottle,Glasses,Lock,Monitor,Thermomter};


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

//        FritzVisionObjectPredictorOptions options = new FritzVisionObjectPredictorOptions.Builder()
//                .confidenceThreshold(.6f).build();
        //objectPredictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel, options);

        FritzOnDeviceModel onDeviceModel = new ObjectDetectionOnDeviceModel();
        objectPredictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);

        // Callback draws a canvas on the OverlayView
        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        // STEP 4: Draw the prediction result

                            switch (object) {
                                case 0:
                                    label.setText("Blender bottle");
                                    break;
                                case 1:
                                    label.setText("Glasses");
                                    break;
                                case 2:
                                    label.setText("Lock");
                                    break;
                                case 3:
                                    label.setText("Monitor");
                                    break;
                                case 4:
                                    label.setText("Thermometer");
                                    break;
                            }


                        if (!(label.getText().equals("TextView")) && !(prevObj.equals(label.getText())) && !(prevprevObj.equals(label.getText()))) {
                            prevObj = label.getText().toString();
                            if(!prevprevObj.equals(prevObj)) {
                                prevprevObj = prevObj;
                            }

                            listenToSpeech();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Is a " + label.getText().toString() + " the correct object?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // FIRE ZE MISSILES!
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // User cancelled the dialog
                                        }
                                    });
                                alert = builder.create();

                                alert.show();

                        }
                            //alert.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

//                        finish();
//                        customModelIntent = new Intent(MainActivity.this, ChosenCustomModel.class);
//                        MainActivity.this.startActivity(customModelIntent);

                        }
                });

    }

    private void listenToSpeech(){
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Pull up instruction for the object? YES/NO");

        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQ_CODE);
        }
        else {
            Toast.makeText(this, "Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data){
                    String answer = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0);

                    if (answer.equalsIgnoreCase("Yes")){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("pdf url"));
                        startActivity(browserIntent);

                    }else {

                        //Go back and restart detecting objects
                    }
                }
                break;
            }
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
