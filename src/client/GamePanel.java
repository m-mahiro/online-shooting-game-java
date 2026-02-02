package client;

import stage.*;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
    private final int myTankID;
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
        int playerCount = networkManager.getPlayerCount();
        this.myTankID = networkManager.getMyTankID();

        // ゲームエンジンの作成
        InputStrategy inputStrategy = createInputStrategy(new MouseKeyboardInput(this));
        StageGenerator generator = createStageGenerator(playerCount);
        this.gameEngine = new GameEngine(this::repaint, inputStrategy, generator, myTankID);

        this.networkManager.setGameEngine(gameEngine);

        // エンジンにリサイズを通知するためのリスナーを追加
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gameEngine.setWindowSize(getWidth(), getHeight());
            }
        });

        // この後、画面が描画されたらaddNotify()が呼ばれ、GameEngineが始動する。
    }

    /**
     * ゲームモードに応じたInputStrategyを生成する。
     *
     * @param inputHandler ユーザー入力を処理するInputHandler
     * @return 生成されたInputStrategy
     */
    private InputStrategy createInputStrategy(InputHandler inputHandler) {
        return new InputStrategy() {

            @Override
            public Point2D.Double getMoveVector(AffineTransform canvasTransform) {
                return inputHandler.getMoveVector(canvasTransform);
            }

            @Override
            public Point2D.Double getAimedCoordinate(AffineTransform canvasTransform) {
                Point2D.Double coordinate = inputHandler.getAimedCoordinate(canvasTransform);
                networkManager.aimAt(myTankID, coordinate);
                return coordinate;
            }

            @Override
            public boolean shootBullet() {
                networkManager.shootGun(myTankID);
                return inputHandler.shootBullet();
            }

            @Override
            public boolean startEnergyCharge() {
                networkManager.startCharge(myTankID);
                return inputHandler.startEnergyCharge();
            }

            @Override
            public boolean finishEnergyCharge() {
                networkManager.finishCharge(myTankID);
                return inputHandler.finishEnergyCharge();
            }

            @Override
            public boolean createBlock() {
                networkManager.createBlock(myTankID);
                return inputHandler.createBlock();
            }

            @Override
            public int getZoomAmount() {
                return inputHandler.getZoomAmount();
            }

            @Override
            public void onFrameUpdate() {
                inputHandler.onFrameUpdate();
            }
        };
    }

    /**
     * ゲームモードに応じたStageGeneratorを生成する。
     *
     * @return 生成されたStageGenerator
     */
    private StageGenerator createStageGenerator(int playerCount) {
            return new StageGenerator() {
                private final Base redBase = new Base(2000, 2000, Team.RED);
                private final Base blueBase = new Base(-2000, -2000, Team.BLUE);
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
            };
    }

    /**
     * パネルがコンテナに追加された際に呼ばれる。
     * ゲームスレッドを開始する。
     */
    @Override
    public void addNotify() {
        super.addNotify();
        if (gameEngine == null) return;
        gameEngine.startGameThread(getWidth(), getHeight());
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
}