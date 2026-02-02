package client;

import client.ui.StartScreenPanel;
import stage.*;
import stage.Team;

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
		StartScreenPanel startPanel = new StartScreenPanel(startListener);

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
		
		StageGenerator generator;
		if (isPractice) {
			generator = new StageGenerator() {
				private final Base redBase = new Base(0, 0, Team.RED);
				private final Base blueBase = new Base(9999, 9999, Team.BLUE);
				private final Tank practiceTank = new Tank(redBase);

				@Override
				public GameObject[] getGameObjects() {
					return new GameObject[]{ practiceTank };
				}

				@Override
				public ScreenObject[] getScreenObjects() {
					return new ScreenObject[0];
				}

				@Override
				public Base getRedBase() {
					return redBase;
				}

				@Override
				public Base getBlueBase() {
					return blueBase;
				}

				@Override
				public int getStageWidth() {
					return 3000;
				}

				@Override
				public int getStageHeight() {
					return 3000;
				}

				@Override
				public Tank[] getTanks() {
					return new Tank[]{ practiceTank };
				}

				@Override
				public boolean isNetworked() {
					return false;
				}
			};
		} else {
			// 通常モード用のStageGeneratorをここに実装
			throw new UnsupportedOperationException("通常モードはまだ実装されていません");
		}

		GamePanel gamePanel = new GamePanel(generator);

		this.getContentPane().removeAll();
		this.add(gamePanel);
		gamePanel.requestFocusInWindow(); // GamePanelにフォーカスを移す
		this.revalidate();
		this.repaint();

		// ゲーム開始 (GamePanelのaddNotifyが呼ばれてからスレッドが開始される)
	}
}