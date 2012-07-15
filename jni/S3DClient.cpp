//----------------------------------------------------------------------
// @@BEGIN_JNI_INCLUDES@@
//----------------------------------------------------------------------
#include <jni.h>
#include <android/log.h>
//----------------------------------------------------------------------        
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <unistd.h>
#include <GLES/gl.h>
//----------------------------------------------------------------------        
#include "S3DClient_Wrapper.h"
#include "S3DX/S3DXAIVariable.h"
#include "S3DX/S3DXAIFunction.h"
//----------------------------------------------------------------------
// @@END_JNI_INCLUDES@@
//----------------------------------------------------------------------        
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "boxParticleLighting", __VA_ARGS__)
//----------------------------------------------------------------------        
extern "C"
{ 
    //----------------------------------------------------------------------
	// @@BEGIN_JNI_GLOBALS@@
    //----------------------------------------------------------------------
    static JavaVM      *pJavaVM                         = NULL ; 
    static char         aCacheDirPath   [512]           = "" ; 
    static char         aHomeDirPath    [512]           = "" ; 
    static char         aPackDirPath    [512]           = "" ; 
    static int          iPackFD                         = -1 ;
    static int          iPackOffset                     = -1 ;
    static int          iPackLength                     = -1 ;
	static char         aOverlayMovie   [512]	        = "" ; 
    static char         aDeviceName      [64]           = "" ; 
    static char         aDeviceModel     [64]           = "" ; 
	static char         aDeviceIPAddress [32]           = "" ;
	static char         aDeviceUUID		[256]			= "" ;
    static char         aSystemVersion   [32]           = "" ; 
    static char         aSystemLanguage  [32]           = "" ; 
    static int          iCameraDeviceCount              = 0 ; 
    static char         aCameraDeviceNames[256][8]      = { "" } ; 
    static int          iSurfaceWidth                   = 320 ; 
    static int          iSurfaceHeight                  = 240 ; 
    static bool 	    bMouseButtonDown                = false ; 
    static bool 	    bSupportLocation                = false ; 
    static bool 	    bSupportHeading                 = false ; 
    static bool         bForceNoViewportRotation        = false ;
	static bool         bSupportLowLatencyAudioTrack	= false ;
	static bool         bForceAudioBackendOpenAL        = false  ;
	static bool         bForceAudioBackendAndroid       = true ;	
	static bool         bWantCameraDeviceCapture        = false ;
	static bool         bWantSwapBuffers        		= true  ;
	//static bool     bVibrate					= false ;	
    //----------------------------------------------------------------------       
	// @@END_JNI_GLOBALS@@
    //----------------------------------------------------------------------       
	static JNIEnv *GetJNIEnv ( )
	{
		JNIEnv *pJNIEnv ;
		
        if ( pJavaVM && ( pJavaVM->GetEnv ( (void**) &pJNIEnv, JNI_VERSION_1_4 ) >= 0 ) )
        {
			return pJNIEnv ;
		}
		return NULL ;
	}

    //----------------------------------------------------------------------       
	// @@BEGIN_JNI_CLIENT_FUNCTIONS@@
    //----------------------------------------------------------------------       
	
	static int ClientFunctionCallback_onEngineEvent ( int _iInCount, const S3DX::AIVariable *_pIn, S3DX::AIVariable *_pOut )
	{
		LOGI("onEngineEvent");
		// This is a sample, in our case we are expecting 2 arguments: a number, and a boolean
		//
		if ( ( _iInCount == 2 ) && _pIn[0].IsNumber ( ) && ( int(_pIn[0].GetNumberValue ( )) == 1 ) && _pIn[1].IsBoolean ( ) )
		{
			bWantSwapBuffers = _pIn[1].GetBooleanValue ( ) ;
		}
		return 0 ;
	}

	static int ClientFunctionCallback_onDropBoxLogin ( int _iInCount, const S3DX::AIVariable *_pIn, S3DX::AIVariable *_pOut )
	{
		LOGI("onDropBoxLogin");
		int started = 0;
		JNIEnv *pJNIEnv = GetJNIEnv();
		if (pJNIEnv)
		{
			//Find the DropBox class
			jclass pJNIActivityClass = pJNIEnv->FindClass("com/test/test/boxParticleLighting");

			if (pJNIActivityClass == NULL)
				LOGI("jclass was null!?!");
			else
			{
				//Find the DropBoxLogin method
				jmethodID pJNIMethodID = pJNIEnv->GetStaticMethodID(pJNIActivityClass, "DropBoxLogin", "()I");

				if (pJNIMethodID == NULL)
					LOGI("jmethodID was null!?!?");
				else
				{
					started = pJNIEnv->CallStaticIntMethod(pJNIActivityClass, pJNIMethodID, NULL);
				}
			}
		}

		return started;
	}

	static int ClientFunctionCallback_onDropBoxLogout ( int _iInCount, const S3DX::AIVariable *_pIn, S3DX::AIVariable *_pOut )
	{
		LOGI("onDropBoxLogout");
		JNIEnv *pJNIEnv = GetJNIEnv();
		if (pJNIEnv)
		{
			//Find the DropBox class
			jclass pJNIActivityClass = pJNIEnv->FindClass("com/test/test/boxParticleLighting");

			if (pJNIActivityClass == NULL)
				LOGI("jclass was null!?!");
			else
			{
				//Find the DropBoxLogin method
				jmethodID pJNIMethodID = pJNIEnv->GetStaticMethodID(pJNIActivityClass, "DropBoxLogout", "()V");

				if (pJNIMethodID == NULL)
					LOGI("jmethodID was null!?!?");
				else
				{
					pJNIEnv->CallStaticVoidMethod(pJNIActivityClass, pJNIMethodID, NULL);
				}
			}
		}
		return 0 ;
	}

	//-----------------------------------------------------------------------------

	static const int		iClientFunctionsCount = 3 ; // Modify this number when adding new functions just below
	static S3DX::AIFunction aClientFunctions  [ ] =
	{
	    { "onEngineEvent", ClientFunctionCallback_onEngineEvent, "...", "...", "...", 0 },
	    { "onDropBoxLogin", ClientFunctionCallback_onDropBoxLogin, "...", "...", "...", 0 },
	    { "onDropBoxLogout", ClientFunctionCallback_onDropBoxLogout, "...", "...", "...", 0 }
	} ;

    //----------------------------------------------------------------------       
	// @@END_JNI_CLIENT_FUNCTIONS@@
    //----------------------------------------------------------------------       
	
    //----------------------------------------------------------------------       
	// @@BEGIN_JNI_CALLBACKS@@
    //----------------------------------------------------------------------       
    static void CreateDotNoMediaFile ( )
    {
        char      aFilePath   [512] ;
        strcpy  ( aFilePath,  aCacheDirPath ) ;
        strcat  ( aFilePath,  "/.nomedia"   ) ;
        
        FILE   * pFile = fopen ( aFilePath, "w" ) ;
        if     ( pFile )
        {
            fclose ( pFile ) ;
        }        
    }
    //----------------------------------------------------------------------        
    static bool DumpBufferToFile ( const char *_pBuffer, int _iBufferSize, const char *_pFilePath )
    {
        FILE   * pFile = fopen ( _pFilePath, "wb" ) ;
        if     ( pFile )
        {
            fwrite ( _pBuffer, _iBufferSize, 1, pFile ) ;
            fclose ( pFile ) ;
            return true ;
        }
        return false ;
    }
    //----------------------------------------------------------------------        
    static void S3DLogCallback ( int _iLogCategory, const char *_pLogMessage )
    {
        LOGI( _pLogMessage ) ;
    }
    //----------------------------------------------------------------------        
    static void S3DOpenURLCallback ( const char *_pURL, const char *_pTarget, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID		= pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onOpenURL", "(Ljava/lang/String;Ljava/lang/String;)V" ) ;
			
            pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, pJNIEnv->NewStringUTF ( _pURL ), pJNIEnv->NewStringUTF ( _pTarget ) ) ;
        }
    }
    //----------------------------------------------------------------------        
	
    static bool S3DSoundDeviceInitializeCallback ( void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID		= pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onInitSound", "()Z" ) ;
			
            return pJNIEnv->CallStaticBooleanMethod ( pJNIActivityClass, pJNIMethodID ) ;
        }
        return false ;
    }
    //----------------------------------------------------------------------        
    static void S3DSoundDeviceShutdownCallback ( void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID		= pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onShutdownSound", "()V" ) ;
			
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundDeviceSuspendCallback ( bool _bSuspend, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID		= pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onSuspendSound", "(Z)V" ) ;
			
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _bSuspend ) ;
        }
    }
    //----------------------------------------------------------------------        
    static int S3DSoundLoadCallback ( const char *_pBuffer, int _iBufferSize, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {	
			if ( _pBuffer && ( _iBufferSize > 4 ) )
			{
             	char      aFileName [32] ;
             	sprintf ( aFileName, "%08x%02x%02x", _iBufferSize, _pBuffer[_iBufferSize>>2], _pBuffer[_iBufferSize>>1] ) ;
             	char      aFilePath   [512] ;
             	strcpy  ( aFilePath,  aCacheDirPath ) ;
             	strcat  ( aFilePath,  "/"         ) ;
             	strcat  ( aFilePath,  aFileName     ) ;
             	if      ( memcmp ( _pBuffer, "OggS", 4 ) == 0 ) strcat ( aFilePath, ".ogg" ) ;
             	else if ( memcmp ( _pBuffer, "VAGp", 4 ) == 0 ) strcat ( aFilePath, ".vag" ) ;
             	else                                            strcat ( aFilePath, ".wav" ) ;
             
             	if ( DumpBufferToFile ( _pBuffer, _iBufferSize, aFilePath ) )
             	{
				 	jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
				 	jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onLoadSound", "(Ljava/lang/String;)I" ) ;
	
                 	return pJNIEnv->CallStaticIntMethod ( pJNIActivityClass, pJNIMethodID, pJNIEnv->NewStringUTF ( aFilePath ) ) ;
             	}
			}
        }
        return 0 ;
    }
    //----------------------------------------------------------------------        
    static void S3DSoundUnloadCallback ( int _iSoundIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onUnloadSound", "(I)V" ) ;
				
            pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static int S3DSoundPlayCallback ( int _iSoundIndex, float _fVolume, bool _bLoop, float _fPriority, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onPlaySound", "(IFZF)I" ) ;
				
            return pJNIEnv->CallStaticIntMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex, _fVolume, _bLoop, _fPriority ) ;
        }
        return -1 ;
    }
    //----------------------------------------------------------------------        
    static void S3DSoundPauseCallback ( int _iSoundIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			 jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			 jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onPauseSound", "(I)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundResumeCallback ( int _iSoundIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onResumeSound", "(I)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundStopCallback ( int _iSoundIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onStopSound", "(I)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundSetVolumeCallback ( int _iSoundIndex, float _fVolume, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onSetSoundVolume", "(IF)V" ) ;
				
			 pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex, _fVolume ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundSetPitchCallback ( int _iSoundIndex, float _fPitch, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onSetSoundPitch", "(IF)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex, _fPitch ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DSoundSetLoopingCallback ( int _iSoundIndex, bool _bLoop, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onSetSoundLooping", "(IZ)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iSoundIndex, _bLoop ) ;
        }
    }

    //----------------------------------------------------------------------        
    static int S3DMusicLoadCallback ( const char *_pBuffer, int _iBufferSize, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {	
			if ( _pBuffer && ( _iBufferSize > 4 ) )
			{	
             	char      aFileName [32] ;
             	sprintf ( aFileName, "%08x%02x%02x", _iBufferSize, _pBuffer[_iBufferSize>>2], _pBuffer[_iBufferSize>>1] ) ;
             	char      aFilePath   [512] ;
	             strcpy  ( aFilePath,  aCacheDirPath ) ;
             	strcat  ( aFilePath,  "/"         ) ;
             	strcat  ( aFilePath,  aFileName     ) ;
             	if      ( memcmp ( _pBuffer, "OggS", 4 ) == 0 ) strcat ( aFilePath, ".ogg" ) ;
             	else if ( memcmp ( _pBuffer, "VAGp", 4 ) == 0 ) strcat ( aFilePath, ".vag" ) ;
             	else                                            strcat ( aFilePath, ".wav" ) ;
             
             	if ( DumpBufferToFile ( _pBuffer, _iBufferSize, aFilePath ) )
             	{
				 	jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
				 	jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onLoadMusic", "(Ljava/lang/String;)I" ) ;
	
                 	return pJNIEnv->CallStaticIntMethod ( pJNIActivityClass, pJNIMethodID, pJNIEnv->NewStringUTF ( aFilePath ) ) ;
             	}
			}
        }
        return 0 ;
    }
    //----------------------------------------------------------------------        
    static void S3DMusicUnloadCallback ( int _iMusicIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onUnloadMusic", "(I)V" ) ;
				
            pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iMusicIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static int S3DMusicPlayCallback ( int _iMusicIndex, float _fVolume, bool _bLoop, float _fPriority, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onPlayMusic", "(IFZF)I" ) ;
				
            return pJNIEnv->CallStaticIntMethod ( pJNIActivityClass, pJNIMethodID, _iMusicIndex, _fVolume, _bLoop, _fPriority ) ;
        }
        return -1 ;
    }
    //----------------------------------------------------------------------        
    static void S3DMusicPauseCallback ( int _iMusicIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
			 jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
			 jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onPauseMusic", "(I)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iMusicIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DMusicResumeCallback ( int _iMusicIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onResumeMusic", "(I)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iMusicIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DMusicStopCallback ( int _iMusicIndex, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onStopMusic", "(I)V" ) ;
				
             pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iMusicIndex ) ;
        }
    }
    //----------------------------------------------------------------------        
    static void S3DMusicSetVolumeCallback ( int _iMusicIndex, float _fVolume, void *_pOwner )
    {
		JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
        if    ( pJNIEnv )
        {
             jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
             jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onSetMusicVolume", "(IF)V" ) ;
				
			 pJNIEnv->CallStaticVoidMethod ( pJNIActivityClass, pJNIMethodID, _iMusicIndex, _fVolume ) ;
        }
    }
    
    //----------------------------------------------------------------------        
    static bool S3DCameraDeviceCaptureStartCallback ( int _iDeviceIndex, int _iImageWidth, int _iImageHeight, int _iFrameRate, void *_pOwner )
    {
        bWantCameraDeviceCapture = true ;
        return true ; // ???
    }
	
    //----------------------------------------------------------------------        
    static void S3DCameraDeviceCaptureStopCallback ( int _iDeviceIndex, void *_pOwner )
    {
        bWantCameraDeviceCapture = false ;
    }
	
    //----------------------------------------------------------------------        
    //static void S3DVibrateCallback ( float _fMagnitude, void *_pOwner )
    //{
	//	bVibrate = ( _fMagnitude <= -0.5f || _fMagnitude >=  0.5f ) ;
    //}
    //----------------------------------------------------------------------        
    static bool S3DPlayOverlayMovieCallback ( const char *_pFileName, void *_pOwner )
    {
        strcpy ( aOverlayMovie, _pFileName ) ;
        return true ;
    }
    //----------------------------------------------------------------------        
    static void S3DStopOverlayMovieCallback ( void *_pOwner )
    {
		aOverlayMovie[0] = '\0' ;
    }
    //----------------------------------------------------------------------        
    static bool S3DEnableLocationCallback ( bool _bEnable, void *_pOwner )
    {
		if ( bSupportLocation )
		{
		    JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
            if    ( pJNIEnv )
            {
                 jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
                 jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onEnableLocationUpdates", "(Z)Z" ) ;
				
                 return pJNIEnv->CallStaticBooleanMethod ( pJNIActivityClass, pJNIMethodID, _bEnable ) ;
            }
		}
        return false ;
    }        
    //----------------------------------------------------------------------        
    static bool S3DEnableHeadingCallback ( bool _bEnable, void *_pOwner )
    {
        if ( bSupportHeading )
		{
		    JNIEnv *pJNIEnv = GetJNIEnv ( ) ;
            if    ( pJNIEnv )
            {
                 jclass    pJNIActivityClass = pJNIEnv->FindClass ( "com/test/test/boxParticleLighting" ) ;
                 jmethodID pJNIMethodID	     = pJNIEnv->GetStaticMethodID ( pJNIActivityClass, "onEnableHeadingUpdates", "(Z)Z" ) ;
				
                 return pJNIEnv->CallStaticBooleanMethod ( pJNIActivityClass, pJNIMethodID, _bEnable ) ;
            }
		}
        return false ;
    }        
    //----------------------------------------------------------------------       
	// @@END_JNI_CALLBACKS@@
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetDirectories ( JNIEnv *_pEnv, jobject obj, jstring sCacheDirPath, jstring sHomeDirPath, jstring sPackDirPath )
    {
        LOGI( "### engineSetDirectories" ) ;
        const char *pCacheStr = _pEnv->GetStringUTFChars ( sCacheDirPath, NULL ) ;
        const char *pHomeStr  = _pEnv->GetStringUTFChars ( sHomeDirPath,  NULL ) ;
        const char *pPackStr  = _pEnv->GetStringUTFChars ( sPackDirPath,  NULL ) ;
        if ( pCacheStr ) strcpy ( aCacheDirPath, pCacheStr ) ;
        if ( pHomeStr  ) strcpy ( aHomeDirPath,  pHomeStr  ) ;
        if ( pPackStr  ) strcpy ( aPackDirPath,  pPackStr  ) ;
        if ( pCacheStr ) _pEnv->ReleaseStringUTFChars ( sCacheDirPath, pCacheStr ) ;
        if ( pHomeStr  ) _pEnv->ReleaseStringUTFChars ( sHomeDirPath,  pHomeStr  ) ;
        if ( pPackStr  ) _pEnv->ReleaseStringUTFChars ( sPackDirPath,  pPackStr  ) ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetPackFileDescriptor ( JNIEnv *_pEnv, jobject obj, jobject fileDescriptor, jlong offset, jlong length )
    {
        LOGI( "### engineSetPackFileDescriptor" ) ;
        if ( fileDescriptor )
        {
            jclass fdClass = _pEnv->FindClass ( "java/io/FileDescriptor" ) ;
            if   ( fdClass )
            {
                jclass   fdClassRef               = (jclass)_pEnv->NewGlobalRef ( fdClass ) ;
                jfieldID fdClassDescriptorFieldID =         _pEnv->GetFieldID   ( fdClass, "descriptor", "I" ) ;
                if     ( fdClassDescriptorFieldID )
                {
                    jint fd     = _pEnv->GetIntField ( fileDescriptor, fdClassDescriptorFieldID ) ;
                    iPackFD     = dup ( fd ) ;
                    iPackOffset = offset ;
                    iPackLength = length ;
                }
            }
        }    
    } 
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetLocationSupport ( JNIEnv *_pEnv, jobject obj, jboolean bSupport )
    {
        bSupportLocation = bSupport ;
    }        
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetHeadingSupport ( JNIEnv *_pEnv, jobject obj, jboolean bSupport )
    {
        bSupportHeading = bSupport ;
    }        
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetDeviceName ( JNIEnv *_pEnv, jobject obj, jstring sName )
	{
        const char *pNameStr = _pEnv->GetStringUTFChars ( sName, NULL ) ;
        if ( pNameStr  ) strcpy ( aDeviceName,  pNameStr  ) ;
        if ( pNameStr ) _pEnv->ReleaseStringUTFChars ( sName, pNameStr ) ;
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetDeviceModel ( JNIEnv *_pEnv, jobject obj, jstring sModel )
	{
        const char *pModelStr = _pEnv->GetStringUTFChars ( sModel, NULL ) ;
        if ( pModelStr  ) strcpy ( aDeviceModel,  pModelStr  ) ;
        if ( pModelStr ) _pEnv->ReleaseStringUTFChars ( sModel, pModelStr ) ;
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetDeviceIPAddress ( JNIEnv *_pEnv, jobject obj, jstring sAddress )
	{
        const char *pAddressStr = _pEnv->GetStringUTFChars ( sAddress, NULL ) ;
        if ( pAddressStr  ) strcpy ( aDeviceIPAddress,  pAddressStr  ) ;
        if ( pAddressStr ) _pEnv->ReleaseStringUTFChars ( sAddress, pAddressStr ) ;
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetDeviceUUID ( JNIEnv *_pEnv, jobject obj, jstring sUUID )
	{
        const char *pUUIDStr = _pEnv->GetStringUTFChars ( sUUID, NULL ) ;
        if ( pUUIDStr  ) strcpy ( aDeviceUUID,  pUUIDStr  ) ;
        if ( pUUIDStr ) _pEnv->ReleaseStringUTFChars ( sUUID, pUUIDStr ) ;
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetSystemVersion ( JNIEnv *_pEnv, jobject obj, jstring sVersion )
	{
        const char *pVersionStr = _pEnv->GetStringUTFChars ( sVersion, NULL ) ;
        if ( pVersionStr  ) strcpy ( aSystemVersion,  pVersionStr  ) ;
        if ( pVersionStr ) _pEnv->ReleaseStringUTFChars ( sVersion, pVersionStr ) ;
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetSystemLanguage ( JNIEnv *_pEnv, jobject obj, jstring sLanguage )
	{
        const char *pLanguageStr = _pEnv->GetStringUTFChars ( sLanguage, NULL ) ;
        if ( pLanguageStr  ) strcpy ( aSystemLanguage,  pLanguageStr  ) ;
        if ( pLanguageStr ) _pEnv->ReleaseStringUTFChars ( sLanguage, pLanguageStr ) ;
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetCameraDeviceCount ( JNIEnv *_pEnv, jobject obj, jint count )
	{
	    iCameraDeviceCount = count ;
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineSetCameraDeviceName ( JNIEnv *_pEnv, jobject obj, jint index, jstring sName )
	{
        const char *pNameStr = _pEnv->GetStringUTFChars ( sName, NULL ) ;
        if ( pNameStr ) strcpy ( aCameraDeviceNames[index],  pNameStr  ) ;
        if ( pNameStr ) _pEnv->ReleaseStringUTFChars ( sName, pNameStr ) ;
	}
    //----------------------------------------------------------------------        
	JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnCameraDeviceFrame ( JNIEnv *_pEnv, jobject obj, jbyteArray data, jint w, jint h )
	{
	    /*???
	    static char buf[ 1024 * 1024 * 4 ] ;
	    int len = _pEnv->GetArrayLength ( data ) ;
	    
	    _pEnv->GetByteArrayRegion ( data, 0, len, (jbyte*)buf);
	    S3DClient_Android_SetCameraDeviceCapturedImage ( 0, buf, w, h, 0, 0 ) ;
	    */
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineForceDefaultOrientation ( JNIEnv *_pEnv, jobject obj, jboolean b )
    {
        bForceNoViewportRotation = b ; // TODO: set an engine's option instead
    }    
    //----------------------------------------------------------------------        
    JNIEXPORT jboolean JNICALL Java_com_test_test_S3DRenderer_engineInitialize ( JNIEnv *_pEnv, jobject obj )
    {
        LOGI( "### engineInitialize" ) ;
        
        // Get a pointer to the Java VM
        //
        _pEnv->GetJavaVM ( &pJavaVM ) ;

        // Create the ".nomedia" file
        //
        CreateDotNoMediaFile ( ) ;

		// Depending on the OS version, use OpenAL/AudioTrack or the crappy SoundPool
		//
		/* Wrong assumption, not depending on the OS but on the hardware...
		if ( strlen( aSystemVersion ) >= 3 )
		{
			int iMajor, iMinor ;
			
			if ( sscanf ( aSystemVersion, "%d.%d",  &iMajor, &iMinor ) == 2 )
			{
				// Starting from Froyo (2.2) LLAT is supported
				//
		    	bSupportLowLatencyAudioTrack = (iMajor >= 2) && (iMinor >= 2) ;
		    }
		}
		*/
		// On Tegra and Qualcomm chips LLAT *seems* to be supported
		//
		const char *pGPUVendor = (const char *)glGetString ( GL_VENDOR ) ; 
		
		bSupportLowLatencyAudioTrack = pGPUVendor && ( strstr ( pGPUVendor, "NVIDIA" ) || strstr ( pGPUVendor, "Qualcomm" ) ) ;

        // Initialize engine
        //
        //char aLoadPackPath [512] ; sprintf ( aLoadPackPath, "file://%s/S3DStartup.stk", aPackDirPath ) ; 
        char aMainPackPath          [512] ; sprintf ( aMainPackPath,  "file://%s/S3DMain.stk", aPackDirPath ) ;
        char aMainPackPathNoPrefix  [512] ; sprintf ( aMainPackPathNoPrefix, "%s/S3DMain.stk", aPackDirPath ) ;
        
        S3DClient_Init                                          ( aHomeDirPath ) ;
		//S3DClient_SetGameOption									( 30, 2 ) ; // S-3D 
        S3DClient_SetGraphicContainer                           ( NULL , 0, 0, iSurfaceWidth, iSurfaceHeight ) ;
        S3DClient_SetInputContainer                             ( NULL , 0, 0, iSurfaceWidth, iSurfaceHeight ) ;
        S3DClient_SetFullscreen                                 ( false ) ;
        S3DClient_SetClientType                                 ( S3DClient_Type_StandAlone ) ;
        S3DClient_SetLogCallbacks                               ( S3DLogCallback, S3DLogCallback, S3DLogCallback ) ;
		S3DClient_SetOpenURLCallback                            ( S3DOpenURLCallback,				NULL ) ;
        S3DClient_SetPlayOverlayMovieCallback                   ( S3DPlayOverlayMovieCallback,      NULL ) ;
        S3DClient_SetStopOverlayMovieCallback                   ( S3DStopOverlayMovieCallback,      NULL ) ;
		if ( bForceAudioBackendAndroid || ( ! bSupportLowLatencyAudioTrack && ! bForceAudioBackendOpenAL ) )
		{
			S3DClient_Android_SetSoundDeviceUseExternalDriver   ( true ) ;
            S3DClient_Android_SetSoundDeviceInitializeCallback  ( S3DSoundDeviceInitializeCallback, NULL ) ;
            S3DClient_Android_SetSoundDeviceShutdownCallback    ( S3DSoundDeviceShutdownCallback,   NULL ) ;
			//S3DClient_Android_SetSoundDeviceSuspendCallback   ( S3DSoundDeviceSuspendCallback,    NULL ) ;
            S3DClient_Android_SetSoundLoadCallback              ( S3DSoundLoadCallback,             NULL ) ;
            S3DClient_Android_SetSoundUnloadCallback            ( S3DSoundUnloadCallback,           NULL ) ;
            S3DClient_Android_SetSoundPlayCallback              ( S3DSoundPlayCallback,             NULL ) ;
            S3DClient_Android_SetSoundPauseCallback             ( S3DSoundPauseCallback,            NULL ) ;
            S3DClient_Android_SetSoundResumeCallback            ( S3DSoundResumeCallback,           NULL ) ;
            S3DClient_Android_SetSoundStopCallback              ( S3DSoundStopCallback,             NULL ) ;
            S3DClient_Android_SetSoundSetVolumeCallback         ( S3DSoundSetVolumeCallback,        NULL ) ;
            S3DClient_Android_SetSoundSetPitchCallback          ( S3DSoundSetPitchCallback,         NULL ) ;
            S3DClient_Android_SetSoundSetLoopingCallback        ( S3DSoundSetLoopingCallback,       NULL ) ;
            S3DClient_Android_SetMusicLoadCallback              ( S3DMusicLoadCallback,             NULL ) ;
            S3DClient_Android_SetMusicUnloadCallback            ( S3DMusicUnloadCallback,           NULL ) ;
            S3DClient_Android_SetMusicPlayCallback              ( S3DMusicPlayCallback,             NULL ) ;
            S3DClient_Android_SetMusicPauseCallback             ( S3DMusicPauseCallback,            NULL ) ;
            S3DClient_Android_SetMusicResumeCallback            ( S3DMusicResumeCallback,           NULL ) ;
            S3DClient_Android_SetMusicStopCallback              ( S3DMusicStopCallback,             NULL ) ;
            S3DClient_Android_SetMusicSetVolumeCallback         ( S3DMusicSetVolumeCallback,        NULL ) ;
            S3DClient_Android_InitializeSoundDevice             ( ) ;
        }
        //TODO: S3DClient_Android_SetVibrateCallback        		( S3DVibrateCallback,               NULL ) ;
        S3DClient_Android_SetLocationSupported                  ( bSupportLocation ) ;
		S3DClient_Android_SetHeadingSupported					( bSupportHeading ) ;
        S3DClient_Android_SetEnableLocationCallback             ( S3DEnableLocationCallback,        NULL ) ;
        S3DClient_Android_SetEnableHeadingCallback              ( S3DEnableHeadingCallback,         NULL ) ;
        S3DClient_Android_SetDeviceName                         ( aDeviceName ) ;
        S3DClient_Android_SetDeviceModel                        ( aDeviceModel ) ;
		S3DClient_Android_SetDeviceIPAddress					( aDeviceIPAddress ) ;
		S3DClient_Android_SetDeviceUUID							( aDeviceUUID ) ;
        S3DClient_Android_SetSystemVersion                      ( aSystemVersion ) ;
        S3DClient_Android_SetSystemLanguage                     ( aSystemLanguage ) ;
        if ( iCameraDeviceCount > 0 )
        {
            S3DClient_Android_SetCameraDeviceCount                  ( iCameraDeviceCount ) ;
            S3DClient_Android_SetCameraDeviceName                   ( 0, aCameraDeviceNames[0] ) ;
            S3DClient_Android_SetCameraDeviceCaptureStartCallback   ( S3DCameraDeviceCaptureStartCallback, NULL ) ; 
            S3DClient_Android_SetCameraDeviceCaptureStopCallback    ( S3DCameraDeviceCaptureStopCallback,  NULL ) ; 
        }
        if ( iPackFD != -1 )
        {
            S3DClient_Android_AddFileAccessibleFromAPK          ( aMainPackPathNoPrefix, iPackFD, iPackOffset, iPackLength ) ; 
        }
        S3DClient_LoadPack                                      ( NULL, aMainPackPath,              NULL ) ;

    	//----------------------------------------------------------------------        
		// @@BEGIN_JNI_REGISTER_CLIENT_FUNCTIONS@@
		//----------------------------------------------------------------------        
        
		for ( int iClientFunction = 0 ; iClientFunction < iClientFunctionsCount ; iClientFunction++ )
		{
			S3DClient_RegisterFunction ( aClientFunctions[ iClientFunction ].pName, aClientFunctions ) ;
		}

    	//----------------------------------------------------------------------        
		// @@END_JNI_REGISTER_CLIENT_FUNCTIONS@@
		//----------------------------------------------------------------------        

		//----------------------------------------------------------------------        
		// @@BEGIN_JNI_INSTALL_EVENT_HOOKS@@
		//----------------------------------------------------------------------        
		
		//----------------------------------------------------------------------        
		// @@END_JNI_INSTALL_EVENT_HOOKS@@
		//----------------------------------------------------------------------        
		
		S3DClient_RunOneFrame               ( ) ; // Call it one time to clear the stopped flag
        S3DClient_iPhone_OnTouchesChanged   ( 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f, 0, 0, 0.0f, 0.0f ) ; // Force no touches (should not be needed...)
        return true ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineShutdown ( JNIEnv *_pEnv, jobject obj ) 
    { 
        LOGI( "### engineShutdown" ) ; 
        S3DClient_Shutdown ( ) ;
        pJavaVM = NULL ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT jboolean JNICALL Java_com_test_test_S3DRenderer_engineRunOneFrame ( JNIEnv *_pEnv, jobject obj )
    {
        //LOGI( "### runOneFrame" ) ; 
        if (   bForceNoViewportRotation  ) S3DClient_iPhone_SetViewportRotation ( 0 ) ; // FIXME: avoid extra call by setting an engine's option instead
        if ( ! S3DClient_RunOneFrame ( ) ) return false ;
        if (   S3DClient_Stopped     ( ) ) return false ;
        return true ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_enginePause ( JNIEnv *_pEnv, jobject obj, jboolean b ) 
    { 
        if ( b ) LOGI( "### enginePause" ) ; 
		else	 LOGI( "### engineResume" ) ; 
		S3DClient_Pause ( b ) ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnSurfaceCreated ( JNIEnv *_pEnv, jobject obj )        
    {
        LOGI( "### engineOnSurfaceCreated" ) ;
		if ( pJavaVM )
		{
			S3DClient_OnGraphicContextLost ( ) ;
		}
	}
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnSurfaceChanged ( JNIEnv *_pEnv, jobject obj, jint w, jint h )        
    {
        LOGI( "### engineOnSurfaceChanged" ) ;
        iSurfaceWidth  = w ;        
        iSurfaceHeight = h ;        

		if ( pJavaVM )
		{
            S3DClient_SetGraphicContainer ( NULL, 0, 0, w, h ) ;        
            S3DClient_SetInputContainer   ( NULL, 0, 0, w, h ) ;        
        }
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnMouseMove ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y )        
    {
        S3DClient_iPhone_OnMouseMoved           ( 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;        
        if ( ! bMouseButtonDown )
        {
            bMouseButtonDown = true ;        
            S3DClient_iPhone_OnMouseButtonPressed ( ) ;
        }
        //LOGI( "### Move: %f %f", 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;      
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnMouseButtonDown ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y )        
    {
        bMouseButtonDown = true ;
        S3DClient_iPhone_OnMouseMoved           ( 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;        
        S3DClient_iPhone_OnMouseButtonPressed   ( ) ;        

        //LOGI( "### Down: %f %f", 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;      
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnMouseButtonUp ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y )        
    {
        bMouseButtonDown = false ;
        S3DClient_iPhone_OnMouseMoved           ( 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;        
        S3DClient_iPhone_OnMouseButtonReleased  ( ) ;
        
        //LOGI( "### Up: %f %f", 2.0f * x / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y ) / (float)iSurfaceHeight - 1.0f ) ;      
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnTouchesChange ( JNIEnv *_pEnv, jobject obj, jint tc1, jfloat x1, jfloat y1, jint tc2, jfloat x2, jfloat y2, jint tc3, jfloat x3, jfloat y3, jint tc4, jfloat x4, jfloat y4, jint tc5, jfloat x5, jfloat y5 )
    {
        if ( S3DClient_iPhone_IsMultiTouchEnabled ( ) )
        {
            S3DClient_iPhone_OnTouchesChanged   ( 0, tc1, 2.0f * x1 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y1 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc2, 2.0f * x2 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y2 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc3, 2.0f * x3 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y3 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc4, 2.0f * x4 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y4 ) / (float)iSurfaceHeight - 1.0f,
                                                  0, tc5, 2.0f * x5 / (float)iSurfaceWidth - 1.0f, 2.0f * ( (float)iSurfaceHeight - y5 ) / (float)iSurfaceHeight - 1.0f ) ;
        }

		//LOGI( "###### %d %d %d %d %d", tc1, tc2, tc3, tc4, tc5 ) ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnDeviceMove ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y, jfloat z )
    {
        if ( iSurfaceWidth > iSurfaceHeight ) // FIXME
            S3DClient_iPhone_OnDeviceMoved (  y / 9.81f, -x / 9.81f, z / 9.81f ) ;
        else
            S3DClient_iPhone_OnDeviceMoved ( -x / 9.81f, -y / 9.81f, z / 9.81f ) ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnKeyboardKeyDown ( JNIEnv *_pEnv, jobject obj, jint key, jint uni )        
    {
        S3DClient_Android_OnKeyboardKeyPressed ( key, uni ) ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnKeyboardKeyUp ( JNIEnv *_pEnv, jobject obj, jint key, jint uni )        
    {
        S3DClient_Android_OnKeyboardKeyReleased ( key, uni ) ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnLocationChanged ( JNIEnv *_pEnv, jobject obj, jfloat x, jfloat y, jfloat z )
    {
        S3DClient_Android_UpdateLocation ( x, y, z ) ;
    }
    //----------------------------------------------------------------------        
    JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnHeadingChanged ( JNIEnv *_pEnv, jobject obj, jfloat angle )
    {
        S3DClient_Android_UpdateHeading ( angle, angle ) ;
    }
    //----------------------------------------------------------------------   
	JNIEXPORT void JNICALL Java_com_test_test_S3DRenderer_engineOnOverlayMovieStopped ( JNIEnv *_pEnv, jobject obj )
	{
		aOverlayMovie[0] = '\0' ;
		S3DClient_OnOverlayMovieStopped ( ) ;
	}
    //----------------------------------------------------------------------   
	JNIEXPORT jstring JNICALL Java_com_test_test_S3DRenderer_engineGetOverlayMovie ( JNIEnv *_pEnv, jobject obj )
	{
		return _pEnv->NewStringUTF ( aOverlayMovie ) ;
	}
    //----------------------------------------------------------------------   
	jboolean JNICALL Java_com_test_test_S3DRenderer_engineGetCameraDeviceState ( JNIEnv *_pEnv, jobject obj )
	{
		return bWantCameraDeviceCapture ;
	}
    //----------------------------------------------------------------------   
	//jboolean Java_com_test_test_S3DRenderer_engineGetVibratorState ( JNIEnv *_pEnv, jobject obj )
	//{
	//	return bVibrate ;
	//}
    //----------------------------------------------------------------------   
	JNIEXPORT jboolean JNICALL Java_com_test_test_S3DRenderer_engineGetWantSwapBuffers ( JNIEnv *_pEnv, jobject obj )
	{
		return bWantSwapBuffers ;
	}
	//----------------------------------------------------------------------        	
} 
