//----------------------------------------------------------------------
package com.wordpress.mobilecoder.aaio;
//----------------------------------------------------------------------                
import java.io.IOException;
import java.io.FileDescriptor;
import java.util.Locale;
import java.util.Enumeration;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.InetAddress;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.app.Application;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Vibrator;
import android.os.Looper;
import android.os.Message;
import android.opengl.GLSurfaceView;
import android.opengl.GLES11;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.SurfaceHolder;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Surface;
import android.view.Display;
import android.content.Context;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager; 
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.LocationListener;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.view.inputmethod.InputMethodManager;
import android.inputmethodservice.KeyboardView;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.Manifest.permission;
import android.content.pm.PackageManager;
//----------------------------------------------------------------------                
class S3DSurfaceView extends GLSurfaceView implements SensorEventListener, LocationListener //, OnClickListener //, OnKeyboardActionListener
{
    //------------------------------------------------------------------
    // Constants.
    
    
    //------------------------------------------------------------------
    // Constructor.
    //
    public S3DSurfaceView ( Context context, String cacheDirPath, String homeDirPath, String packDirPath, FileDescriptor packFileDescriptor, long packFileOffset, long packFileLength, boolean wantGLES2, boolean forceDefaultOrientation )
    {
        super ( context ) ;
        
        // Required to get key events
        //
        setFocusableInTouchMode ( true ) ;
        
        // Remember useful directories
        //
        sCacheDirPath   = cacheDirPath ;
        sHomeDirPath    = homeDirPath  ;
        sPackDirPath    = packDirPath  ;
        
        // Create the renderer
        //
        oRenderer       = new S3DRenderer ( context, sCacheDirPath, sHomeDirPath, sPackDirPath, packFileDescriptor, packFileOffset, packFileLength, forceDefaultOrientation ) ;
        
		// Detect display orientation
		//
		detectDisplayOrientation ( context ) ;
        
        // Initialize OpenGL ES
        //
        initOpenGLES    ( wantGLES2, true, true ) ;
        
        // Finally set the renderer
        //
        setRenderer     ( oRenderer ) ;
    }
    
    //------------------------------------------------------------------
    // Display orientation detection (code taken from nVidia paper)
    //        
	private void detectDisplayOrientation ( Context context )
	{
		WindowManager 	wm 			= (WindowManager)context.getSystemService ( Context.WINDOW_SERVICE ) ; 
		Display 		display 	= wm.getDefaultDisplay ( ) ;
		Class<Display>  c 			= (Class<Display>)display.getClass ( ) ; 
		Method[] 		methods 	= c.getDeclaredMethods ( ) ; 
		String 			rotFnName   = new String ( "getRotation" ) ; 
		Method		 	getRotation = null ;
		
		for ( Method method : methods ) 
		{
			if ( method.getName ( ).equals( rotFnName ) ) 
			{
				getRotation = method ; 
				break;
			}
		}
		if ( getRotation != null ) 
		{
			try
			{
				Integer i = (Integer)getRotation.invoke ( display ) ;
				iDisplayOrient = i.intValue ( ) ;
			}
			catch (Exception e) { }
		}
		else 
		{
			iDisplayOrient = display.getOrientation ( ) ;
		}
	}	

    //------------------------------------------------------------------
    // Initialization control.
    //        
    public void allowInit ( )
    {
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onAllowInit ( ) ; } } ) ;
    }        
    
    //------------------------------------------------------------------
    // Resume handling.
    //        
    public void onResume ( )
    {
        super.onResume ( ) ;

        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onResume ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Pause handling.
    //        
    public void onPause ( )
    {
        super.onPause ( ) ; 

        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onPause ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Terminate handling.
    //        
    public void onTerminate ( )
    {
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onShutdown ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Restart handling.
    //        
    public void onRestart ( )
    {
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onRestart ( ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Camera capture handling.
    //        
    public void onCameraDeviceFrame ( final byte[] data, final int w, final int h )
    {
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onCameraDeviceFrame ( data, w, h ) ; } } ) ;
    }        

    //------------------------------------------------------------------
    // Text input handling
    //
    /*
    @Override 
    public boolean onCheckIsTextEditor ( )
    {
        return true ;
    }
    
    //------------------------------------------------------------------
    @Override 
    public InputConnection onCreateInputConnection ( EditorInfo outAttrs )
    {
        outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_NONE ;
        return new BaseInputConnection ( this, false ) ;
    }
    */
    
    //------------------------------------------------------------------
    /*
    public void onClick ( View v )
    {
        Log.d ( "#########", "onClick" ) ;
    }
    */

    //------------------------------------------------------------------
    // Location input event.
    //        
    @Override
    public void onLocationChanged ( Location location )
    {
        //Log.d ( "########", "Location: " + location.getLatitude ( ) + ", " + location.getLongitude ( ) + ", " + location.getAltitude ( ) ) ;

        final float x = (float)location.getLatitude  ( ) ;
        final float y = (float)location.getLongitude ( ) ;
        final float z = (float)location.getAltitude  ( ) ;
        
        queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onLocationEvent ( x, y, z ) ; } } ) ;
    }

    //------------------------------------------------------------------
    @Override
    public void onProviderDisabled ( String provider )
    {
        //Log.d ( "#########", "Location provider disabled: " + provider.toString() ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void  onProviderEnabled ( String provider )
    {
        //Log.d ( "#########", "Location provider enabled: " + provider.toString() ) ;
    }
    
    //------------------------------------------------------------------
    @Override
    public void onStatusChanged ( String provider, int status, Bundle extras )
    {
        //Log.d ( "#########", "Location provider status: " + provider.toString() ) ;
    }

    //------------------------------------------------------------------
    // Sensor input event.
    //        
    public void onAccuracyChanged ( Sensor arg0, int arg1 )
    {
    }

    //------------------------------------------------------------------

    public void onSensorChanged ( SensorEvent event )
    {
        if ( event.sensor.getType ( ) == Sensor.TYPE_ACCELEROMETER )
        {
            final float x = event.values[0] ;
            final float y = event.values[1] ;
            final float z = event.values[2] ;
            
			switch ( iDisplayOrient )
			{
				
				case 3  : queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onAccelerometerEvent (  y, -x, z ) ; } } ) ; break ;
				case 2  : queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onAccelerometerEvent ( -x, -y, z ) ; } } ) ; break ;
				case 1  : queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onAccelerometerEvent ( -y,  x, z ) ; } } ) ; break ;
				case 0  : 
				default : queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onAccelerometerEvent (  x,  y, z ) ; } } ) ; break ;
			}
        }
        else if ( event.sensor.getType ( ) == Sensor.TYPE_ORIENTATION )
        {
            final float azimuth =  event.values[0] ;
            final float pitch   =  event.values[1] ;
            final float roll    = -event.values[2] ;

            queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onHeadingEvent ( azimuth ) ; } } ) ;
        }
        //else if ( event.sensor.getType ( ) == Sensor.TYPE_MAGNETIC_FIELD )
        //{
        //    final float x = event.values[0] ;
        //    final float y = event.values[1] ;
        //    final float z = event.values[2] ;
        //}
    }
         
    //------------------------------------------------------------------
    // Touch input event.
    //
    class TouchEventRunnable implements Runnable
    {
        int cnt, act;
        float[] x;
        float[] y;
    
         public TouchEventRunnable ( final MotionEvent oEvent )
         {
             act = oEvent.getAction         ( ) ;
             cnt = oEvent.getPointerCount   ( ) ;
            
             x = new float[cnt];
             y = new float[cnt];
            
             for ( int i = 0 ; i < cnt ; i++ )
             {
                 try
                 {
                     x[i] = oEvent.getX ( i ) ;
                     y[i] = oEvent.getY ( i ) ;
                 }
                 catch ( Exception e )
                 {
                     //Log.d ( "S3DSurfaceView", "Catch exception: " + e.toString() );
                     //Log.d ( "S3DSurfaceView", "\t\t X and Y Invalid for pointer " + i );
                     x[i] = 0 ;
                     y[i] = 0 ;
                 }
            }
        }

        public void run ( )
        {
            if ( oRenderer != null )
            {
                switch ( cnt )
                {
                case  0 : oRenderer.onTouchEvent ( act, cnt, 0,   0,    0,  0,   0,    0,  0,   0,    0,  0,   0,    0,  0,   0,    0  ) ; break ;
                case  1 : oRenderer.onTouchEvent ( act, cnt, 1, x[0], y[0], 0,   0,    0,  0,   0,    0,  0,   0,    0,  0,   0,    0  ) ; break ;
                case  2 : oRenderer.onTouchEvent ( act, cnt, 1, x[0], y[0], 1, x[1], y[1], 0,   0,    0,  0,   0,    0,  0,   0,    0  ) ; break ;
                case  3 : oRenderer.onTouchEvent ( act, cnt, 1, x[0], y[0], 1, x[1], y[1], 1, x[2], y[2], 0,   0,    0,  0,   0,    0  ) ; break ;
                case  4 : oRenderer.onTouchEvent ( act, cnt, 1, x[0], y[0], 1, x[1], y[1], 1, x[2], y[2], 1, x[3], y[3], 0,   0,    0  ) ; break ;
                default : oRenderer.onTouchEvent ( act, cnt, 1, x[0], y[0], 1, x[1], y[1], 1, x[2], y[2], 1, x[3], y[3], 1, x[4], y[4] ) ; break ;
                }            
            }
        }
    } ;
        
    public boolean onTouchEvent ( final MotionEvent event )
    {
		// Note that event.getPressure ( ... ) does not work on all devices, so assume full pressure...
		// Note that some devices send onTouchEvent continously (eg. HTC ones), and others not (eg. Samsung ones)
		//
        super.onTouchEvent ( event ) ;
        
        queueEvent
        (
            new TouchEventRunnable ( event )
        ) ;
        return true ;
    }

    //------------------------------------------------------------------
    // Keyboard input events.
    //
    public boolean onKeyDown ( final int keyCode, KeyEvent event )
    {
        if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP )
		{
			return super.onKeyDown ( keyCode, event ) ;
		}
		else 
        {
        	final int uniCode = event.getUnicodeChar ( ) ;
            queueEvent
            (
                new Runnable ( )
                {
                    public void run ( )
                    {
                        if ( oRenderer != null ) oRenderer.onKeyEvent ( keyCode, uniCode, true ) ;
                    }
                }
            ) ;
        }
        return true ;
    }
    public boolean onKeyUp ( final int keyCode, KeyEvent event )
    {
        if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP )
		{
			return super.onKeyUp ( keyCode, event ) ;
		}
		else 
        {
	        final int uniCode = event.getUnicodeChar ( ) ;

        	queueEvent
        	(
                new Runnable ( )
                {
                    public void run ( )
                    {
                        if ( oRenderer != null ) oRenderer.onKeyEvent ( keyCode, uniCode, false ) ;
                    }
                }
            ) ;
		}
        return true ;
    }

    //------------------------------------------------------------------
    // Movie events.
    //
	public void onOverlayMovieStopped ( )
	{
		queueEvent ( new Runnable ( ) { public void run ( ) { if ( oRenderer != null ) oRenderer.onOverlayMovieStopped ( ) ; } } ) ;
	}

    //------------------------------------------------------------------
    // OpenGL initialization helpers
    //
    private void initOpenGLES ( boolean _bWantGLES2, boolean _bWant32bpp, boolean _bWantStencil )
    {
        // Search for device specific preferences.
        //
        int iPreferedRedSize        =  _bWant32bpp   ? 8 : 5 ;
        int iPreferedGreenSize      =  _bWant32bpp   ? 8 : 6 ;
        int iPreferedBlueSize       =  _bWant32bpp   ? 8 : 5 ;
        int iPreferedAlphaSize      =  _bWant32bpp   ? 8 : 0 ; // For rendermaps and/or 3D overlay
        int iPreferedDepthSize      =                     16 ;
        int iPreferedStencilSize    =  _bWantStencil ? 8 : 0 ; // For auto-stereoscopic 3D screens 
            
        if ( _bWantGLES2 )
        {
            /* By default, GLSurfaceView() creates a RGB_565 opaque surface.
             * If we want a translucent one, we should change the surface's
             * format here, using PixelFormat.TRANSLUCENT for GL Surfaces
             * is interpreted as any 32-bit surface with alpha by SurfaceFlinger.
             */
             if ( iPreferedAlphaSize > 0 )
             {
                this.getHolder ( ).setFormat ( PixelFormat.TRANSLUCENT ) ;
             }

            /* Setup the context factory for 2.0 rendering.
             * See ContextFactory class definition below
             */
            setEGLContextFactory ( new S3DContextFactoryGLES2 ( ) ) ;

            /* We need to choose an EGLConfig that matches the format of
             * our surface exactly. This is going to be done in our
             * custom config chooser. See ConfigChooser class definition
             * below.
             */
            setEGLConfigChooser ( new S3DConfigChooserGLES2 ( iPreferedRedSize, iPreferedGreenSize, iPreferedBlueSize, iPreferedAlphaSize, iPreferedDepthSize, iPreferedStencilSize ) ) ;
        }
        else
        {
            /* By default, GLSurfaceView() creates a RGB_565 opaque surface.
             * If we want a translucent one, we should change the surface's
             * format here, using PixelFormat.TRANSLUCENT for GL Surfaces
             * is interpreted as any 32-bit surface with alpha by SurfaceFlinger.
             */
             if ( iPreferedAlphaSize > 0 )
             {
                 this.getHolder ( ).setFormat ( PixelFormat.TRANSLUCENT ) ;
             }

            /* We need to choose an EGLConfig that matches the format of
             * our surface exactly. This is going to be done in our
             * custom config chooser. See ConfigChooser class definition
             * below.
             */
            setEGLConfigChooser ( new S3DConfigChooserGLES1 ( iPreferedRedSize, iPreferedGreenSize, iPreferedBlueSize, iPreferedAlphaSize, iPreferedDepthSize, iPreferedStencilSize ) ) ;
        }
    }
    
    //------------------------------------------------------------------
    // Renderer instance.
    //
    private S3DRenderer     oRenderer ;
        
    //------------------------------------------------------------------
    // Some other vars.
    //
    private String          sCacheDirPath   ;
    private String          sHomeDirPath    ;
    private String          sPackDirPath    ;
	private int             iDisplayOrient  = 0 ;
}


//----------------------------------------------------------------------
class S3DRenderer implements GLSurfaceView.Renderer
{
    //------------------------------------------------------------------
    // Constructor.
    //
    public S3DRenderer ( Context context, String sCacheDirPath, String sHomeDirPath, String sPackDirPath, FileDescriptor oPackFileDescriptor, long iPackFileOffset, long iPackFileLength, boolean bForceDefaultOrientation )
    {
        //engineSetDeviceName             (  ) ; 
        engineSetCameraDeviceCount      ( 1 ) ; 
        engineSetCameraDeviceName       ( 0, "Default" ) ; 
        engineSetDeviceModel            ( Build.MODEL ) ; 
        engineSetSystemVersion          ( Build.VERSION.RELEASE ) ; 
        engineSetSystemLanguage         ( Locale.getDefault ( ).getLanguage ( ) ) ; 
        engineSetDirectories            ( sCacheDirPath, sHomeDirPath, sPackDirPath ) ;        
        engineForceDefaultOrientation   ( bForceDefaultOrientation ) ;
        
        if ( oPackFileDescriptor != null )
        {
            engineSetPackFileDescriptor ( oPackFileDescriptor, iPackFileOffset, iPackFileLength ) ;
        }
		if ( ( context.checkCallingOrSelfPermission ( android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) &&
		     ( context.checkCallingOrSelfPermission ( android.Manifest.permission.ACCESS_FINE_LOCATION   ) == PackageManager.PERMISSION_GRANTED ) )
		{
        	engineSetLocationSupport    ( ((AAIOMain)oContext).areLocationUpdatesSupported ( ) ) ;
        	
		}
		engineSetHeadingSupport     	( ((AAIOMain)oContext).areHeadingUpdatesSupported  ( ) ) ;		

		if ( context.checkCallingOrSelfPermission ( android.Manifest.permission.INTERNET ) == PackageManager.PERMISSION_GRANTED )
		{
			engineSetDeviceIPAddress	( getDeviceIPAddress ( ) ) ;
		}
		if ( context.checkCallingOrSelfPermission ( android.Manifest.permission.READ_PHONE_STATE ) == PackageManager.PERMISSION_GRANTED )
		{
			engineSetDeviceUUID			( getDeviceUUID ( context ) ) ;
		}

        oContext 	= context ;
        bPaused  	= false ;
    }

    //------------------------------------------------------------------
    // Initialization handling
    //
    public void onAllowInit ( )
    {
		bAllowInit  = true ;
    }

    //------------------------------------------------------------------
    // Pause handling
    //
    public void onPause ( )
    {
		enginePause     ( true ) ;
		bPaused		    = true   ;
		bSplashVisible  = true   ; // For resume
    }

    //------------------------------------------------------------------
    // Resume handling
    //
    public void onResume ( )
    {
		enginePause ( false ) ;
		bPaused		= false   ;
    }
    
    //------------------------------------------------------------------
    // Engine shutdown.
    //
    public void onShutdown ( )
    {
        engineShutdown ( ) ;
    }

    //------------------------------------------------------------------
    // Engine restart.
    //
    public void onRestart ( )
    {
        //engineShutdown    ( ) ;
        //engineInitialize  ( ) ;
    }

    //------------------------------------------------------------------
    // Camera handling
    //
    public void onCameraDeviceFrame ( byte[] data, int w, int h )
    {
        if ( ! bPaused ) engineOnCameraDeviceFrame ( data, w, h ) ;
    }

    //------------------------------------------------------------------
    // Surface creation.
    //
    public void onSurfaceCreated ( GL10 gl, EGLConfig config )
    {
		engineOnSurfaceCreated ( ) ;
    }

    //------------------------------------------------------------------
    // Surface resize handling.
    //
    public void onSurfaceChanged ( GL10 gl, int w, int h )
    {
        engineOnSurfaceChanged ( w, h ) ;
    }

    //------------------------------------------------------------------
    // Drawing function.
    //
    public void onDrawFrame ( GL10 gl )
    {
        if ( bAllowInit && ! bInitialized )
        {
            engineShutdown    ( ) ;
            engineInitialize  ( ) ;
            bInitialized   = true ;            
        }
        if ( bInitialized && ! bPaused )
        {
            // Run one engine frame
            //
			boolean bWantSwap = true ;
			do
			{
            	if ( engineRunOneFrame ( ) )
				{
					// Overlay movie handling
					//
					String   sOverlayMovieToPlay  = engineGetOverlayMovie ( ) ;
					if   ( ! sOverlayMovieToPlay.equals ( sOverlayMovie ) )
					{
						sOverlayMovie = sOverlayMovieToPlay ;
						if ( sOverlayMovie.length ( ) > 0 ) Message.obtain ( ((AAIOMain)oContext).oUIHandler, AAIOMain.MSG_PLAY_OVERLAY_MOVIE, sOverlayMovie ).sendToTarget ( ) ;
						else                                Message.obtain ( ((AAIOMain)oContext).oUIHandler, AAIOMain.MSG_STOP_OVERLAY_MOVIE                ).sendToTarget ( ) ;
					}

					// Camera device handling
					//
					boolean bCameraDeviceState  = engineGetCameraDeviceState ( ) ;
					if    ( bCameraDeviceState != bCameraDevice )
					{
						bCameraDevice = bCameraDeviceState ;
						Message.obtain ( ((AAIOMain)oContext).oUIHandler, AAIOMain.MSG_ENABLE_CAMERA_DEVICE, bCameraDevice ? 1 : 0, 0 ).sendToTarget ( ) ;
					}
				
					// Swap buffers handling (only useful when doing some benchmarking, not actually needed in a retail app)
					//
					bWantSwap = engineGetWantSwapBuffers ( ) ;
					
					if ( ! bWantSwap )
					{
						gl.glFinish ( ) ;
					}
				
					// Vibrator handling
					//
					//boolean bVibratorState  = engineGetVibratorState ( ) ;
					//if    ( bVibratorState != bVibrate )
					//{
					//	bVibrate = bVibratorState ;
					//	Message.obtain ( ((AAIOMain)oContext).oUIHandler, AAIOMain.MSG_ENABLE_VIBRATOR, bVibrate ? 1 : 0, 0 ).sendToTarget ( ) ;
					//}
				}
				else
	            {
					bWantSwap = true ;
	                ((Activity) oContext).finish ( ) ;
	            }
			}
			while ( ! bWantSwap ) ;
			
            // Hide splash sceen once engine has been initialized and that one frame has passed
		    //
            if ( bSplashVisible )
            {
                Message.obtain ( ((AAIOMain)oContext).oUIHandler, AAIOMain.MSG_HIDE_SPLASH ).sendToTarget ( ) ;
                bSplashVisible = false ;
            }			
        }
    }

    //------------------------------------------------------------------
    // Keyboard input handling.
    //
    public void onKeyEvent ( int keyCode, int uniCode, boolean keyDown )
    {
        if ( ! bPaused )
        {
            if ( keyDown )  engineOnKeyboardKeyDown ( keyCode, uniCode ) ;
            else            engineOnKeyboardKeyUp   ( keyCode, uniCode ) ;
        }
    }

    //------------------------------------------------------------------
    // Touch input handling.
    //
    public void onTouchEvent ( int action, int cnt, float p0, float x0, float y0, float p1, float x1, float y1, float p2, float x2, float y2, float p3, float x3, float y3, float p4, float x4, float y4 )
    {
        if ( ! bPaused )
        {
            int     iActionMasked   = ( action & 0x000000ff ) ;
            boolean bNoMoreContacts = ( cnt == 0 ) ;
        
            if ( cnt == 1 )
            {
                switch ( iActionMasked )
                {
                    case MotionEvent.ACTION_MOVE        : engineOnMouseMove       ( x0, y0 ) ; break ;
                    case MotionEvent.ACTION_DOWN        : engineOnMouseButtonDown ( x0, y0 ) ; break ;
                    case MotionEvent.ACTION_UP          :
                    case MotionEvent.ACTION_CANCEL      : 
                    case MotionEvent.ACTION_POINTER_UP  : engineOnMouseButtonUp   ( x0, y0 ) ; bNoMoreContacts = true ; break ;
                }
            }
			else if ( iActionMasked == MotionEvent.ACTION_POINTER_UP )
			{
                int      iActionPointer = ( action & 0x0000ff00 ) >> 8 ;
                switch ( iActionPointer )
                {
                    case 0 : p0 = 0.0f ; break ;
                    case 1 : p1 = 0.0f ; break ;
                    case 2 : p2 = 0.0f ; break ;
                    case 3 : p3 = 0.0f ; break ;
                    case 4 : p4 = 0.0f ; break ;
                }				
			}
        
            if ( bNoMoreContacts )
            {
                engineOnTouchesChange ( 0, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0.0f, 0.0f, 0, 0.0f, 0.0f ) ;
            }
            else
            {
                engineOnTouchesChange ( ( p0 > 0.01f ) ? 1 : 0, x0, y0,
                                        ( p1 > 0.01f ) ? 1 : 0, x1, y1,
                                        ( p2 > 0.01f ) ? 1 : 0, x2, y2,
                                        ( p3 > 0.01f ) ? 1 : 0, x3, y3,
                                        ( p4 > 0.01f ) ? 1 : 0, x4, y4 ) ;
            }
        }
    }

    //------------------------------------------------------------------
    // Accelerometer input handling.
    //
    public void onAccelerometerEvent ( float x, float y, float z )
    {
        if ( ! bPaused ) engineOnDeviceMove ( x, y, z ) ;
    }

    //------------------------------------------------------------------
    // Location input handling.
    //
    public void onLocationEvent ( float x, float y, float z )
    {
        if ( ! bPaused ) engineOnLocationChanged ( x, y, z ) ;
    }

    //------------------------------------------------------------------
    // Heading input handling.
    //
    public void onHeadingEvent ( float angle )
    {
        if ( ! bPaused ) engineOnHeadingChanged ( angle ) ;
    }

    //------------------------------------------------------------------
    // Movie handling.
    //
	public void onOverlayMovieStopped ( )
	{
	    if ( ! bPaused ) engineOnOverlayMovieStopped ( ) ;
	}
	
    //------------------------------------------------------------------
    // Utilities.
    //
	static String getDeviceIPAddress ( ) 
	{
		try 
		{
			Log.d ( "S3DRenderer", "Retrieving device IP address..." ) ;
			
			for ( Enumeration en = NetworkInterface.getNetworkInterfaces ( ) ; en.hasMoreElements ( ) ; ) 
			{
				NetworkInterface intf = (NetworkInterface)en.nextElement ( ) ;
				
				for ( Enumeration enumIpAddr = intf.getInetAddresses ( ) ; enumIpAddr.hasMoreElements ( ) ; ) 
				{
					InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement ( ) ;
					
					if ( ! inetAddress.isLoopbackAddress ( ) ) 
					{
						return inetAddress.getHostAddress ( ).toString ( ) ;
					}
				}
			}
		} 
		catch ( SocketException ex ) 
		{
			Log.e ( "S3DRenderer", ex.toString ( ) ) ;
		}
		return "" ;
    }	
    //------------------------------------------------------------------
	static String getDeviceUUID ( Context context ) 
	{
		// Source: http://stackoverflow.com/questions/2785485/is-there-a-unique-android-device-id
		//
		UUID  uuid = null;
		try
		{
	    	final String PREFS_FILE	     = "device_id.xml";
	    	final String PREFS_DEVICE_ID = "device_id";
			final SharedPreferences prefs = context.getSharedPreferences( PREFS_FILE, 0);
			final String id = prefs.getString(PREFS_DEVICE_ID, null );

			if (id != null) 
			{
				// Use the ids previously computed and stored in the prefs file
				uuid = UUID.fromString(id);
				//Log.d ( "S3DRenderer", "Using stored UUID: " + uuid ) ;
			} 
			else
			{
				final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

				//Log.d ( "S3DRenderer", "Android ID: " + androidId ) ;
				
				// Use the Android ID unless it's broken, in which case fallback on deviceId,
				// unless it's not available, then fallback on a random number which we store
				// to a prefs file
				try 
				{
					// On some devices (eg. Motorola Milestone) the ANDROID_ID is NULL,
					// at least if no SIM is present.
					//
					if ((androidId != null) && (!"9774d56d682e549c".equals(androidId)) ) 
					{
						uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
					} 
					else
					{
						TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService( Context.TELEPHONY_SERVICE );
						final String deviceId = (telephonyMgr!=null) ? telephonyMgr.getDeviceId() : null;
						uuid = (deviceId!=null) ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
					}
				} 
				catch (UnsupportedEncodingException e) 
				{
					throw new RuntimeException(e);
				}

				// Write the value out to the prefs file
				prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString() ).commit();
			}
		}
		catch ( Exception e )
		{
			Log.e ( "S3DRenderer", e.toString ( ) ) ;
		}
		return (uuid!=null)?uuid.toString():"";
    }	

    //------------------------------------------------------------------
    // Member variables.
    //
    private boolean bAllowInit      = false ; // To for engine to start ONCE the splash view is on top
    private boolean bSplashVisible  = true  ;
    private boolean bInitialized    = false ;
    private boolean bPaused  		= false ;
    private Context oContext 		;
	private String  sOverlayMovie 	= ""    ;
	private boolean bCameraDevice   = false ;
	//private boolean bVibrate		= false ;

    //------------------------------------------------------------------
    // Engine native functions interface.
    //
    public native void      engineSetPackFileDescriptor         ( FileDescriptor fileDescriptor, long offset, long length ) ;
    public native void      engineSetDirectories            	( String sCacheDirPath, String sHomeDirPath, String sPackDirPath ) ;
    public native void      engineSetLocationSupport        	( boolean b ) ;
    public native void      engineSetHeadingSupport         	( boolean b ) ;
    public native void      engineSetDeviceName         		( String sName ) ;
    public native void      engineSetDeviceModel        		( String sModel ) ;
    public native void      engineSetDeviceIPAddress        	( String sAddress ) ;
	public native void      engineSetDeviceUUID					( String sUUID ) ;
    public native void      engineSetSystemVersion				( String sVersion ) ;
    public native void      engineSetSystemLanguage         	( String sLanguage ) ;
    public native void      engineSetCameraDeviceCount          ( int n ) ;
    public native void      engineSetCameraDeviceName           ( int n, String sName ) ;
    public native void      engineOnCameraDeviceFrame           ( byte[] data, int w, int h ) ;
    public native boolean   engineInitialize                	( ) ;
    public native void      engineShutdown                  	( ) ;
    public native boolean   engineRunOneFrame               	( ) ;
	public native void      enginePause							( boolean b ) ;
    public native void      engineOnSurfaceCreated              ( ) ;
    public native void      engineOnSurfaceChanged              ( int w, int h ) ;
    public native void      engineOnMouseMove               	( float x, float y ) ;
    public native void      engineOnMouseButtonDown         	( float x, float y ) ;
    public native void      engineOnMouseButtonUp           	( float x, float y ) ;
    public native void      engineOnDeviceMove              	( float x, float y, float z ) ;
    public native void      engineOnKeyboardKeyDown         	( int key, int uni ) ;
    public native void      engineOnKeyboardKeyUp           	( int key, int uni ) ;
    public native void      engineOnTouchesChange           	( int tc1, float x1, float y1, int tc2, float x2, float y2, int tc3, float x3, float y3, int tc4, float x4, float y4, int tc5, float x5, float y5 ) ;
    public native void      engineOnLocationChanged         	( float x, float y, float z ) ;
    public native void      engineOnHeadingChanged          	( float angle ) ;
    public native void      engineForceDefaultOrientation   	( boolean b ) ;
    public native void      engineOnOverlayMovieStopped			( ) ;

    public native String 	engineGetOverlayMovie				( ) ;
    public native boolean 	engineGetCameraDeviceState		    ( ) ;
    public native boolean 	engineGetWantSwapBuffers		    ( ) ;
	//public native boolean	engineGetVibratorState				( ) ;
}

//----------------------------------------------------------------------
class S3DContextFactoryGLES1 implements GLSurfaceView.EGLContextFactory 
{
    private static void checkEglError ( String prompt, EGL10 egl) 
    {
        int error;
        if ( ( error = egl.eglGetError ( ) ) != EGL10.EGL_SUCCESS )
        {
            Log.e("S3DContextFactory", String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) 
    {
        Log.w("S3DContextFactory", "creating OpenGL ES 1.1 context");
        checkEglError("Before eglCreateContext", egl);
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 1, EGL10.EGL_NONE };
        EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError("After eglCreateContext", egl);
        return context;
    }

    public void destroyContext ( EGL10 egl, EGLDisplay display, EGLContext context ) 
    {
        egl.eglDestroyContext ( display, context ) ;
    }
}

//----------------------------------------------------------------------
class S3DContextFactoryGLES2 implements GLSurfaceView.EGLContextFactory 
{
    private static void checkEglError ( String prompt, EGL10 egl) 
    {
        int error;
        if ( ( error = egl.eglGetError ( ) ) != EGL10.EGL_SUCCESS )
        {
            Log.e("S3DContextFactory", String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }

    private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) 
    {
        Log.w("S3DContextFactory", "creating OpenGL ES 2.0 context");
        checkEglError("Before eglCreateContext", egl);
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
        EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
        checkEglError("After eglCreateContext", egl);
        return context;
    }

    public void destroyContext ( EGL10 egl, EGLDisplay display, EGLContext context ) 
    {
        egl.eglDestroyContext ( display, context ) ;
    }
}

//----------------------------------------------------------------------
class S3DConfigChooserGLES1 implements GLSurfaceView.EGLConfigChooser 
{
    private static final boolean DEBUG = false;
    
    public S3DConfigChooserGLES1 ( int r, int g, int b, int a, int depth, int stencil ) 
    {
        mRedSize        = r;
        mGreenSize      = g;
        mBlueSize       = b;
        mAlphaSize      = a;
        mDepthSize      = depth;
        mStencilSize    = stencil;
    }

    /* This EGL config specification is used to specify 1.0 rendering.
     * We use a minimum size of 4 bits for red/green/blue, but will
     * perform actual matching in chooseConfig() below.
     */
    //private static int EGL_OPENGL_ES_BIT = 1;
    private static int[] s_configAttribs2 =
    {
        //EGL10.EGL_RED_SIZE, 4,
        //EGL10.EGL_GREEN_SIZE, 4,
        //EGL10.EGL_BLUE_SIZE, 4,
        //EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES_BIT,
		EGL10.EGL_DEPTH_SIZE, 16,
        EGL10.EGL_NONE
    };

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

        /* Get the number of minimally matching EGL configurations
         */
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        /* Allocate then read the array of minimally matching EGL configs
         */
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);

        if (DEBUG) {
             printConfigs(egl, display, configs);
        }
        /* Now return the "best" one
         */
        return chooseConfig(egl, display, configs);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
		// Try best match first
		//
        for(EGLConfig config : configs) 
        {
            int d = findConfigAttrib ( egl, display, config, EGL10.EGL_DEPTH_SIZE,   0 ) ;
            int s = findConfigAttrib ( egl, display, config, EGL10.EGL_STENCIL_SIZE, 0 ) ;

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize)
                continue;

            // We want an *exact* match for red/green/blue/alpha
            int r = findConfigAttrib ( egl, display, config, EGL10.EGL_RED_SIZE,   0 ) ;
            int g = findConfigAttrib ( egl, display, config, EGL10.EGL_GREEN_SIZE, 0 ) ;
            int b = findConfigAttrib ( egl, display, config, EGL10.EGL_BLUE_SIZE,  0 ) ;
            int a = findConfigAttrib ( egl, display, config, EGL10.EGL_ALPHA_SIZE, 0 ) ;

            if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                return config;
        }
		// Retry with no stencil buffer 
		//
		if ( mStencilSize > 0 )
		{
			mStencilSize    = 0;
			return chooseConfig(egl, display, configs);
		}
        // Fallback to RGB565 with 16bits depth
        //
        if ( mRedSize       != 5    &&
             mGreenSize     != 6    &&
             mBlueSize      != 5    &&
             mAlphaSize     != 0    &&
             mDepthSize     != 16   &&
             mStencilSize   != 0    )
        {     
            mRedSize        = 5;
            mGreenSize      = 6;
            mBlueSize       = 5;
            mAlphaSize      = 0;
            mDepthSize      = 16;
            mStencilSize    = 0;
            return chooseConfig(egl, display, configs);
        }
        
        // Really no luck :(
        //
        return null;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) 
    {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

    private void printConfigs(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
        int numConfigs = configs.length;
        Log.w("S3DConfigChooserGLES1", String.format("%d configurations", numConfigs));
        for (int i = 0; i < numConfigs; i++) {
            Log.w("S3DConfigChooserGLES1", String.format("Configuration %d:\n", i));
            printConfig(egl, display, configs[i]);
        }
    }

    private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) 
    {
        int[] attributes = 
        {
                EGL10.EGL_BUFFER_SIZE,
                EGL10.EGL_ALPHA_SIZE,
                EGL10.EGL_BLUE_SIZE,
                EGL10.EGL_GREEN_SIZE,
                EGL10.EGL_RED_SIZE,
                EGL10.EGL_DEPTH_SIZE,
                EGL10.EGL_STENCIL_SIZE,
                EGL10.EGL_CONFIG_CAVEAT,
                EGL10.EGL_CONFIG_ID,
                EGL10.EGL_LEVEL,
                EGL10.EGL_MAX_PBUFFER_HEIGHT,
                EGL10.EGL_MAX_PBUFFER_PIXELS,
                EGL10.EGL_MAX_PBUFFER_WIDTH,
                EGL10.EGL_NATIVE_RENDERABLE,
                EGL10.EGL_NATIVE_VISUAL_ID,
                EGL10.EGL_NATIVE_VISUAL_TYPE,
                0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                EGL10.EGL_SAMPLES,
                EGL10.EGL_SAMPLE_BUFFERS,
                EGL10.EGL_SURFACE_TYPE,
                EGL10.EGL_TRANSPARENT_TYPE,
                EGL10.EGL_TRANSPARENT_RED_VALUE,
                EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                EGL10.EGL_LUMINANCE_SIZE,
                EGL10.EGL_ALPHA_MASK_SIZE,
                EGL10.EGL_COLOR_BUFFER_TYPE,
                EGL10.EGL_RENDERABLE_TYPE,
                0x3042 // EGL10.EGL_CONFORMANT
        };
        String[] names = 
        {
                "EGL_BUFFER_SIZE",
                "EGL_ALPHA_SIZE",
                "EGL_BLUE_SIZE",
                "EGL_GREEN_SIZE",
                "EGL_RED_SIZE",
                "EGL_DEPTH_SIZE",
                "EGL_STENCIL_SIZE",
                "EGL_CONFIG_CAVEAT",
                "EGL_CONFIG_ID",
                "EGL_LEVEL",
                "EGL_MAX_PBUFFER_HEIGHT",
                "EGL_MAX_PBUFFER_PIXELS",
                "EGL_MAX_PBUFFER_WIDTH",
                "EGL_NATIVE_RENDERABLE",
                "EGL_NATIVE_VISUAL_ID",
                "EGL_NATIVE_VISUAL_TYPE",
                "EGL_PRESERVED_RESOURCES",
                "EGL_SAMPLES",
                "EGL_SAMPLE_BUFFERS",
                "EGL_SURFACE_TYPE",
                "EGL_TRANSPARENT_TYPE",
                "EGL_TRANSPARENT_RED_VALUE",
                "EGL_TRANSPARENT_GREEN_VALUE",
                "EGL_TRANSPARENT_BLUE_VALUE",
                "EGL_BIND_TO_TEXTURE_RGB",
                "EGL_BIND_TO_TEXTURE_RGBA",
                "EGL_MIN_SWAP_INTERVAL",
                "EGL_MAX_SWAP_INTERVAL",
                "EGL_LUMINANCE_SIZE",
                "EGL_ALPHA_MASK_SIZE",
                "EGL_COLOR_BUFFER_TYPE",
                "EGL_RENDERABLE_TYPE",
                "EGL_CONFORMANT"
        };
        int[] value = new int[1];
        for (int i = 0; i < attributes.length; i++) 
        {
            int attribute = attributes[i];
            String name = names[i];
            if ( egl.eglGetConfigAttrib(display, config, attribute, value)) 
            {
                Log.w("S3DConfigChooserGLES1", String.format("  %s: %d\n", name, value[0]));
            } 
            else 
            {
                Log.w("S3DConfigChooserGLES1", String.format("  %s: failed\n", name));
                // LOOPS FOREVER ON HTC HERO: while (egl.eglGetError() != EGL10.EGL_SUCCESS);
            }
        }
    }

    // Subclasses can adjust these values:
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    private int[] mValue = new int[1];
}

//----------------------------------------------------------------------
class S3DConfigChooserGLES2 implements GLSurfaceView.EGLConfigChooser 
{
    private static final boolean DEBUG = false;
    
    public S3DConfigChooserGLES2 ( int r, int g, int b, int a, int depth, int stencil ) 
    {
        mRedSize        = r;
        mGreenSize      = g;
        mBlueSize       = b;
        mAlphaSize      = a;
        mDepthSize      = depth;
        mStencilSize    = stencil;
    }

    /* This EGL config specification is used to specify 2.0 rendering.
     * We use a minimum size of 4 bits for red/green/blue, but will
     * perform actual matching in chooseConfig() below.
     */
    private static int EGL_OPENGL_ES2_BIT = 4;
    private static int[] s_configAttribs2 =
    {
        EGL10.EGL_RED_SIZE, 		4,
        EGL10.EGL_GREEN_SIZE, 		4,
        EGL10.EGL_BLUE_SIZE, 		4,
        EGL10.EGL_RENDERABLE_TYPE,  EGL_OPENGL_ES2_BIT,
		EGL10.EGL_DEPTH_SIZE, 		16,
        EGL10.EGL_NONE
    };

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

        /* Get the number of minimally matching EGL configurations
         */
        int[] num_config = new int[1];
        egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

        int numConfigs = num_config[0];

        if (numConfigs <= 0) {
            throw new IllegalArgumentException("No configs match configSpec");
        }

        /* Allocate then read the array of minimally matching EGL configs
         */
        EGLConfig[] configs = new EGLConfig[numConfigs];
        egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);

        if (DEBUG) {
             printConfigs(egl, display, configs);
        }
        /* Now return the "best" one
         */
        return chooseConfig(egl, display, configs);
    }

    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
		// Try best match first
		//
		for(EGLConfig config : configs) 
        {
            int d = findConfigAttrib ( egl, display, config, EGL10.EGL_DEPTH_SIZE,   0 ) ;
            int s = findConfigAttrib ( egl, display, config, EGL10.EGL_STENCIL_SIZE, 0 ) ;

            // We need at least mDepthSize and mStencilSize bits
            if (d < mDepthSize || s < mStencilSize)
                continue;

            // We want an *exact* match for red/green/blue/alpha
            int r = findConfigAttrib ( egl, display, config, EGL10.EGL_RED_SIZE,   0 ) ;
            int g = findConfigAttrib ( egl, display, config, EGL10.EGL_GREEN_SIZE, 0 ) ;
            int b = findConfigAttrib ( egl, display, config, EGL10.EGL_BLUE_SIZE,  0 ) ;
            int a = findConfigAttrib ( egl, display, config, EGL10.EGL_ALPHA_SIZE, 0 ) ;

            if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                return config;                
        }
		// Retry with no stencil buffer 
		//
		if ( mStencilSize > 0 )
		{
			mStencilSize    = 0;
			return chooseConfig(egl, display, configs);
		}
        // Fallback to RGB565 with 16bits depth
        //
        if ( mRedSize       != 5    &&
             mGreenSize     != 6    &&
             mBlueSize      != 5    &&
             mAlphaSize     != 0    &&
             mDepthSize     != 16   &&
             mStencilSize   != 0    )
        {     
            mRedSize        = 5;
            mGreenSize      = 6;
            mBlueSize       = 5;
            mAlphaSize      = 0;
            mDepthSize      = 16;
            mStencilSize    = 0;
            return chooseConfig(egl, display, configs);
        }
        
        // Really no luck :(
        //        
        return null;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) 
    {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }

    private void printConfigs(EGL10 egl, EGLDisplay display, EGLConfig[] configs) 
    {
        int numConfigs = configs.length;
        Log.w("S3DConfigChooserGLES2", String.format("%d configurations", numConfigs));
        for (int i = 0; i < numConfigs; i++) {
            Log.w("S3DConfigChooserGLES2", String.format("Configuration %d:\n", i));
            printConfig(egl, display, configs[i]);
        }
    }

    private void printConfig(EGL10 egl, EGLDisplay display, EGLConfig config) 
    {
        int[] attributes = 
        {
                EGL10.EGL_BUFFER_SIZE,
                EGL10.EGL_ALPHA_SIZE,
                EGL10.EGL_BLUE_SIZE,
                EGL10.EGL_GREEN_SIZE,
                EGL10.EGL_RED_SIZE,
                EGL10.EGL_DEPTH_SIZE,
                EGL10.EGL_STENCIL_SIZE,
                EGL10.EGL_CONFIG_CAVEAT,
                EGL10.EGL_CONFIG_ID,
                EGL10.EGL_LEVEL,
                EGL10.EGL_MAX_PBUFFER_HEIGHT,
                EGL10.EGL_MAX_PBUFFER_PIXELS,
                EGL10.EGL_MAX_PBUFFER_WIDTH,
                EGL10.EGL_NATIVE_RENDERABLE,
                EGL10.EGL_NATIVE_VISUAL_ID,
                EGL10.EGL_NATIVE_VISUAL_TYPE,
                0x3030, // EGL10.EGL_PRESERVED_RESOURCES,
                EGL10.EGL_SAMPLES,
                EGL10.EGL_SAMPLE_BUFFERS,
                EGL10.EGL_SURFACE_TYPE,
                EGL10.EGL_TRANSPARENT_TYPE,
                EGL10.EGL_TRANSPARENT_RED_VALUE,
                EGL10.EGL_TRANSPARENT_GREEN_VALUE,
                EGL10.EGL_TRANSPARENT_BLUE_VALUE,
                0x3039, // EGL10.EGL_BIND_TO_TEXTURE_RGB,
                0x303A, // EGL10.EGL_BIND_TO_TEXTURE_RGBA,
                0x303B, // EGL10.EGL_MIN_SWAP_INTERVAL,
                0x303C, // EGL10.EGL_MAX_SWAP_INTERVAL,
                EGL10.EGL_LUMINANCE_SIZE,
                EGL10.EGL_ALPHA_MASK_SIZE,
                EGL10.EGL_COLOR_BUFFER_TYPE,
                EGL10.EGL_RENDERABLE_TYPE,
                0x3042 // EGL10.EGL_CONFORMANT
        };
        String[] names = 
        {
                "EGL_BUFFER_SIZE",
                "EGL_ALPHA_SIZE",
                "EGL_BLUE_SIZE",
                "EGL_GREEN_SIZE",
                "EGL_RED_SIZE",
                "EGL_DEPTH_SIZE",
                "EGL_STENCIL_SIZE",
                "EGL_CONFIG_CAVEAT",
                "EGL_CONFIG_ID",
                "EGL_LEVEL",
                "EGL_MAX_PBUFFER_HEIGHT",
                "EGL_MAX_PBUFFER_PIXELS",
                "EGL_MAX_PBUFFER_WIDTH",
                "EGL_NATIVE_RENDERABLE",
                "EGL_NATIVE_VISUAL_ID",
                "EGL_NATIVE_VISUAL_TYPE",
                "EGL_PRESERVED_RESOURCES",
                "EGL_SAMPLES",
                "EGL_SAMPLE_BUFFERS",
                "EGL_SURFACE_TYPE",
                "EGL_TRANSPARENT_TYPE",
                "EGL_TRANSPARENT_RED_VALUE",
                "EGL_TRANSPARENT_GREEN_VALUE",
                "EGL_TRANSPARENT_BLUE_VALUE",
                "EGL_BIND_TO_TEXTURE_RGB",
                "EGL_BIND_TO_TEXTURE_RGBA",
                "EGL_MIN_SWAP_INTERVAL",
                "EGL_MAX_SWAP_INTERVAL",
                "EGL_LUMINANCE_SIZE",
                "EGL_ALPHA_MASK_SIZE",
                "EGL_COLOR_BUFFER_TYPE",
                "EGL_RENDERABLE_TYPE",
                "EGL_CONFORMANT"
        };
        int[] value = new int[1];
        for (int i = 0; i < attributes.length; i++) 
        {
            int attribute = attributes[i];
            String name = names[i];
            if ( egl.eglGetConfigAttrib(display, config, attribute, value)) 
            {
                Log.w("S3DConfigChooserGLES2", String.format("  %s: %d\n", name, value[0]));
            } 
            else 
            {
                Log.w("S3DConfigChooserGLES2", String.format("  %s: failed\n", name));
                // LOOPS FOREVER ON HTC HERO: while (egl.eglGetError() != EGL10.EGL_SUCCESS);
            }
        }
    }

    // Subclasses can adjust these values:
    protected int mRedSize;
    protected int mGreenSize;
    protected int mBlueSize;
    protected int mAlphaSize;
    protected int mDepthSize;
    protected int mStencilSize;
    private int[] mValue = new int[1];
}