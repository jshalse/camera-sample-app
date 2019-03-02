package ai.fritz.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.core.FritzOnDeviceModel;


import ai.fritz.customtflite.FritzTFLiteInterpreter;
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

    FritzVisionObjectPredictor objectPredictor;
    FritzVisionObjectResult objectResult;
    FritzVisionImage fritzVisionImage;
    FritzTFLiteInterpreter tfliteInterpreter;
    int imageRotation;

    private Size cameraViewSize;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize Fritz
        Fritz.configure(this,"c8df3628771648f2960de5e3fca29053");

        // STEP 1: Get the predictor and set the options.
//        FritzOnDeviceModel onDeviceModel = new ObjectDetectionOnDeviceModel();
//        objectPredictor = FritzVision.ObjectDetection.getPredictor(onDeviceModel);
        FritzOnDeviceModel onDeviceModel = new Optimized_graphCustomModel();
        tfliteInterpreter = new FritzTFLiteInterpreter(onDeviceModel);

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

        this.cameraViewSize = cameraViewSize;

        imageRotation = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
        final Size targetSize = new Size(1280, 630);
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
                            objectResult.drawBoundingBoxes(canvas, targetSize);
                        }
                    }
                });
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = reader.acquireLatestImage();
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        if (image == null) {
            return;
        }

        if (!computing.compareAndSet(false, true)) {
            image.close();
            return;
        }

        // STEP 2: Create the FritzVisionImage object from media.Image
        int imageRotationFromCamera = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
        fritzVisionImage  = FritzVisionImage.fromMediaImage(image, imageRotationFromCamera);

        // ------------------------------------------------------------------------
        // END STEP 2

        image.close();

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        // STEP 3: Run predict on the image
                        objectResult = objectPredictor.predict(fritzVisionImage);


                        // Fire callback to change the OverlayView
                        requestRender();
                        computing.set(false);
                    }
                });
    }
}
