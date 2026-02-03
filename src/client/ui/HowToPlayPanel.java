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
 * 遊び方画面のパネルクラス。
 * Backボタンを配置し、ゲームの操作説明を表示する。
 */
public class HowToPlayPanel extends JPanel {
	private final GameEngine gameEngine;
	private final JButton backButton;

	/**
	 * HowToPlayPanelのコンストラクタ。
	 * Backボタンを生成し、配置する。
	 *
	 * @param onBack Backボタンが押された時の処理
	 */
	public HowToPlayPanel(ActionListener onBack) {

		// ===========================　ボタンの配置  ===========================

		// レイアウトマネージャーをnullに設定（絶対配置）
		setLayout(null);

		// Backボタンを生成
		backButton = new JButton("Back");
		backButton.setFont(new Font("Segoe UI", Font.BOLD, 35));

		// モダンな色設定
		backButton.setBackground(new Color(0, 122, 255));
		backButton.setForeground(Color.WHITE);
		backButton.setFocusPainted(false);
		backButton.setBorderPainted(false);

		// ボタンにアクションリスナーを設定
		backButton.addActionListener(onBack);

		// ボタンを追加
		add(backButton);

		// 画面サイズに応じてボタン位置とサイズを更新するメソッド
		Runnable updateButtonBounds = () -> {
			int panelWidth = getWidth();
			int panelHeight = getHeight();

			// ボタンのサイズ（画面幅の割合で設定）
			int buttonWidth = panelWidth / 8;
			int buttonHeight = buttonWidth / 3;

			// 左上に配置（マージン20px）
			int x = 20;
			int y = 20;

			// 座標を設定
			backButton.setBounds(x, y, buttonWidth, buttonHeight);
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
		backButton.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				backButton.setBackground(new Color(0, 100, 220));
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				backButton.setBackground(new Color(0, 122, 255));
			}
		});

		// =========================== 背景のデモ映像(ゲームステージ)を作成 ===========================

		// ステージの作成
		StageGenerator generator = createStageGenerator();
		GameStage stage = new GameStage(generator);
		ScreenObject[] screenObjects = generator.getScreenObjects();

		// UIの作成
		GameUI ui = new HowToPlaySlideShow();

		// 入力や通信に関する取り決め(Strategy)を作成
		InputStrategy inputStrategy = createInputStrategy(new MouseKeyboardInput(this));

		// GameEngineを作成
		int myTankID = 1;
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
	 * 遊び方画面用のInputStrategyを生成する。
	 * 遊び方画面では入力があっても何もしない
	 *
	 * @param inputHandler ユーザー入力を処理するInputHandler
	 * @return 生成されたInputStrategy
	 */
	private InputStrategy createInputStrategy(InputHandler inputHandler) {
		return new InputStrategy() {

			@Override
			public void handleInput(Tank myTank, AffineTransform canvasTransform, GameStage stage) {
				// 照準合わせ
				Point2D.Double coordinate = inputHandler.getAimedCoordinate(canvasTransform);
				myTank.aimAt(coordinate);

				boolean hasFinished = stage.hasFinished();

				// 発射
				if (inputHandler.shootBullet() && !hasFinished) {
					Bullet bullet = myTank.shootBullet();
					if (bullet != null) {
						stage.addGameObject(bullet);
					}
				}

				// 移動
				Point2D.Double moveVector = inputHandler.getMotionDirection(canvasTransform);
				if (moveVector.x != 0 || moveVector.y != 0) {
					myTank.move(moveVector);
				}

				// ブロック生成
				if (inputHandler.createBlock() && !hasFinished) {
					Block block = myTank.createBlock();
					if (block != null) {
						stage.addGameObject(block);
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
	 * 遊び方画面用のStageGeneratorを生成する。
	 * 背景のデモ映像として使用するゲームステージを作成する。
	 * GamePanelのものをベースに、テクスチャをなくしたもの。
	 *
	 * @return 生成されたStageGenerator
	 */
	private StageGenerator createStageGenerator() {
		return new StageGenerator() {
			private final Base redBase = new Base(1100, 1100, Team.RED, 200);
			private final Base blueBase = new Base(-1100, -1100, Team.BLUE, 200);
			private final int stageWidth = 3000;
			private final int stageHeight = 3000;

			@Override
			public GameObject[] getGameObjects() {
				ArrayList<GameObject> objects = new ArrayList<>();

				// 戦車の生成
				for (int i = 0; i < 4; i++) {
					Tank tank = new Tank(i % 2 != 0 ? redBase : blueBase);
					tank.setPosition(new Point2D.Double(0, 0));
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
			public double getStageWidth() {
				return stageWidth;
			}

			@Override
			public double getStageHeight() {
				return stageHeight;
			}

			@Override
			public void drawBackground(Graphics2D graphics, double visibleWidth, double visibleHeight, double animationFrame) {
			}
		};
	}

}
