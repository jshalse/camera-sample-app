package ai.fritz.camera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.Image;
import android.media.ImageReader;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicBoolean;

import ai.fritz.core.Fritz;
import ai.fritz.fritzvisionstylemodel.ArtisticStyle;
import ai.fritz.fritzvisionstylemodel.FritzStyleResolution;
import ai.fritz.fritzvisionstylemodel.FritzVisionStylePredictor;
import ai.fritz.fritzvisionstylemodel.FritzVisionStylePredictorOptions;
import ai.fritz.fritzvisionstylemodel.FritzVisionStyleTransfer;
import ai.fritz.vision.inputs.FritzVisionImage;
import ai.fritz.vision.inputs.FritzVisionOrientation;
import ai.fritz.vision.predictors.FritzVisionPredictor;

public class MainActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);

    private AtomicBoolean computing = new AtomicBoolean(false);

    private FritzVisionImage styledImage;

    // STEP 1:
    // TODO: Define the predictor variable
    // private FritzVisionStylePredictor predictor;
    // END STEP 1

    private Size cameraViewSize;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Fritz
        Fritz.configure(this);

        // STEP 1: Get the predictor and set the options.
        // ----------------------------------------------
        // TODO: Add the predictor snippet here
        // predictor = FritzVisionStyleTransfer.getPredictor(this, ArtisticStyle.STARRY_NIGHT);
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
        // int rotationFromCamera = FritzVisionOrientation.getImageRotationFromCamera(this, cameraId);
        // final FritzVisionImage fritzImage = FritzVisionImage.fromMediaImage(image, rotationFromCamera);
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

                        // Fire callback to change the OverlayView
                        requestRender();
                        computing.set(false);
                    }
                });
    }
}
