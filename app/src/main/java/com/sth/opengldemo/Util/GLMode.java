package com.sth.opengldemo.Util;

/**
 * Created by Fishby on 2017/5/23.
 */

public class GLMode {

    public enum MODE_VIEW {
        MODE_VIEW_IN, MODE_VIEW_OUT
    }

    public enum MODE_SENSOR {
        MODE_SENSOR_GESTURE, MODE_SENSOR_ROTATION
    }

    public enum MODE_VR {
        MODE_VR_OFF, MODE_VR_ON
    }

    //public static final int MODE_VIEW_IN = 1;
    //public static final int MODE_VIEW_OUT = 2;

    //public static final int MODE_SENSOR_GESTURE = 1;
    //public static final int MODE_SENSOR_ROTATION = 2;

    //public static final int MODE_VR_OFF = 1;
    //public static final int MODE_VR_ON = 2;

    public MODE_VIEW Mode_View_InOut = MODE_VIEW.MODE_VIEW_IN;
    public MODE_SENSOR Mode_Sensor_GesRot = MODE_SENSOR.MODE_SENSOR_ROTATION;
    public MODE_VR Mode_View_VR = MODE_VR.MODE_VR_OFF;
}
