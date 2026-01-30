package client;

import javax.swing.JFrame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

public class GameWindow extends JFrame {

	public GameWindow() {
		setTitle("Tank Game 2D");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//		// 1. ウィンドウの枠（タイトルバーなど）を削除
//		setUndecorated(true);
//		setResizable(false);

		// GamePanelを作成してウィンドウに追加
		GamePanel gamePanel = new GamePanel();
		this.add(gamePanel);

		// 2. フルスクリーン化の処理
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();

//		if (device.isFullScreenSupported()) {
		if (false) {
			// 自分自身(this)をフルスクリーンウィンドウに設定
			device.setFullScreenWindow(this);
		} else {
			// 万が一フルスクリーン非対応の環境だった場合のフォールバック
			System.err.println("フルスクリーンモードがサポートされていません。ウィンドウモードで起動します。");
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}

		// ゲーム開始
		gamePanel.startGameThread();
	}
}