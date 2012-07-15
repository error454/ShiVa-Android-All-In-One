//----------------------------------------------------------------------
package com.test.test;
//----------------------------------------------------------------------
// @@BEGIN_ACTIVITY_IMPORTS@@
//----------------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.util.List;
import java.util.Locale;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.LinkedList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.app.Application;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Vibrator;
import android.opengl.GLSurfaceView;
import android.opengl.GLES11;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.view.Menu;
import android.inputmethodservice.KeyboardView;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ActivityInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationListener;
import android.location.Criteria;
import android.widget.VideoView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.net.Uri;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.MediaController;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.os.PowerManager;
import android.graphics.PixelFormat;
//----------------------------------------------------------------------

// @@END_ACTIVITY_IMPORTS@@
//----------------------------------------------------------------------

class Globals
{
    //------------------------------------------------------------------

    // @@BEGIN_ACTIVITY_GLOBALS@@
    //------------------------------------------------------------------

    public static String sPackageName = "com.test.test";

    public static String sApplicationName = "boxParticleLighting";

    public static boolean bUseGLES2 = true;

    public static boolean bForceDefaultOrientation = false;

    //------------------------------------------------------------------

    // @@END_ACTIVITY_GLOBALS@@
    //------------------------------------------------------------------

}
//----------------------------------------------------------------------

public class boxParticleLighting extends Activity implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener
{
    //------------------------------------------------------------------
    // @@BEGIN_ADDONS@@
    //------------------------------------------------------------------
    //------------------------------------------------------------------
    // @@END_ADDONS@@
    //------------------------------------------------------------------
    
    //------------------------------------------------------------------
	// @@BEGIN_ACTIVITY_MESSAGES_LIST@@
    //------------------------------------------------------------------
	public static final int MSG_START_ENGINE 		    = 0 ;
	public static final int MSG_RESUME_ENGINE 		    = 1 ;
	public static final int MSG_PAUSE_ENGINE 		    = 2 ;
	public static final int MSG_HIDE_SPLASH 		    = 3 ;
	public static final int MSG_PLAY_OVERLAY_MOVIE 	    = 4 ;
	public static final int MSG_STOP_OVERLAY_MOVIE 	    = 5 ;
	public static final int MSG_ENABLE_CAMERA_DEVICE    = 6 ;
	public static final int MSG_ENABLE_VIBRATOR 	    = 7 ;
    //------------------------------------------------------------------
	// @@END_ACTIVITY_MESSAGES_LIST@@
    //------------------------------------------------------------------
	
    //------------------------------------------------------------------
    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        // Call parent constructor
        //
        super.onCreate  ( savedInstanceState ) ;

        // Get singleton
        //
        oThis = this ;

        // Print some infos about the device :
        //
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Create activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Device infos :" ) ;
        Log.d ( Globals.sApplicationName, "    BOARD:         " + Build.BOARD ) ;
        Log.d ( Globals.sApplicationName, "    BRAND:         " + Build.BRAND ) ;
        Log.d ( Globals.sApplicationName, "    CPU_ABI:       " + Build.CPU_ABI ) ;
        Log.d ( Globals.sApplicationName, "    DEVICE:        " + Build.DEVICE ) ;
        Log.d ( Globals.sApplicationName, "    DISPLAY:       " + Build.DISPLAY ) ;
        Log.d ( Globals.sApplicationName, "    MANUFACTURER:  " + Build.MANUFACTURER ) ;
        Log.d ( Globals.sApplicationName, "    MODEL:         " + Build.MODEL ) ;
        Log.d ( Globals.sApplicationName, "    PRODUCT:       " + Build.PRODUCT ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;

        // Get APK file path
        //
        PackageManager  oPackageManager = getPackageManager ( ) ;
        try
        {
            ApplicationInfo oApplicationInfo = oPackageManager.getApplicationInfo ( Globals.sPackageName, 0 );
            mAPKFilePath  = oApplicationInfo . sourceDir ;
        }
        catch ( NameNotFoundException e ) { e.printStackTrace ( ) ; }

        // Request fullscreen mode
        //
        setFullscreen   ( ) ;
        setNoTitle      ( ) ;

		// Create the main view group and inflate startup screen (but do not add it right now, to avoid a "black flash")
		//
		oSplashView 		= View.inflate ( this, R.layout.main, null ) ;
		oViewGroup 			= new RelativeLayout ( this ) ;
        setContentView  	( oViewGroup ) ;

    	//--------------------------------------------------------------
		// @@ON_ACTIVITY_CREATED@@
    	//--------------------------------------------------------------
		
        // Asynchronously initialize engine and other stuff 
        //
        createAsync ( ) ;     

		// Register lock-screen intent handler
		//
		registerLockScreenHandlers ( ) ;
		
		if(ProjectSettings.UseDropboxAPI)
		{
		    DropBox.Init(ProjectSettings.DROPBOX_KEY, ProjectSettings.DROPBOX_SECRET);
		    DropBox.onCreate(this);
		}
    }

    //------------------------------------------------------------------
    protected void createAsync ( )
    {
        Thread t = new Thread ( ) 
        {
		    public void run ( ) 
		    {
	            // Create useful directories, extract packs and create 3DView
                //
                if ( ! createCacheDirectory 	( ) ||
                     ! createHomeDirectory  	( ) ||
                     ! extractMainPack         	( ) ||
                     ! extractAdditionalFiles   ( ) )
                {
			        try { runOnUiThread ( new Runnable ( ) { public void run ( ) { onStorageError ( ) ; } } ) ; } 
			        catch ( Exception e ) { }
                }
                else
                {
			        try { runOnUiThread ( new Runnable ( ) { public void run ( ) { onStartEngine ( ) ; } } ) ; } 
			        catch ( Exception e ) { }
                }		        		        
            }
	    } ;
	    t.start ( ) ;
    }

    //------------------------------------------------------------------
    protected void onStartEngine ( )
    {
        // Get useful system services
        //
        oVibrator           = (Vibrator)        getSystemService ( Context.VIBRATOR_SERVICE ) ;
        oLocationManager    = (LocationManager) getSystemService ( Context.LOCATION_SERVICE ) ;
        oSensorManager      = (SensorManager)   getSystemService ( Context.SENSOR_SERVICE ) ;
        oPowerManager       = (PowerManager)    getSystemService ( Context.POWER_SERVICE ) ;

        // Create the 3D view
        //
        o3DView             = new S3DSurfaceView ( (Context)this, mCacheDirPath, mHomeDirPath, mPackDirPath, mPackFileDescriptor, mPackFileOffset, mPackFileLength, Globals.bUseGLES2, Globals.bForceDefaultOrientation ) ;

        if ( o3DView != null )
        {
			//o3DView.setZOrderOnTop ( true ) ; // Uncomment to make transparent background to work
			oViewGroup.addView    ( o3DView ) ;

            // Add the splash view on top of the 3D view
            //
            oViewGroup.removeView ( oSplashView ) ;
    	    oViewGroup.addView    ( oSplashView ) ;
			
            // Enable wake lock
            //
            onEnableWakeLock ( true ) ;

    		// Inform the system we want the volume buttons to control the multimedia stream
    		//
    		setVolumeControlStream ( AudioManager.STREAM_MUSIC ) ;

            // Send a delayed event to actually start engine and show the 3D View
            //
            Message msg     = new Message ( )  ;
            msg.what        = MSG_START_ENGINE ;
            msg.obj         = this ;
    		oUIHandler  	.sendMessage ( msg ) ;
		}
    }

    //------------------------------------------------------------------
    protected void onStorageError ( )
    {
        // FIXME: verify translations !!!
        //
        String    sLocale = Locale.getDefault  ( ).getLanguage ( ) ;
        if      ( sLocale.contentEquals ( "fr" ) ) showError   ( "L'espace de stockage disponible est insuffisant pour lancer cette application. Veuillez en liberer et relancer l'application." ) ; // OK
        else if ( sLocale.contentEquals ( "it" ) ) showError   ( "Spazio libero in memoria insufficiente per lanciare l'applicazione. Liberare pi\371 spazio e ripetere l'operazione." ) ; // OK
        else if ( sLocale.contentEquals ( "es" ) ) showError   ( "Esta aplicaci\363n no puede comenzar debido al espacio de almacenamiento libre escaso. Libere por favor para arriba un cierto espacio y vuelva a efectuar la aplicaci\363n." ) ;
        else if ( sLocale.contentEquals ( "de" ) ) showError   ( "Diese Anwendung kann auf Grund von unzureichend freiem Speicherplatz nicht starten. Geben Sie bitte etwas Speicherplatz frei und starten Sie die Anwendung erneut." ) ;
        else                                       showError   ( "This application cannot start due to insufficient free storage space. Please free up some space and rerun the application." ) ; // OK
    }
    
    //------------------------------------------------------------------
    public Handler oUIHandler = new Handler ( ) 
    {
        @Override
        public void handleMessage ( Message msg ) 
        {
            switch ( msg.what ) 
            {
    		//----------------------------------------------------------
			// @@BEGIN_ACTIVITY_MESSAGES_HANDLING@@
    		//----------------------------------------------------------
            case MSG_START_ENGINE :
                {
                    if ( o3DView != null )
                    {
                        // At this point, we can actually initialize the engine
                        //
                        o3DView.allowInit ( ) ;

                        // Enable motion sensors
                        //
            			onEnableAccelerometerUpdates ( true ) ;
						
    					//----------------------------------------------
						// @@ON_ACTIVITY_ENGINE_STARTED@@
    					//----------------------------------------------
                    }
                }
                break ;
                
            case MSG_RESUME_ENGINE :
                {
                    // Handle the case when the user locks/unlocks screen rapidly 
                    // Not clean, but no choice as events are not sent in the right order, ie. onResume is sent *before* onScreenLocked... just great.
                    //
                    if ( bScreenLocked )
                    {
                        bWantToResume = true ;
                    }
                    else if ( o3DView != null )
                    {
                        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                        Log.d ( Globals.sApplicationName, "Resume activity " + Globals.sApplicationName ) ;
                        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;

                        // Resume view
                        //
                        o3DView.onResume ( ) ;

                        // Enable motion sensors (FIXME : enable them only if they were enabled before pause)
                        //
                        if ( bCameraDeviceWasEnabledBeforePause          ) onOpenCameraDevice           ( ) ;
                        if ( bAccelerometerUpdatesWereEnabledBeforePause ) onEnableAccelerometerUpdates ( true ) ;
                        if ( bHeadingUpdatesWereEnabledBeforePause       ) onEnableHeadingUpdates       ( true ) ;
                        if ( bLocationUpdatesWereEnabledBeforePause      ) onEnableLocationUpdates      ( true ) ;
                        if ( bWakeLockWasEnabledBeforePause              ) onEnableWakeLock             ( true ) ;
                        //TODO: if ( sOverlayMoviePlayingBeforePause != null     ) onPlayOverlayMovie           ( sOverlayMoviePlayingBeforePause ) ;
                    }
                }
                break ;

            case MSG_PAUSE_ENGINE :
                {
			        if ( o3DView != null )
			        {
                        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                        Log.d ( Globals.sApplicationName, "Pause activity " + Globals.sApplicationName ) ;
                        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
			            
				        // Disable sensors, camera capture, location updates, wake lock...
				        //
				        bCameraDeviceWasEnabledBeforePause          = bCameraDeviceEnabled         ;
				        bAccelerometerUpdatesWereEnabledBeforePause = bAccelerometerUpdatesEnabled ;
				        bHeadingUpdatesWereEnabledBeforePause       = bHeadingUpdatesEnabled       ;
				        bLocationUpdatesWereEnabledBeforePause      = bLocationUpdatesEnabled      ;
				        bWakeLockWasEnabledBeforePause              = bWakeLockEnabled             ;
				        //TODO: sOverlayMoviePlayingBeforePause             = sOverlayMoviePlaying         ;
        
						onCloseCameraDevice          ( ) ;
				        onEnableAccelerometerUpdates ( false ) ;
				        onEnableHeadingUpdates       ( false ) ;
				        onEnableLocationUpdates      ( false ) ;
				        onEnableWakeLock             ( false ) ;
				        onStopOverlayMovie           ( ) ;
				
						// Pause view
						//
			            o3DView.onPause ( ) ;
			        }
				}
				break ;
                
            case MSG_HIDE_SPLASH :
                {
					if ( o3DView != null )
                	{
                        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                        Log.d ( Globals.sApplicationName, "Hide splash view" ) ;
                        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
                	    
                        // Remove splash view
                        //
						oViewGroup.removeView ( oSplashView ) ;
					
						// Force focus to 3D view
						//
						o3DView.requestFocus ( ) ;
					}
                }
                break ;

            case MSG_ENABLE_CAMERA_DEVICE :
                {
                    if ( msg.arg1 > 0 ) onOpenCameraDevice  ( ) ;
                    else                onCloseCameraDevice ( ) ;
                }
                break ;

			case MSG_PLAY_OVERLAY_MOVIE :
				{
					onPlayOverlayMovie ( (String)msg.obj ) ;
				}
				break ;

			case MSG_STOP_OVERLAY_MOVIE :
				{
					onStopOverlayMovie ( ) ;
				}
				break ;

			case MSG_ENABLE_VIBRATOR :
				{
					onVibrate ( msg.arg1 > 0 ) ;
				}
				break ;
    		//----------------------------------------------------------
			// @@END_ACTIVITY_MESSAGES_HANDLING@@
    		//----------------------------------------------------------
            }
            super.handleMessage ( msg ) ;
        }
    } ;

	
    //------------------------------------------------------------------
	// @@BEGIN_ACTIVITY_METHODS@@	
    //------------------------------------------------------------------
    @Override
    protected void onStart ( )
    {
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Start activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        super.onStart ( ) ;
    }
        
    //------------------------------------------------------------------
    @Override
    protected void onRestart ( )
    {
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Restart activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        super.onRestart ( ) ;
    }
                
    //------------------------------------------------------------------
    @Override
    protected void onResume ( )
    {
        super.onResume ( ) ;

        if(ProjectSettings.UseDropboxAPI)
        {
            DropBox.onResume(this);
        }
        
        // If screen is locked, just wait for unlock
        //
        if ( bScreenLocked )
        {
            bWantToResume = true ;
        }
        else
        {
            onResumeActually ( ) ;
		}
    }
    
    protected void onResumeActually ( )
    {
        // Clear flag
        //
        bWantToResume   = false ;

        // Add splash view if needed
        //
        if ( ( o3DView != null ) && ( oSplashView != null ) && ( oSplashView.getParent ( ) != oViewGroup ) )
        {
    	    oViewGroup.addView ( oSplashView ) ;
	    }
            
        // Send a delayed event to actually resume engine and show the 3D View
        //
        Message msg     = new Message ( )  ;
        msg.what        = MSG_RESUME_ENGINE ;
        msg.obj         = this ;
        //oUIHandler  	.sendMessageDelayed ( msg, 500 ) ;
		oUIHandler  	.sendMessage ( msg ) ;		        
    }
    
    //------------------------------------------------------------------
    @Override
    protected void onPause ( ) 
    {
        super.onPause ( ) ;

        // Security
        //
        bWantToResume   = false ;

        // Send a delayed event to actually pause engine and hide the 3D View
        //
        Message msg     = new Message ( )  ;
        msg.what        = MSG_PAUSE_ENGINE ;
        msg.obj         = this ;
        //oUIHandler  	.sendMessageDelayed ( msg, 500 ) ;
		oUIHandler  	.sendMessage ( msg ) ;		
    }
    
    //------------------------------------------------------------------
    protected void onStop ( ) 
    {
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Stop activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        super.onStop ( ) ;
    }
    
    //------------------------------------------------------------------
    protected void onDestroy ( ) 
    {
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        Log.d ( Globals.sApplicationName, "Destroy activity " + Globals.sApplicationName ) ;
        Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        super.onDestroy ( ) ;

    	//--------------------------------------------------------------
		// @@ON_ACTIVITY_DESTROYED@@
    	//--------------------------------------------------------------
		
		// Unregister lock-screen intent handler
		//
		unregisterLockScreenHandlers ( ) ;

        // Destroy 3D view
        //
        if ( o3DView != null )
        {
        	o3DView.onTerminate ( ) ;
		}
    }
    
    //------------------------------------------------------------------
    @Override
    public void onConfigurationChanged ( Configuration newConfig )
    {
        super.onConfigurationChanged ( newConfig ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onBackPressed ( )
    {
		// Just forward to the view so the game can handle it (input.kKeyEscape in the script)
		//
        if ( o3DView != null )
		{
			o3DView.onKeyDown ( KeyEvent.KEYCODE_BACK, new KeyEvent ( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK ) ) ;
			o3DView.onKeyUp   ( KeyEvent.KEYCODE_BACK, new KeyEvent ( KeyEvent.ACTION_UP,   KeyEvent.KEYCODE_BACK ) ) ;
		}
    }

    //------------------------------------------------------------------
    @Override
	public boolean onCreateOptionsMenu (Menu menu) 
    {
		// Just forward to the view so the game can handle it (input.kKeyMenu in the script)
		//
        if ( o3DView != null )
		{
			o3DView.onKeyDown ( KeyEvent.KEYCODE_MENU, new KeyEvent ( KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU ) ) ;
			o3DView.onKeyUp   ( KeyEvent.KEYCODE_MENU, new KeyEvent ( KeyEvent.ACTION_UP,   KeyEvent.KEYCODE_MENU ) ) ;
		}
		return false ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onLowMemory ( )
    {
    
    }

    //------------------------------------------------------------------
    // Screen lock.
    //
    //------------------------------------------------------------------
    protected void registerLockScreenHandlers ( )
    {
		IntentFilter oIntentFilter = new IntentFilter ( ) ;
        oIntentFilter.addAction ( Intent.ACTION_USER_PRESENT ) ;
        oIntentFilter.addAction ( Intent.ACTION_SCREEN_OFF ) ;
        oIntentReceiver = new BroadcastReceiver ( ) 
        {
            @Override
            public void onReceive ( Context context, Intent intent ) 
            {
                final String action = intent.getAction ( ) ;
                
                if ( action.contentEquals ( Intent.ACTION_USER_PRESENT ) )
                {
                    ((boxParticleLighting)context).onScreenUnlocked ( ) ;
                }
                else if ( action.contentEquals ( Intent.ACTION_SCREEN_OFF ) )
                {
                    ((boxParticleLighting)context).onScreenLocked ( ) ;
                }
            }
        } ;
        registerReceiver ( oIntentReceiver, oIntentFilter ) ;                
    }    
    
    protected void unregisterLockScreenHandlers ( )
    {
		if ( oIntentReceiver != null )
		{
		    unregisterReceiver  ( oIntentReceiver ) ;
		    oIntentReceiver     = null ;
	    }
    }	        
    
	public void onScreenLocked ( )
	{
        //Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        //Log.d ( Globals.sApplicationName, "Screen locked" ) ;
        //Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        bScreenLocked = true ;
	}
	
	public void onScreenUnlocked ( )
	{
        //Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;
        //Log.d ( Globals.sApplicationName, "Screen unlocked" ) ;
        //Log.d ( Globals.sApplicationName, "--------------------------------------------" ) ;	    
        bScreenLocked = false ;
        
        // Screen has been unlocked, do we need to resume?
        //
        if ( bWantToResume )
        {
            onResumeActually ( ) ;
        }
	}
    
    //------------------------------------------------------------------
    // OpenURL callback.
    //
	public static void onOpenURL ( String sURL, String sTarget )
	{
	    if ( oThis != null )
	    {
            Intent i            = new Intent ( Intent.ACTION_VIEW ) ;
		    i.setData		    ( Uri.parse  ( sURL ) ) ;
		    oThis.startActivity ( i ) ;
	    }
	}

    //------------------------------------------------------------------
    // Sound functions.
    //
    public static boolean onInitSound ( )
    {
        if ( oSoundPool == null )
        {
            oSoundPool = new SoundPool ( 15, AudioManager.STREAM_MUSIC, 0 ) ;
        }
        return ( oSoundPool != null ) ;
    }
    
    //------------------------------------------------------------------
    public static void onShutdownSound ( )
    {
        if ( oSoundPool != null )
        {
            oSoundPool.release ( ) ;
            oSoundPool = null ;
        }
    }
    
    //------------------------------------------------------------------
    public static void onSuspendSound ( boolean bSuspend )
    {
		/* Only available starting from 2.2... so let the engine do it
        if ( oSoundPool != null )
        {
			if ( bSuspend ) oSoundPool.autoPause  ( ) ;
			else 			oSoundPool.autoResume ( ) ;
		}*/
    }
    
    //------------------------------------------------------------------
    public static int onLoadSound ( String sURI )
    {
		Log.d ( Globals.sApplicationName, "### onLoadSound: " + sURI ) ;
        return oSoundPool.load ( sURI, 1 ) ;
        /*
        try
        {
            FileInputStream fis = new FileInputStream ( sURI ) ;
            return oSoundPool.load ( fis.getFD ( ), 0, fis.available ( ), 1 ) ;
        }
        catch ( IOException e ) { e.printStackTrace ( ) ; }
        return 0 ;
        */
    }
    
    //------------------------------------------------------------------
    public static void onUnloadSound ( int iSound )
    {
        oSoundPool.unload ( iSound ) ;
    }
    
    //------------------------------------------------------------------
    public static int onPlaySound ( int iSound, float fVolume, boolean bLoop, float fPriority )
    {
		//Log.d ( Globals.sApplicationName, "### onPlaySound: " + String.format ( "%d, %f, %s, %f", iSound, fVolume, bLoop ? "true" : "false", fPriority ) ) ;
		int iStream = oSoundPool.play ( iSound, fVolume, fVolume, (int)(fPriority * 255.0f), bLoop ? -1 : 0, 1.0f )  ;		
		//Log.d ( Globals.sApplicationName, "### onPlaySound: " + String.format ( "%d", iStream ) ) ;		
		return ( iStream > 0 ) ? iStream : -1 ;
    }

    //------------------------------------------------------------------
    public static void onPauseSound ( int iStream )
    {
		if ( iStream > 0 )
		{
        	//Log.d ( Globals.sApplicationName, "### onPauseSound: " + String.format ( "%d", iStream ) ) ;
        	oSoundPool.pause ( iStream ) ;
		}
    }
    
    //------------------------------------------------------------------
    public static void onResumeSound ( int iStream )
    {
		if ( iStream > 0 )
		{
        	oSoundPool.resume ( iStream ) ;
		}
    }
    
    //------------------------------------------------------------------
    public static void onStopSound ( int iStream )
    {
		if ( iStream > 0 )
		{
			//Log.d ( Globals.sApplicationName, "### onStopSound: " + String.format ( "%d", iStream ) ) ;
        	oSoundPool.setVolume ( iStream, 0.0f, 0.0f ) ;
        	oSoundPool.setLoop   ( iStream, 0 ) ;
        	oSoundPool.stop	     ( iStream ) ;
		}
    }
    
    //------------------------------------------------------------------
    public static void onSetSoundPitch ( int iStream, float fPitch )
    {
		if ( iStream > 0 )
		{
         	oSoundPool.setRate ( iStream, fPitch ) ;
		}
    }
    
    //------------------------------------------------------------------
    public static void onSetSoundLooping ( int iStream, boolean bLoop )
    {
		if ( iStream > 0 )
		{
        	oSoundPool.setLoop ( iStream, bLoop ? -1 : 0 ) ;
		}
    }
    
    //------------------------------------------------------------------
    public static void onSetSoundVolume ( int iStream, float fVolume )
    {
		if ( iStream > 0 )
		{
        	oSoundPool.setVolume ( iStream, fVolume, fVolume ) ;
		}
    }
    
    //------------------------------------------------------------------
    // Music functions
    //
    public static int onLoadMusic ( String sURI )
    {
		//Log.d ( Globals.sApplicationName, "### onLoadMusic: " + sURI ) ;
		
		for ( int i = 1 ; i < 64 ; i++ )
		{
			if ( aMusicsList[i] == null )
			{
				aMusicsList[i] = sURI ;
				return i ;
			}
		}
		return 0 ; // Means "failed"
    }
    
    //------------------------------------------------------------------
    public static void onUnloadMusic ( int iMusic )
    {
		//Log.d ( Globals.sApplicationName, "### onUnloadMusic: " + String.format ( "%d", iMusic ) ) ;
		
		if ( iMusic < 64 )
		{
			aMusicsList[ iMusic ] = null ;
		}
    }

    //------------------------------------------------------------------
    public static int onPlayMusic ( int iMusic, float fVolume, boolean bLoop, float fPriority )
    {
		//Log.d ( Globals.sApplicationName, "### onPlayMusic: " + String.format ( "%d, %f, %s, %f", iMusic, fVolume, bLoop ? "true" : "false", fPriority ) ) ;

		if ( ( iMusic < 64 ) && ( aMusicsList[ iMusic ] != null ) )
		{
			if ( oMediaPlayer != null )
			{
				oMediaPlayer.stop ( ) ;
				try
				{
					oMediaPlayer.setDataSource 	( aMusicsList[ iMusic ] ) ;
				}
				catch ( Exception e ) { e.printStackTrace ( ) ; return -1 ; }
			}
			else
			{
				oMediaPlayer = MediaPlayer.create ( oThis, Uri.parse ( aMusicsList[ iMusic ] ) ) ;
			}

			if ( oMediaPlayer != null )
			{
				oMediaPlayer.setAudioStreamType	( AudioManager.STREAM_MUSIC ) ;
				oMediaPlayer.setLooping			( bLoop ) ;
				oMediaPlayer.setVolume 			( fVolume, fVolume ) ;
				oMediaPlayer.start	 			( ) ;
				return 0 ; // Stream 0 is reserved for music
			}
		}
        return -1 ;
    }

    //------------------------------------------------------------------
    public static void onPauseMusic ( int iStream )
    {
		Log.d ( Globals.sApplicationName, "### onPauseMusic: " + String.format ( "%d", iStream ) ) ;        

		if ( oMediaPlayer != null )
		{
			oMediaPlayer.pause ( ) ;
		}
    }
    
    //------------------------------------------------------------------
    public static void onResumeMusic ( int iStream )
    {
		//Log.d ( Globals.sApplicationName, "### onResumeMusic: " + String.format ( "%d", iStream ) ) ;                

		if ( oMediaPlayer != null )
		{
			oMediaPlayer.start ( ) ;
		}
    }
    
    //------------------------------------------------------------------
    public static void onStopMusic ( int iStream )
    {
		//Log.d ( Globals.sApplicationName, "### onStopMusic: " + String.format ( "%d", iStream ) ) ;              
		
		if ( oMediaPlayer != null )
		{
			oMediaPlayer.stop    ( ) ;
			oMediaPlayer.release ( ) ;
			oMediaPlayer	  = null ;
		}          
    }
    
    //------------------------------------------------------------------
    public static void onSetMusicVolume ( int iStream, float fVolume )
    {
		//Log.d ( Globals.sApplicationName, "### onSetMusicVolume: " + String.format ( "%d, %f", iStream, fVolume ) ) ;        
		
		if ( oMediaPlayer != null )
		{
			oMediaPlayer.setVolume 	( fVolume, fVolume ) ;
		}
    }

    //------------------------------------------------------------------
    // Vibrator control
    //
    private static void onVibrate ( boolean b )
    {
        if ( b )
        {
            oVibrator.vibrate ( 10000 ) ;
        }
        else
        {
            oVibrator.cancel ( ) ;
        }
    }    
    
    //------------------------------------------------------------------
    // Wake lock control
    //
    public static void onEnableWakeLock ( boolean bEnable )
    {
        if ( bEnable )
        {
            if ( oPowerManager != null )
            {
                oWakeLock = oPowerManager.newWakeLock ( PowerManager.SCREEN_DIM_WAKE_LOCK, "S3DEngineWakeLock" ) ;

                if ( oWakeLock != null )
                {
                	oWakeLock.acquire ( ) ;
					Log.d ( Globals.sApplicationName, "#### onEnableWakeLock: ON" ) ;
				}
            }
        }
        else
        {
            if ( oWakeLock != null )
            {
                if ( oWakeLock.isHeld ( ) )
                {
					Log.d ( Globals.sApplicationName, "#### onEnableWakeLock: OFF" ) ;
                    oWakeLock.release ( ) ;
                }
				oWakeLock = null ;
            }
        }

		if ( o3DView != null )
		{
			o3DView.setKeepScreenOn ( bEnable ) ;
		}
		
		bWakeLockEnabled = bEnable ;
    }    
    
    //------------------------------------------------------------------
    // Movie playback related methods
    //
    private static boolean onPlayOverlayMovie ( String sURI )
    {
        Log.d ( Globals.sApplicationName, "#### onPlayOverlayMovie: " + sURI ) ;

		try 
		{
	        if ( oVideoView == null )        
	        {
	            oVideoView = new VideoView ( oThis ) ;    
                
	            if ( oVideoView != null )
	            {
					oVideoView.setOnPreparedListener	( oThis ) ;
					oVideoView.setOnErrorListener		( oThis ) ;
	                oVideoView.setOnCompletionListener 	( oThis ) ;
	
					RelativeLayout.LayoutParams oVideoViewLayoutParams = new RelativeLayout.LayoutParams ( RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT ) ;
					oVideoViewLayoutParams.addRule 	( RelativeLayout.CENTER_IN_PARENT ) ;
					oViewGroup.addView				( oVideoView, oVideoViewLayoutParams ) ;
					//o3DView.setVisibility 			( View.INVISIBLE ) ; // Kills the rendering context, play with ZOrder instead
		            oVideoView.setVideoURI  		( Uri.parse ( sURI ) ) ;
					oVideoView.setMediaController 	( new MediaController ( oThis ) ) ;
				   	oVideoView.requestFocus 		( ) ;
		            oVideoView.start 		        ( ) ;
					oVideoView.setZOrderMediaOverlay( true ) ;
                    if ( ! sURI.contains ( ".mp3" ) )
                    {
	 					// TODO: backup the current orientation
                        oThis.setRequestedOrientation ( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ) ;
                    }		
		            return oVideoView.isPlaying 	( ) ;
				}
	        }
		}
		catch ( Exception e )
		{
			Log.d ( Globals.sApplicationName, "onPlayOverlayMovie: " + e.getMessage ( ), e ) ;
			
			onStopOverlayMovie ( ) ;
		}

        return false ;
    }   

    //------------------------------------------------------------------
    private static void onStopOverlayMovie ( )
    {
        Log.d ( Globals.sApplicationName, "#### onStopOverlayMovie" ) ;
        
        if ( oVideoView != null )
        {
            oVideoView.stopPlayback 		( ) ;
            oVideoView.setVisibility        ( View.INVISIBLE ) ;
			oViewGroup.removeView   		( oVideoView ) ;
			oVideoView 						= null ;
			//o3DView.setVisibility 			( View.VISIBLE ) ;
			o3DView.onOverlayMovieStopped	( ) ;
        }
		oThis.setRequestedOrientation ( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ) ; // TODO: restore the original orientation
    }   

    //------------------------------------------------------------------
    public void onPrepared ( MediaPlayer mp )
    {
        
    } 
    
    //------------------------------------------------------------------
    public void onCompletion ( MediaPlayer mp )
    {
        onStopOverlayMovie ( ) ;
    } 
    
    //------------------------------------------------------------------
    public boolean onError ( MediaPlayer mp, int what, int extra )
    {
		return false ;
    } 
    
    //------------------------------------------------------------------
    // Camera
    //
    public static boolean onOpenCameraDevice ( )
    {
		if ( oCameraPreview == null )
		{
       		oCameraPreview = new CameraPreview ( oThis, 320, 240 ) ;
		
			if ( oCameraPreview != null )
			{
				oViewGroup.addView   ( oCameraPreview ) ;
				//oCameraPreview.setZOrderMediaOverlay ( true ) ; // Uncomment to display the preview surface
				bCameraDeviceEnabled = true ;
				return bCameraDeviceEnabled ;
			}
			return false ;
		}
		return true ;
	}
	
    //------------------------------------------------------------------
    public static void onCloseCameraDevice ( )
    {
		if ( oCameraPreview != null )
		{
			oCameraPreview.clearPreviewCallbackWithBuffer ( ) ;
			oViewGroup.removeView ( oCameraPreview ) ;
        	oCameraPreview       = null  ;
        	bCameraDeviceEnabled = false ;
		}
	}

    //------------------------------------------------------------------
    public static void onNewCameraFrame ( byte [] data, int w, int h )
    {
        if ( o3DView != null )
        {
            o3DView.onCameraDeviceFrame ( data, w, h ) ;
        }
	}
	
    //------------------------------------------------------------------
    // Location
    //
    public static boolean areLocationUpdatesSupported ( )
    {
        try 
        { 
            return oLocationManager.isProviderEnabled ( LocationManager.GPS_PROVIDER     ) ||
                   oLocationManager.isProviderEnabled ( LocationManager.NETWORK_PROVIDER ) ;
        }
        catch ( Exception e ) { return false ; }
    }
    
    //------------------------------------------------------------------
    public static boolean onEnableLocationUpdates ( boolean bEnable )
    {
        if ( ( oLocationManager != null ) && ( o3DView != null ) )
        {
            if ( bEnable )
            {
                boolean bAtLeastOneProviderEnabled = false ;
                
                if ( oLocationManager.isProviderEnabled ( LocationManager.NETWORK_PROVIDER ) )
                {
                    Log.d ( Globals.sApplicationName, "Coarse location sensor available" ) ;
                    try { oLocationManager.requestLocationUpdates ( LocationManager.NETWORK_PROVIDER, 0, 0, o3DView, Looper.getMainLooper ( ) ) ; bAtLeastOneProviderEnabled = true ; }
					catch ( Exception e ) { e.printStackTrace ( ) ; }
                }
                else
                {
                    Log.d ( Globals.sApplicationName, "Coarse location sensor not available" ) ;
                }
                if ( oLocationManager.isProviderEnabled ( LocationManager.GPS_PROVIDER ) )
                {
                    Log.d ( Globals.sApplicationName, "Fine location sensor available" ) ;
					try { oLocationManager.requestLocationUpdates ( LocationManager.GPS_PROVIDER, 0, 0, o3DView, Looper.getMainLooper ( ) ) ; bAtLeastOneProviderEnabled = true ; }
					catch ( Exception e ) { e.printStackTrace ( ) ; }
                }
                else
                {
                    Log.d ( Globals.sApplicationName, "Fine location sensor not available" ) ;
                }                
                return (bLocationUpdatesEnabled = bAtLeastOneProviderEnabled) ;
            }
            else
            {
                oLocationManager.removeUpdates ( o3DView ) ;
                Log.d ( Globals.sApplicationName, "Disabled location sensor" ) ;
            }           
        }
        return false ;
    }
 
    //------------------------------------------------------------------
    // Heading
    //
    public static boolean areHeadingUpdatesSupported ( )
    {
        return ( oSensorManager != null ) && ! oSensorManager.getSensorList ( Sensor.TYPE_ORIENTATION ).isEmpty ( ) ;
    }

    //------------------------------------------------------------------
    public static boolean onEnableHeadingUpdates ( boolean bEnable )
    {
        if ( oSensorManager != null )
        {
		    Sensor oDefaultOrientationSensor  = oSensorManager.getDefaultSensor ( Sensor.TYPE_ORIENTATION ) ;
		    if   ( oDefaultOrientationSensor != null )
		    {
                 if ( bEnable ) oSensorManager.registerListener   ( o3DView, oDefaultOrientationSensor, SensorManager.SENSOR_DELAY_GAME ) ;
                 else           oSensorManager.unregisterListener ( o3DView, oDefaultOrientationSensor ) ;
                 return (bHeadingUpdatesEnabled = bEnable) ;
            }
        }
        return false ;
	}
	    
    //------------------------------------------------------------------
    // Accelerometer
    //
    public static boolean onEnableAccelerometerUpdates ( boolean bEnable )
    {
        if ( oSensorManager != null )
        {
            Sensor oDefaultAccelerometerSensor  = oSensorManager.getDefaultSensor ( Sensor.TYPE_ACCELEROMETER ) ;
		    if   ( oDefaultAccelerometerSensor != null )
		    {
                 if ( bEnable ) oSensorManager.registerListener   ( o3DView, oDefaultAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME ) ;
                 else           oSensorManager.unregisterListener ( o3DView, oDefaultAccelerometerSensor ) ;
                 return (bAccelerometerUpdatesEnabled = bEnable) ;
            }
        }
        return false ;
	}
	    
    //------------------------------------------------------------------
    // View options methods (must be called before SetContentView).
    //
    public void setFullscreen ( )
    {
        requestWindowFeature   ( Window.FEATURE_NO_TITLE ) ;
        getWindow ( ).setFlags ( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN ) ;
    }
    public void setNoTitle ( )
    {
        requestWindowFeature ( Window.FEATURE_NO_TITLE ) ;
    }

    //------------------------------------------------------------------
    // Utility function to create a writable directory
    //
    protected boolean createWritableDirectory ( String sDir, boolean bDeleteOnExit )
    { 
        // Can we create to the output directory ?
        //
        try 
        { 
            File dir = new File ( sDir ) ;
            
            if ( ! dir.exists ( ) )
            {
                if ( ! dir.mkdirs ( ) ) 
                {
                    Log.d ( Globals.sApplicationName, "Could not create directory: " + sDir ) ;
                    return false ;
                }
            }

            if ( bDeleteOnExit ) dir.deleteOnExit ( ) ; // We want the directory to delete itself when the activity is finished
        } 
        catch ( SecurityException e ) { e.printStackTrace ( ) ; return false ; }
        
        // Can we write to the output directory ?
        //
        try 
        { 
            if ( System.getSecurityManager ( ) != null )
            {
                System.getSecurityManager ( ).checkWrite ( sDir ) ;
            }
        } 
        catch ( SecurityException e ) { e.printStackTrace ( ) ; return false ; }
    
        // Seems ok :)
        //
        return true ;
    }
    
    //------------------------------------------------------------------
    // Utility function to extract and dump a STK file from the APK.
    //
    protected static final int EXTRACT_ASSET_BUFFER_SIZE = 524288 ; // 512kb

    protected boolean extractAssetFromAPK ( String sAssetName, String sOutputDirPath, String sOutputName )
    { 
        if ( ! createWritableDirectory ( sOutputDirPath, true ) )
        {
			Log.d ( Globals.sApplicationName, "Could not create folder " + sOutputDirPath ) ;
            return false ;
        }

        // Extract data
        //
        try
        {
            InputStream oIn  = getAssets ( ).open ( sAssetName ) ;
            if ( oIn != null )
            {
                FileOutputStream oOut = new FileOutputStream ( sOutputDirPath + "/" + sOutputName ) ;
                byte aBuffer [ ] = new byte [ EXTRACT_ASSET_BUFFER_SIZE ] ; 
                while ( oIn.available ( ) > 0 )
                {
                    int iLen = ( oIn.available ( ) > EXTRACT_ASSET_BUFFER_SIZE ) ? EXTRACT_ASSET_BUFFER_SIZE : (int)oIn.available ( ) ;
                    oIn .read  ( aBuffer, 0, iLen ) ;
                    oOut.write ( aBuffer, 0, iLen ) ;
                }
                oIn .close ( ) ;
                oOut.close ( ) ;

                Log.d ( Globals.sApplicationName, "Extracted asset " + sOutputName + " to folder" + sOutputDirPath ) ;
                return true ;               
            }
        }
        catch ( IOException e ) { e.printStackTrace ( ) ; }
		Log.d ( Globals.sApplicationName, "Could not extract asset " + sOutputName + " to folder" + sOutputDirPath ) ;
        return false ;               
    }    
    
    //------------------------------------------------------------------
    // Utility function to extract main STK file to a temporary directory
    //    
    private boolean extractMainPack ( )
    {
        // 20120614: try to get a file descriptor inside the APK directly, in order to avoid the copy
        //
        try
        {
            mPackFileAFD = getAssets ( ).openFd ( "S3DMain.smf" ) ; // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
            
            if ( mPackFileAFD != null )
            {
                mPackFileDescriptor = mPackFileAFD.getFileDescriptor ( ) ;
                mPackFileOffset     = mPackFileAFD.getStartOffset    ( ) ;
                mPackFileLength     = mPackFileAFD.getLength         ( ) ;

                if ( ( mPackFileDescriptor != null ) && ( mPackFileLength != AssetFileDescriptor.UNKNOWN_LENGTH ) )
                {
                    Log.d ( Globals.sApplicationName, "Successfully opened file descriptor for main pack" ) ;

                    // Ok, we still need to fill the mPackDirPath variable, for other files.
                    // Try SD card first:
                    //
                    mPackDirPath = "/sdcard/Android/data/" + Globals.sPackageName ;

                    if ( ! createWritableDirectory ( mPackDirPath, true ) )
                    {
            			Log.d ( Globals.sApplicationName, "Could not create folder " + mPackDirPath ) ;
            			
                        // If something went wrong try on the phone internal filesystem 
                        //
                        mPackDirPath = getCacheDir ( ).getAbsolutePath ( ) ;
                    
                        if ( ! createWritableDirectory ( mPackDirPath, true ) )
                        {
                			Log.d ( Globals.sApplicationName, "Could not create folder " + mPackDirPath ) ;

                			return false ; // No choice...
                        }                                			
                    }
                    
                    return true ;
                }
            }
        }
        catch ( IOException e ) { e.printStackTrace ( ) ; }
    
        // Then try to extract on the SD card
        //
        mPackDirPath = "/sdcard/Android/data/" + Globals.sPackageName ;
                
        // Extract STK files from the APK and dump them to the packs directory
        //
        if ( extractAssetFromAPK ( "S3DMain.smf", mPackDirPath, "S3DMain.stk" ) ) // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
        {
            return true ;
        }

        // If something went wrong try on the phone internal filesystem 
        //
        mPackDirPath = getCacheDir ( ).getAbsolutePath ( ) ;
                
        // Extract STK files from the APK and dump them to the packs directory
        //
        if ( extractAssetFromAPK ( "S3DMain.smf", mPackDirPath, "S3DMain.stk" ) ) // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
        {
            return true ;
        }

        // No more alternatives :(
        //
        mPackDirPath = "" ;
        return false ;
    }

    //------------------------------------------------------------------
    // Utility function to extract STK files to a temporary directory
    //    
    private boolean extractAdditionalFiles ( )
    {
        if ( mPackDirPath != "" )
        {
            try 
            {
                // List assets
                //
                String aAssets [] = getAssets ( ).list ( "" ) ;
            
                for ( int i = 0 ; i < aAssets.length ; i++ )
                {
                    //if ( ! aAssets[i].endsWith ( ".stk" ) )
					if ( ! aAssets[i].endsWith ( "S3DMain.smf" ) ) // Using the SMF extension instead of STK so it forces AAPT to not compress the file (Android < 2.2 only)
                    {
                        // Extract file
                        //
                        if ( ! extractAssetFromAPK ( aAssets[i], mPackDirPath, aAssets[i] ) )
                        {
                            //return false ;
                        }
                    }
                }
                
                // OK
                //
                return true ;
            }
            catch ( IOException e ) { e.printStackTrace ( ) ; return false ; }
        }
        return false ;
    }    
    
    //------------------------------------------------------------------
    // Utility function to create the cache directory
    //    
    private boolean createCacheDirectory ( )
    {
        // First try on the SD card
        //
        mCacheDirPath = "/sdcard/Android/data/" + Globals.sPackageName + "/cache" ;
          
        if ( createWritableDirectory ( mCacheDirPath, false ) )
        {
            Log.d ( Globals.sApplicationName, "Using cache directory: " + mCacheDirPath ) ;
            return true ;
        }

        // If something went wrong try on the phone internal filesystem 
        //
        File dir  = getCacheDir ( ) ;
        if ( dir != null )
        {
            mCacheDirPath = dir.getAbsolutePath ( ) ;
            Log.d ( Globals.sApplicationName, "Using cache directory: " + mCacheDirPath ) ;
            return true ;
        }
        
        // No more alternatives :(
        //
        mCacheDirPath = "" ;
        return false ;
    }

    //------------------------------------------------------------------
    // Utility function to create the home directory
    //    
    private boolean createHomeDirectory ( )
    {
        // Get home directory path (persistent scratch pad)
        //
        File dir  = getDir ( "home", 0 ) ;
        if ( dir != null )
        {
            mHomeDirPath = getDir ( "home", 0 ).getAbsolutePath ( ) ;
            Log.d ( Globals.sApplicationName, "Using home directory: " + mHomeDirPath ) ;
            return true ;
        }
        return false ;
    }
    
    //------------------------------------------------------------------
    // Utility function to display a fatal error message.
    //    
    private void showError ( String s )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder ( this ) ;
        builder.setMessage ( s ) ;
        builder.setTitle   ( Globals.sApplicationName ) ;
        //???builder.setIcon    ( R.drawable.app_icon ) ;
        builder.setPositiveButton ( "OK", new DialogInterface.OnClickListener ( ) 
                                              {
                                                  public void onClick ( DialogInterface dialog, int id) 
                                                  {
                                                     finish ( ) ;
                                                  }
                                              }
                                   ) ;
        AlertDialog dialog = builder.create ( ) ;
        dialog.show ( ) ;
    }    

    //------------------------------------------------------------------
	// @@END_ACTIVITY_METHODS@@	
    //------------------------------------------------------------------

    //------------------------------------------------------------------
	// @@BEGIN_ACTIVITY_VARIABLES@@	
    //------------------------------------------------------------------
    // Software keyboard view.
    //
    //private static KeyboardView     oKeyboardView   ;
    //private static EditText         oEditText       ;

    //------------------------------------------------------------------
    // Main view group.
    //
    private static RelativeLayout   oViewGroup      ;
    
    //------------------------------------------------------------------
    // Splash screen view.
    //
    private static View 			oSplashView		;

    //------------------------------------------------------------------
    // Video surface view.
    //
    private static VideoView        oVideoView      ;
    
    //------------------------------------------------------------------
    // 3D surface view.
    //
    private static S3DSurfaceView   o3DView         ;

    //------------------------------------------------------------------
    // Sound pool object to play sounds from Java.
    //
    private static SoundPool        oSoundPool      ;

    //------------------------------------------------------------------
    // Media player object to play musics from Java.
    //
    private static MediaPlayer      oMediaPlayer    ;
	private static String [ ]       aMusicsList     = new String [64] ;

    //------------------------------------------------------------------
    // Vibrator object.
    //
    private static Vibrator         oVibrator       ;
    
    //------------------------------------------------------------------
    // Camera.
    //
	private static CameraPreview 	oCameraPreview  ;
    
    //------------------------------------------------------------------
    // Sensor manager object.
    //
    private static SensorManager    oSensorManager  ;       
    
    //------------------------------------------------------------------
    // Sensor manager object.
    //
    private static LocationManager  oLocationManager;
    
    //------------------------------------------------------------------
    // Power manager & wake lock object.
    //
    private static PowerManager             oPowerManager   ;
    private static PowerManager.WakeLock    oWakeLock       ;
    private static BroadcastReceiver        oIntentReceiver ;
          
    //------------------------------------------------------------------
    // Singleton object.
    //
    private static boxParticleLighting   oThis         ;        
          
    //------------------------------------------------------------------
    // Various files access infos.
    //
    private String              mCacheDirPath       ;
    private String              mHomeDirPath        ;
    private String              mAPKFilePath        ;
    private String              mPackDirPath        ;
    private AssetFileDescriptor mPackFileAFD        ;
    private FileDescriptor      mPackFileDescriptor ;   
    private long                mPackFileOffset     ;
    private long                mPackFileLength     ;

    //------------------------------------------------------------------
    // State variables
    //
	private static boolean bCameraDeviceEnabled                        = false ;
    private static boolean bAccelerometerUpdatesEnabled                = false ;
    private static boolean bHeadingUpdatesEnabled                      = false ;
    private static boolean bLocationUpdatesEnabled                     = false ;
    private static boolean bWakeLockEnabled                            = false ;
    private static boolean bScreenLocked                               = false ;
    private static boolean bWantToResume                               = false ;
    //TODO: private static String  sOverlayMoviePlaying                        ;
                           
	private static boolean bCameraDeviceWasEnabledBeforePause          = false ;
    private static boolean bAccelerometerUpdatesWereEnabledBeforePause = false ;
    private static boolean bHeadingUpdatesWereEnabledBeforePause       = false ;
    private static boolean bLocationUpdatesWereEnabledBeforePause      = false ;
    private static boolean bWakeLockWasEnabledBeforePause              = false ;
    //TODO: private static String  sOverlayMoviePlayingBeforePause             ;

    //------------------------------------------------------------------
	// @@END_ACTIVITY_VARIABLES@@	
    //------------------------------------------------------------------

    //------------------------------------------------------------------
    // @@BEGIN_JNI_CALLBACK_METHODS@@   
    //------------------------------------------------------------------
    public static int DropBoxLogin(){
        Log.i("boxParticleLighting", "in java login!");
        if(ProjectSettings.UseDropboxAPI)
            return DropBox.logIn(oThis);
        return 0;
    }
    
    public static void DropBoxLogout(){
        Log.i("boxParticleLighting", "in java logout!");
        if(ProjectSettings.UseDropboxAPI)
            DropBox.logOut(oThis);
    }
    //------------------------------------------------------------------
    // @@END_JNI_CALLBACK_METHODS@@   
    //------------------------------------------------------------------
    
    
    //------------------------------------------------------------------
    // Engine native library loading.
    //
    static 
    {
    	//--------------------------------------------------------------
		// @@BEGIN_ACTIVITY_NATIVE_LIBRARIES@@	
    	//--------------------------------------------------------------
		System.loadLibrary ( "crypto" ) ;
		System.loadLibrary ( "ssl" ) ;
        System.loadLibrary ( "openal" ) ;
        System.loadLibrary ( "S3DClient" ) ;
    	//--------------------------------------------------------------
		// @@END_ACTIVITY_NATIVE_LIBRARIES@@	
    	//--------------------------------------------------------------
    }    
}

//----------------------------------------------------------------------
//  Camera preview class (TODO: move it to another file)
//
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback
{
	SurfaceHolder mHolder;
	Camera mCamera;

	//private NativeProcessor processor;

	private int preview_width, preview_height;
	private int pixelformat;
	private PixelFormat pixelinfo;
	private boxParticleLighting processor;

	public CameraPreview(boxParticleLighting context, int preview_width, int preview_height) 
	{
		super(context);

		//listAllCameraMethods();
		
		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		this.preview_width = preview_width;
		this.preview_height = preview_height;

		//processor = new NativeProcessor();
        processor = context;
	}

	public void surfaceCreated(SurfaceHolder holder) 
	{
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();
		try 
		{
			listAllCameraSupportedPreviewSizes ( ) ;
			mCamera.setPreviewDisplay(holder);
		} 
		catch (IOException exception) 
		{
			mCamera.release();
			mCamera = null;
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();

		processor = null;
		mCamera = null;
		mAcb 	= null;
		mPCWB 	= null;

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) 
	{
		// Now that the size is known, set up the camera parameters and begin
		// the preview.

		Camera.Parameters parameters = mCamera.getParameters();
		List<Camera.Size> pvsizes = mCamera.getParameters().getSupportedPreviewSizes();
		int best_width  = 1000000;
		int best_height = 1000000;
		for(Size x: pvsizes)
		{
			if ( x.width - preview_width > 0 && x.width <= best_width )
			{
				best_width  = x.width;
				best_height = x.height;
			}
		}
		preview_width  = best_width;
		preview_height = best_height;

		parameters.setPreviewSize(preview_width, preview_height);

		mCamera.setParameters(parameters);

		pixelinfo = new PixelFormat();
		pixelformat = mCamera.getParameters().getPreviewFormat();
		PixelFormat.getPixelFormatInfo(pixelformat, pixelinfo);

		Size preview_size = mCamera.getParameters().getPreviewSize();
		preview_width = preview_size.width;
		preview_height = preview_size.height;
		int bufSize = preview_width * preview_height * pixelinfo.bitsPerPixel / 8;

		// Must call this before calling addCallbackBuffer to get all the
		// reflection variables setup
		initForACB();
		initForPCWB();

		// Use only one buffer, so that we don't preview to many frames and bog
		// down system
		byte[] buffer = new byte[bufSize];
		addCallbackBuffer(buffer);
		setPreviewCallbackWithBuffer();

		mCamera.startPreview();
		
		Log.d("boxParticleLighting", String.format ( "Preview started (%dx%d)", preview_width, preview_height ) ) ;
	}

	/**
	 * This method will list all methods of the android.hardware.Camera class,
	 * even the hidden ones. With the information it provides, you can use the
	 * same approach I took below to expose methods that were written but hidden
	 * in eclair
	 */
	private void listAllCameraMethods() 
	{
		try 
		{
			Class<?> c = Class.forName("android.hardware.Camera");
			Method[] m = c.getMethods();
			for (int i = 0; i < m.length; i++) 
			{
				Log.d("boxParticleLighting", "  method:" + m[i].toString());
			}
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			Log.e("boxParticleLighting", e.toString());
		}
	}

	private void listAllCameraSupportedPreviewSizes() 
	{
		if ( mCamera != null )
		{
			Log.d("boxParticleLighting", "Camera supported preview sizes:");
			Camera.Parameters parameters = mCamera.getParameters();
			List<Camera.Size> pvsizes = mCamera.getParameters().getSupportedPreviewSizes();
			for(Size x: pvsizes)
			{
				Log.d("boxParticleLighting", String.format("    - %dx%d", x.width, x.height));
			}
		}
	}

	/**
	 * These variables are re-used over and over by addCallbackBuffer
	 */
	Method mAcb;

	private void initForACB() {
		try 
		{
			mAcb = Class.forName("android.hardware.Camera").getMethod("addCallbackBuffer", byte[].class);

		} 
		catch (Exception e) 
		{
			Log.e("boxParticleLighting", "Problem setting up for addCallbackBuffer: " + e.toString());
		}
	}

	/**
	 * This method allows you to add a byte buffer to the queue of buffers to be
	 * used by preview. See:
	 * http://android.git.kernel.org/?p=platform/frameworks
	 * /base.git;a=blob;f=core/java/android/hardware/Camera.java;hb=9d
	 * b3d07b9620b4269ab33f78604a36327e536ce1
	 * 
	 * @param b
	 *            The buffer to register. Size should be width * height *
	 *            bitsPerPixel / 8.
	 */
	private void addCallbackBuffer(byte[] b) 
	{
		/* TODO:
		try 
		{
			mAcb.invoke(mCamera, b);
		} 
		catch (Exception e) 
		{
			Log.e("boxParticleLighting", "invoking addCallbackBuffer failed: " + e.toString());
		}
		*/
	}

	Method mPCWB;

	private void initForPCWB() 
	{
		/* TODO:
		try 
		{
			mPCWB = Class.forName("android.hardware.Camera").getMethod("setPreviewCallbackWithBuffer", PreviewCallback.class);
		} 
		catch (Exception e) 
		{
			Log.e("boxParticleLighting","Problem setting up for setPreviewCallbackWithBuffer: " + e.toString());
		}
		*/
	}

	/**
	 * Use this method instead of setPreviewCallback if you want to use manually
	 * allocated buffers. Assumes that "this" implements Camera.PreviewCallback
	 */
	private void setPreviewCallbackWithBuffer() 
	{
		 mCamera.setPreviewCallback(this);
		 /* TODO:
		try {

			// If we were able to find the setPreviewCallbackWithBuffer method
			// of Camera,
			// we can now invoke it on our Camera instance, setting 'this' to be
			// the
			// callback handler
			mPCWB.invoke(mCamera, this);

			Log.d("boxParticleLighting","setPreviewCallbackWithBuffer: called");

		} catch (Exception e) {

			Log.e("boxParticleLighting", e.toString());
		}*/
	}

	protected void clearPreviewCallbackWithBuffer() 
	{
		 mCamera.setPreviewCallback(null);
		/* TODO:
		try {

			// If we were able to find the setPreviewCallbackWithBuffer method
			// of Camera,
			// we can now invoke it on our Camera instance, setting 'this' to be
			// the
			// callback handler
			mPCWB.invoke(mCamera, (PreviewCallback) null);

			Log.d("boxParticleLighting","setPreviewCallbackWithBuffer: cleared");

		} catch (Exception e) {

			Log.e("boxParticleLighting", e.toString());
		}*/
	}

	Date start;
	int fcount = 0;
	boolean processing = false;

	/**
	 * Demonstration of how to use onPreviewFrame. In this case I'm not
	 * processing the data, I'm just adding the buffer back to the buffer queue
	 * for re-use
	 */
	public void onPreviewFrame(byte[] data, Camera camera) 
	{        
		//Log.d("boxParticleLighting","onPreviewFrame: called");

        processor.onNewCameraFrame ( data, preview_width, preview_height ) ;
		//processor.post(data, preview_width, preview_height, pixelformat, this);
		
		/*		
		if (start == null) 
		{
			start = new Date();
		}
		fcount++;
		if (fcount % 100 == 0) {
			double ms = (new Date()).getTime() - start.getTime();
			Log.d("boxParticleLighting", "fps:" + fcount / (ms / 1000.0));
			start = new Date();
			fcount = 0;
		}
		*/
	}

	//@Override
	//public void onDoneNativeProcessing(byte[] buffer) {
	//	addCallbackBuffer(buffer);
	//}

	//public void addCallbackStack(LinkedList<PoolCallback> callbackstack) {
	//	processor.addCallbackStack(callbackstack);
	//}

	/**This must be called when the activity pauses, in Activity.onPause
	 * This has the side effect of clearing the callback stack.
	 * 
	 */
	//public void onPause() {
	//	addCallbackStack(null);
	//	processor.stop();	
	//}

	//public void onResume() {
	//	processor.start();
	//}

}