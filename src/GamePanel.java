import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;

public class GamePanel extends JPanel implements Runnable {

	// 画面サイズ定数
	public static final int FPS = 60;

	private final AffineTransform canvasTransform = new AffineTransform();
	private double cameraZoom = 1;
	public double CAMERA_ZOOM_UPPER_THRESHOLD = 10;
	public double CAMERA_ZOOM_LOWER_THRESHOLD = 0.1;

	private Thread gameThread;
	private MouseKeyboardInput input;

	private NetworkManager networkManager;
	private int networkClientID;
	public GameStage gameStage;
	private int myTankID;

	public GamePanel() {

		// ============================= ウィンドウの諸設定 =============================
		// パネルの設定
		this.setBackground(Color.WHITE);
		this.setDoubleBuffered(true);
//		this.setPreferredSize(new Dimension(500, 300));

		// 入力ハンドラの登録
		this.input = new MouseKeyboardInput();
		this.addKeyListener(input);
		this.addMouseMotionListener(input);
		this.addMouseListener(input);
		this.addMouseWheelListener(input);
		this.setFocusable(true);
		this.requestFocusInWindow();
		setFocusable(true);
		requestFocusInWindow();

		// サーバに接続、クライアントIDをもらう
		this.networkManager = new NetworkManager(this);
		networkClientID = this.networkManager.getNetworkClientID();
		myTankID = this.networkManager.getMyTankID();
//		myTankID = 1;

		// ============================= オブジェクトの配置 =============================
		this.gameStage = new GameStage(networkClientID, 20);

		// カメラの初期設定
		cameraZoom = 0.5;
		setCameraLocation(getMyTank().getX(), getMyTank().getY());

		// サーバからのメッセージ受け取り開始
		this.networkManager.start();
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D graphics2D = (Graphics2D) graphics;
		graphics2D.transform(this.canvasTransform);
		gameStage.draw(graphics2D);
	}

	public void setCameraLocation(double x, double y) {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.getWidth() / 2.0, this.getHeight() / 2.0);
		trans.scale(this.cameraZoom, this.cameraZoom);
		trans.translate(-x, -y);
		this.canvasTransform.setTransform(trans);
	}

	public void zoomCamera(double zoomDelta) {
		this.cameraZoom -= zoomDelta;
		if (this.cameraZoom < CAMERA_ZOOM_LOWER_THRESHOLD) {
			this.cameraZoom = CAMERA_ZOOM_LOWER_THRESHOLD;
		} else if (CAMERA_ZOOM_UPPER_THRESHOLD < this.cameraZoom) {
			this.cameraZoom = CAMERA_ZOOM_UPPER_THRESHOLD;
		}
	}

	public void startGameThread() {
		this.gameThread = new Thread(this);
		gameThread.start();
		int displayWidth = (int) (this.getWidth() / CAMERA_ZOOM_LOWER_THRESHOLD);
		int displayHeight = (int) (this.getHeight() / CAMERA_ZOOM_LOWER_THRESHOLD);
		gameStage.setDisplayRange(displayWidth, displayHeight);
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

		int frame = 0;


		while (gameThread != null) {
			update();
			repaint();

			// 3. SLEEP: 時間調整
			double remainingTime = nextDrawTime - System.nanoTime();

			try {
				// 時間が余っていれば寝る
				if (remainingTime > 0) {
					Thread.sleep((long) remainingTime / 1000000);
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

		try {
			gameStage.update();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ============================= 自分の操作 =============================
		Tank myTank = getMyTank();
		if (myTank.isDead()) return;

		// カメラアングルを調整
		zoomCamera(input.getZoomAmount() * 0.1);
		setCameraLocation(myTank.getX(), myTank.getY());

		// 戦車に移動命令を出す
		Point2D.Double moveVector = input.getMoveVector(this.canvasTransform);
		if (moveVector.x != 0 || moveVector.y != 0) {
			myTank.move(moveVector);
		}
		networkManager.moveTank(myTankID, myTank.getX(), myTank.getY());

		// マウス位置へ砲塔を向ける命令を出す
		Point2D.Double coordinate = input.getAimedCoordinate(this.canvasTransform);
		myTank.aimAt(coordinate);
		networkManager.aimAt(myTankID, coordinate.x, coordinate.y);

		// 戦車に発砲命令を出す。
		if (input.gunButtonPressed()) {
			gameStage.addObject(myTank.shotBullet());
			networkManager.shootGun(myTankID, myTank.getX(), myTank.getY(), myTank.getGunAngle());
		}

		// 戦車のブロック作成命令を出す。
		if (input.blockButtonPressed()) {
			gameStage.addObject(myTank.createBlock());
			networkManager.createBlock(myTankID, myTank.getTranslate());
		}
	}

	private Tank getMyTank() {
		GameObject object = this.gameStage.getObject(myTankID);
		if (object instanceof Tank) {
			return (Tank) object;
		} else {
			throw new RuntimeException("myTankIDに対応するobjectはTankのインスタンスではありませんでした。");
		}
	}

	public int getMyTankID() {
		return this.myTankID;
	}

}