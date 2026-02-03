package client;

import client.ui.GamePanelUI;
import client.ui.GameUI;
import stage.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * ゲームの描画とゲームエンジンを管理するパネルクラス。
 * ゲームの状態を表示し、入力を受け付ける役割を持つ。
 */
public class GamePanel extends JPanel {

    private final GameEngine gameEngine;
    private final NetworkManager networkManager;

    /**
     * 練習モードまたは通常モードでゲームパネルを初期化する。
     * パネルの設定、入力ハンドラの登録、ゲームエンジンの生成を行う。
     */
    public GamePanel() {

        // パネル設定
        this.setBackground(Color.WHITE);
        this.setDoubleBuffered(true);
        this.setPreferredSize(new Dimension(1000, 700));

        // サーバに接続して色々情報をもらう
        // todo: 接続してから、これら二つの情報をもらうまではかなり時間がかかるので、将来的には別のJPanelにしないといけない
        this.networkManager = new NetworkManager();
        int myTankID = networkManager.getMyTankID();
        int playerCount = networkManager.getPlayerCount();

        // ステージの作成
        StageGenerator generator = createStageGenerator(playerCount);
        GameStage stage = new GameStage(generator);

        // 自分の戦車とチームを取得
        Tank myTank = (Tank) stage.getGameObject(myTankID);
        Team myTeam = myTank.getTeam();

        // UIの作成
        GameUI ui = new GamePanelUI(stage, myTeam);

        // 入力や通信に関する取り決め(Strategy)を作成
        NetworkStrategy networkStrategy = createNetworkStrategy();
        InputStrategy inputStrategy = createInputStrategy(new MouseKeyboardInput(this), networkStrategy, myTankID);

        // GameEngineを作成
        this.gameEngine = new GameEngine(stage, ui, myTankID, this::repaint, inputStrategy);

        // エンジンにリサイズを通知するためのリスナーを追加
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gameEngine.setWindowSize(getWidth(), getHeight());
            }
        });

        // サーバからの情報を基にGameEngineを操作する必要がある
        this.networkManager.setGameEngine(gameEngine);

        // この後、画面が描画されたらaddNotify()が呼ばれ、GameEngineが始動する。
    }

    /**
     * パネルがコンテナに追加された際に呼ばれる。
     * ゲームスレッドを開始する。
     */
    @Override
    public void addNotify() {
        super.addNotify();
        if (gameEngine == null) return;
        gameEngine.startGameThread();
    }

    /**
     * パネルの描画を行う。
     * ゲームオブジェクトとUIを描画する。
     *
     * @param graphics 描画に使用するGraphicsオブジェクト
     */
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;

        if (gameEngine == null) return;
        gameEngine.draw(g);

    }

    /**
     * ゲーム画面用のInputStrategyを生成する。
     *
     * @param inputHandler ユーザー入力を処理するInputHandler
     * @param network ネットワーク通信を管理するNetworkStrategy
     * @param tankID 戦車のID
     * @return 生成されたInputStrategy
     */
    private InputStrategy createInputStrategy(InputHandler inputHandler, NetworkStrategy network, int tankID) {
        return new InputStrategy() {

            @Override
            public void handleInput(Tank myTank, AffineTransform canvasTransform, GameStage stage) {
                // 照準合わせ
                Point2D.Double coordinate = inputHandler.getAimedCoordinate(canvasTransform);
                myTank.aimAt(coordinate);
                network.aimAt(tankID, coordinate);

                // 発射
                if (inputHandler.shootBullet()) {
                    Bullet bullet = myTank.shootBullet();
                    if (bullet != null) {
                        stage.addGameObject(bullet);
                        network.shootGun(tankID);
                    }
                }

                // 移動
                Point2D.Double moveVector = inputHandler.getMotionDirection(canvasTransform);
                if (moveVector.x != 0 || moveVector.y != 0) {
                    myTank.move(moveVector);
                }

                // ブロック生成
                if (inputHandler.createBlock()) {
                    Block block = myTank.createBlock();
                    if (block != null) {
                        stage.addGameObject(block);
                        network.createBlock(tankID);
                    }
                }
            }

            @Override
            public void onFrameUpdate() {
                inputHandler.onFrameUpdate();
            }
        };
    }

    /**
     * ゲーム画面用のNetworkStrategyを生成する。
     *
     * @return 生成されたNetworkStrategy
     */
    private NetworkStrategy createNetworkStrategy() {
        return new NetworkStrategy() {
            @Override
            public void aimAt(int tankID, Point2D.Double coordinate) {
                networkManager.aimAt(tankID, coordinate);
            }

            @Override
            public void shootGun(int tankID) {
                networkManager.shootGun(tankID);
            }

            @Override
            public void startCharge(int tankID) {
                networkManager.startCharge(tankID);
            }

            @Override
            public void finishCharge(int tankID) {
                networkManager.finishCharge(tankID);
            }

            @Override
            public void createBlock(int tankID) {
                networkManager.createBlock(tankID);
            }
        };
    }

    /**
     * ゲーム画面用のStageGeneratorを生成する。
     *
     * @return 生成されたStageGenerator
     */
    private StageGenerator createStageGenerator(int playerCount) {
            return new StageGenerator() {
                private final Base redBase = new Base(2000, 2000, Team.RED, playerCount * 80);
                private final Base blueBase = new Base(-2000, -2000, Team.BLUE, playerCount * 80);
                private final int stageWidth = 6000;
                private final int stageHeight = 6000;

                @Override
                public GameObject[] getGameObjects() {
                    ArrayList<GameObject> objects = new ArrayList<>();

                    // 戦車の生成
                    for (int i = 0; i < playerCount; i++) {
                        Tank tank = new Tank(i % 2 == 0 ? redBase : blueBase);
                        objects.add(tank);
                    }

                    // 壁の生成
                    int verticalWall = stageHeight / Wall.HEIGHT;
                    int horizontalWall = stageWidth / Wall.WIDTH;
                    for (int i = 0; i <= verticalWall; i++) {
                        for (int j = 0; j <= horizontalWall; j++) {
                            if (i != 0 && i != verticalWall && j != 0 && j != horizontalWall) continue;
                            double x = Wall.WIDTH * i - stageWidth / 2.0;
                            double y = Wall.HEIGHT * j - stageHeight / 2.0;
                            Point2D.Double point = new Point2D.Double(x, y);
                            Wall wall = new Wall(point);
                            objects.add(wall);
                        }
                    }

                    // リスポーン地点の生成
                    objects.add(redBase);
                    objects.add(blueBase);


                    return objects.toArray(new GameObject[0]);
                }

                @Override
                public ScreenObject[] getScreenObjects() {
                    return new ScreenObject[0];
                }

                @Override
                public Base getRedBase() {
                    return redBase;
                }

                @Override
                public Base getBlueBase() {
                    return blueBase;
                }

                @Override
                public int getStageWidth() {
                    return stageWidth;
                }

                @Override
                public int getStageHeight() {
                    return stageHeight;
                }

                @Override
                public void drawBackground(Graphics2D graphics, double visibleWidth, double visibleHeight, double animationFrame) {
                    // 画像リソースの読み込み
                    java.awt.image.BufferedImage floorTexture, outerStageTexture;
                    try {
                        floorTexture = javax.imageio.ImageIO.read(java.util.Objects.requireNonNull(getClass().getResource("assets/floor_texture.png")));
                        outerStageTexture = javax.imageio.ImageIO.read(java.util.Objects.requireNonNull(getClass().getResource("assets/ocean_texture.png")));
                    } catch (java.io.IOException e) {
                        throw new RuntimeException(e);
                    }

                    // ステージ外の描画
                    double textureSize = 1000;
                    double translate = animationFrame * 10 % textureSize;
                    java.awt.geom.Rectangle2D outerStageAnchor = new java.awt.geom.Rectangle2D.Double(translate, translate, textureSize, textureSize);
                    TexturePaint outerStagePaint = new TexturePaint(outerStageTexture, outerStageAnchor);
                    graphics.setPaint(outerStagePaint);
                    int fillWidth = (int) (stageWidth + visibleWidth);
                    int fillHeight = (int) (stageHeight + visibleHeight);
                    graphics.fillRect(-fillWidth / 2, -fillHeight / 2, fillWidth, fillHeight);

                    // フローリングの描画
                    java.awt.geom.Rectangle2D floorAnchor = new java.awt.geom.Rectangle2D.Double(0, 0, floorTexture.getWidth(), floorTexture.getHeight());
                    TexturePaint floorPaint = new TexturePaint(floorTexture, floorAnchor);
                    graphics.setPaint(floorPaint);
                    graphics.fillRect(-stageWidth / 2, -stageHeight / 2, stageWidth, stageHeight);
                }
            };
    }

}