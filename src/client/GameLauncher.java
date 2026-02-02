package client;

import javax.swing.*;

/**
 * ゲームアプリケーションのエントリーポイントとなるクラス。
 * ゲームウィンドウを起動する。
 */
public class GameLauncher {
	/**
	 * アプリケーションのメインメソッド。
	 * イベントディスパッチスレッド上でゲームウィンドウを起動する。
	 *
	 * @param args コマンドライン引数（使用しない）
	 */
	public static void main(String[] args) {

		// Swingの推奨作法に従い、イベントディスパッチスレッド(EDT)でGUIを起動
		SwingUtilities.invokeLater(() -> {
			new GameWindow();
		});	}
}
