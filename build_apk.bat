@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "ANDROID_HOME=G:\Android\SDK"
set "GRADLE_USER_HOME=G:\.gradle"
set "TEMP=G:\temp"
set "TMP=G:\temp"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Starting optimized single-worker build with SerialGC...
call "G:\transit-reminder\gradle\gradle-8.6\bin\gradle.bat" assembleDebug --no-daemon -Dorg.gradle.jvmargs="-Xmx1024m -XX:+UseSerialGC" --max-workers=1
