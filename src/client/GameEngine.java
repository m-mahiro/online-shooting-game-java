package client;

import client.ui.GamePanelUI;
import client.ui.GameUI;
import stage.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ゲームのメインエンジンクラス。
 * ゲームループ、入力処理、描画、ゲームオブジェクトの管理を担当します。
 */
public class GameEngine implements Runnable {

    /** ゲームのフレームレート（1秒あたりのフレーム数） */
    public static final int FPS = 60;

    // 表示関連
    private GameStage stage;
    private GameUI ui;

    // 入力用
    private InputStrategy input;

    // ネットワーク通信用
    private NetworkStrategy network;

    // update→drawのゲームループ用スレッド
    private Thread gameThread;

    // オブジェクト管理
    private int nextScreenObjectID = 0;
    private final Map<Integer, ScreenObject> screenObjects = new ConcurrentHashMap<>();
    private Tank myTank;
    private int myTankID;

    // キャンバス・カメラ関連
    private int windowWidth, windowHeight;
    private double zoomDegrees;
    private Point2D.Double cameraPosition;
    public double CAMERA_ZOOM_UPPER_THRESHOLD = 5;
    public double CAMERA_ZOOM_LOWER_THRESHOLD = 0.07;

    private final Runnable repaintCallback;

    /**
     * GameEngineのコンストラクタ。
     * ゲームの初期化、ステージ生成、ネットワーク接続、入力戦略の設定を行います。
     *
     * @param repaintCallback 画面を再描画するためのコールバック
     * @param inputStrategy 入力処理を管理するInputStrategy
     * @param networkStrategy ネットワーク送信を管理するNetworkStrategy
     */
    public GameEngine(GameStage stage, GameUI ui, int myTankID, Runnable repaintCallback, InputStrategy inputStrategy, NetworkStrategy networkStrategy) {
        this.stage = stage;
        this.ui = ui;
        this.myTankID = myTankID;
        this.repaintCallback = repaintCallback;
        this.input = inputStrategy;
        this.network = networkStrategy;


        // 自分の戦車を取得
        this.myTank = (Tank) this.stage.getGameObject(myTankID);

        // 自分の戦車にマーカーを追加
        this.addScreenObject(new Marker(myTank));

        // UIを生成
        this.ui = new GamePanelUI(stage, myTank.getTeam());

        // カメラの初期設定
        this.cameraPosition = new Point2D.Double(0, 0);
    }

    /**
     * ウィンドウサイズを設定します。
     *
     * @param width ウィンドウの幅
     * @param height ウィンドウの高さ
     */
    public void setWindowSize(int width, int height) {
        this.windowWidth = width;
        this.windowHeight = height;
        this.zoomDegrees = getJustZoom(width, height);
    }

    /**
     * ゲームスレッドを開始します。
     * ゲームループが別スレッドで実行されます。
     */
    public void startGameThread() {
        this.gameThread = new Thread(this);
        gameThread.start();
    }

    /**
     * ステージ全体が表示されるちょうど良いズーム倍率を計算します。
     *
     * @param windowWidth ウィンドウの幅
     * @param windowHeight ウィンドウの高さ
     * @return ステージ全体が表示されるズーム倍率
     */
    public double getJustZoom(double windowWidth, double windowHeight) {
        double x = windowWidth / stage.getStageWidth();
        double y = windowHeight / stage.getStageHeight();
        return Math.min(x, y);
    }

    /**
     * ゲームループを実行します。
     * 一定のフレームレートで更新と描画を繰り返します。
     */
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

    /**
     * ゲーム状態を1フレーム分更新します。
     * ステージ、UI、入力、戦車の状態などを更新します。
     */
    public void update() {

        // 各クラスにフレーム更新を通知
        stage.update();
        ui.update();
        input.onFrameUpdate();

        // 削除可能なScreenObjectがあれば削除
        Iterator<ScreenObject> iterator = this.screenObjects.values().iterator();
        while (iterator.hasNext()) {
            ScreenObject object = iterator.next();
            object.update();
            if (object.isExpired()) {
                iterator.remove();
            }
        }
        
        // =========================== 自分の戦車の操作 ===========================

        if (myTank.isDead()) return;

        // 照準合わせ
        Point2D.Double coordinate = input.getAimedCoordinate(getCanvasTransform());
        myTank.aimAt(coordinate);
        network.aimAt(myTankID, coordinate);

        // 発射
        if (input.shootBullet()) {
            Bullet bullet = myTank.shootBullet();
            stage.addGameObject(bullet);
            network.shootGun(myTankID);
        }

        // 移動
        Point2D.Double moveVector = input.getMotionDirection(getCanvasTransform());
        if (moveVector.x != 0 || moveVector.y != 0) {
            myTank.move(moveVector);
        }

        // ブロック生成
        if (input.createBlock()) {
            Block block = myTank.createBlock();
            if (block != null) {
                stage.addGameObject(block);
                network.createBlock(myTankID);
            }
        }
    }

    /**
     * ゲーム画面を描画します。
     * カメラ変換を適用し、ステージとスクリーンオブジェクトを描画します。
     *
     * @param graphics 描画に使用するGraphics2Dオブジェクト
     */
    public void draw(Graphics2D graphics) {
        // カメラの移動・ズームを反映させるアフィン変換を作成
        AffineTransform canvasTransform = new AffineTransform();
        canvasTransform.translate(this.windowWidth / 2.0, this.windowHeight / 2.0);
        canvasTransform.translate(-this.cameraPosition.x, -this.cameraPosition.y);
        canvasTransform.scale(this.zoomDegrees, this.zoomDegrees);

        // カメラに映る(ウィンドウから見える)範囲を計算
        double visibleWidth = this.windowWidth / this.zoomDegrees;
        double visibleHeight = this.windowHeight / this.zoomDegrees;

        // ステージを描画
        AffineTransform originalTransform = graphics.getTransform();
        graphics.setTransform(canvasTransform);
        this.stage.draw(graphics, visibleWidth, visibleHeight);

        // ScreenObjectの描画
        for (ScreenObject object : screenObjects.values()) {
            object.draw(graphics);
        }

        graphics.setTransform(originalTransform);  // 元の座標系に戻す

        // GameUIの描画
        ui.draw(graphics, this.windowWidth, this.windowHeight);

    }

    /**
     * カメラのズームを変更します。
     * ズームの上限と下限が適用されます。
     *
     * @param zoomDelta ズームの変化量（負の値でズームイン、正の値でズームアウト）
     */
    public void zoomCamera(double zoomDelta) {
        this.zoomDegrees -= zoomDelta;
        if (this.zoomDegrees < CAMERA_ZOOM_LOWER_THRESHOLD) {
            this.zoomDegrees = CAMERA_ZOOM_LOWER_THRESHOLD;
        } else if (CAMERA_ZOOM_UPPER_THRESHOLD < this.zoomDegrees) {
            this.zoomDegrees = CAMERA_ZOOM_UPPER_THRESHOLD;
        }
    }

    /**
     * キャンバスの座標変換（カメラ位置とズームを反映）を取得します。
     *
     * @return カメラ変換を含むAffineTransform
     */
    public AffineTransform getCanvasTransform() {
        AffineTransform trans = new AffineTransform();
        trans.translate(this.windowWidth / 2.0, this.windowHeight / 2.0);
        trans.scale(this.zoomDegrees, this.zoomDegrees);
        trans.translate(-this.cameraPosition.x, -this.cameraPosition.y);
        return trans;
    }

    /**
     * ゲームステージを取得します。
     *
     * @return ゲームステージ
     */
    public GameStage getStage() {
        return stage;
    }


    /**
     * プレイヤーが操作する戦車のIDを取得します。
     *
     * @return 自分の戦車のID
     */
    public int getMyTankID() {
        return myTankID;
    }

    /**
     * 現在のズーム倍率を取得します。
     *
     * @return ズーム倍率
     */
    public double getZoomDegrees() {
        return zoomDegrees;
    }

    /**
     * ステージの座標系を用いてスクリーンに表示されるオブジェクト(<code>ScreenObject</code>)を追加します。
     *
     * @param screenObject 追加したいスクリーンオブジェクト
     * @return 追加されたスクリーンオブジェクトに割り振られたオブジェクトID
     */
    public int addScreenObject(ScreenObject screenObject) {
        if (screenObject == null) return -1;
        int id = getNextScreenObjectID();
        screenObjects.put(id, screenObject);
        return id;
    }

    /**
     * ステージの座標系を用いてスクリーンに表示されるオブジェクト(<code>ScreenObject</code>)を追加します。
     *
     * @param screenObjects 追加したいスクリーンオブジェクトの配列
     * @return 追加されたスクリーンオブジェクトに割り振られたオブジェクトIDの配列。順番は引数に与えれた<code>screenObjects</code>に対応しています。
     */
    public int[] addScreenObjects(ScreenObject[] screenObjects) {
        int length = screenObjects.length;
        int[] idList = new int[length];
        int i = 0;
        for (ScreenObject obj : screenObjects) idList[i++] = addScreenObject(obj);
        return idList;
    }

    /**
     * 与えらえらたオブジェクトIDに対応する<code>ScreenObject</code>を返す。
     *
     * @param id オブジェクトID
     * @return idに対応する対応する<code>ScreenObject</code>
     */
    public ScreenObject getScreenObject(int id) {
        return screenObjects.get(id);
    }

    /**
     * オブジェクトIDを返す。
     *
     * @return オブジェクトID
     */
    private int getNextScreenObjectID() {
        synchronized (this) {
            int id = nextScreenObjectID;
            nextScreenObjectID++;
            return id;
        }
    }
}
