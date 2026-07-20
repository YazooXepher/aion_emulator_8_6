@echo off
mode con:cols=150 
color 1B 
TITLE Aion German - Game Server Console
SET "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
SET "PATH=%JAVA_HOME%\bin;%PATH%"
:START
CLS

echo.

echo Starting Aion German Version 6.x Game Server.

echo.

REM -------------------------------------
REM Default parameters for a basic server.
java -Xms512m -Xmx2048m --enable-preview -ea -javaagent:./libs/al-commons.jar -cp ./libs/*;AL-Game.jar com.aionemu.gameserver.GameServer
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
if ERRORLEVEL 0 goto end

REM Restart...
:restart
echo.
echo Administrator Restart ...
echo.
goto start

REM Error...
:error
echo.
echo Server terminated abnormaly ...
echo.
goto end

REM End...
:end
echo.
echo Server terminated ...
echo.
pause
