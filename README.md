ShiVa-Android-All-In-One
========================
An Android project for the ShiVa game engine that aims on integrating various Android SDKs.  

#Dropbox

##Integration Requirements

1. In the Android Manifest, search for the entry below and enter your dropbox app key i.e. "db-123456":


    `<!-- Change this to be db- followed by your Dropbox app key -->`<br>
    `<data android:scheme="db-APP-KEY-GOES-HERE" />`
2. In ProjectSettings.java, set **UseDropboxAPI** to true
3. In ProjectSettings.java, set **DROPBOX_KEY** and **DROPBOX_SECRET** appropriately

##Using from ShiVa
The following calls are available from ShiVa

###Login to dropbox (requires the dropbox app to be installed)
    system.callClientFunction ( "onDropBoxLogin" )

###Logout of dropbox
    system.callClientFunction ( "onDropBoxLogout" )

###Write a string to a file, the file path must be a valid path in your dropbox app folder
    system.callClientFunction ( "onDropBoxPutFileOverwrite", "folder/myfile.txt", "Overwrite the file contents with this!" )

###Copy a file from the dropbox app folder to the local cache folder
    system.callClientFunction ( "onDropBoxGetFile" )

##Required Callbacks
To receive status from onDropBoxPutFileOverwrite and onDropBoxGetFile, the following callbacks must be implemented in an AI named DropBoxAI

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
    function DropBoxAI.onGetFileResult ( path )
    --------------------------------------------------------------------------------
        
        log.message ( "The path to the file is: " .. path )
        
    --------------------------------------------------------------------------------
    end
    --------------------------------------------------------------------------------
  

What's Missing
--------------
The obj/* folder is not included.  This is what contains the S3DClient which is specific to the processor you've chosen along with a couple other smaller libraries.  I'd recommend that you export an Android project with the processor settings you want and grab the obj folder from there.

Note on original code
---------------------
The code that this project is based on was written by Stonetrip and was produced by their Unified Authoring Tool.