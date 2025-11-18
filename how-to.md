# OrganizeMe Setup Documentation

Complete setup and execution guide for the OrganizeMe JavaFX application and server.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Setup Instructions](#setup-instructions)
4. [Running the Application](#running-the-application)
5. [Running the Server](#running-the-server)
6. [VSCode Configuration](#vscode-configuration)
7. [Troubleshooting](#troubleshooting)

---

## Prerequisites

Before setting up OrganizeMe, ensure you have:

- **Java Runtime Environment (JRE)** with JavaFX support
- **JavaFX SDK 25** ([Download here](https://gluonhq.com/products/javafx/))
- **OrganizeMe JAR files**:
  - `OrganizeMeJavaFX.jar` (Application)
  - `OrganizeMeServer.jar` (Server)
- **Visual Studio Code** (optional, for development)

---

## Project Structure

Your project directory should be organized as follows:

```
OrganizeMe/
├── lib/
│   └── javafx-sdk-25/
│       └── lib/
│           ├── javafx.base.jar
│           ├── javafx.controls.jar
│           ├── javafx.fxml.jar
│           └── ... (other JavaFX libraries)
├── src/
│   ├── OrganizeMeJavaFX.jar
│   └── OrganizeMeServer.jar
├── .vscode/
│   └── launch.json
├── Run.bat
└── Server.bat
```

---

## Setup Instructions

### Step 1: Install JavaFX SDK

1. Download JavaFX SDK 25 from [Gluon](https://gluonhq.com/products/javafx/)
2. Extract the downloaded archive
3. Place the entire `javafx-sdk-25` folder into your project's `lib` directory
4. Verify the path: `lib/javafx-sdk-25/lib` should contain all JavaFX JAR files

### Step 2: Place JAR Files

1. Ensure the `src` directory exists in your project root
2. Copy the following JAR files to the `src` directory:
   - `OrganizeMeJavaFX.jar`
   - `OrganizeMeServer.jar`

### Step 3: Create Batch Files

#### Create `Run.bat` for Application

1. Create a new file named `Run.bat` in your project root
2. Copy and paste the following code:

```batch
@echo off
REM Change directory to the parent folder of the script
cd /d "%~dp0"

REM Define relative path to JavaFX SDK
set FX_SDK_PATH=lib/javafx-sdk-25/lib

REM Change console name
title OrganizeMe Console

REM Run the JavaFX JAR with relative paths
java --enable-native-access=javafx.graphics --module-path "%FX_SDK_PATH%" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeJavaFX.jar
```

3. Save the file

#### Create `Server.bat` for Server

1. Create a new file named `Server.bat` in your project root
2. Copy and paste the following code:

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

3. Save the file

---

## Running the Application

### Using Batch File (Windows)

1. Double-click `Run.bat` in your project root directory
2. The application will launch in a new console window titled "OrganizeMe Console"
3. The console will display any application output or errors

### Using Command Line

Navigate to your project directory and run:

```bash
java --enable-native-access=javafx.graphics --module-path "lib/javafx-sdk-25/lib" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeJavaFX.jar
```

---

## Running the Server

### Using Batch File (Windows)

1. Double-click `Server.bat` in your project root directory
2. The server will start in a new console window titled "OrganizeMe Server Console"
3. The console will remain open after execution (due to `pause` command)
4. Press any key to close the server console

### Using Command Line

Navigate to your project directory and run:

```bash
java --enable-native-access=javafx.graphics --module-path "lib/javafx-sdk-25/lib" --add-modules javafx.controls,javafx.fxml -jar src/OrganizeMeServer.jar
```

---

## VSCode Configuration

To run the OrganizeMe application directly from Visual Studio Code, you need to configure the `launch.json` file with the appropriate VM arguments.

### Setup Instructions

1. Create a `.vscode` folder in your project root (if it doesn't exist)
2. Inside the `.vscode` folder, create a file named `launch.json`
3. Copy and paste the following configuration:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "OrganizeMe App",
            "request": "launch",
            "mainClass": "App",
            "projectName": "OrganizeMeJavaFX_5ff4a90a",
            "vmArgs": "--module-path lib/javafx-sdk-25/lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics"
        }
    ]
}
```

4. Save the file

### Running from VSCode

1. Open your project in Visual Studio Code
2. Press `F5` or click **Run > Start Debugging**
3. Alternatively, use the **Run and Debug** panel (Ctrl+Shift+D)
4. Select "OrganizeMe App" from the dropdown
5. Click the green play button

### Important Notes

- Ensure the `mainClass` matches your application's main class name
- Update `projectName` if your project has a different identifier
- The `vmArgs` parameter must point to the correct JavaFX SDK path

---

## Troubleshooting

### Common Issues

#### Issue: "Module not found" Error

**Solution:** Verify that JavaFX SDK is correctly placed in `lib/javafx-sdk-25/lib`

#### Issue: "JAR file not found" Error

**Solution:** Ensure JAR files are in the `src` directory with correct names:
- `OrganizeMeJavaFX.jar`
- `OrganizeMeServer.jar`

#### Issue: Application Window Doesn't Appear

**Solution:** 
- Check console output for errors
- Ensure Java version is compatible with JavaFX 25
- Verify all JavaFX modules are present in the SDK folder

#### Issue: Server Won't Start

**Solution:**
- Check if the port is already in use
- Review server logs in the console window
- Ensure both JAR files are not corrupt

### Getting Help

If you encounter issues not covered here:

1. Check the console output for detailed error messages
2. Verify all paths and filenames match exactly
3. Ensure Java and JavaFX versions are compatible
4. Review the JavaFX documentation at [openjfx.io](https://openjfx.io/)

---

## Script Breakdown

### Batch File Commands Explained

- `@echo off`: Suppresses command echoing in the console
- `cd /d "%~dp0"`: Changes directory to the batch file's location (ensures portability)
- `set FX_SDK_PATH=lib/javafx-sdk-25/lib`: Defines a variable for the JavaFX library path
- `title [Name]`: Sets the console window title for easy identification
- `--enable-native-access=javafx.graphics`: Grants native access permissions to JavaFX graphics
- `--module-path`: Specifies the location of JavaFX modules
- `--add-modules`: Adds required JavaFX modules to the application
- `pause`: Keeps the console window open after execution (server only)

### VSCode Configuration Explained

- `type`: Specifies the debugger type (Java)
- `name`: Display name in the debug dropdown
- `request`: Launch type (launch for starting new process)
- `mainClass`: Entry point of your application
- `projectName`: Unique identifier for your project
- `vmArgs`: Java Virtual Machine arguments (includes JavaFX configuration)

---

## Additional Resources

- [JavaFX Documentation](https://openjfx.io/)
- [Gluon JavaFX Downloads](https://gluonhq.com/products/javafx/)
- [Visual Studio Code Java Documentation](https://code.visualstudio.com/docs/java/java-tutorial)

