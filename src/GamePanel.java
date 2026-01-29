import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class GamePanel extends JPanel implements Runnable {

	// 画面サイズ定数
	public static final int FPS = 60;

	private final AffineTransform canvasTransform = new AffineTransform();
	private double cameraZoom = 1;
	public double CAMERA_ZOOM_UPPER_THRESHOLD = 10;
	public double CAMERA_ZOOM_LOWER_THRESHOLD = 0.1;

	private Thread gameThread;
	private InputHandler input;

	private NetworkManager networkManager;
	private int networkClientID;
	public GameStage gameStage;
	private int myTankID;

	public GamePanel() {

		// ============================= ウィンドウの諸設定 =============================
		// パネルの設定
		this.setBackground(Color.WHITE);
		this.setDoubleBuffered(true);
		this.setPreferredSize(new Dimension(1000, 700));

		// 入力ハンドラの登録
		this.input = new MouseKeyboardInput(this);

		// サーバに接続、クライアントIDをもらう
		this.networkManager = new NetworkManager(this);
		networkClientID = this.networkManager.getNetworkClientID();
		myTankID = this.networkManager.getMyTankID();
//		myTankID = 0;

		// ============================= オブジェクトの配置 =============================
		this.gameStage = new GameStage(networkClientID, 10);

		// カメラの初期設定
		cameraZoom = 0.5;
		setCameraPosition(getMyTank().getPosition());

		// サーバからのメッセージ受け取り開始
		this.networkManager.start();
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D graphics2D = (Graphics2D) graphics;

		// カメラの位置・ズーム度合いに合わせてあらかじめキャンバスをずらしておく。
		graphics2D.transform(this.canvasTransform);

		// Windowから見える範囲を更新する
		updateVisibleRange();

		// GameObjectの描画
		gameStage.draw(graphics2D);

		// GUIの描画
		// todo: ここでGUIを描画
	}

	public void setCameraPosition(Point2D.Double position) {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.getWidth() / 2.0, this.getHeight() / 2.0);
		trans.scale(this.cameraZoom, this.cameraZoom);
		trans.translate(-position.x, -position.y);
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
		updateVisibleRange();
	}

	public void updateVisibleRange() {
		int displayWidth = (int) (this.getWidth() / CAMERA_ZOOM_LOWER_THRESHOLD);
		int displayHeight = (int) (this.getHeight() / CAMERA_ZOOM_LOWER_THRESHOLD);
		gameStage.setVisibleRange(displayWidth, displayHeight);
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

	public void update() {

		try {
			gameStage.update();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// ============================= 自分の操作 =============================
		input.onFrameUpdate();

		Tank myTank = getMyTank();
		if (myTank.isDead()) return;

		// カメラアングルを調整
		zoomCamera(input.getZoomAmount() * 0.1);
		setCameraPosition(myTank.getPosition());

		// 戦車に移動命令を出す
		Point2D.Double moveVector = input.getMoveVector(this.canvasTransform);
		if (moveVector.x != 0 || moveVector.y != 0) {
			myTank.move(moveVector);
		}
		networkManager.locateTank(myTankID, myTank.getPosition());

		// マウス位置へ砲塔を向ける命令を出す
		Point2D.Double coordinate = input.getAimedCoordinate(this.canvasTransform);
		myTank.aimAt(coordinate);
		networkManager.aimAt(myTankID, coordinate);

		// 戦車に発砲命令を出す。
		if (input.shootBullet()) {
			Bullet bullet = myTank.shootBullet();
			gameStage.addObject(bullet);
			networkManager.shootGun(myTankID);
		}

//		// 戦車にエネルギーチャージ命令を出す
//		if (input.chargeButtonPressed()) {
//			Missile missile = myTank.startEnergyCharge();
//			gameStage.addObject(missile);
//			System.out.println("チャージ開始");
//		} else {
//			myTank.finishEnergyCharge();
//			System.out.println("チャージ終了");
//		}

		// 戦車のブロック作成命令を出す。
		if (input.createBlock()) {
			Block block = myTank.createBlock();
			gameStage.addObject(block);
			networkManager.createBlock(myTankID);
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