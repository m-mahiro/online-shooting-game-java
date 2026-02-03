package client;

import client.ui.HowToPlayPanel;
import client.ui.HowToPlaySlideShow;
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
	public void showStartScreen() {
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
	 */
	public void showHowToPlay() {
		ActionListener onBack = e -> showStartScreen();
		HowToPlayPanel howToPlayPanel = new HowToPlayPanel(onBack);

		this.getContentPane().removeAll();
		this.add(howToPlayPanel);
		howToPlayPanel.requestFocusInWindow(); // GamePanelにフォーカスを移す
		this.revalidate();
		this.repaint();

	}

	/**
	 * ゲームを開始する。
	 * 待機室画面を表示し、プレイヤーが揃うのを待つ。
	 */
	public void startGame() {
		client.ui.WaitingRoomPanel waitingRoomPanel = new client.ui.WaitingRoomPanel(this);

		this.getContentPane().removeAll();
		this.add(waitingRoomPanel);
		waitingRoomPanel.requestFocusInWindow();
		this.revalidate();
		this.repaint();
	}

	/**
	 * GamePanelを表示する。
	 *
	 * @param networkManager ネットワークマネージャー
	 */
	public void showGamePanel(NetworkManager networkManager) {
		GamePanel gamePanel = new GamePanel(networkManager);

		this.getContentPane().removeAll();
		this.add(gamePanel);
		gamePanel.requestFocusInWindow(); // GamePanelにフォーカスを移す
		this.revalidate();
		this.repaint();
	}
}