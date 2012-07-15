#!/bin/bash

#####REQUIRED VALUES#####

#The name of your package. 
#Example: com.atari.frogger
newPackage="default"

#The name of your project class, this cannot contain spaces.
#Example: froggerClass
newName="default"

#The title of your project, this can contain spaces and is what is displayed on the Android home screen.
#Example: Frogger Extreme"
newFriendlyName="default"

#The absolute path that contains the ndk-build command from the Android NDK
#Example: "/cygdrive/C/sdks/android-ndk-r7/"
newNDKPath="default"

#####END REQUIRED VALUES#####

#####REQUIRED ONLY IF YOU ARE RENAMING#####
#These are the default names of the git repo, you do not 
#need to change these unless you are renaming your project
#for a second time.
oldPackage="com.test.test"
oldName="boxParticleLighting"
oldFriendlyName="boxParticleLighting"
oldNDKPath="/cygdrive/C/sdks/android-ndk-r7/"
#####DO NOT EDIT BELOW THIS LINE#####
source functions.sh

sanityCheck
renameFiles
