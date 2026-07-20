@ECHO off
TITLE Aion Lightning - Chat Server Console
SET "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot"
SET "PATH=%JAVA_HOME%\bin;%PATH%"
:START
CLS
IF "%MODE%" == "" (
CALL PanelCS.bat
)
ECHO Starting Aion Lightning Chat Server in %MODE% mode.
JAVA %JAVA_OPTS% --enable-preview -cp ./libs/*;AL-Chat.jar com.aionemu.chatserver.ChatServer
SET CLASSPATH=%OLDCLASSPATH%
IF ERRORLEVEL 2 GOTO START
IF ERRORLEVEL 1 GOTO ERROR
IF ERRORLEVEL 0 GOTO END
:ERROR
ECHO.
ECHO Chat Server has terminated abnormaly!
ECHO.
PAUSE
EXIT
:END
ECHO.
ECHO Chat Server is terminated!
ECHO.
PAUSE
EXIT