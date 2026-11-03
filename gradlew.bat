@echo off
setlocal
set VERSION=9.2.1
if not defined GRADLE_USER_HOME set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set DIST_ROOT=%GRADLE_USER_HOME%\wrapper\dists\orange-client\gradle-%VERSION%-bin
set GRADLE_HOME=%DIST_ROOT%\gradle-%VERSION%
if exist "%GRADLE_HOME%\bin\gradle.bat" goto run
where gradle >nul 2>nul
if %ERRORLEVEL%==0 (
  gradle --version | findstr /C:"Gradle %VERSION%" >nul
  if %ERRORLEVEL%==0 goto systemgradle
)
if not exist "%DIST_ROOT%" mkdir "%DIST_ROOT%"
set ZIP=%DIST_ROOT%\gradle-%VERSION%-bin.zip
if not exist "%ZIP%" (
  echo Downloading Gradle %VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%VERSION%-bin.zip' -OutFile '%ZIP%'"
  if errorlevel 1 exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP%' '%DIST_ROOT%'"
if errorlevel 1 exit /b 1
:run
call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %ERRORLEVEL%
:systemgradle
gradle %*
exit /b %ERRORLEVEL%
