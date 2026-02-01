package client;

import client.ui.StartScreenPanel;
import stage.Team;

import javax.swing.JFrame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;

public class GameWindow extends JFrame {

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

	private void showStartScreen() {
		// スタート画面のリスナーを作成（クリックされたらゲームを開始する）
		ActionListener startListener = e -> startGame(true);
		StartScreenPanel startPanel = new StartScreenPanel(startListener);

		this.getContentPane().removeAll();
		this.add(startPanel);
		this.revalidate();
		this.repaint();
	}

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
				public UpperStageObject[] getUpperStageObjects() {
					return new UpperStageObject[0];
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