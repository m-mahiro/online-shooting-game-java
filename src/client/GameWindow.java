package client;

import client.ui.StartPanel;

import javax.swing.JFrame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;

/**
 * ゲームのメインウィンドウを管理するクラス。
 * スタート画面とゲーム画面の切り替えを行う。
 */
public class GameWindow extends JFrame {

	/**
	 * ゲームウィンドウを初期化し、フルスクリーンモードで表示する。
	 * 初期状態ではスタート画面を表示する。
	 */
	public GameWindow() {
		setTitle("Tank Game 2D");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		showStartScreen();

		// フルスクリーン化の処理
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();

		if (device.isFullScreenSupported()) {
			// 自分自身(this)をフルスクリーンウィンドウに設定
			device.setFullScreenWindow(this);
		} else {
			// 万が一フルスクリーン非対応の環境だった場合のフォールバック
			System.err.println("フルスクリーンモードがサポートされていません。ウィンドウモードで起動します。");
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}

	/**
	 * スタート画面を表示する。
	 * ユーザーがクリックするとゲームが開始される。
	 */
	private void showStartScreen() {
		// スタート画面のリスナーを作成（クリックされたらゲームを開始する）
		ActionListener startListener = e -> startGame(true);
		StartPanel startPanel = new StartPanel(startListener);

		this.getContentPane().removeAll();
		this.add(startPanel);
		this.revalidate();
		this.repaint();
	}

	/**
	 * ゲームを開始する。
	 * 練習モードまたは通常モードのステージを生成し、ゲームパネルに切り替える。
	 *
	 * @param isPractice true の場合は練習モード、false の場合は通常モード
	 */
	private void startGame(boolean isPractice) {
		GamePanel gamePanel = new GamePanel();

		this.getContentPane().removeAll();
		this.add(gamePanel);
		gamePanel.requestFocusInWindow(); // GamePanelにフォーカスを移す
		this.revalidate();
		this.repaint();
	}
}