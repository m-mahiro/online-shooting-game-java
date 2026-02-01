package stage;

import util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static stage.Team.BLUE;
import static stage.Team.RED;

public class GameStage implements StageInfo {

	// 描画範囲の情報
	private final int stageWidth = 6000;
	private final int stageHeight = 6000;

	// オブジェクト管理
	private int nextPrivateObjectID = 0;

	// スポーン地点はgameObjectsとは別に、フィールドでも持っておく
	private final Base redBase, blueBase;

	// ステージ上のオブジェクト。
	private final Map<Integer, GameObject> object = new ConcurrentHashMap<>();
	// ステージ上空に張り付いているオブジェクト。GameUIよりは下層(隠れる)。
	private final Map<Integer, UpperStageObject> upperObject = new ConcurrentHashMap<>();

	// ステージ外のテクスチャのアニメーション用
	double outerStageAnimationFrame = 0;


	// 画像リソース
	private static BufferedImage floorTexture, outerStageTexture;

	static {
		try {
			floorTexture = ImageIO.read(Objects.requireNonNull(GameStage.class.getResource("../client/assets/floor_texture.png")));
			outerStageTexture = ImageIO.read(Objects.requireNonNull(GameStage.class.getResource("../client/assets/ocean_texture.png")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public GameStage(Base redBase, Base blueBase) {
		this.redBase = redBase;
		this.blueBase = blueBase;
		addStageObject(redBase);
		addStageObject(blueBase);
	}

	public int getStageWidth() {
		return stageWidth;
	}

	public int getStageHeight() {
		return stageHeight;
	}

	public int addStageObject(GameObject gameObject) {
		int id = getNextPrivateObjectID();
		object.put(id, gameObject);
		return id;
	}

	public int[] addStageObjects(Collection<GameObject> gameObjects) {
		int n = gameObjects.size();
		int[] idList = new int[n];
		synchronized (this) {
			int i = 0;
			for (GameObject object : gameObjects) {
				int id = this.addStageObject(object);
				idList[i++] = id;
			}
		}
		return idList;
	}

	public int addScreenObject(UpperStageObject upperStageObject) {
		int id = getNextPrivateObjectID();
		this.upperObject.put(id, upperStageObject);
		return id;
	}

	public GameObject getGameObject(int id) {
		return object.get(id);
	}

	public UpperStageObject getScreenObject(int id) {
		return upperObject.get(id);
	}

	private int getNextPrivateObjectID() {
		synchronized (this) {
			int id = nextPrivateObjectID;
			nextPrivateObjectID++;
			return id;
//			return myNetworkClientID * 100000 + lastPrivateObjectID;
		}
	}


	public void draw(Graphics2D graphics, int windowWidth, int windowHeight, double zoomDegrees) {

		// ステージ外の描画
		double textureSize = 1000;
		double translate = outerStageAnimationFrame * 10 % textureSize;
		Rectangle2D outerStageAnchor = new Rectangle2D.Double(translate, translate, textureSize, textureSize);
		TexturePaint outerStagePaint = new TexturePaint(outerStageTexture, outerStageAnchor);
		graphics.setPaint(outerStagePaint);
		int fillWidth = (int) (stageWidth + windowWidth / zoomDegrees);
		int fillHeight = (int) (stageHeight + windowHeight / zoomDegrees);
		graphics.fillRect(-fillWidth / 2, -fillHeight / 2, fillWidth, fillHeight);

		// フローリングの描画
		Rectangle2D floorAnchor = new Rectangle2D.Double(0, 0, floorTexture.getWidth(), floorTexture.getHeight());
		TexturePaint floorPaint = new TexturePaint(floorTexture, floorAnchor);
		graphics.setPaint(floorPaint);
		graphics.fillRect(-stageWidth / 2, -stageHeight / 2, stageWidth, stageHeight);

		// GameObjectの描画
		for (RenderLayer layer : RenderLayer.values()) {
			for (GameObject object : object.values()) {
				if (object.getRenderLayer() != layer) continue;
				object.draw(graphics);
			}
		}

		// ScreenObjectの描画
		for (UpperStageObject object : upperObject.values()) {
			object.draw(graphics);
		}
	}

	/**
	 * ゲームの状態を更新する
	 */
	public void update() {

		// ゲームオブジェクトのフレーム更新
		for (GameObject object : object.values()) {
			object.update();
		}

		// 削除可能なオブジェクトがあれば削除
		checkObjectToRemove();

		// 衝突判定。衝突があれば該当オブジェクトに通知を送る
		checkCollision();

		// ステージ演出アニメーション用の変数をインクリメント
		outerStageAnimationFrame++;

		// ステージ上空のオブジェクトのフレームを更新
		for (UpperStageObject upperStageObject : upperObject.values()) {
			upperStageObject.update();
		}
	}

	public void checkCollision() {
		ArrayList<GameObject> objectList = new ArrayList<>(object.values());
		for (int i = 0; i < objectList.size(); i++) {
			for (int j = i + 1; j < objectList.size(); j++) {
				GameObject o1 = objectList.get(i);
				GameObject o2 = objectList.get(j);

				if (o1 == o2) continue;
				if (!o1.isTangible() || !o2.isTangible()) continue;

				stage.Shape shape1 = o1.getShape();
				stage.Shape shape2 = o2.getShape();

				boolean isCollided = false;

				if (shape1 instanceof stage.Rectangle && shape2 instanceof stage.Rectangle) {

					// ============================= 長方形 vs 長方形 =============================
					// 相対的な位置関係を取得
					stage.Rectangle rec1 = (stage.Rectangle) shape1;
					stage.Rectangle rec2 = (stage.Rectangle) shape2;
					Point2D.Double vector = Util.subtract(rec1.center, rec2.center);

					// 衝突判定
					double xCollisionRange = (rec1.width + rec2.width) / 2.0;
					double yCollisionRange = (rec1.height + rec2.height) / 2.0;
					if (Math.abs(vector.x) < xCollisionRange && Math.abs(vector.y) < yCollisionRange) {
						isCollided = true;
					}

				} else if (shape1 instanceof Circle && shape2 instanceof Circle) {

					// ============================= 円 vs 円 =============================
					// 相対的な位置関係を取得
					Circle circle1 = (Circle) shape1;
					Circle circle2 = (Circle) shape2;
					Point2D.Double vector = Util.subtract(circle1.center, circle2.center);

					// 衝突判定
					double collisionRange = circle1.radius + circle2.radius;
					double distance = Util.norm(vector);
					if (distance < collisionRange) {
						isCollided = true;
					}

				} else {

					// ============================= 長方形 vs 円 =============================
					// 型のキャスト
					GameObject circleObj, rectObj;
					Circle circle;
					stage.Rectangle rectangle;
					if (shape1 instanceof Circle) {
						circleObj = o1;
						rectObj = o2;
					} else {
						circleObj = o2;
						rectObj = o1;
					}
					circle = (Circle) circleObj.getShape();
					rectangle = (Rectangle) rectObj.getShape();

					// 1. 円の中心座標を、長方形を原点としたローカル座標系に変換
					Point2D.Double circleCenterInRectLocal = Util.subtract(circle.center, rectangle.center);

					// 2. 長方形の境界内に、円の中心座標をクランプ（射影）する
					//    clampedX/Yは、長方形上で円の中心に最も近い点の座標（ローカル座標）
					double halfW = rectangle.width / 2.0;
					double halfH = rectangle.height / 2.0;
					double clampedX = Math.max(-halfW, Math.min(halfW, circleCenterInRectLocal.x));
					double clampedY = Math.max(-halfH, Math.min(halfH, circleCenterInRectLocal.y));

					// 3. クランプした点と、円の中心（ローカル座標）との距離を計算
					Point2D.Double clampedPoint = new Point2D.Double(clampedX, clampedY);
					double distance = Util.norm(Util.subtract(circleCenterInRectLocal, clampedPoint));

					// 4. 距離が円の半径より小さければ衝突している
					if (distance < circle.radius) {
						isCollided = true;
					}
				}

				if(isCollided) {
					o1.onCollision(o2);
					o2.onCollision(o1);
				}
			}
		}
	}

	public void checkObjectToRemove() {
		Iterator<GameObject> iterator = this.object.values().iterator();
		while (iterator.hasNext()) {
			GameObject object = iterator.next();

			if (object == null) {
				iterator.remove();
				continue;
			}

			if (object.isExpired()) {
				iterator.remove();
			}
		}
	}

	public double  getJustZoomDegrees(double windowWidth, double windowHeight) {
		double zoomWidth = windowWidth / stageWidth;
		double zoomHeight = windowHeight / stageWidth;
		return Math.min(zoomWidth, zoomHeight);
	}


	// ============================= StageInfoインターフェースのメソッド =============================

	@Override
	public int getRedBaseHP() {
		return this.redBase.getHP();
	}

	@Override
	public int getBlueBaseHP() {
		return this.blueBase.getHP();
	}

	@Override
	public Base.State getRedBaseState() {
		return this.redBase.getState();
	}

	@Override
	public Base.State getBlueBaseState() {
		return this.blueBase.getState();
	}

	@Override
	public int getRemainRedTank() {
		int count = 0;
		synchronized (this.object) {
			for (GameObject object : this.object.values()) {
				if (object instanceof Tank) {
					Tank tank = (Tank) object;
					boolean isRed = tank.getTeam() == RED;
					boolean isAlive = !tank.isDead();
					if (isRed && isAlive) count++;
				}
			}
		}
		return count;
	}

	@Override
	public int getRemainBlueTank() {
		int count = 0;
		synchronized (this.object) {
			for (GameObject object : this.object.values()) {
				if (object instanceof Tank) {
					Tank tank = (Tank) object;
					boolean isBlue = tank.getTeam() == BLUE;
					boolean isAlive = !tank.isDead();
					if (isBlue && isAlive) count++;
				}
			}
		}
		return count;
	}


}
