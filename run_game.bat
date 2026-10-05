@echo off
rem Windows上でJava2DがDirect3D(GPU)パイプラインを使うよう明示する。
rem 環境変数やドライバの状態によってD3Dが無効化されているとGDI(CPU)での
rem ソフトウェア描画にフォールバックし、描画が大幅に重くなることがあるため。
rem もしD3Dでちらつき等の問題が出る場合は、sun.java2d.d3d=false に変更して
rem sun.java2d.opengl=true を試すこと（GPU/ドライバとの相性の問題であることが多い）。
java -Dsun.java2d.d3d=true -jar online-shooting-game.jar
