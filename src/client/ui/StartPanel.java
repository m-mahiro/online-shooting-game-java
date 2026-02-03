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

import static stage.Team.*;

/**
 * スタート画面のパネルクラス。
 * ゲーム開始ボタンを配置し、ゲームの開始を制御する。
 */
public class StartPanel extends JPanel {
	private final GameEngine gameEngine;
	private final JButton startButton;
	private final JButton howToPlayButton;

	/**
	 * StartScreenPanelのコンストラクタ。
	 * スタートボタンとHowToPlayボタンを生成し、配置する。
	 *
	 * @param onStartGame スタートボタンが押された時の処理
	 * @param onHowToPlay HowToPlayボタンが押された時の処理
	 */
	public StartPanel(ActionListener onStartGame, ActionListener onHowToPlay) {

		// ===========================　ボタンの配置  ===========================

		// レイアウトマネージャーをnullに設定（絶対配置）
		setLayout(null);

		// How to Playボタンを生成
		howToPlayButton = new JButton("How to Play");
		howToPlayButton.setFont(new Font("Segoe UI", Font.BOLD, 28));

		// Start Gameボタンを生成
		startButton = new JButton("Start Game");
		startButton.setFont(new Font("Segoe UI", Font.BOLD, 28));

		// モダンな色設定
		howToPlayButton.setBackground(new Color(0, 122, 255));
		howToPlayButton.setForeground(Color.WHITE);
		howToPlayButton.setFocusPainted(false);
		howToPlayButton.setBorderPainted(false);

		startButton.setBackground(new Color(0, 122, 255));
		startButton.setForeground(Color.WHITE);
		startButton.setFocusPainted(false);
		startButton.setBorderPainted(false);

		// ボタンにアクションリスナーを設定
		startButton.addActionListener(onStartGame);
		howToPlayButton.addActionListener(onHowToPlay);

		// ボタンを追加
		add(howToPlayButton);
		add(startButton);

		// 画面サイズに応じてボタン位置とサイズを更新するメソッド
		Runnable updateButtonBounds = () -> {
			int panelWidth = getWidth();
			int panelHeight = getHeight();

			// ボタンのサイズ（画面幅の割合で設定）
			int buttonWidth = panelWidth / 8;
			int buttonHeight = buttonWidth / 3;

			// ボタンのy座標（共通)
			int y = (int) (panelHeight * 0.6);

			// ボタンのx座標
			int center = panelWidth / 2;
			int howToPlayX = center - 200 - buttonWidth / 2;
			int startX = center + 200 - buttonWidth / 2;

			// 座標を設定
			howToPlayButton.setBounds(howToPlayX, y, buttonWidth, buttonHeight);
			startButton.setBounds(startX, y, buttonWidth, buttonHeight);
		};

		// 初期配置
		updateButtonBounds.run();

		// ウィンドウサイズ変更時に再配置
		addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentResized(java.awt.event.ComponentEvent evt) {
				updateButtonBounds.run();
			}
		});

		// マウスホバーエフェクト
		howToPlayButton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				howToPlayButton.setBackground(new Color(0, 100, 220));
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				howToPlayButton.setBackground(new Color(0, 122, 255));
			}
		});

		startButton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				startButton.setBackground(new Color(0, 100, 220));
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				startButton.setBackground(new Color(0, 122, 255));
			}
		});

		// =========================== 背景のデモ映像(ゲームステージ)を作成 ===========================

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
	 * スタート画面用のInputStrategyを生成する。
	 * スタート画面では入力があっても何もしない
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
	 * スタート画面用のNetworkStrategyを生成する。
	 * スタート画面では何も通信を行わない。
	 *
	 * @return 生成されたNetworkStrategy
	 */
	private NetworkStrategy createNetworkStrategy() {
		return new NetworkStrategy() {
			@Override
			public void aimAt(int tankID, Point2D.Double coordinate) {
			}

			@Override
			public void shootGun(int tankID) {
			}

			@Override
			public void startCharge(int tankID) {
			}

			@Override
			public void finishCharge(int tankID) {
			}

			@Override
			public void createBlock(int tankID) {
			}
		};
	}

	/**
	 * スタート画面用のStageGeneratorを生成する。
	 * 背景のデモ映像として使用するゲームステージを作成する。
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

				// 自動で動くスタート画面用の戦車
				int tankCount = 10;
				double spawnIntervalFrame = 120;
				double velocity = 5;
				double travelLimit = velocity * spawnIntervalFrame * tankCount;
				for (int i = 0; i < 10; i++) {
					double x = spawnIntervalFrame * velocity * i;
					Tank redTank = new StartScreenTank(redBase, velocity, travelLimit);
					Tank blueTank = new StartScreenTank(blueBase, velocity, travelLimit);
					double redX = redBase.getPosition().x - x;
					double blueX = blueBase.getPosition().x + x;
					double redY = redBase.getPosition().y;
					double blueY = blueBase.getPosition().y;
					redTank.setPosition(new Point2D.Double(redX, redY));
					blueTank.setPosition(new Point2D.Double(blueX, blueY));
					objects.add(redTank);
					objects.add(blueTank);
				}

				// リスポーン地点
				objects.add(redBase);
				objects.add(blueBase);

				return objects.toArray(new GameObject[0]);
			}

			@Override
			public ScreenObject[] getScreenObjects() {
				int n = 8;
				ScreenObject[] screenObjects = new ScreenObject[n];

				// RESPAWN!!
				String[] list = new String[n];
				list[0] = "R";
				list[1] = "E";
				list[2] = "S";
				list[3] = "P";
				list[4] = "A";
				list[5] = "W";
				list[6] = "N";
				list[7] = "!!";

				for (int i = 0; i < list.length; i++) {
					String s = list[i];
					double x = 400.0 * (i - list.length / 2.0) + RotationChars.size / 2.0;
					double y = -300;
					screenObjects[i] = new RotationChars(s, new Point2D.Double(x, y), i * 10, Color.BLACK);
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
				// スタート画面では何も描画しない
			}
		};
	}

	/**
	 * スタート画面用のGameUIを生成する。
	 * スタート画面では何も描画しないため、空の実装を返す。
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

}
