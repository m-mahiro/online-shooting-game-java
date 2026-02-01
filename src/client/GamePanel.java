package client;

import client.ui.GameUI;
import stage.*;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class GamePanel extends JPanel implements Runnable {

	// 画面サイズ定数
	public static final int FPS = 60;

	// カメラに関する情報
	private double zoomDegrees;
	private Point2D.Double cameraPosition = new Point2D.Double();
	public double CAMERA_ZOOM_UPPER_THRESHOLD = 5;
	public double CAMERA_ZOOM_LOWER_THRESHOLD = 0.07;

	// このインスタンス内で、update->drawのループが繰り返される
	private Thread gameThread;

	// ソフトウェアコンポーネント(?)
	private InputHandler input;
	private NetworkManager network;
	private int networkID;
	public GameStage stage;
	public GameUI ui;

	// 自分の操作している戦車
	private int myTankID;
	private Tank myTank;

	public GamePanel() {

		// ============================= ウィンドウの諸設定 =============================
		// パネルの設定
		this.setBackground(Color.WHITE);
		this.setDoubleBuffered(true);
		this.setPreferredSize(new Dimension(1000, 700));

		// 入力ハンドラの登録
		this.input = new MouseKeyboardInput(this);

		// サーバに接続、クライアントIDをもらう
		this.network = new NetworkManager(this);
		networkID = this.network.getNetworkClientID();
		myTankID = this.network.getMyTankID();

		// ============================= オブジェクトの配置 =============================

		// ステージの作成
		this.stage = new GameStage(networkID, 10);
		this.myTank = (Tank) this.stage.getGameObject(myTankID);
		stage.addScreenObject(new Marker(myTank));

		// ゲームUIの作成（HUD）
		this.ui = new GameUI(stage, myTank.getTeam());

		// カメラの初期設定
		this.cameraPosition = new Point2D.Double(0, 0);

		// サーバからのメッセージ受け取り開始
		this.network.start();
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics2D graphics2D = (Graphics2D) graphics;

		// カメラの位置・ズーム度合いに合わせてあらかじめキャンバスをずらしておく。
		graphics2D.transform(this.getCanvasTransform());

		// GameObjectの描画
		stage.draw(graphics2D, this.getWidth(), this.getHeight(), zoomDegrees);

		// GUIの描画
		graphics2D.setTransform(new AffineTransform());
		ui.draw(graphics2D, this.getWidth(), this.getHeight());
	}

	public AffineTransform getCanvasTransform() {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.getWidth() / 2.0, this.getHeight() / 2.0);
		trans.scale(this.zoomDegrees, this.zoomDegrees);
		trans.translate(-this.cameraPosition.x, -this.cameraPosition.y);
		return trans;
	}

	public void zoomCamera(double zoomDelta) {
		this.zoomDegrees -= zoomDelta;
		if (this.zoomDegrees < CAMERA_ZOOM_LOWER_THRESHOLD) {
			this.zoomDegrees = CAMERA_ZOOM_LOWER_THRESHOLD;
		} else if (CAMERA_ZOOM_UPPER_THRESHOLD < this.zoomDegrees) {
			this.zoomDegrees = CAMERA_ZOOM_UPPER_THRESHOLD;
		}
	}

	public void startGameThread() {
		this.gameThread = new Thread(this);
		gameThread.start();
		this.zoomDegrees = this.stage.getJustZoomDegrees(this.getWidth(), this.getHeight()) * 0.9;
	}

	@Override
	public void run() {
		// 1フレームの持ち時間 (ナノ秒)
		long drawInterval = 1000000000L / FPS;
		long nextDrawTime = System.nanoTime() + drawInterval;


		while (gameThread != null) {
			update();
			repaint();

			// フレーム内の処理はは早く終わったりするので時間調整
			long remainingTime = nextDrawTime - System.nanoTime();
			try {
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

		// 画面に描画されている各種オブジェクトのフレームを更新
		stage.update();
		ui.update();

		// ============================= 自分の操作 =============================

		// マウスの長押し判定などに必要
		input.onFrameUpdate();

		if (myTank.isDead()) return;

		// カメラアングルを調整
//		zoomCamera(input.getZoomAmount() * 0.1);

		// 戦車に移動命令を出す
		Point2D.Double moveVector = input.getMoveVector(this.getCanvasTransform());
		if (moveVector.x != 0 || moveVector.y != 0) {
			myTank.move(moveVector);
		}
		network.locateTank(myTankID, myTank.getPosition());

		// マウス位置へ砲塔を向ける命令を出す
		Point2D.Double coordinate = input.getAimedCoordinate(this.getCanvasTransform());
		myTank.aimAt(coordinate);
		network.aimAt(myTankID, coordinate);

		// 戦車に発砲命令を出す。
		if (input.shootBullet()) {
			Bullet bullet = myTank.shootBullet();
			stage.addStageObject(bullet);
			network.shootGun(myTankID);
		}

		// 戦車のブロック作成命令を出す。
		if (input.createBlock()) {
			Block block = myTank.createBlock();
			if (block == null) return;
			stage.addStageObject(block);
			network.createBlock(myTankID);
		}
	}

	private Tank getMyTank() {
		GameObject object = this.stage.getGameObject(myTankID);
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