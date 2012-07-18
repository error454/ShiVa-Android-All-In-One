ShiVa-Android-All-In-One
========================
An Android project for the ShiVa game engine that aims on integrating various Android SDKs.  

Dropbox
-------
The dropbox API implementation provides 3 system calls to ShiVa:

**onDropBoxLogin( )**

    system.callClientFunction ( "onDropBoxLogin" )
    
**onDropBoxLogout( )**

    system.callClientFunction ( "onDropBoxLogout" )
     
**onDropBoxPutFileOverwrite( file, content )**

    system.callClientFunction ( "onDropBoxPutFileOverwrite", "folder/myfile.txt", "Overwrite the file contents with this!" )
        
The implementation also expects you to have a userAI named DropBoxAI to allow for callbacks:

**onPutFileOverwriteResult**

    --------------------------------------------------------------------------------
    function DropBoxAI.onPutFileOverwriteResult ( filename, bytesWritten )
    --------------------------------------------------------------------------------
        
        log.message ( "File: " .. filename .. " was written to with : " .. bytesWritten .. " bytes" )
        
    --------------------------------------------------------------------------------
    end
    --------------------------------------------------------------------------------

**onGetFileResult**

    --------------------------------------------------------------------------------
    function DropBoxAI.onGetFileResult ( filename )
    --------------------------------------------------------------------------------
        
        log.message ( "The path to the file is: " .. filename )
        
    --------------------------------------------------------------------------------
    end
    --------------------------------------------------------------------------------
  

What's Missing
--------------
The obj/* folder is not included.  This is what contains the S3DClient which is specific to the processor you've chosen along with a couple other smaller libraries.  I'd recommend that you export an Android project with the processor settings you want and grab the obj folder from there.

Note on original code
---------------------
The code that this project is based on was written by Stonetrip and was produced by their Unified Authoring Tool.