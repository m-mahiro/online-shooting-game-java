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
	
	private HashSet<GameObject> objects = new HashSet<>();
	private Tank myTank;

	private Thread gameThread;
	private MouseKeyboardInput input = new MouseKeyboardInput();

	public GamePanel() {
		// パネルの設定
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setBackground(Color.WHITE);
		this.setDoubleBuffered(true);

		// 入力ハンドラの登録
		this.addKeyListener(input);
		this.addMouseMotionListener(input);
		this.addMouseListener(input);
		this.setFocusable(true);
		this.requestFocusInWindow();

		// これがないとキー入力を受け取れません
		setFocusable(true);
		requestFocusInWindow();

		for (int i = 0; i < 4; i++) {
			Team team = (i % 2 == 0) ? Team.RED : Team.BLUE;
			Tank tank = new Tank(200 * i + 200, 100, team);
			this.objects.add(tank);
			if (i == 0) myTank = tank;
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// より高度な描画機能を使うためにGraphics2Dにキャスト
		Graphics2D g2d = (Graphics2D) g;
		for (GameObject object : objects) {
			object.draw(g2d);
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
		double drawInterval = 1000000000.0 / FPS;
		double nextDrawTime = System.nanoTime() + drawInterval;

		while (gameThread != null) {
			// 1. UPDATE: 計算
			update();

			// 2. REPAINT: 描画
			repaint();

			// 3. SLEEP: 時間調整
			double remainingTime = nextDrawTime - System.nanoTime();

			try {
				// 時間が余っていれば寝る
				if (remainingTime > 0) {
					Thread.sleep((long)remainingTime / 1000000);
				}
				nextDrawTime += drawInterval;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * ゲームの状態を更新する
	 */
	public void update() {
		for (GameObject object : objects) {
			object.update();
		}

		if (myTank != null) {

			// 戦車に移動命令を出す
			double[] moveVector = input.getMoveVector();
			myTank.move(moveVector[0], moveVector[1]);

			// マウス位置へ砲塔を向ける命令を出す
			myTank.aimAt(input.mouseX, input.mouseY);

			// 戦車に発砲命令を出す。
			if (input.gunButtonPressed()) {
				objects.add(myTank.shootBullet());
			}
		}
	}
}