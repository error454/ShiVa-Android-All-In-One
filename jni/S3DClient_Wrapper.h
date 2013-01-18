//-----------------------------------------------------------------------------
#ifndef __S3DClient_Wrapper_h__
#define __S3DClient_Wrapper_h__
//-----------------------------------------------------------------------------
// Forward declarations
//
namespace S3DX
{
    struct  AIFunction  ;
}
//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------
//  Constants
//-----------------------------------------------------------------------------

#define S3DClientOptions_PosX			                    0x00000001
#define S3DClientOptions_PosY		                        0x00000002
#define S3DClientOptions_Width                              0x00000004
#define S3DClientOptions_Height                             0x00000008
#define S3DClientOptions_AllowFullScreen                    0x00000010
#define S3DClientOptions_ForceFailsafe 	                    0x00000020
#define S3DClientOptions_AppAntiAliasingLevel               0x00000100
#define S3DClientOptions_AppDynamicShadowsLevel             0x00000200
#define S3DClientOptions_AppPostRenderingEffectsLevel       0x00000400
#define S3DClientOptions_AppReflectionLevel                 0x00000800

#define S3DClient_Type_Embedded	                            0
#define S3DClient_Type_StandAlone                           1
#define S3DClient_Type_Option                               2
#define S3DClient_Type_About                                3

#define S3DClient_TouchStatus_None                          0
#define S3DClient_TouchStatus_Began                         1
#define S3DClient_TouchStatus_Moved                         2
#define S3DClient_TouchStatus_Stationary                    3
#define S3DClient_TouchStatus_Ended                         4
#define S3DClient_TouchStatus_Cancelled                     5


//-----------------------------------------------------------------------------
//  Callbacks
//-----------------------------------------------------------------------------

typedef void ( * S3DClient_OpenURLCallback )                ( const char *_pURL, const char *_pTarget, void *_pUserData ) ;
typedef void ( * S3DClient_LogCallback )                    ( int _iLogCategory, const char *_pLogMessage ) ;
typedef bool ( * S3DClient_WakeUpConnectionCallback )       ( void *_pUserData ) ;

typedef bool ( * S3DClient_PlayOverlayMovieCallback )       ( const char *_pFileName, void *_pUserData ) ;
typedef void ( * S3DClient_StopOverlayMovieCallback )       (                         void *_pUserData ) ;

typedef bool ( * S3DClient_SoundDeviceInitializeCallback )  ( void *_pUserData ) ;
typedef void ( * S3DClient_SoundDeviceShutdownCallback )    ( void *_pUserData ) ;
typedef void ( * S3DClient_SoundDeviceSuspendCallback )     ( bool _bSuspend, void *_pUserData ) ;

typedef int  ( * S3DClient_SoundLoadCallback )              ( const char *_pBuffer, int _iBufferSize, void *_pUserData ) ;
typedef void ( * S3DClient_SoundUnloadCallback )            ( int _iSoundIndex, void *_pUserData ) ;
typedef int  ( * S3DClient_SoundPlayCallback )              ( int _iSoundIndex, float _fVolume, bool _bLoop, float _fPriority, void *_pUserData ) ;
typedef void ( * S3DClient_SoundPauseCallback )             ( int _iStreamIndex, void *_pUserData ) ;
typedef void ( * S3DClient_SoundResumeCallback )            ( int _iStreamIndex, void *_pUserData ) ;
typedef void ( * S3DClient_SoundStopCallback )              ( int _iStreamIndex, void *_pUserData ) ;
typedef void ( * S3DClient_SoundSetVolumeCallback )         ( int _iStreamIndex, float _fVolume, void *_pUserData ) ;
typedef void ( * S3DClient_SoundSetPitchCallback )          ( int _iStreamIndex, float _fPitch, void *_pUserData ) ;
typedef void ( * S3DClient_SoundSetLoopingCallback )        ( int _iStreamIndex, bool  _bLooping, void *_pUserData ) ;

typedef int  ( * S3DClient_MusicLoadCallback )              ( const char *_pBuffer, int _iBufferSize, void *_pUserData ) ;
typedef void ( * S3DClient_MusicUnloadCallback )            ( int _iMusicIndex, void *_pUserData ) ;
typedef int  ( * S3DClient_MusicPlayCallback )              ( int _iMusicIndex, float _fVolume, bool _bLoop, float _fPriority, void *_pUserData ) ;
typedef void ( * S3DClient_MusicPauseCallback )             ( int _iStreamIndex, void *_pUserData ) ;
typedef void ( * S3DClient_MusicResumeCallback )            ( int _iStreamIndex, void *_pUserData ) ;
typedef void ( * S3DClient_MusicStopCallback )              ( int _iStreamIndex, void *_pUserData ) ;
typedef void ( * S3DClient_MusicSetVolumeCallback )         ( int _iStreamIndex, float _fVolume, void *_pUserData ) ;

typedef void ( * S3DClient_VibrateCallback )                ( float _fMagnitude, void *_pUserData ) ;

typedef bool ( * S3DClient_EnableLocationCallback )         ( bool _bEnable, void *_pUserData ) ;
typedef bool ( * S3DClient_EnableHeadingCallback )          ( bool _bEnable, void *_pUserData ) ;

typedef bool ( * S3DClient_CameraDeviceCaptureStartCallback)( int _iDeviceIndex, int _iImageWidth, int _iImageHeight, int _iFrameRate, void *_pUserData ) ;
typedef void ( * S3DClient_CameraDeviceCaptureStopCallback) ( int _iDeviceIndex, void *_pUserData ) ;

typedef void ( * S3DClient_HomeButtonCallback )             ( void *_pUserData ) ;

typedef bool ( * S3DClient_DisplayBindCallback )            ( unsigned char _iDisplay, void *_pUserData ) ;


//-----------------------------------------------------------------------------
//  API
//-----------------------------------------------------------------------------

extern "C" bool             S3DClient_Init                                          ( const char * _pWorkPath ) ;
extern "C" void             S3DClient_LoadPack                                      ( const char *_pLoadingPackPath, const char *_pPackPath, const char *_pConfigURI   ) ;
extern "C" void             S3DClient_DisplayOption                                 ( const char * _pOptionPackPath ) ;
extern "C" void             S3DClient_DisplayAbout                                  ( const char * _pOptionPackPath ) ;
extern "C" bool             S3DClient_RunOneFrame			                        ( ) ;
extern "C" void             S3DClient_Pause                                         ( bool _bPause ) ;
extern "C" void             S3DClient_Stop                                          ( ) ;
extern "C" void             S3DClient_Shutdown                                      ( ) ;
extern "C" bool             S3DClient_Stopped                                       ( ) ;
extern "C" bool             S3DClient_IsFullScreen                                  ( ) ;
extern "C" bool             S3DClient_GetWakeLock                                   ( ) ;
                                                                                     
extern "C" void             S3DClient_SetClientType		                            ( int _iClientType  ) ;     // S3DClient_Type_StandAlone will manage HTTP session, S3DClient_Type_Embedded not
extern "C" void             S3DClient_SetGraphicContainer	                        ( void * _pGraphicContainer , int _iLeft, int _iTop, int _iRight, int _iBottom ) ; // _pGraphicContainer is handle of window
extern "C" void             S3DClient_SetInputContainer		                        ( void * _pInputContainer   , int _iLeft, int _iTop, int _iRight, int _iBottom ) ; // _pGraphicContainer is handle of _pInputContainer
extern "C" void             S3DClient_SetAudioDisabled                              ( ) ;
extern "C" void             S3DClient_SetOption                                     ( unsigned long _iOption, unsigned long  _iValue ) ;
extern "C" void             S3DClient_SetGameOption                                 ( unsigned long _iOption, float  _fValue ) ;
extern "C" void             S3DClient_SetFullscreen                                 ( bool _bFullscreen ) ;
extern "C" void             S3DClient_SetCachePath                                  ( const char * _pCachePath ) ;
extern "C" void             S3DClient_SetSavesPath                                  ( const char * _pSavesPath ) ;
extern "C" void             S3DClient_OnGraphicContextLost                          ( ) ;
                                                                                     
extern "C" void             S3DClient_CallHUDAction                                 ( const char * _pCommand, unsigned short _iArgumentCount, const char ** _pArguments ) ;
extern "C" void             S3DClient_SetOpenURLCallback                            ( S3DClient_OpenURLCallback _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_SetWakeUpConnectionCallback                   ( S3DClient_WakeUpConnectionCallback _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_SetLogCallbacks                               ( S3DClient_LogCallback _pLogMessageCallback, S3DClient_LogCallback _pLogWarningCallback, S3DClient_LogCallback _pLogErrorCallback ) ;
                                                                                     
extern "C" bool             S3DClient_InstallCurrentUserEventHook                   ( const char * _pAIModel, const char * _pHandler, void ( * _pCallback  	) ( unsigned char, const void *, void *  ), void *_pUserData ) ;
extern "C" void             S3DClient_UninstallCurrentUserEventHook                 ( const char * _pAIModel, const char * _pHandler ) ;
extern "C" bool             S3DClient_SendEventToCurrentUser                        ( const char * _pAIModel, const char * _pHandler, unsigned char _iArgumentCount, const void * _pArguments ) ;
extern "C" bool             S3DClient_FlushEvents                                   ( ) ;

extern "C" bool             S3DClient_RegisterFunction                              ( const char * _pName, S3DX::AIFunction *_pFunction ) ;
extern "C" void             S3DClient_UnregisterFunction                            ( const char * _pName ) ;
                                                                                     
extern "C" void             S3DClient_SetPlayOverlayMovieCallback                   ( S3DClient_PlayOverlayMovieCallback _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_SetStopOverlayMovieCallback                   ( S3DClient_StopOverlayMovieCallback _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_OnOverlayMovieStopped                         ( ) ;
                                                                 
extern "C" bool             S3DClient_IsVirtualKeyboardNeeded                       ( ) ;
extern "C" void             S3DClient_OnVirtualKeyboardTextChanged                  ( const char *_pText ) ;
extern "C" const char *     S3DClient_GetVirtualKeyboardText                        ( ) ;
extern "C" void             S3DClient_OnVirtualKeyboardValidate                     ( ) ;

extern "C" unsigned long    S3DClient_GetPixelMapHandle                             ( const char         *_pName   ) ;
extern "C" bool             S3DClient_LockPixelMap                                  ( const unsigned long _iHandle ) ;
extern "C" unsigned long    S3DClient_GetPixelMapWidth                              ( const unsigned long _iHandle ) ;
extern "C" unsigned long    S3DClient_GetPixelMapHeight                             ( const unsigned long _iHandle ) ;
extern "C" void		        S3DClient_GetPixelMapPixel                              ( const unsigned long _iHandle, unsigned int _iX, unsigned int _iY, unsigned char * _pRed, unsigned char * _pGreen, unsigned char * _pBlue, unsigned char * _pAlpha ) ;
extern "C" void  	        S3DClient_SetPixelMapPixel                              ( const unsigned long _iHandle, unsigned int _iX, unsigned int _iY, unsigned char   _iRed, unsigned char   _iGreen, unsigned char   _iBlue, unsigned char   _iAlpha ) ;
extern "C" void  	        S3DClient_SetPixelMapPixels                             ( const unsigned long _iHandle, unsigned char *_pPixelsRGBA8888 ) ;
extern "C" void             S3DClient_UnlockPixelMap                                ( const unsigned long _iHandle ) ;

//------- iPhone ---------//

extern "C" void             S3DClient_iPhone_SetViewportRotation                    ( int _iAngle ) ;
extern "C" int              S3DClient_iPhone_GetViewportRotation                    ( ) ;
extern "C" bool             S3DClient_iPhone_IsMultiTouchEnabled                    ( ) ;
extern "C" void             S3DClient_iPhone_OnMouseMoved                           ( float _x, float _y ) ;
extern "C" void             S3DClient_iPhone_OnMouseButtonPressed                   ( ) ;
extern "C" void             S3DClient_iPhone_OnMouseButtonReleased                  ( ) ;
extern "C" void             S3DClient_iPhone_OnDeviceMoved                          ( float _x, float _y, float _z ) ;
extern "C" void             S3DClient_iPhone_OnTouchesChanged                       ( int _iS0, int _iTC0, float _fX0, float _fY0, 
                                                                                      int _iS1, int _iTC1, float _fX1, float _fY1,
                                                                                      int _iS2, int _iTC2, float _fX2, float _fY2, 
                                                                                      int _iS3, int _iTC3, float _fX3, float _fY3, 
                                                                                      int _iS4, int _iTC4, float _fX4, float _fY4 ) ;
//------- Android ---------//

extern "C" void             S3DClient_Android_SetSoundDeviceUseExternalDriver       ( bool _bEnable ) ;
extern "C" void             S3DClient_Android_SetSoundDeviceInitializeCallback      ( S3DClient_SoundDeviceInitializeCallback _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundDeviceShutdownCallback        ( S3DClient_SoundDeviceShutdownCallback   _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundDeviceSuspendCallback         ( S3DClient_SoundDeviceSuspendCallback    _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundLoadCallback                  ( S3DClient_SoundLoadCallback             _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundUnloadCallback                ( S3DClient_SoundUnloadCallback           _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundPlayCallback                  ( S3DClient_SoundPlayCallback             _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundPauseCallback                 ( S3DClient_SoundPauseCallback            _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundResumeCallback                ( S3DClient_SoundResumeCallback           _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundStopCallback                  ( S3DClient_SoundStopCallback             _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundSetVolumeCallback             ( S3DClient_SoundSetVolumeCallback        _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundSetPitchCallback              ( S3DClient_SoundSetPitchCallback         _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetSoundSetLoopingCallback            ( S3DClient_SoundSetLoopingCallback       _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetMusicLoadCallback                  ( S3DClient_MusicLoadCallback             _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetMusicUnloadCallback                ( S3DClient_MusicUnloadCallback           _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetMusicPlayCallback                  ( S3DClient_MusicPlayCallback             _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetMusicPauseCallback                 ( S3DClient_MusicPauseCallback            _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetMusicResumeCallback                ( S3DClient_MusicResumeCallback           _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetMusicStopCallback                  ( S3DClient_MusicStopCallback             _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetMusicSetVolumeCallback             ( S3DClient_MusicSetVolumeCallback        _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_InitializeSoundDevice                 ( ) ;
extern "C" void             S3DClient_Android_ShutdownSoundDevice                   ( ) ;
extern "C" void             S3DClient_Android_SetVibrateCallback                    ( S3DClient_VibrateCallback               _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetEnableLocationCallback             ( S3DClient_EnableLocationCallback        _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetEnableHeadingCallback              ( S3DClient_EnableHeadingCallback         _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetLocationSupported                  ( bool _bSupported ) ;
extern "C" void             S3DClient_Android_SetHeadingSupported                   ( bool _bSupported ) ;
extern "C" void             S3DClient_Android_SetDeviceName                         ( const char *_pName ) ;
extern "C" void             S3DClient_Android_SetDeviceModel                        ( const char *_pModel ) ;
extern "C" void             S3DClient_Android_SetDeviceIPAddress                    ( const char *_pAddress ) ;
extern "C" void             S3DClient_Android_SetDeviceUUID                         ( const char *_pUUID ) ;
extern "C" void             S3DClient_Android_SetSystemVersion                      ( const char *_pVersion ) ;
extern "C" void             S3DClient_Android_SetSystemLanguage                     ( const char *_pLanguage ) ;
extern "C" void             S3DClient_Android_UpdateLocation                        ( float _fLatitude, float _fLongitude, float _fAltitude ) ;
extern "C" void             S3DClient_Android_UpdateHeading                         ( float _fMagnetic, float _fCorrected ) ;
extern "C" void             S3DClient_Android_OnKeyboardKeyPressed                  ( int _iKeyCode, int _iUCS4 ) ;
extern "C" void             S3DClient_Android_OnKeyboardKeyReleased                 ( int _iKeyCode, int _iUCS4 ) ;
extern "C" void             S3DClient_Android_SetCameraDeviceCaptureStartCallback   ( S3DClient_CameraDeviceCaptureStartCallback _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetCameraDeviceCaptureStopCallback    ( S3DClient_CameraDeviceCaptureStopCallback  _pCallback, void *_pUserData ) ;
extern "C" void             S3DClient_Android_SetCameraDeviceCapturedImage          ( int _iDeviceIndex, const void *_pImageData, int _iImageWidth, int _iImageHeight, int _iImageStride, int _iImageFormat ) ;
extern "C" void             S3DClient_Android_SetCameraDeviceCount                  ( int _iDeviceCount ) ;
extern "C" void             S3DClient_Android_SetCameraDeviceName                   ( int _iDeviceIndex, const char *_pName ) ;
extern "C" void             S3DClient_Android_AddFileAccessibleFromAPK              ( const char *_pFileName, int _iFileDescriptor, int _iOffset, int _iLength ) ;
extern "C" void             S3DClient_Android_SetJoypadType                         ( int _iJoypad, int _iType ) ;
extern "C" void             S3DClient_Android_SetJoypadButtonPressure               ( int _iJoypad, int _iButton, float _fPressure ) ;
extern "C" void             S3DClient_Android_SetJoypadStickAxis                    ( int _iJoypad, int _iStick,  float _fAxisX, float _fAxisY ) ;

//------- Wii ---------//

extern "C" void             S3DClient_Wii_SetHomeButtonCallback                     ( S3DClient_HomeButtonCallback _pCallback, void *_pUserData ) ;

//------- NaCl --------//

extern "C" void             S3DClient_NaCl_SetModuleInstance                        ( void *_pInstance ) ;
extern "C" void             S3DClient_NaCl_OnMouseMoved                             ( float _x, float _y ) ;
extern "C" void             S3DClient_NaCl_OnMouseButtonPressed                     ( int _iButton ) ;
extern "C" void             S3DClient_NaCl_OnMouseButtonReleased                    ( int _iButton ) ;
extern "C" void             S3DClient_NaCl_OnMouseWheel                             ( float _fDeltaX, float _fDeltaY ) ;
extern "C" void             S3DClient_NaCl_OnKeyboardKeyPressed                     ( int _iKeyCode, const char *_pUTF8 ) ;
extern "C" void             S3DClient_NaCl_OnKeyboardKeyReleased                    ( int _iKeyCode, const char *_pUTF8 ) ;
extern "C" void             S3DClient_NaCl_SetFocus                                 ( bool _bFocus ) ;

//------- Flash --------//

extern "C" void             S3DClient_Flash_OnMouseMoved                            ( float _x, float _y ) ;
extern "C" void             S3DClient_Flash_OnMouseButtonPressed                    ( int _iButton ) ;
extern "C" void             S3DClient_Flash_OnMouseButtonReleased                   ( int _iButton ) ;
extern "C" void             S3DClient_Flash_OnMouseWheel                            ( float _fDeltaX, float _fDeltaY ) ;
extern "C" void             S3DClient_Flash_OnKeyboardKeyPressed                    ( int _iKeyCode, const char *_pUTF8 ) ;
extern "C" void             S3DClient_Flash_OnKeyboardKeyReleased                   ( int _iKeyCode, const char *_pUTF8 ) ;
extern "C" void             S3DClient_Flash_SetFocus                                ( bool _bFocus ) ;

//-------- XNA ---------//

extern "C" void             S3DClient_XNA_SetViewportRotation                       ( int _iAngle ) ;
extern "C" bool             S3DClient_XNA_IsMultiTouchEnabled                       ( ) ;
extern "C" void             S3DClient_XNA_OnMouseMoved                              ( float _x, float _y ) ;
extern "C" void             S3DClient_XNA_OnMouseButtonPressed                      ( int _iButton ) ;
extern "C" void             S3DClient_XNA_OnMouseButtonReleased                     ( int _iButton ) ;
extern "C" void             S3DClient_XNA_OnKeyboardKeyPressed                      ( int _iKeyCode, const char *_pUTF8 ) ;
extern "C" void             S3DClient_XNA_OnKeyboardKeyReleased                     ( int _iKeyCode, const char *_pUTF8 ) ;
extern "C" void             S3DClient_XNA_OnMouseWheel                              ( float _fDeltaX, float _fDeltaY ) ;
extern "C" void             S3DClient_XNA_OnDeviceMoved                             ( float _x, float _y, float _z ) ;
extern "C" void             S3DClient_XNA_OnTouchesChanged                          ( int _iS0, int _iTC0, float _fX0, float _fY0, 
                                                                                      int _iS1, int _iTC1, float _fX1, float _fY1,
                                                                                      int _iS2, int _iTC2, float _fX2, float _fY2, 
                                                                                      int _iS3, int _iTC3, float _fX3, float _fY3, 
                                                                                      int _iS4, int _iTC4, float _fX4, float _fY4 ) ;

//-----------------------------------------------------------------------------
// Experimental (do not use)
//-----------------------------------------------------------------------------

extern "C" void             S3DClient_SetDisplayBindCallback                        ( S3DClient_DisplayBindCallback _pCallback, void *_pUserData ) ;

//-----------------------------------------------------------------------------
#endif // __S3DClient_Wrapper_h__
//-----------------------------------------------------------------------------
