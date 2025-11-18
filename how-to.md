# OrganizeMe Batch Scripts

This document contains the batch files needed to run the OrganizeMe application and server.

---

### Bat file code for running 
## "OrganizeMe app"
```batch
@echo off
REM Change directory to the parent folder of the script
cd /d "%~dp0"

REM Define relative path to JavaFX SDK
set FX_SDK_PATH=lib/javafx-sdk-25/lib

REM Change console name
title OrganizeMe console

REM Run the JavaFX JAR with relative paths
java --enable-native-access=javafx.graphics --module-path "%FX_SDK_PATH%" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeJavaFX.jar
```

---

### Bat file code for running 
## "OrganizeMe server"
```batch
@echo off
REM Change directory to the parent folder of the script
cd /d "%~dp0"

REM Name the terminal window
title OrganizeMe Server Console

REM Define relative path to JavaFX SDK
set FX_SDK_PATH=lib/javafx-sdk-25/lib

REM Run the JavaFX JAR with relative paths
java --enable-native-access=javafx.graphics --module-path "%FX_SDK_PATH%" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeServer.jar

pause
```

---

## Setup Instructions

1. Ensure JavaFX SDK 25 is located in `lib/javafx-sdk-25/lib`
2. Place your JAR files in the `src` directory:
   - `OrganizeMeServer.jar` for the server
   - `OrganizeMeJavaFX.jar` for the application
3. Run the appropriate batch file to start the server or application
   - create new `Run.txt` file
   - copy-paste code: [OrganizeMe app](#organizeme-app)
   - save as `Run.bat` file
   - create new `Server.txt` file
   - copy-paste code: [OrganizeMe Server](#organizeme-server)
   - save as `Server.bat` file

## Requirements

- Java Runtime Environment (JRE) with JavaFX support
- JavaFX SDK 25
- OrganizeMe JAR files

---

## Breakdown of the Script

• `@echo off`: Hides the commands from being displayed in the command prompt.

• `cd /d "%~dp0"`: Changes the current directory to where the batch file is located, ensuring portability. ○ `set FX_SDK_PATH=lib/javafx-sdk-25/lib`: Sets a variable for the path to your JavaFX libraries.

• `title OrganizeMe console`: This changes the console title to "OrganizeMe console"

• `start "OrganizeMe Server" java ...`: This is the key line. ○ `start "OrganizeMe Server"`: Launches a new command prompt window with the title "OrganizeMe Server". This gives your server its own dedicated console, making it easy to identify and manage. ○ `java ...`: The rest of the command launches your Java application.
