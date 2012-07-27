ShiVa-Android-All-In-One
========================
An Android project for the ShiVa game engine that aims on integrating various Android SDKs.  

#Dropbox

##Integration Requirements

1. In the Android Manifest, search for the entry below and enter your dropbox app key i.e. "db-123456":

    `<!-- Change this to be db- followed by your Dropbox app key -->`<br>
    `<data android:scheme="db-APP-KEY-GOES-HERE" />`
2. In the Android Manifest, search for all instances of the below comment:

    `<!--Begin Dropbox --`
    
    Add a right arrow to the end of the comment to enable Dropbox:
    `<!--Begin Dropbox -->`
    
3. In ProjectSettings.java, set **UseDropboxAPI** to true
4. In ProjectSettings.java, set **DROPBOX_KEY** and **DROPBOX_SECRET** appropriately

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

#Google Cloud Messaging

##Integration Requirements 
1. [Follow the instructions](http://developer.android.com/guide/google/gcm/gs.html) for Creating a new service, enabling the GCM service and getting an API key
2. In the Android Manifest, search for all instances of the below comment:

    `<!--Begin Google Cloud Messaging --`
    
    Add a right arrow to the end of the comment to enable Dropbox:
    `<!--Begin Google Cloud Messaging -->`
3. In ProjectSettings.java, set UseGoogleCloudMessaging to true
4. In ProjectSettings.java, set the GCM_PROJECT_ID, you get this from your project URL on the google site, it is also called Sender ID.
5. In ProjectSettings.java, set the GCM_API_KEY with your google service API key

##Required Callbacks
To receive status and messages, the following callbacks must be implemented in an AI named GCMAI

**onRegistered**

    --------------------------------------------------------------------------------
    function GCMAI.onRegistered ( id )
    --------------------------------------------------------------------------------
        
        log.message ( "GCM successfully registered, your device id is: " .. id )
        
    --------------------------------------------------------------------------------
    end
    --------------------------------------------------------------------------------
    
**onUnregistered**

    --------------------------------------------------------------------------------
    function GCMAI.onUnregistered ( id )
    --------------------------------------------------------------------------------
        
        log.message ( "GCM successfully unregistered device id: " .. id )
        
    --------------------------------------------------------------------------------
    end
    --------------------------------------------------------------------------------

**onMessageReceived**

    --------------------------------------------------------------------------------
    function GCMAI.onMessageReceived ( msg )
    --------------------------------------------------------------------------------
        
        log.message ( "GCM Message Received: " .. msg )
        
    --------------------------------------------------------------------------------
    end
    --------------------------------------------------------------------------------

#Scoreloop

##Integration Requirements 
1. blah
2. In the Android Manifest, search for all instances of the below comment:

    `<!-- Begin Scoreloop --`
    
    Add a right arrow to the end of the comment to enable Dropbox:
    `<!-- Begin Scoreloop -->`
3. bleh
4. more

##Using from ShiVa
The following calls are available from ShiVa

###Show the Scoreloop UI
####args: true to open the main screen, false to open achievements
    system.callClientFunction ( "onScoreloopShowUI", true )

###Award an achievement
####args: The award postfix as defined in scoreloop (com.test.jedimaster = jedimaster)
####returns: You will receive a callback in achievementAI onShowAchievement if awarded
    system.callClientFunction ( "onScoreloopAwardAchievement", "jedimaster" )

###Increment an achievement
####args: The award postfix as defined in scoreloop (com.test.jedimaster = jedimaster)
####returns: You will receive a callback in achievementAI onShowAchievement if awarded
    system.callClientFunction ( "onScoreloopIncrementAchievement", "jedimaster" )

###Increment tiered achievement
####Description: Takes a list of awards and treats them as a tiered achievement.  For instance if you wanted achievements for winning 5, 10 and 15 times you could pass win5, win10, win15.  The function checks if the first award is achieved, if not, it increments it, if so it increments the next achievement and alerts you.
####args: The award postfix as defined in scoreloop (com.test.jedimaster = jedimaster)
####returns: You will receive a callback in achievementAI onShowAchievement if awarded
    system.callClientFunction ( "onScoreloopIncrementTieredAchievement", "win5", "win10", "win15" )
    
##Required Callbacks
To receive status and messages, the following callback must be implemented in an AI named achievementAI

**onShowAchievement**

    --------------------------------------------------------------------------------
    function achievementAI.onShowAchievement ( achievement )
    --------------------------------------------------------------------------------
        
        log.message ( "Achievement " .. achievement .. " was awarded." )
        
    --------------------------------------------------------------------------------
    end
    --------------------------------------------------------------------------------
    
#What's Missing

The obj/* folder is not included.  This is what contains the S3DClient which is specific to the processor you've chosen along with a couple other smaller libraries.  I'd recommend that you export an Android project with the processor settings you want and grab the obj folder from there.

#Note on original code
The code that this project is based on was written by Stonetrip and was produced by their Unified Authoring Tool.