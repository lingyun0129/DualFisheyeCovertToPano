package com.sth.opengldemo.View;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;

import com.sth.opengldemo.Util.GLMode;
import com.sth.opengldemo.Util.PanoMediaPlayerWrapper;
import com.sth.opengldemo.Util.ShaderUtils;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Fishby on 2017/5/23.
 */

public class GLRenderer_Fisheye2Sphere implements GLSurfaceView.Renderer
        , SurfaceTexture.OnFrameAvailableListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SensorEventListener {

    private static final String TAG = "GLRenderer_F2S";
    private Context context;
    private int aPositionHandle;
    private int programId;

    private Lock modelMatrixLock = new ReentrantLock();

    private float[] modelMatrix = new float[16];
    private float[] projectionMatrix=new float[16];
    private float[] viewMatrix = new float[16];
    private float[] modelViewMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private int uMatrixHandle;

    private int uTextureSamplerHandle;
    private int aTextureCoordHandle_left;
    private int aTextureCoordHandle_right;
    private int aTextureAlphaHandle;

    public float info_FOVY = 0;
    public int info_SCREEN_WIDTH = 0;
    public int info_SCREEN_HEIGHT = 0;
    public int info_VIDEO_WIDTH = 0;
    public int info_VIDEO_HEIGHT = 0;

    private Handler mHandler;

    private int textureId;

    private SurfaceTexture surfaceTexture;
    //private MediaPlayer mediaPlayer;
    private float[] mSTMatrix = new float[16];
    private int uSTMMatrixHandle;

    private boolean updateSurface;
    private boolean playerPrepared;
    private int screenWidth,screenHeight;
    private PanoMediaPlayerWrapper wrapperMediaPlayer=null;
    private PanoUIController panoUIController=null;
    public GLMode glMode = new GLMode();

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTexCoord_left;\n" +
                    "attribute vec4 aTexCoord_right;\n" +
                    "attribute float aTexAlpha;\n" +
                    "varying vec2 vTexCoord_left;\n" +
                    "varying vec2 vTexCoord_right;\n" +
                    "varying float vTexAlpha;\n" +
                    "uniform mat4 uMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "void main() {\n" +
                    "    vTexCoord_left = (uSTMatrix * aTexCoord_left).xy;\n" +
                    "    vTexCoord_right = (uSTMatrix * aTexCoord_right).xy;\n" +
                    "    vTexAlpha = aTexAlpha;\n" +
                    "    gl_Position = uMatrix*aPosition;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTexCoord_left;\n" +
                    "varying vec2 vTexCoord_right;\n" +
                    "varying float vTexAlpha;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor=vTexAlpha * texture2D(sTexture, vTexCoord_left) + (1.0 - vTexAlpha) * texture2D(sTexture, vTexCoord_right);\n" +
                    "}\n";

    private Fisheye2SphereModel fisheye2SphereModel;

    public GLRenderer_Fisheye2Sphere(Context context, String videoPath, Handler mHandler,PanoMediaPlayerWrapper player,PanoUIController controller) {
        this.context = context;
        this.mHandler = mHandler;
        playerPrepared=false;
        wrapperMediaPlayer=player;
        panoUIController=controller;
        synchronized(this) {
            updateSurface = false;
        }

        fisheye2SphereModel =new Fisheye2SphereModel(15,180,360);

/*        mediaPlayer=new MediaPlayer();
        try{
            mediaPlayer.setDataSource(context, Uri.parse(videoPath));
        }catch (IOException e){
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setLooping(true);

        mediaPlayer.setOnVideoSizeChangedListener(this);*/

        Matrix.setIdentityM(modelMatrix,0);
        initSensor();
        initGestureHandler();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        programId=ShaderUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        aPositionHandle= GLES20.glGetAttribLocation(programId,"aPosition");

        uMatrixHandle=GLES20.glGetUniformLocation(programId,"uMatrix");
        uSTMMatrixHandle = GLES20.glGetUniformLocation(programId, "uSTMatrix");
        uTextureSamplerHandle=GLES20.glGetUniformLocation(programId,"sTexture");
        aTextureCoordHandle_left=GLES20.glGetAttribLocation(programId,"aTexCoord_left");
        aTextureCoordHandle_right=GLES20.glGetAttribLocation(programId,"aTexCoord_right");
        aTextureAlphaHandle=GLES20.glGetAttribLocation(programId,"aTexAlpha");
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        textureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        ShaderUtils.checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);

        surfaceTexture = new SurfaceTexture(textureId);
        surfaceTexture.setOnFrameAvailableListener(this);

        Surface surface = new Surface(surfaceTexture);


        wrapperMediaPlayer.getmMediaPlayer().setSurface(surface);

        surface.release();
/*
        if (!playerPrepared){
            try {
                mediaPlayer.prepare();
                playerPrepared=true;
            } catch (IOException t) {
                Log.e(TAG, "media player prepare failed");
            }
            mediaPlayer.start();
            playerPrepared=true;
        }*/

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: "+width+" "+height);
        screenWidth=width; screenHeight=height;
        float ratio = (float)screenWidth/screenHeight;
        switch (glMode.Mode_View_VR){
            case MODE_VR_OFF:
                ratio = (float)screenWidth/screenHeight;
                break;
            case MODE_VR_ON:
                ratio = (float)screenWidth/screenHeight/2;
                break;
        }
        Matrix.perspectiveM(projectionMatrix, 0, 90, ratio, 1f, 500f);
        info_FOVY = 90.0f;
        info_SCREEN_WIDTH = screenWidth;
        info_SCREEN_HEIGHT = screenHeight;
        // bug with eyeZ
        Matrix.setLookAtM(viewMatrix, 0,
                0.0f, 0.0f, 17.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //清除屏幕缓存和深度存储
        GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        synchronized (this){
            if (updateSurface){
                surfaceTexture.updateTexImage();
                surfaceTexture.getTransformMatrix(mSTMatrix);
                updateSurface = false;
            }
        }

        //gl.glFrontFace(GL10.GL_CCW);

        gl.glCullFace(GL10.GL_FRONT);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        GLES20.glUseProgram(programId);

        switch (glMode.Mode_View_InOut)
        {
            case MODE_VIEW_IN:
                Matrix.setLookAtM(viewMatrix, 0,
                        0.0f, 0.0f, 0.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 1.0f, 0.0f);
                break;
            case MODE_VIEW_OUT:
                Matrix.setLookAtM(viewMatrix, 0,
                        0.0f, 0.0f, 17.0f,
                        0.0f, 0.0f, -1.0f,
                        0.0f, 1.0f, 0.0f);
                break;
        }

        modelMatrixLock.lock();
        switch (glMode.Mode_Sensor_GesRot)
        {
            case MODE_SENSOR_GESTURE:
                Matrix.setIdentityM(modelMatrix, 0);
                //Matrix.translateM(modelMatrix, 0, 0.0f, 0.0f, 0.0f);
                Matrix.rotateM(modelMatrix, 0, mDeltaY, 1.0f, 0.0f, 0.0f);
                Matrix.rotateM(modelMatrix, 0, mDeltaX, 0.0f, 1.0f, 0.0f);
                break;
            case MODE_SENSOR_ROTATION:

                break;
        }

        //视角从90度到14度
        float currentDegree= (float) (Math.toDegrees(Math.atan(mScale))*2);
        info_FOVY = currentDegree;

        float ratio = (float)screenWidth/screenHeight;
        switch (glMode.Mode_View_VR){
            case MODE_VR_OFF:
                ratio = (float)screenWidth/screenHeight;
                break;
            case MODE_VR_ON:
                ratio = (float)screenWidth/screenHeight/2;
                break;
        }

        Matrix.perspectiveM(projectionMatrix, 0, currentDegree, ratio, 1f, 500f);

        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
        GLES20.glUniformMatrix4fv(uMatrixHandle,1,false,mMVPMatrix,0);

        modelMatrixLock.unlock();

        GLES20.glUniformMatrix4fv(uSTMMatrixHandle, 1, false, mSTMatrix, 0);
        fisheye2SphereModel.uploadVerticesBuffer(aPositionHandle);
        fisheye2SphereModel.uploadTexCoordinateBuffer(aTextureCoordHandle_left, aTextureCoordHandle_right, aTextureAlphaHandle);
        //sphere2.uploadVerticesBuffer(aPositionHandle);
        //sphere2.uploadTexCoordinateBuffer(aTextureCoordHandle);
        //sphereNoTexture.uploadVerticesBuffer(aPositionHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);

        GLES20.glUniform1i(uTextureSamplerHandle,0);

        switch (glMode.Mode_View_VR) {
            case MODE_VR_OFF:
                GLES20.glViewport(0, 0, screenWidth, screenHeight);
                fisheye2SphereModel.draw();
                break;
            case MODE_VR_ON:
                GLES20.glViewport(0, 0, screenWidth / 2, screenHeight);
                fisheye2SphereModel.draw();
                GLES20.glViewport(screenWidth / 2, 0, screenWidth - screenWidth / 2, screenHeight);
                fisheye2SphereModel.draw();
                break;
        }
        mHandler.sendEmptyMessage(1);
    }

    @Override
    synchronized public void onFrameAvailable(SurfaceTexture surface) {
        updateSurface = true;
        if (wrapperMediaPlayer!=null&&wrapperMediaPlayer.getPlayerCallback()!=null){
            wrapperMediaPlayer.getPlayerCallback().updateProgress();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        Log.d(TAG, "onVideoSizeChanged: "+width+" "+height);
        info_VIDEO_WIDTH = width;
        info_VIDEO_HEIGHT = height;
        //updateProjection(width,height);
    }

    private void updateProjection(int videoWidth, int videoHeight){
        float screenRatio=(float)screenWidth/screenHeight;
        float videoRatio=(float)videoWidth/videoHeight;
        if (videoRatio>screenRatio){
            Matrix.orthoM(projectionMatrix,0,-1f,1f,-videoRatio/screenRatio,videoRatio/screenRatio,-1f,1f);
        }else Matrix.orthoM(projectionMatrix,0,-screenRatio/videoRatio,screenRatio/videoRatio,-1f,1f,-1f,1f);
    }

  /*  public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }*/

    private static float[] mTmp = new float[16];

    public void initSensor(){
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorRot = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (sensorRot==null) return;
        Log.i("Sensor", "ON");
        sensorManager.registerListener(this, sensorRot, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy != 0){
            int type = event.sensor.getType();
            switch (type){
                case Sensor.TYPE_ROTATION_VECTOR:
                    modelMatrixLock.lock();
                    float[] rotat_values = event.values;
                    SensorManager.getRotationMatrixFromVector(mTmp, rotat_values);
                    Log.i("Sensor","do");
                    System.arraycopy(mTmp,0,modelMatrix,0,mTmp.length);
                    SensorManager.remapCoordinateSystem(mTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, modelMatrix);
                    Matrix.rotateM(modelMatrix, 0, 90.0F, 1.0F, 0.0F, 0.0F);
                    modelMatrixLock.unlock();
                    break;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private static final float sDensity =  Resources.getSystem().getDisplayMetrics().density;
    private static final float sDamping = 0.2f;
    private float mDeltaX;
    private float mDeltaY;
    private float mScale;


    private void initGestureHandler(){
        mDeltaX=mDeltaY=0;
        mScale=1;
        gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (panoUIController!=null){
                    if (panoUIController.isVisible()) panoUIController.hide();
                    else panoUIController.show();
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                mDeltaX+= distanceX / sDensity * sDamping;
                mDeltaY+= distanceY / sDensity * sDamping;
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        });

        scaleGestureDetector=new ScaleGestureDetector(context, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor=detector.getScaleFactor();
                updateScale(scaleFactor);
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                //return true to enter onScale()
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        });
    }

    public boolean handleTouchEvent(MotionEvent event) {
        boolean ret=scaleGestureDetector.onTouchEvent(event);
        if (!scaleGestureDetector.isInProgress()){
            ret=gestureDetector.onTouchEvent(event);
        }
        return  ret;
    }

    public void updateScale(float scaleFactor){
        mScale=mScale+(1.0f-scaleFactor);
        mScale=Math.max(0.122f,Math.min(2.0f,mScale)); // 0.122 2.0
    }

    public void releaseResource(){
        if(wrapperMediaPlayer!=null){
            wrapperMediaPlayer.getmMediaPlayer().setSurface(null);
            if (surfaceTexture!=null) surfaceTexture=null;
            wrapperMediaPlayer.getmMediaPlayer().stop();;
            wrapperMediaPlayer.getmMediaPlayer().release();
        }
    }
    public  GLMode getGLMode(){
        return glMode;
    }
}
