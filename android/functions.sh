#!/bin/bash
function sanityCheck {
    if [ '$newPackage' == "default" -o '$newName' == "default" -o '$newFriendlyName' == "default" -o '$newNDKPath' == "default" ] 
    then
        echo "You need to configure this script by editing it first"
        exit 1
    fi
}

function renameFiles {    
    #Replace . with /
    oldSlashPackage=$(echo $oldPackage | sed "s/\./\//g")
    newSlashPackage=$(echo $newPackage | sed "s/\./\//g")
    
    #Replace . with _
    oldUnderScorePackage=$(echo $oldPackage | sed "s/\./\_/g")
    newUnderScorePackage=$(echo $newPackage | sed "s/\./\_/g")
    
    echo Renaming project from $oldName to $newName in strings.xml
    find -type f -iname "*strings.xml" -exec sed -i s/"$oldFriendlyName"/"$newFriendlyName"/g {} \;
    
    echo Renaming project $oldName to $newName in *.java *.cpp
    find -type f -iname "*.java" -exec sed -i 's/'$oldName'/'$newName'/g' {} \;
    find -type f -iname "*.cpp" -exec sed -i 's/'$oldName'/'$newName'/g' {} \;
    
    echo Renaming manifest package $oldPackage to $newPackage in AndroidManifest.xml
    find -type f -iname "*AndroidManifest.xml" -exec sed -i 's/'$oldPackage'/'$newPackage'/g' {} \;
    
    echo Renaming main activity in AndroidManifest.xml
    find -type f -iname "*AndroidManifest.xml" -exec sed -i 's/'$oldName'/'$newName'/g' {} \;
    
    echo Renaming package $oldPackage to $newPackage in *.java
    find -type f -iname "*.java" -exec sed -i 's/'$oldPackage'/'$newPackage'/g' {} \;
    
    echo Renaming package reference $oldSlashPackage to $newSlashPackage in *.cpp
    find -type f -iname "*.cpp" -exec sed -i 's@'$oldSlashPackage'@'$newSlashPackage'@g' {} \;
    
    echo Renaming package reference $oldUnderScorePackage to $newUnderScorePackage in *.cpp
    find -type f -iname "*.cpp" -exec sed -i 's@'$oldUnderScorePackage'@'$newUnderScorePackage'@g' {} \;
    
    echo Updating build.xml with new NDK path
    find -type f -iname "build.xml" -exec sed -i 's@'$oldNDKPath'@'$newNDKPath'@g' {} \;
    
    echo Renaming main class from $oldName.java to $newName.java
    mv src/$oldSlashPackage/$oldName.java src/$oldSlashPackage/$newName.java
    
    echo Renaming build.xml project name $oldProjectName to $newFriendlyName
    find -type f -iname "build.xml" -exec sed -i s@"$oldProjectName"@"$newFriendlyName"@g {} \;
    
    echo Moving folder $oldSlashPackage to $newSlashPackage
    mkdir -p src/$newSlashPackage
    mv src/$oldSlashPackage/* src/$newSlashPackage
    rm -rf src/$oldSlashPackage
}