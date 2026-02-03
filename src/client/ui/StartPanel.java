package client.ui;

import client.*;
import stage.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * スタート画面のパネルクラス。
 * ゲーム開始ボタンを配置し、ゲームの開始を制御する。
 */
public class StartPanel extends JPanel {
    private final GameEngine gameEngine;
    private JButton startButton;

    /**
     * StartScreenPanelのコンストラクタ。
     * スタートボタンを生成し、中央に配置する。
     *
     * @param startListener スタートボタンが押された時の処理
     */
    public StartPanel(ActionListener startListener) {

        // =========================== スタートボタンの配置 ===========================

        // レイアウトマネージャーを設定
        setLayout(new GridBagLayout());

        // モダンなスタートボタンを生成
        startButton = new JButton("Start Game");
        startButton.setFont(new Font("Segoe UI", Font.BOLD, 28));
        startButton.setPreferredSize(new Dimension(250, 80));

        // モダンな色設定
        startButton.setBackground(new Color(0, 122, 255)); // 鮮やかな青
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);

        // 角丸ボーダー風の効果（マウスホバー時のエフェクト）
        startButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(0, 100, 220));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                startButton.setBackground(new Color(0, 122, 255));
            }
        });

        // ボタンにアクションリスナーを設定
        startButton.addActionListener(startListener);

        // ボタンをパネル中央に配置
        add(startButton);


        // =========================== 背景のデモ映像(ゲームステージ)を作成 ===========================

        // ステージの作成
        StageGenerator generator = createStageGenerator();
        GameStage stage = new GameStage(generator);

        // 自分の戦車とチームを取得
        int myTankID = 0;
        Tank myTank = (Tank) stage.getGameObject(myTankID);
        Team myTeam = myTank.getTeam();

        // UIの作成
        GameUI ui = createGameUI();

        // 入力や通信に関する取り決め(Strategy)を作成
        InputStrategy inputStrategy = createInputStrategy(new MouseKeyboardInput(this));
        NetworkStrategy networkStrategy = createNetworkStrategy();

        // GameEngineを作成
        this.gameEngine = new GameEngine(stage, ui, myTankID, this::repaint, inputStrategy, networkStrategy);

        // エンジンにリサイズを通知するためのリスナーを追加
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                gameEngine.setWindowSize(getWidth(), getHeight());
            }
        });

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
     * スタート画面用のInputStrategyを生成する。
     *
     * @param inputHandler ユーザー入力を処理するInputHandler
     * @return 生成されたInputStrategy
     */
    private InputStrategy createInputStrategy(InputHandler inputHandler) {
        return new InputStrategy() {

            @Override
            public Point2D.Double getMotionDirection(AffineTransform canvasTransform) {
                return inputHandler.getMotionDirection(canvasTransform);
            }

            @Override
            public Point2D.Double getAimedCoordinate(AffineTransform canvasTransform) {
                return inputHandler.getAimedCoordinate(canvasTransform);
            }

            @Override
            public boolean shootBullet() {
                return inputHandler.shootBullet();
            }

            @Override
            public boolean startEnergyCharge() {
                return inputHandler.startEnergyCharge();
            }

            @Override
            public boolean finishEnergyCharge() {
                return inputHandler.finishEnergyCharge();
            }

            @Override
            public boolean createBlock() {
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
     * スタート画面用のNetworkStrategyを生成する。
     * スタート画面では何も通信を行わない。
     *
     * @return 生成されたNetworkStrategy
     */
    private NetworkStrategy createNetworkStrategy() {
        return new NetworkStrategy() {
            @Override
            public void aimAt(int tankID, Point2D.Double coordinate) {}

            @Override
            public void shootGun(int tankID) {}

            @Override
            public void startCharge(int tankID) {}

            @Override
            public void finishCharge(int tankID) {}

            @Override
            public void createBlock(int tankID) {}
        };
    }

    /**
     * スタート画面用のStageGeneratorを生成する。
     *
     * @return 生成されたStageGenerator
     */
    private StageGenerator createStageGenerator() {
        return new StageGenerator() {
            private final Base redBase = new Base(2000, 2000, Team.RED, 100);
            private final Base blueBase = new Base(-2000, -2000, Team.BLUE, 100);
            private final int stageWidth = 6000;
            private final int stageHeight = 6000;
            private final int playerCount = 2;

            @Override
            public GameObject[] getGameObjects() {
                ArrayList<GameObject> objects = new ArrayList<>();

                // 戦車の生成
                for (int i = 0; i < playerCount; i++) {
                    Tank tank = new Tank(i % 2 == 0 ? redBase : blueBase);
                    objects.add(tank);
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
                // スタート画面では何も描画しない
            }
        };
    }

    private GameUI createGameUI() {
        return new GameUI() {
            @Override
            public void update() {

            }

            @Override
            public void draw(Graphics2D graphics2D, int windowWidth, int windowHeight) {

            }
        };
    }

}
