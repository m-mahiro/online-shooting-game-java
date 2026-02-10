@echo off
setlocal

echo Creating output directory...
if not exist out mkdir out
if not exist out\client mkdir out\client

echo Copying assets...
xcopy src\client\assets out\client\assets /E /I /Y /Q

echo Compiling Java source files...
set "SRCS="
for /r "src" %%F in (*.java) do (
    call set "SRCS=%%SRCS%% "%%F""
)
javac -d out -encoding UTF-8 %SRCS%
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)

echo Creating MANIFEST.MF...
(echo Main-Class: client.GameLauncher) > MANIFEST.MF

echo Creating JAR file...
jar cvfm online-shooting-game.jar MANIFEST.MF -C out .

echo Deleting MANIFEST.MF...
del MANIFEST.MF

echo.
echo JAR file 'online-shooting-game.jar' created successfully.
echo To run the game, execute 'run_game.bat' or type: java -jar online-shooting-game.jar

endlocal
