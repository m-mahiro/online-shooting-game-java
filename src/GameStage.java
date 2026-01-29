import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GameStage {

	private static BufferedImage floorTexture, outerStageTexture;

	private final int stageWidth = 6000;
	private final int stageHeight = 6000;
	private int visibleWidth = 2000;
	private int visibleHeight = 1000;

	static {
		try {
			floorTexture = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/floor_texture.png")));
			outerStageTexture = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/ocean_texture.png")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private final Map<Integer, GameObject> objects = new ConcurrentHashMap<>();
	private int myNetworkClientID;
	private int nextPrivateObjectID = 0;

	public GameStage(int myNetworkClientID, int players) {
		this.myNetworkClientID = myNetworkClientID;

		ArrayList<GameObject> objects = new ArrayList<>();

		Base redBase = new Base(-2000, -2000, Team.RED);
		Base blueBase = new Base(2000, 2000, Team.BLUE);

		// まず最初に戦車
		objects.add(new Tank(redBase));
		for (int i = 1; i < players; i++) {
			Team team = (i % 2 == 0 ) ? Team.RED : Team.BLUE;
			switch (team) {
				case RED: {
//					Tank tank = new AutoTank(redBase, this);
					Tank tank = new Tank(redBase);
					objects.add(tank);
					break;
				}
				case BLUE: {
//					Tank tank = new AutoTank(blueBase, this);
					Tank tank = new Tank(blueBase);
					objects.add(tank);
					break;

				}
			}
		}

		objects.add(redBase);
		objects.add(blueBase);


		// 壁
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

		addObjects(objects);
	}

	public void setVisibleRange(int width, int height) {
		this.visibleWidth = width;
		this.visibleHeight = height;
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
			int id = nextPrivateObjectID;
			nextPrivateObjectID++;
			return id;
//			return myNetworkClientID * 100000 + lastPrivateObjectID;
		}
	}


	public void draw(Graphics2D graphics) {

		// ステージ外の描画
		Rectangle2D outerStageAnchor = new Rectangle2D.Double(0, 0, 1000, 1000);
		TexturePaint outerStagePaint = new TexturePaint(outerStageTexture, outerStageAnchor);
		graphics.setPaint(outerStagePaint);
		int fillWidth = stageWidth + visibleWidth;
		int fillHeight = stageHeight + visibleHeight;
		graphics.fillRect(-fillWidth / 2, -fillHeight / 2, fillWidth, fillHeight);

		// フローリングの描画
		Rectangle2D floorAnchor = new Rectangle2D.Double(0, 0, floorTexture.getWidth(), floorTexture.getHeight());
		TexturePaint floorPaint = new TexturePaint(floorTexture, floorAnchor);
		graphics.setPaint(floorPaint);
		graphics.fillRect(-stageWidth / 2, -stageHeight / 2, stageWidth, stageHeight);

		// GameObjectの描画
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

				if (o1 == o2) continue;
				if (!o1.isTangible() || !o2.isTangible()) continue;

				Shape shape1 = o1.getShape();
				Shape shape2 = o2.getShape();

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
