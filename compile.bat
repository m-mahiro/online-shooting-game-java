@echo off
echo Creating output directory...
if not exist out mkdir out
if not exist out\client mkdir out\client

echo Copying assets...
xcopy src\client\assets out\client\assets /E /I /Y

echo Compiling Java source files...
set "SRCS="
for /r src %%F in (*.java) do (call set "SRCS=%%SRCS%% "%%F"")
javac -d out -encoding  -J-Duser.language=en -J-Duser.country=US UTF-8 %SRCS%

echo.
echo Compilation finished.
