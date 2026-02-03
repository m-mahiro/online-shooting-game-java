package client.ui;

import client.GameWindow;
import client.NetworkManager;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class WaitingRoomPanel extends JPanel {

	private List<RotationChars> rotationCharsList;
	private GameWindow gameWindow;
	private NetworkManager networkManager;
	private Timer animationTimer;

	public WaitingRoomPanel(GameWindow gameWindow) {
		this.gameWindow = gameWindow;

		// NetworkManagerを作成し、コールバックを設定
		this.networkManager = new NetworkManager(createOnReadyCallback());
		this.networkManager.start(); // スレッドを開始

		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(800, 600));

		// WAITING...の文字を作成
		rotationCharsList = new ArrayList<>();
		String[] chars = {"W", "A", "I", "T", "I", "N", "G", "..."};

		// 初期位置で作成（後で更新される）
		for (int i = 0; i < chars.length; i++) {
			rotationCharsList.add(new RotationChars(chars[i], new Point2D.Double(0, 0), i * 10, Color.BLACK));
		}

		// 画面サイズに応じて文字位置を更新するメソッド
		Runnable updateCharPositions = () -> {
			int panelWidth = getWidth();
			int panelHeight = getHeight();

			if (panelWidth > 0 && panelHeight > 0) {
				// 文字の総幅を計算
				double center = panelWidth / 2.0;
				double spacing = RotationChars.size * 1.1; // 文字間隔

				// 中央揃え用のx座標開始位置
				double y = panelHeight / 2.0;

				// 各文字の位置を更新
				for (int i = 0; i < rotationCharsList.size(); i++) {
					double x = center + spacing * (i - rotationCharsList.size() / 2.0);
					rotationCharsList.get(i).setPosition(new Point2D.Double(x, y));
				}
			}
		};

		// 初期配置
		updateCharPositions.run();

		// ウィンドウサイズ変更時に再配置
		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				updateCharPositions.run();
			}
		});

		// アニメーションタイマーを開始
		animationTimer = new Timer(16, e -> {
			for (RotationChars rc : rotationCharsList) {
				rc.update();
			}
			repaint();
		});
		animationTimer.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;

		// アンチエイリアシング
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 文字を描画
		for (RotationChars rc : rotationCharsList) {
			rc.draw(g2d);
		}
	}

	/**
	 * PLAYER_COUNTが揃ったときに呼ばれるコールバック関数
	 */
	public Runnable createOnReadyCallback() {
		return () -> {
			SwingUtilities.invokeLater(() -> {
				animationTimer.stop();
				gameWindow.showGamePanel(networkManager);
			});
		};
	}
}
