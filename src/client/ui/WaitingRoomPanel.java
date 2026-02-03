package client.ui;

import client.GamePanel;
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

		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(800, 600));

		// WAITING...の文字を作成
		rotationCharsList = new ArrayList<>();
		String[] chars = {"W", "A", "I", "T", "I", "N", "G", "..."};
		double startX = 200;
		double y = 300;
		double spacing = 60;

		for (int i = 0; i < chars.length; i++) {
			double x = startX + i * spacing;
			rotationCharsList.add(new RotationChars(chars[i], new Point2D.Double(x, y), i * 0.15));
		}

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
