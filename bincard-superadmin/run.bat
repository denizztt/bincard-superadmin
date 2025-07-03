@echo off
REM Set JAVA_HOME temporarily to one of the JDKs in your .jdks folder
SET JAVA_HOME=C:\Users\dtata\.jdks\temurin-21.0.7
echo JAVA_HOME set to %JAVA_HOME%

REM If the specific version doesn't exist, you can try another one
if not exist "%JAVA_HOME%" (
    echo JDK not found at %JAVA_HOME%, trying alternative location...
    SET JAVA_HOME=C:\Users\dtata\.jdks\openjdk-24.0.1
    echo JAVA_HOME set to %JAVA_HOME%
)

REM If that also doesn't exist, try another
if not exist "%JAVA_HOME%" (
    echo JDK not found at %JAVA_HOME%, trying alternative location...
    SET JAVA_HOME=C:\Users\dtata\.jdks\ms-21.0.7
    echo JAVA_HOME set to %JAVA_HOME%
)

REM Final fallback if none of the above work
if not exist "%JAVA_HOME%" (
    echo JDK not found at %JAVA_HOME%, trying to find any JDK in .jdks...
    for /d %%i in (C:\Users\dtata\.jdks\*) do (
        SET JAVA_HOME=%%i
        echo Found JDK: %JAVA_HOME%
        goto :run
    )
)

:run
echo Running the application...
call mvnw.cmd javafx:run

pause
