package ai.fritz.camera;

import ai.fritz.core.FritzOnDeviceModel;

public class Optimized_graphCustomModel extends FritzOnDeviceModel {

    private static final String MODEL_PATH = "file:///android_asset/optimized_graph.tflite";
    private static final String MODEL_ID = "b5a722100d0b4fa3af4a46bc9c166440";
    private static final int MODEL_VERSION = 1;

    public Optimized_graphCustomModel() {
        super(MODEL_PATH, MODEL_ID, MODEL_VERSION);
    }
}


