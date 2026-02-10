package client.ui;

import client.*;
import stage.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static stage.Team.*;

public class WaitingRoomPanel extends JPanel {

	private final GameEngine gameEngine;
	private GameWindow gameWindow;
	private NetworkManager networkManager;

	public WaitingRoomPanel(GameWindow gameWindow) {
		this.gameWindow = gameWindow;

		// NetworkManagerを作成し、コールバックを設定
		this.networkManager = new NetworkManager(createOnReadyCallback());
		this.networkManager.start(); // スレッドを開始

		setBackground(Color.WHITE);
		setPreferredSize(new Dimension(800, 600));

		// =========================== 背景のアニメーション(ゲームステージ)を作成 ===========================

		// ステージの作成
		StageGenerator generator = createStageGenerator();
		GameStage stage = new GameStage(generator);
		ScreenObject[] screenObjects = generator.getScreenObjects();

		// UIの作成
		GameUI ui = createGameUI();

		// 入力や通信に関する取り決め(Strategy)を作成
		InputStrategy inputStrategy = createInputStrategy(new MouseKeyboardInput(this));

		// GameEngineを作成
		int myTankID = 0;
		Runnable onFinishCallback = () -> {};
		this.gameEngine = new GameEngine(stage, ui, screenObjects, myTankID, this::repaint, onFinishCallback, inputStrategy);

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
	 * 待機室用のInputStrategyを生成する。
	 * 待機室では入力があっても何もしない
	 *
	 * @param inputHandler ユーザー入力を処理するInputHandler
	 * @return 生成されたInputStrategy
	 */
	private InputStrategy createInputStrategy(InputHandler inputHandler) {
		return new InputStrategy() {
			@Override
			public void handleInput(Tank myTank, AffineTransform canvasTransform, GameStage stage) {
				//　何もしない
			}

			@Override
			public void onFrameUpdate() {
				// 何もしない
			}
		};
	}

	/**
	 * 待機室用のStageGeneratorを生成する。
	 * WAITING...のアニメーションを表示する。
	 *
	 * @return 生成されたStageGenerator
	 */
	private StageGenerator createStageGenerator() {
		return new StageGenerator() {
			private final double baseHorizontalOffset = 1500;
			private final double baseVerticalOffset = 800;
			private final Base redBase = new Base(baseHorizontalOffset, baseVerticalOffset, RED, 100);
			private final Base blueBase = new Base(-baseHorizontalOffset, -baseVerticalOffset, BLUE, 100);

			@Override
			public GameObject[] getGameObjects() {
				ArrayList<GameObject> objects = new ArrayList<>();

				// ダミーの戦車（myTankID = 0用）
				Tank dummyTank = new Tank(redBase);
				dummyTank.setPosition(new Point2D.Double(10000, 10000)); // 画面外に配置
				objects.add(dummyTank);

				// リスポーン地点
				objects.add(redBase);
				objects.add(blueBase);

				return objects.toArray(new GameObject[0]);
			}

			@Override
			public ScreenObject[] getScreenObjects() {
				int n = 8;
				ScreenObject[] screenObjects = new ScreenObject[n];

				// WAITING...
				String[] list = new String[n];
				list[0] = "W";
				list[1] = "A";
				list[2] = "I";
				list[3] = "T";
				list[4] = "I";
				list[5] = "N";
				list[6] = "G";
				list[7] = "...";

				int customSize = RotationChars.size / 2; // 半分のサイズ
				for (int i = 0; i < list.length; i++) {
					String s = list[i];
					double x = 200.0 * (i - list.length / 2.0) + customSize / 2.0;
					double y = 0;
					screenObjects[i] = new RotationChars(s, new Point2D.Double(x, y), i * 10.0, Color.WHITE, customSize);
				}
				return screenObjects;
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
			public double getStageWidth() {
				return baseHorizontalOffset * 3;
			}

			@Override
			public double getStageHeight() {
				return 1;
			}

			@Override
			public void drawBackground(Graphics2D graphics, double visibleWidth, double visibleHeight, double animationFrame) {
				// 待機室では何も描画しない
			}
		};
	}

	/**
	 * 待機室用のGameUIを生成する。
	 * 待機室では何も描画しないため、空の実装を返す。
	 *
	 * @return 生成されたGameUI
	 */
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

	/**
	 * PLAYER_COUNTが揃ったときに呼ばれるコールバック関数
	 */
	public Runnable createOnReadyCallback() {
		return () -> {
			SwingUtilities.invokeLater(() -> {
				gameWindow.showGamePanel(networkManager);
			});
		};
	}
}
