# voidtool

### Very Omnipotent iOS Device Tool

## What it is?

App is a tool that aids in development and quality assurance processes for iOS applications.

## Features

* Collecting, filtering and saving logs. Reads syslog from iOS devices.
* Enabling Developer Options. Mounts developer image of iOS.
* Making screenshots. Saves tiff file to working directory. Depends on developer image.

## Build

### Pre-requisites

`sudo apt-get install git openjdk-8-jdk openjfx` -- for Linux 

`brew tap AdoptOpenJDK/openjdk` and

`brew install adoptopenjdk-openjdk8` -- for macOS

Visit this [link](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) to download Windows binaries of JDK8, don't forget to add it to [PATH](https://java.com/en/download/help/path.xml)

### Build jar

* `git clone ` repo

* `./gradlew shadowJar` on macOS/Linux terminal or `.\gradlew shadowJar` on Windows command prompt

## Run

Just double click on jar file or run it from command line `java -jar voidtool.jar`

### Troubleshooting

1. Be sure that binaries are in PATH or in same directory as *.jar file
2. Run binary from command line to be sure that it's working properly

## Documentation

To be done

## Code style

Default code style of IntelliJ IDEA for Kotlin projects

## Dependencies

* JDK 8
* Compiled libimobiledevice binaries
* iOS Developer Images - can be found inside of Xcode.app `/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/DeviceSupport` -- should be copied inside `dev_image` directory alongside of jar file.

## License

See LICENSE file
