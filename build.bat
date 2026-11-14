@echo off
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle not found. Install it from https://gradle.org/install/ or open this folder in IntelliJ IDEA and run the "build" task.
  pause & exit /b 1
)
if not exist gradlew.bat gradle wrapper --gradle-version 9.2.1
call gradlew.bat build
echo.
echo Done! Your jar is in build\libs\zyntro-client-0.3.0.jar
pause
