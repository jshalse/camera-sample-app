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

public class MainActivity extends BaseCameraActivity implements ImageReader.OnImageAvailableListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Size DESIRED_PREVIEW_SIZE = new Size(1280, 960);

    private AtomicBoolean computing = new AtomicBoolean(false);

    private FritzVisionImage styledImage;

    private FritzVisionStylePredictor predictor;
    private FritzVisionStylePredictorOptions options;

    private Size cameraViewSize;

    private OverlayView overlayView;
    private int activeStyleIndex = 0;

    private FritzVisionOrientation orientation;

    private Bitmap styledBitmap = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fritz.configure(this);
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

        // Step 1: Get the predictor and set the options
        options = new FritzVisionStylePredictorOptions.Builder()
                .imageResolution(FritzStyleResolution.NORMAL)
                .build();
        ArtisticStyle[] styles = ArtisticStyle.values();
        predictor = FritzVisionStyleTransfer.getPredictor(this, styles[activeStyleIndex], options);
        orientation = FritzVisionOrientation.getImageOrientationFromCamera(this, cameraId);

        this.cameraViewSize = cameraViewSize;

        // Callback draws a canvas on the OverlayView
        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        // Step 4: Draw the prediction result
                        if (styledImage != null) {
                            Matrix matrix = new Matrix();
                            canvas.drawBitmap(styledBitmap, matrix, new Paint());
                        }
                    }
                });

        // Change the predictor
        overlayView = findViewById(R.id.debug_overlay);
        overlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNextPredictor();
            }
        });
    }

    private void getNextPredictor() {
        ArtisticStyle[] styles = ArtisticStyle.values();
        activeStyleIndex = ++activeStyleIndex % styles.length;

        Toast.makeText(this,
                styles[activeStyleIndex].name() + " Style Shown", Toast.LENGTH_LONG).show();
        predictor = FritzVisionStyleTransfer.getPredictor(this, styles[activeStyleIndex], options);
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

        // Step 2: Create the FritzVisionImage object from media.Image
        final FritzVisionImage fritzImage = FritzVisionImage.fromMediaImage(image);
        fritzImage.setOrientation(orientation);
        image.close();


        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {

                        // Step 3: Run inference on the image
                        final long startTime = SystemClock.uptimeMillis();
                        styledImage = predictor.predict(fritzImage);
                        styledImage.scale(cameraViewSize.getWidth(), cameraViewSize.getHeight());
                        styledBitmap = styledImage.getBitmap();
                        Log.d(TAG, "INFERENCE TIME:" + (SystemClock.uptimeMillis() - startTime));

                        requestRender();
                        computing.set(false);
                    }
                });
    }
}
