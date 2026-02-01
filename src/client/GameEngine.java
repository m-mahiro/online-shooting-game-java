package client;

import client.ui.GameUI;
import stage.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class GameEngine implements Runnable {

    public static final int FPS = 60;

    // ゲームオブジェクト
    private GameStage stage;
    private GameUI ui;
    private Tank myTank;

    // 入力・ネットワーク関連
    private InputHandler input;
    private NetworkManager network;

    // ゲームの状態
    private int networkID;
    private int myTankID;
    private Thread gameThread;
    private final StageGenerator generator;

    // キャンバス・カメラ関連
    private int canvasWidth, canvasHeight;
    private double zoomDegrees;
    private Point2D.Double cameraPosition = new Point2D.Double();
    public double CAMERA_ZOOM_UPPER_THRESHOLD = 5;
    public double CAMERA_ZOOM_LOWER_THRESHOLD = 0.07;

    private final Runnable repaintCallback;

    public GameEngine(Runnable repaintCallback, InputHandler inputHandler, StageGenerator generator) {
        this.repaintCallback = repaintCallback;
        this.input = inputHandler;
        this.generator = generator;

        // StageGeneratorを使ってステージを生成
        this.stage = new GameStage(generator);
        
        if (generator.isNetworked()) {
            // 通常モード
            this.network = new NetworkManager(this);
            networkID = this.network.getNetworkClientID();
            myTankID = this.network.getMyTankID();
        } else {
            // 練習モード (ネットワークなし)
            this.network = null;
            // 練習モードでは、生成された最初の戦車を操作対象とする
            this.myTankID = 0;
        }

        // ステージから自分の戦車を取得
        GameObject myTankObject = this.stage.getGameObject(myTankID);
        if (myTankObject instanceof Tank) {
            this.myTank = (Tank) myTankObject;
        } else {
            throw new RuntimeException("My assigned object ("+ myTankID +") is not a Tank!");
        }

        // 自分の戦車にマーカーを追加
        stage.addScreenObject(new Marker(myTank));

        // UIを生成
        this.ui = new GameUI(stage, myTank.getTeam());

        // カメラの初期設定
        this.cameraPosition = new Point2D.Double(0, 0);

        // サーバーからのメッセージ受信を開始
        if (generator.isNetworked()) {
            this.network.start();
        }
    }

    public void setCanvasSize(int width, int height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    public void startGameThread(int width, int height) {
        setCanvasSize(width, height);
        this.gameThread = new Thread(this);
        gameThread.start();
        this.zoomDegrees = this.stage.getJustZoomDegrees(width, height) * 0.9;
    }

    @Override
    public void run() {
        long drawInterval = 1000000000L / FPS;
        long nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaintCallback.run();

            long remainingTime = nextDrawTime - System.nanoTime();
            try {
                if (remainingTime > 0) {
                    Thread.sleep(remainingTime / 1000000);
                }
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        stage.update();
        ui.update();

        input.onFrameUpdate();

        if (myTank.isDead()) return;

        // 照準合わせ
        Point2D.Double coordinate = input.getAimedCoordinate(getCanvasTransform());
        myTank.aimAt(coordinate);
        if (generator.isNetworked()) {
            network.aimAt(myTankID, coordinate);
        }

        // 発射
        if (input.shootBullet()) {
            Bullet bullet = myTank.shootBullet();
            stage.addGameObject(bullet);
            if (generator.isNetworked()) {
                network.shootGun(myTankID);
            }
        }

        // 練習モードではここで処理終了
        if (!generator.isNetworked()) {
            return;
        }

        // --- 通常モードでのみ実行される処理 ---

        // 移動
        Point2D.Double moveVector = input.getMoveVector(getCanvasTransform());
        if (moveVector.x != 0 || moveVector.y != 0) {
            myTank.move(moveVector);
        }
        network.locateTank(myTankID, myTank.getPosition());


        // ブロック生成
        if (input.createBlock()) {
            Block block = myTank.createBlock();
            if (block != null) {
                stage.addGameObject(block);
                network.createBlock(myTankID);
            }
        }
    }

    public void draw(Graphics2D graphics, double windowWidth, double windowHeight) {
        // カメラの移動・ズームを反映させるアフィン変換を作成
        AffineTransform canvasTransform = new AffineTransform();
        canvasTransform.translate(this.canvasWidth / 2.0, this.canvasHeight / 2.0);
        canvasTransform.scale(this.zoomDegrees, this.zoomDegrees);
        canvasTransform.translate(-this.cameraPosition.x, -this.cameraPosition.y);

        // カメラに映る(ウィンドウから見える)範囲を計算
        double visibleWidth = windowWidth / this.zoomDegrees;
        double visibleHeight = windowHeight / this.zoomDegrees;

        // ステージを描画
        AffineTransform originalTransform = graphics.getTransform();
        graphics.setTransform(canvasTransform);
        this.stage.draw(graphics, visibleWidth, visibleHeight);
        graphics.setTransform(originalTransform);  // 元の座標系に戻しておく
    }
    
    public void zoomCamera(double zoomDelta) {
        this.zoomDegrees -= zoomDelta;
        if (this.zoomDegrees < CAMERA_ZOOM_LOWER_THRESHOLD) {
            this.zoomDegrees = CAMERA_ZOOM_LOWER_THRESHOLD;
        } else if (CAMERA_ZOOM_UPPER_THRESHOLD < this.zoomDegrees) {
            this.zoomDegrees = CAMERA_ZOOM_UPPER_THRESHOLD;
        }
    }

    public AffineTransform getCanvasTransform() {
        AffineTransform trans = new AffineTransform();
        trans.translate(this.canvasWidth / 2.0, this.canvasHeight / 2.0);
        trans.scale(this.zoomDegrees, this.zoomDegrees);
        trans.translate(-this.cameraPosition.x, -this.cameraPosition.y);
        return trans;
    }

    public GameStage getStage() {
        return stage;
    }

    public GameUI getUi() {
        return ui;
    }

    public int getMyTankID() {
        return myTankID;
    }
    
    public double getZoomDegrees() {
        return zoomDegrees;
    }
}
