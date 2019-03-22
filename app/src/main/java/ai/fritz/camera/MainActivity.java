package ai.fritz.camera;

import android.graphics.Canvas;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.core.FritzOnDeviceModel;

import ai.fritz.fritzvisionobjectmodel.ObjectDetectionOnDeviceModel;
import ai.fritz.vision.FritzVision;
import ai.fritz.vision.FritzVisionImage;
import ai.fritz.vision.FritzVisionObject;
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

    private int object = -1;

    private TextView label;



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
                        // ----------------------------------
                        if(objectResult != null){
                            objectResult.drawBoundingBoxes(canvas, cameraViewSize);
                        }

                        switch(object){
                            case 0:label.setText("Blender bottle"); break;
                            case 1:label.setText("Glasses");break;
                            case 2:label.setText("Lock");break;
                            case 3:label.setText("Monitor");break;
                            case 4:label.setText("Thermometer");break;
                        }

                    }
                });
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
                        List<FritzVisionObject> visionObjects = objectResult.getVisionObjects();

                        float bottom = visionObjects.get(0).getBoundingBox().bottom;
                        float top = visionObjects.get(0).getBoundingBox().top;
                        float left = visionObjects.get(0).getBoundingBox().left;
                        float right = visionObjects.get(0).getBoundingBox().right;

                        Log.d("tag",bottom + " " + top + " " + left + " " + right + "");

                        object = classifier.classify(fritzVisionImage.getBitmap());


                        // Fire callback to change the OverlayView
                        requestRender();
                        computing.set(false);
                    }
                });
    }
}
