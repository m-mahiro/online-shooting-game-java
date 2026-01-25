import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameStage {

	private final Map<Integer, GameObject> objects = new ConcurrentHashMap<>();

	private int myNetworkClientID;
	private int lastPrivateObjectID = 0;

	public GameStage(int myNetworkClientID) {
		this.myNetworkClientID = myNetworkClientID;
	}

	public void addObject(GameObject gameObject) {
		int id = getNextPrivateObjectID();
		objects.put(id, gameObject);
	}

	public void addObjects(Collection<GameObject> gameObjects) {
		synchronized (this) {
			for (GameObject object : gameObjects) {
				this.addObject(object);
			}
		}
	}

	public GameObject getObject(int objectID) {
		return objects.get(objectID);
	}

	private int getNextPrivateObjectID() {
		synchronized (this) {
			lastPrivateObjectID++;
			return lastPrivateObjectID;
//			return myNetworkClientID * 100000 + lastPrivateObjectID;
		}
	}


	public void draw(Graphics2D graphics) {
		for (RenderLayer layer : RenderLayer.values()) {
			for (GameObject object : objects.values()) {
				if (object.getRenderLayer() != layer) continue;
				object.draw(graphics);
			}
		}
	}


	/**
	 * ゲームの状態を更新する
	 */
	public void update() {
		checkObjectToRemove();
		checkCollision();

		for (GameObject object : objects.values()) {
			object.update();
		}
	}

	public void checkCollision() {
		ArrayList<GameObject> objectList = new ArrayList<>(objects.values());
		for (int i = 0; i < objectList.size(); i++) {
			for (int j = i + 1; j < objectList.size(); j++) {
				GameObject o1 = objectList.get(i);
				GameObject o2 = objectList.get(j);
				Shape shape1 = o1.getShape();
				Shape shape2 = o2.getShape();

				if (o1 == o2) continue;
				if (!o1.isTangible() || !o2.isTangible()) continue;

				boolean isCollided = false;

				if (shape1 instanceof Rectangle && shape2 instanceof Rectangle) {

					// ============================= 長方形 vs 長方形 =============================
					// 相対的な位置関係を取得
					Rectangle rec1 = (Rectangle) shape1;
					Rectangle rec2 = (Rectangle) shape2;
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
					Rectangle rectangle;
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
		Iterator<GameObject> iterator = this.objects.values().iterator();
		while (iterator.hasNext()) {
			GameObject object = iterator.next();

			if (object == null) {
				iterator.remove();
				continue;
			}

			if (object.shouldRemove()) {
				iterator.remove();
			}
		}
	}


}
