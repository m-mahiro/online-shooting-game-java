@echo off
echo Creating output directory...
if not exist out mkdir out

echo Copying assets...
xcopy src\assets out\assets /E /I /Y

echo Compiling Java source files...
javac -d out -encoding UTF-8 -cp src src/*.java

echo Running the game...
java -cp out GameLauncher

pause
