package com.sth.opengldemo.Acitivty;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.sth.opengldemo.Constant.PanoStatus;
import com.sth.opengldemo.R;
import com.sth.opengldemo.Util.GLMode;
import com.sth.opengldemo.Util.PanoMediaPlayerWrapper;
import com.sth.opengldemo.Util.StatusHelper;
import com.sth.opengldemo.Util.UIUtils;
import com.sth.opengldemo.View.GLRenderer_Fisheye2Sphere;
import com.sth.opengldemo.View.PanoUIController;

public class MainActivity extends Activity {
    private GLSurfaceView glSurfaceView;
    private Handler mHandler;
    private GLRenderer_Fisheye2Sphere glRenderer;
    private String filePath = "";
    private PanoUIController mPanoUIController;
    private StatusHelper statusHelper;
    private PanoMediaPlayerWrapper wrapperMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        //no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        filePath = getIntent().getStringExtra("filePath");
        init();
        Log.i("MainActivity", "filePath=" + filePath);

    }

    private void init() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };
        mPanoUIController = new PanoUIController((RelativeLayout) findViewById(R.id.player_toolbar_progress), this, false);
        wrapperMediaPlayer=new PanoMediaPlayerWrapper();
        glSurfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        glSurfaceView.setEGLContextClientVersion(2);
        glRenderer = new GLRenderer_Fisheye2Sphere(this, filePath/*Environment.getExternalStorageDirectory().getPath()+"/input.mp4"*/, mHandler,wrapperMediaPlayer,mPanoUIController);
        glSurfaceView.setRenderer(glRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setClickable(true);
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPanoUIController.startHideControllerTimer();
                return glRenderer.handleTouchEvent(event);
            }
        });
        statusHelper=new StatusHelper(this);
        statusHelper.setPanoStatus(PanoStatus.IDLE);

        wrapperMediaPlayer.setStatusHelper(statusHelper);
        wrapperMediaPlayer.setMediaPlayerFromUri(Uri.parse(filePath));
        mPanoUIController.setAutoHideController(true);
        mPanoUIController.setUiCallback(new PanoUIController.UICallback() {
            @Override
            public void changeInteractiveMode() {
                if(glRenderer.getGLMode().Mode_Sensor_GesRot== GLMode.MODE_SENSOR.MODE_SENSOR_ROTATION) {
                    glRenderer.getGLMode().Mode_Sensor_GesRot = GLMode.MODE_SENSOR.MODE_SENSOR_GESTURE;
                }else{
                    glRenderer.getGLMode().Mode_Sensor_GesRot= GLMode.MODE_SENSOR.MODE_SENSOR_ROTATION;
                }
            }

            @Override
            public void changePlayingStatus() {
                if (wrapperMediaPlayer.getStatusHelper().getPanoStatus()== PanoStatus.PLAYING){
                    wrapperMediaPlayer.pauseByUser();
                }else if (wrapperMediaPlayer.getStatusHelper().getPanoStatus()== PanoStatus.PAUSED_BY_USER){
                    wrapperMediaPlayer.start();
                }
            }

            @Override
            public void playerSeekTo(int pos) {
                if (wrapperMediaPlayer.getmMediaPlayer()!=null){
/*                    PanoStatus panoStatus=wrapperMediaPlayer.getStatusHelper().getPanoStatus();
                    if (panoStatus==PanoStatus.PLAYING
                            || panoStatus==PanoStatus.PAUSED
                            || panoStatus== PanoStatus.PAUSED_BY_USER)*/
                        wrapperMediaPlayer.getmMediaPlayer().seekTo(pos);
                }
            }

            @Override
            public int getPlayerDuration() {
                if (wrapperMediaPlayer.getmMediaPlayer()!=null)
                {

                    return wrapperMediaPlayer.getmMediaPlayer().getDuration();
                }
                return 0;
            }

            @Override
            public int getPlayerCurrentPosition() {
                if (wrapperMediaPlayer.getmMediaPlayer()!=null){

                    return wrapperMediaPlayer.getmMediaPlayer().getCurrentPosition();
                }
                return 0;
            }
        });
        wrapperMediaPlayer.setPlayerCallback(new PanoMediaPlayerWrapper.PlayerCallback() {
            @Override
            public void updateProgress() {
                mPanoUIController.updateProgress();
            }

            @Override
            public void updateInfo() {
               // UIUtils.setBufferVisibility(mImgBufferAnim,false);
                mPanoUIController.startHideControllerTimer();
                mPanoUIController.setInfo();
            }

            @Override
            public void requestFinish() {
                finish();
            }
        });
        wrapperMediaPlayer.prepare();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        glSurfaceView.onPause();
        if(wrapperMediaPlayer!=null && wrapperMediaPlayer.getStatusHelper().getPanoStatus()== PanoStatus.PLAYING){
            wrapperMediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
        if (wrapperMediaPlayer!=null){
            if(wrapperMediaPlayer.getStatusHelper().getPanoStatus()==PanoStatus.PAUSED){
                wrapperMediaPlayer.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(wrapperMediaPlayer!=null){
            wrapperMediaPlayer.releaseResource();
            wrapperMediaPlayer=null;
        }
        //glRenderer.releaseResource();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
