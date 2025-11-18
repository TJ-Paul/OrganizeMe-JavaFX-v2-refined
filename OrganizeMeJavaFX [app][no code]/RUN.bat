@echo off
REM Change directory to the parent folder of the script
cd /d "%~dp0"

REM Define relative path to JavaFX SDK
set FX_SDK_PATH=lib/javafx-sdk-25/lib

REM Change console name
title OrganizeMe console

REM Run the JavaFX JAR with relative paths
java --enable-native-access=javafx.graphics --module-path "%FX_SDK_PATH%" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeJavaFX.jar