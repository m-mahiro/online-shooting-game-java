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
	 * ユーザーがボタンをクリックするとゲームが開始される。
	 * 現在のコンテンツをクリアし、スタートパネルに切り替える。
	 */
	private void showStartScreen() {
		// スタート画面のリスナーを作成
		ActionListener onStartGame = e -> startGame();
		ActionListener onHowToPlay = e -> showHowToPlay();
		StartPanel startPanel = new StartPanel(onStartGame, onHowToPlay);

		this.getContentPane().removeAll();
		this.add(startPanel);
		this.revalidate();
		this.repaint();
	}

	/**
	 * How to Play画面を表示する。
	 * TODO: 実装予定
	 */
	private void showHowToPlay() {
		System.out.println("How to Play button clicked!");
		// TODO: How to Play画面の実装
	}

	/**
	 * ゲームを開始する。
	 * 練習モードまたは通常モードのステージを生成し、ゲームパネルに切り替える。
	 *
	 * @param isPractice true の場合は練習モード、false の場合は通常モード
	 */
	private void startGame() {
		GamePanel gamePanel = new GamePanel();

		this.getContentPane().removeAll();
		this.add(gamePanel);
		gamePanel.requestFocusInWindow(); // GamePanelにフォーカスを移す
		this.revalidate();
		this.repaint();
	}
}