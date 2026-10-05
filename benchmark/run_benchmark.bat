@echo off
rem 描画最適化(変更点1〜3)のビフォーアフターを計測するベンチマークを実行する(Windows用)。
rem 使い方や結果の見方は benchmark\README.md を参照。
rem
rem 使用例:
rem   run_benchmark.bat              (デフォルト設定: warmup=200, iterations=2000で実行)
rem   run_benchmark.bat 50 500       (手早く確認したい場合: warmup=50, iterations=500)

setlocal
cd /d "%~dp0\.."

set WARMUP=%1
if "%WARMUP%"=="" set WARMUP=200
set ITER=%2
if "%ITER%"=="" set ITER=2000

echo Compiling benchmark sources (and the game sources they depend on)...
if not exist benchmark\out mkdir benchmark\out

set "SRCS="
for /r src %%F in (*.java) do (call set "SRCS=%%SRCS%% "%%F"")
for /r benchmark\src %%F in (*.java) do (call set "SRCS=%%SRCS%% "%%F"")

javac -d benchmark\out -encoding UTF-8 %SRCS%
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)

echo.
echo Running benchmarks (warmup=%WARMUP%, iterations=%ITER%)...
rem java.awt.headless=true: このベンチマーク(変更点1〜3)はディスプレイ・GPUに依存しない
rem 純粋なCPU/IOの計測なので、通常のWindows環境でもLinux(ヘッドレス)環境でも同じ条件で測れる。
java -Djava.awt.headless=true -cp benchmark\out benchmark.BenchmarkMain "%cd%" %WARMUP% %ITER%

endlocal
pause
