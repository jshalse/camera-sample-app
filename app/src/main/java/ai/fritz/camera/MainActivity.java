package ai.fritz.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.fritzvisionobjectmodel.FritzVisionObjectPredictor;
import ai.fritz.fritzvisionobjectmodel.FritzVisionObjectResult;

import ai.fritz.vision.inputs.FritzVisionImage;
import ai.fritz.vision.inputs.FritzVisionOrientation;
import ai.fritz.vision.predictors.FritzVisionPredictor;
import ai.fritz.visionlabel.FritzVisionLabelPredictor;
import ai.fritz.visionlabel.FritzVisionLabelResult;

public class MainActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);

    private AtomicBoolean computing = new AtomicBoolean(false);

    private FritzVisionImage styledImage;
    FritzVisionImage visionImage;
    TextView textView;


    // STEP 1:
    // TODO: Define the predictor variable
    // private FritzVisionStylePredictor predictor;
     FritzVisionLabelPredictor visionPredictor;
     FritzVisionLabelResult labelResult;
    // END STEP 1

    private Size cameraViewSize;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        textView = (TextView)findViewById(R.id.textView);

        // Initialize Fritz
        Fritz.configure(this,"c8df3628771648f2960de5e3fca29053");

        // STEP 1: Get the predictor and set the options.
        // ----------------------------------------------
        // TODO: Add the predictor snippet here
        // predictor = FritzVisionStyleTransfer.getPredictor(this, ArtisticStyle.STARRY_NIGHT);
         visionPredictor = new FritzVisionLabelPredictor();
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

        // Callback draws a canvas on the OverlayView
        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        // STEP 4: Draw the prediction result
                        // ----------------------------------
                        if (styledImage != null) {
                            // TODO: Draw or show the result here
                            // styledImage.drawOnCanvas(canvas);
                            // Draw the original image that was passed into the predictor

                        }
                        // ----------------------------------
                        // END STEP 4
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
        // ------------------------------------------------------------------------
        // TODO: Add code for creating FritzVisionImage from a media.Image object
        // Get the system service for the camera manager
        final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Gets the first camera id
        String cameraId = null;
        try {
            cameraId = manager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

// Determine the rotation on the FritzVisionImage from the camera orientaion and the device rotation.
// "this" refers to the calling Context (Application, Activity, etc)
        int imageRotationFromCamera = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);

        // int rotationFromCamera = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
        // final FritzVisionImage fritzImage = FritzVisionImage.fromMediaImage(image, rotationFromCamera);
       visionImage = FritzVisionImage.fromMediaImage(image, imageRotationFromCamera);
        // ------------------------------------------------------------------------
        // END STEP 2

        image.close();


        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        // STEP 3: Run predict on the image
                        // ---------------------------------------------------
                        // TODO: Add code for running prediction on the image
                        // final long startTime = SystemClock.uptimeMillis();
                        // styledImage = predictor.predict(fritzImage);
                        // styledImage.scale(cameraViewSize.getWidth(), cameraViewSize.getHeight());
                        // Log.d(TAG, "INFERENCE TIME:" + (SystemClock.uptimeMillis() - startTime));
                        // ----------------------------------------------------
                        // END STEP 3
                         labelResult = visionPredictor.predict(visionImage);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                // Stuff that updates the UI
                                textView.setText(labelResult.getResultString());
                                labelResult.logResult();
                            }
                        });
                        // Fire callback to change the OverlayView
                        requestRender();
                        computing.set(false);
                    }
                });
    }
}
