# voidtool

### Very Omnipotent iOS Device Tool

## What it is?

App itself is wrapper around binaries of [libimobiledevice](https://github.com/libimobiledevice). Written in Kotlin.

## Features

* Collecting, filtering and saving logs. Reads syslog from iOS devices.
* Enabling Developer Options. Mounts developer image of iOS.
* Making screenshots. Saves tiff file to working directory. Depends on developer image.

## Build

`.\gradlew shadowJar`

## Dependencies

* JDK 8
* Compiled libimobiledevice binaries
* iOS Developer Images - can be found inside of Xcode.app `/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/DeviceSupport`

