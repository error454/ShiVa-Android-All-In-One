# ShiVa-Android-All-In-One
An Android project for the ShiVa game engine that aims on integrating various Android SDKs.  Additionally, various fixes and enhancements are made to the UAT generated code as needed.

The following SDKs are integrated:

* Dropbox
* Google Cloud Messaging 

The following enhancements and bugfixes are integrated:

* Default orientation is Landscape
* Splash screen is centered and aspect ratio corrected
* Better handling of screen off/on events where a lock screen isn't enabled or hasn't hit the timeout threshold
* S3DClient_RegisterFunction registers all native functions instead of just the 1st

## How this repository is organized
**android** contains the eclipse project along with a handful of libraries (GCM and Dropbox for now)

**shiva** contains a sample ShiVa project that is meant to illustrate how to use each SDK.  This project has AI Models that are specific to each SDK and will be required in your project.  You'll want to grab these AI Models for use in your ShiVa app and poke around the simple sample app for an example of how to wire things.

## How to get started using AAIO
###1. Acquire the Source
The first step is to get the source files, you can do this by forking or cloning the repository with a git client or you can download the repository as a zip above.

### 2. Rename the Project
The 2nd step is to rename the project.  To do so I have provided a script that you need to modify, android/configure.sh which handles renaming all of the source files and build scripts for you.

### 3 . Import Into Eclipse
You can now import the project into eclipse and build.

## How to Enable/Configure SDKs in Eclipse
This project tries to take a familiar approach for enabling the various SDKs in eclipse.  By default, everything is disabled.  Enabling an SDK is meant to be a two-step process.

#### 1. Editing the Manifest
The first step is to edit the AndroidManifest.xml, search for the headers you are interested in and uncomment the section.  For instance, Dropbox is sandwiched like so:

    <!--Begin Dropbox -->
    <!--
    ...
    -->
    <!--End Dropbox -->
To enable it, you would remove the comment brackets:

    <!--Begin Dropbox -->
    ...
    <!--End Dropbox -->

Note that some SDKs may have more than 1 section in the manifest.  Any additional integration requirement above and beyond this step is called out in detail for each SDK below.

#### 2. Edit ProjectSettings.java
This is the master configuration file where you turn on various SDKs and set their API keys and secrets.  For instance, if you want to use Dropbox, set the appropriate boolean and settings here:

    public static final boolean UseDropboxAPI = true;
    public static final String DROPBOX_KEY = "your key";
    public static final String DROPBOX_SECRET = "your secret";

## Dropbox
Dropbox integration relies on the official Dropbox app being installed.  The official app is leveraged to perform authentication.  Using this API, you can copy files to/from Dropbox.  You will first need to configure a Dropbox application on the Dropbox website to get the required key and secret.

#### Integration Requirements

1. In the Android Manifest, search for the entry below and enter your dropbox app key i.e. "db-123456":

    `<data android:scheme="db-INSERT-APP-KEY-HERE" />`

#### ShiVa Usage
Refer to the DropBoxAI model in the ShiVa project for an example of how to use.  The following functionality is included:

* Login
* Logout
* Write a string to a file in the Dropbox app folder
* Copy a file from the Dropbox app folder to local cache

## Google Cloud Messaging
This allows you to receive push notifications from Google's cloud messaging service.  Note that you will need to have a server somewhere that is generating these messages.  Special note that GCM_PROJECT_ID can be found from your project URL on the google site, it is also called Sender ID.

#### Integration Requirements 
1. [Follow the instructions](http://developer.android.com/guide/google/gcm/gs.html) for Creating a new service, enabling the GCM service and getting an API key

#### ShiVa Usage
Refer to the GCMAI model in the ShiVa project for an example of how to use.  The following functionality is included:

* Notification when registered/unregistered
* Notification when message is received
* Notification on error

#### Message Format
By default, the implementation assumes that the JSON data from GCM will look like this:

`"data": {"command": "blah"}`

The message received notification piped into ShiVa forwards on  the contents of the "command" extra.  Obviously you may want to change this, you'll find where to do so in GCMIntentService.java in the onMessage override.

## What's Missing

The obj/* folder is not included.  This is what contains the S3DClient which is specific to the processor you've chosen along with a couple other smaller libraries.  I'd recommend that you export an Android project with the processor settings you want and grab the obj folder from there.  Until you do this, you won't be able to compile.

## Note on original code
The code that this project is based on was written by Stonetrip and was produced by their Unified Authoring Tool.

## How to Contribute
There are two primary ways to contribute to this project.

1. File Bugs.  To file a bug, click on the Issues tab above and create a new issue.  This could be device-specific problems "XYZ doesn't work on Galaxy S", it could be an enhancement request "Service XYZ isn't integrated".  Etc.

2. Contribute an SDK implementation.  If you are interested in contributing an SDK implementation, please follow these guidlines:
    * Fork this project
    * Create your own branch for developing your new feature/fix on (don't make changes in any of my branches)
    * Submit a pull request to me when finished

Coding guidlines:

* Spaces instead of tabs, tabs == 4 spaces
* Keep new code out of the main Java implementation (AAIOMain.java).  If you need to tap lifecycle events, use AAIO.java (AAIOMain extends this).
* If you see any way of making any of this cleaner, please let me know
