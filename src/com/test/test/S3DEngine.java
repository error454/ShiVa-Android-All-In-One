//----------------------------------------------------------------------
package com.test.test;
//----------------------------------------------------------------------        
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10; 
import android.app.Application;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Vibrator;
import android.opengl.GLSurfaceView;
import android.opengl.GLES11;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.SurfaceHolder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.widget.VideoView;
import android.net.Uri;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager; 
//----------------------------------------------------------------------                
public class S3DEngine extends Application        
{
    //------------------------------------------------------------------
    // Application overrides
    //
    @Override
    public void onCreate ( )
    {
        //Log.d ( "S3DEngine", "### onCreate" ) ;
        
        // Call parent constructor
        //
        super.onCreate  ( ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onTerminate ( ) 
    {
        //Log.d ( "S3DENGINE MAIN", "### onTerminate" ) ;
        super.onTerminate ( ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onConfigurationChanged ( Configuration newConfig )
    {
        //Log.d ( "S3DENGINE MAIN", "### onConfigurationChanged : " + newConfig.orientation ) ;
        //super.onConfigurationChanged ( newConfig ) ;
    }
}

