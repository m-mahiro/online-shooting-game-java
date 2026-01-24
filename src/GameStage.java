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
		objects.put(getNextPrivateObjectID(), gameObject);
	}

	public void addObjects(Collection<GameObject> gameObjects) {
		synchronized (this) {
			for (GameObject object : gameObjects) {
				this.objects.put(getNextPrivateObjectID(), object);
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
		for (GameObject object : objects.values()) {
			object.draw(graphics);
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
				GameObject a = objectList.get(i);
				GameObject b = objectList.get(j);

				if (a == b) continue;
				if (!a.isTangible() || !b.isTangible()) continue;

				// 衝突範囲の計算
				double collisionRange = a.getCollisionRadius() + b.getCollisionRadius();

				// オブジェクト間の距離の計算
				Point2D.Double vector = Util.subtract(a.getTranslate(), b.getTranslate());
				double diff = Util.getNorm(vector);

				if (diff < collisionRange) {
					a.onCollision(b);
					b.onCollision(a);
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
