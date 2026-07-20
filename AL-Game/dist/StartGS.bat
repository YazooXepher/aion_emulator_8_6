@ECHO off
mode con:cols=150 
color 1B 
TITLE Aion German - Game Server Console
SET "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
SET "PATH=%JAVA_HOME%\bin;%PATH%"
:START
CLS
SET NUMAENABLE=false
CLS
IF "%MODE%" == "" (
CALL PanelGS.bat
)

IF "%NUMAENABLE%" == "true" (
SET JAVA_OPTS=-XX:+UseNUMA %JAVA_OPTS%
)
ECHO Starting Aion German Game Server in %MODE% mode.
JAVA %JAVA_OPTS% --enable-preview -ea -Xlog:gc*:file=./log/gc.log:time,uptime:filecount=1 -javaagent:./libs/al-commons.jar -cp ./libs/*;AL-Game.jar com.aionemu.gameserver.GameServer
SET CLASSPATH=%OLDCLASSPATH%
IF ERRORLEVEL 2 GOTO START
IF ERRORLEVEL 1 GOTO ERROR
IF ERRORLEVEL 0 GOTO END
:ERROR
ECHO.
ECHO Game Server has terminated abnormaly!
ECHO.
PAUSE
EXIT
:END
ECHO.
ECHO Game Server is terminated!
ECHO.
PAUSE
EXIT