package ai.fritz.camera;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;


import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.fritz.core.FritzManagedModel;
import ai.fritz.core.FritzOnDeviceModel;
import ai.fritz.core.ModelReadyListener;
import ai.fritz.core.utils.FritzModelManager;
import ai.fritz.customtflite.FritzTFLiteInterpreter;


public class CustomTFLiteClassifier {

    private final String TAG = this.getClass().getSimpleName();

    // The tensorflow lite file
    private FritzTFLiteInterpreter tfliteInterpreter;


    protected ByteBuffer imgData = null;
    // Output array [batch_size, 10]
    private float[][] mnistOutput = null;

    // Name of the file in the assets folder

    // Specify the output size
    private static final int NUMBER_LENGTH = 5;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    // Specify the input size
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;
    private static final int DIM_PIXEL_SIZE = 3;

    // Number of bytes to hold a float (32 bits / float) / (8 bits / byte) = 4 bytes / float
    private static final int BYTE_SIZE_OF_FLOAT = 4;

    public CustomTFLiteClassifier(Activity activity){

        FritzOnDeviceModel onDeviceModel = new Optimized_graphCustomModel();
        tfliteInterpreter = new FritzTFLiteInterpreter(onDeviceModel);
        imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * DIM_IMG_SIZE_X
                                * DIM_IMG_SIZE_Y
                                * DIM_PIXEL_SIZE
                                *  BYTE_SIZE_OF_FLOAT);
        imgData.order(ByteOrder.nativeOrder());
        mnistOutput = new float[DIM_BATCH_SIZE][NUMBER_LENGTH];
        Log.d(TAG, "Created a Tensorflow Lite MNIST Classifier.");
    }

    /**
     * Run the TFLite model
     */
    protected void runInference() {
        tfliteInterpreter.run(imgData, mnistOutput);
    }

    /**
     * Classifies the number with the mnist model.
     *
     * @param bitmap
     * @return the identified number
     */
    public int classify(Bitmap bitmap) {
        if (tfliteInterpreter == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
            return -1;
        }
        convertBitmapToByteBuffer(bitmap);
        runInference();

        return getResult();
    }

    /**
     * Go through the output and find the number that was identified.
     *
     * @return the number that was identified (returns -1 if one wasn't found)
     */
    private int getResult() {
        float highest = mnistOutput[0][0];
        int obj = -1;

        for (int i = 0; i < mnistOutput[0].length; i++) {

            float value = mnistOutput[0][i];

            if(value > highest){
                highest = value;
                obj = i;
            }
            //blender bottle
            //glasses
            //lock
            //monitor
            //thermometer
            Log.d(TAG, "Output for " + Integer.toString(i) + ": " + Float.toString(value));

        }
        Log.d(TAG,"obj is " + obj);
        return obj;
    }

    /**
     * Converts it into the Byte Buffer to feed into the model
     *
     * @param bitmap
     */
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();

        Bitmap resize = Bitmap.createScaledBitmap(bitmap, 224, 224, false);

        int width = resize.getWidth();
        int height = resize.getHeight();

        int[] pixels = new int[width * height];

        resize.getPixels(pixels, 0, resize.getWidth(), 0, 0, resize.getWidth(), resize.getHeight());


        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                final int val = pixels[pixel++];
                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }

    }


}
