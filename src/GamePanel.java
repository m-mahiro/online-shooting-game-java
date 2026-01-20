import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GamePanel extends JPanel implements Runnable {

	// 画面サイズ定数
	public static final int WIDTH = 1000;
	public static final int HEIGHT = 400;
	public static final int FPS = 60;

	private final AffineTransform canvasTransform = new AffineTransform();

	private final List<GameObject> objects = Collections.synchronizedList(new ArrayList<>());
	private final Map<Integer, Tank> tanks = new ConcurrentHashMap<>();

	private Thread gameThread;
	private MouseKeyboardInput input = new MouseKeyboardInput();

	private NetworkManager networkManager;
	private int myTankID;

	private double cameraZoom = 1;

	public GamePanel() {

		// ============================= 諸設定 =============================
		// パネルの設定
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setBackground(Color.WHITE);
		this.setDoubleBuffered(true);

		// 入力ハンドラの登録
		this.addKeyListener(input);
		this.addMouseMotionListener(input);
		this.addMouseListener(input);
		this.addMouseWheelListener(input);
		this.setFocusable(true);
		this.requestFocusInWindow();

		// これがないとキー入力を受け取れません
		setFocusable(true);
		requestFocusInWindow();

		// ============================= オブジェクトの配置 =============================

		Tank tank1 = new Tank(0, 0, Team.BLUE);
		Tank tank2 = new Tank(100, 500, Team.BLUE);
		Tank tank3 = new Tank(600, 200, Team.RED);
		Tank tank4 = new Tank(600, 500, Team.RED);
		this.addGameObject(tank1);
		this.addGameObject(tank2);
		this.addGameObject(tank3);
		this.addGameObject(tank4);
		this.tanks.put(0, tank1);
		this.tanks.put(1, tank2);
		this.tanks.put(2, tank3);
		this.tanks.put(3, tank4);

		// ブロック
		for (int j = 0; j < 4; j++) {
			Block block = new Block(350, 400 * j);
			this.addGameObject(block);
		}
		myTankID = 0;

		this.networkManager = new NetworkManager(this);
		this.networkManager.start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.transform(this.canvasTransform);

		// 画面上のオブジェクトを描画
		for (GameObject object : objects) {
			object.draw(g2d);
		}
	}

	public void setCameraLocation(double x, double y) {
		AffineTransform trans = new AffineTransform();
		trans.translate(WIDTH / 2.0, HEIGHT / 2.0);
		trans.scale(this.cameraZoom, this.cameraZoom);
		trans.translate(-x, -y);
		this.canvasTransform.setTransform(trans);
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

			update();

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

		checkObjectToRemove();
		checkCollision();
		repaint();


		for (GameObject object : objects) {
			object.update();
		}
		Tank myTank = this.getMyTank();
		// 自分の操作
		if (myTank != null) {

			// 戦車に移動命令を出す
			double[] moveVector = input.getMoveVector(this.canvasTransform);
			double x = moveVector[0];
			double y = moveVector[1];
			if (x != 0 || y != 0) {
				myTank.moveFor(moveVector[0], moveVector[1]);
				networkManager.moveTank(myTankID, myTank.getX(), myTank.getY(), myTank.getChassisAngle());
			}

			// カメラアングルを調整
			cameraZoom += input.getZoomAmount() * 0.1;
			setCameraLocation(myTank.getX(), myTank.getY());


			// マウス位置へ砲塔を向ける命令を出す
			double[] aim = input.getAimedCoordinate(this.canvasTransform);
			double gunTargetX = aim[0];
			double gunTargetY = aim[1];
			myTank.aimAt(gunTargetX, gunTargetY);
			networkManager.aimAt(myTankID, gunTargetX, gunTargetY);

			// 戦車に発砲命令を出す。
			if (input.gunButtonPressed()) {
				addGameObject(myTank.shootBullet());
				networkManager.shootGun(myTankID, myTank.getX(), myTank.getY(), myTank.getGunAngle());
			}
		}
	}

	public void checkCollision() {
		for (int i = 0; i < objects.size(); i++) {
			for (int j = i + 1; j < objects.size(); j++) {
				GameObject a = objects.get(i);
				GameObject b = objects.get(j);

				if (a == b) continue;
				if (!a.isTangible() || !b.isTangible()) continue;

				// 衝突範囲の計算
				double collisionRange = a.getRadius() + b.getRadius();

				// オブジェクト間の距離の計算
				Point2D.Double vector = Util.subtract(a.getTranslate(), b.getTranslate());
				double diff = Util.getNorm(vector);

				if (diff < collisionRange) {
					a.onCollision(b);
					b.onCollision(a);
				}
			}
		}
	}

	public void checkObjectToRemove() {
		Iterator<GameObject> iterator = this.objects.iterator();
		while (iterator.hasNext()) {
			GameObject object = iterator.next();

			if (object == null) {
				iterator.remove();
				continue;
			}

			if (object.shouldRemove()) {
				iterator.remove();
			}
		}
	}

	public Tank getTankByID(int id) {
		return this.tanks.get(id);
	}

	public Tank getMyTank() {
		return getTankByID(getMyTankID());
	}

	public int getMyTankID() {
		return this.myTankID;
	}

	public void setMyTankID(int myTankID) {
		System.out.println("myTankID: " + myTankID);
		this.myTankID = myTankID;
	}

	public void addGameObject(GameObject object) {
		this.objects.add(object);
	}
}