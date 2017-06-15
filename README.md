
Media for Mobile
================

Overview
--------

Media for Mobile is a set of easy to use components and API for a wide range of media scenarios such as video editing and capturing. It contains several complete pipelines for most popular use cases and provides a possibility to add user-developed components to those pipelines.

Media for Mobile is now available as an open source project on GitHub.

System Requirements
-------------------

- Operating Systems: Microsoft Windows, Apple OS X or Linux
- IDE: Eclipse, Google Android Studio or JetBrains IntelliJ IDEA 
- Android Platform SDK 
- Java Development Kit (JDK) 6 or later
- Supported Android versions: Jelly Bean 4.3 or higher

How to build the library
------------------------------------

Run "gradle build" command to build the Media for Mobile library and the samples.

To use the Media for Mobile components in your project, add dependencies on "android", "domain" and "effects" M4M folders to your gradle build scripts.



**Jitpack.io Integration**

Mobile 4 Media uses JitPack to provide ready-to-use artifacts of any branch or commit.

1. Add the JitPack repository to your project's root build.gradle:
```
 allprojects {
     repositories {
         jcenter()
         maven { url "https://jitpack.io" }
     }
 }
```

 2. Add the M4M dependency to your module-level build.gradle:
```
 dependencies {
     compile 'com.github.indexOS.media-for-mobile:android:master-SNAPSHOT'
 }
 ```

You can instead point to any commit hash permanently using:
```
 dependencies {
     compile 'com.github.indexOS.media-for-mobile:android:fd9f7c56cfab63eee2ac3ea5a8b222a54cb7f9fc'
 }
 ```


List of Media for Mobile samples
---------------------------------

Video processing samples
- Transcode video 
- Join Video 
- Cut Video 
- Video Effect 
- Audio Effect 
- Get Media File Info 
- Time Scaling

Capturing samples 
- Game Capturing 
- Camera Capturing 
                                                                                                                                            
Legal Information
-----------------

Media for Mobile is distributed under Apache License 2.0, see LICENSE.txt and NOTICE.txt files in the root folder for details.