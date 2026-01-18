import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.*;

public class GamePanel extends JPanel implements Runnable {

	// 画面サイズ定数
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	public static final int FPS = 60;
	
	private HashSet<Tank> tanks = new HashSet<>();
	private Tank myTank;

	private Thread gameThread;
	private InputHandler input = new InputHandler();

	public GamePanel() {
		// パネルの推奨サイズと背景色を設定
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.WHITE);

		// InputHandlerをパネルに登録して、入力を受け取れるようにする
		this.addKeyListener(input);
		this.addMouseMotionListener(input);

		// これがないとキー入力を受け取れません
		setFocusable(true);
		requestFocusInWindow();

		for (int i = 0; i < 4; i++) {
			Team team = (i % 2 == 0) ? Team.RED : Team.BLUE;
			Tank tank = new Tank(200 * i + 200, 100, team);
			this.tanks.add(tank);
			if (i == 0) myTank = tank;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// より高度な描画機能を使うためにGraphics2Dにキャスト
		Graphics2D g2d = (Graphics2D) g;
		for (Tank tank : tanks) {
			tank.draw(g2d);
		}
	}

	public void startGameThread() {
		this.gameThread = new Thread(this);
		gameThread.start();
	}

	/**
	 * ゲームループ (60FPS固定)
	 */
	@Override
	public void run() {
		// 1フレームの持ち時間 (ナノ秒)
		// 1秒 = 1,000,000,000 ナノ秒
		long drawInterval = 1000000000 / FPS;

		long currentTime;

		while (gameThread != null) {
			// 現在時刻を取得
			currentTime = System.nanoTime();

			// 1. UPDATE: 計算
			update();

			// 2. REPAINT: 描画
			repaint();

			// 3. SLEEP: 時間調整
			// 処理にかかった時間を計算して、持ち時間から引く
			long usedTime = System.nanoTime() - currentTime;
			long sleepTime = drawInterval - usedTime;

			try {
				// 時間が余っていれば寝る
				if (sleepTime > 0) {
					// ナノ秒をミリ秒に変換してsleep
					Thread.sleep(sleepTime / 1000000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * ゲームの状態を更新する
	 */
	public void update() {
		for (Tank tank : tanks) {
			tank.update();
		}
		if (myTank != null) {

			// 1. キー入力から移動方向を決める
			double xDir = 0;
			double yDir = 0;

			if (input.up) yDir = -1;
			if (input.down) yDir = 1;
			if (input.left) xDir = -1;
			if (input.right) xDir = 1;

			// 2. 戦車に移動命令を出す
			myTank.move(xDir, yDir);

			// 3. マウス位置へ砲塔を向ける命令を出す
			myTank.aimAt(input.mouseX, input.mouseY);
		}
	}
}