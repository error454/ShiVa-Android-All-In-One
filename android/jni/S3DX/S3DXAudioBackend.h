//-----------------------------------------------------------------------------
#ifndef __S3DXAudioBackend_h__
#define __S3DXAudioBackend_h__
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
namespace S3DX
//-----------------------------------------------------------------------------
{
    class AudioBackend
    {
    public :

        //---------------------------------------------------------------------
        //  Virtual destructor
        //
        virtual        ~AudioBackend                ( ) { }

        //---------------------------------------------------------------------
        //  Callbacks
        //
        typedef int ( * StreamReadCallback )        ( void *_pStream, const char *_pBuffer, int _iBufferSize ) ;
        typedef int ( * StreamSeekCallback )        ( void *_pStream, int _iOffset, int _iWhence ) ;
        typedef int ( * StreamTellCallback )        ( void *_pStream ) ;

        //---------------------------------------------------------------------
        //  Methods
        //
        virtual const char *GetName                     ( ) = 0 ;

        virtual bool        OnInitialize                ( ) = 0 ;
        virtual bool        OnUpdate                    ( ) = 0 ;
        virtual void        OnShutdown                  ( ) = 0 ;
        virtual void        OnSuspend                   ( bool _bSuspend ) = 0 ;
        virtual void        OnSetMasterVolume           ( float _fVolume ) = 0 ;

        virtual int         OnSampleLoad                ( const char *_pName, const char *_pBuffer, int _iBufferSize ) = 0 ;
        virtual void        OnSampleUnload              ( int _iSampleID ) = 0 ;
        virtual float       OnSampleGetDuration         ( int _iSampleID ) = 0 ;
        virtual int         OnSamplePlay                ( int _iSampleID, float _fVolume, bool _bLoop, float _fPriority, float _fStartProgress, float _x, float _y, float _z, float _fRolloffFactor, float _fReferenceDistance ) = 0 ;

        virtual int         OnStreamOpen                ( const char *_pName, void *_pStream, StreamReadCallback _pReadCallback, StreamSeekCallback _pSeekCallback, StreamTellCallback _pTellCallback ) = 0 ;
        virtual void        OnStreamClose               ( int _iStreamID ) = 0 ;
        virtual int         OnStreamPlay                ( int _iStreamID, float _fVolume, bool _bLoop, float _fPriority ) = 0 ;

        virtual void        OnSourcePause               ( int _iSourceID ) = 0 ;
        virtual void        OnSourceResume              ( int _iSourceID ) = 0 ;
        virtual void        OnSourceStop                ( int _iSourceID ) = 0 ;
        virtual void        OnSourceSetPosition         ( int _iSourceID, float _x, float _y, float _z ) = 0 ;
        virtual void        OnSourceSetVolume           ( int _iSourceID, float _fVolume ) = 0 ;
        virtual void        OnSourceSetPitch            ( int _iSourceID, float _fPitch  ) = 0 ;
        virtual void        OnSourceSetLooping          ( int _iSourceID, bool _bLooping ) = 0 ;
        virtual void        OnSourceSetRolloffFactor    ( int _iSourceID, float _fFactor ) = 0 ;
        virtual bool        OnSourceIsPlaying           ( int _iSourceID ) = 0 ;
        virtual bool        OnSourceIsPaused            ( int _iSourceID ) = 0 ;
        virtual void        OnSourceSetProgress         ( int _iSourceID, float _fProgress ) = 0 ;
        virtual float       OnSourceGetProgress         ( int _iSourceID ) = 0 ;

        virtual void        OnListenerSetPosition       ( float _x, float _y, float _z ) = 0 ;
        virtual void        OnListenerSetOrientation    ( float _tox, float _toy, float _toz, float _upx, float _upy, float _upz ) = 0 ;

    } ;
}
//-----------------------------------------------------------------------------


//-----------------------------------------------------------------------------
#endif
//-----------------------------------------------------------------------------
