package stage;

import util.Util;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static stage.Team.*;

public class GameStage implements StageInfo {

	// 描画範囲の情報
	private final double stageWidth;
	private final double stageHeight;

	// オブジェクト管理
	private int nextPrivateObjectID = 0;

	// スポーン地点はgameObjectsとは別に、フィールドでも持っておく
	private final Base redBase, blueBase;

	// ステージ上のオブジェクト。
	private final Map<Integer, GameObject> objects = new ConcurrentHashMap<>();

	// ステージ外のテクスチャのアニメーション用
	double outerStageAnimationFrame = 0;

	// ステージジェネレータへの参照
	private final StageGenerator generator;

	/**
	 * コンストラクタ。ステージジェネレータからステージの幅、高さ、各種初期オブジェクトを受け取り、ゲームステージを初期化します。
	 * @param generator ステージの初期設定を含むStageGeneratorオブジェクト
	 */
	public GameStage(StageGenerator generator) {
		this.generator = generator;
		this.stageWidth = generator.getStageWidth();
		this.stageHeight = generator.getStageHeight();
		this.redBase = generator.getRedBase();
		this.blueBase = generator.getBlueBase();
		addGameObjects(generator.getGameObjects());
	}

	/**
	 * ステージの幅を取得します。
	 *
	 * @return ステージの幅
	 */
	public double getStageWidth() {
		return stageWidth;
	}

	/**
	 * ステージの高さを取得します。
	 *
	 * @return ステージの高さ
	 */
	public double getStageHeight() {
		return stageHeight;
	}

	/**
	 * ステージに配置したいオブジェクトをこのメソッドに与えます。
	 * @param gameObject ステージに配置したいゲームオブジェクト
	 * @return 配置されたゲームオブジェクトに割り振られたオブジェクトID
	 */
	public int addGameObject(GameObject gameObject) {
		if (gameObject == null) throw new NullPointerException();
		int id = getNextPrivateObjectID();
		objects.put(id, gameObject);
		return id;
	}

	/**
	 * ステージに配置したいオブジェクトの配列をこのメソッドに与えます。
 	 * @param gameObjects ステージに配置したいゲームオブジェクトの配列
	 * @return 配置されたゲームオブジェクトに割り振られたオブジェクトIDの配列。順番は引数に与えれた<code>gameObjects</code>に対応しています。
	 */
	public int[] addGameObjects(GameObject[] gameObjects) {
		int length = gameObjects.length;
		int[] idList = new int[length];
		int i = 0;
		for (GameObject obj : gameObjects) idList[i++] = addGameObject(obj);
		return idList;
	}

	/**
	 * 与えらたオブジェクトIDに対応する<code>GameObject</code>を返す。
	 * @param id オブジェクトID
	 * @return idに対応する対応する<code>GameObject</code>
	 */
	public GameObject getGameObject(int id) {
		return objects.get(id);
	}

	/**
	 * 与えられた<code>Graphics2D</code>に、ステージ上の<code>GameObject</code>を描画していく。
	 * ステージのテクスチャやステージ外の背景なども描画される。
	 *
	 * @param graphics カメラ位置・ズームによる座標変換を適用済みのGraphics2D
	 * @param visibleWidth カメラに映る（ウィンドウに映る）範囲の幅(ステージ上の座標系)
	 * @param visibleHeight カメラに映る（ウィンドウに映る）範囲の高さ(ステージ上の座標系)
	 */
	public void draw(Graphics2D graphics, double visibleWidth, double visibleHeight) {

		// 背景の描画（ステージジェネレータに委譲）
		generator.drawBackground(graphics, visibleWidth, visibleHeight, outerStageAnimationFrame);

		// GameObjectをレイヤーの下から順に描画する。
		//
		// 以前はここで「1回のスキャンでレイヤーごとにArrayListへ振り分けてから描画する」
		// 方式を試したが、ベンチマーク(benchmark/LayerBucketBenchmark, benchmark/README.md参照)で
		// 計測した結果、毎フレームArrayListを4つ新規確保するコストの方が、
		// 単純な「レイヤー数ぶん全オブジェクトを走査する」方式より実測で遅いことが判明したため、
		// この単純な実装に戻した。
		//
		// 理由の裏付け:
		//  - 各GameObjectのgetRenderLayer()は、対応するオブジェクトの内部状態（HP等）から毎回
		//    計算される値で、実際に値が変わるのは「壁・基地は生成されてから一度も変わらない」
		//    「戦車は死亡・リスポーン時のみ」「弾・ミサイルは着弾時に一度だけ」という、
		//    フレームごとに見ればごく稀なイベントである。つまり、この一覧の「レイヤーごとの
		//    まとまり具合」自体はフレームをまたいでほぼ変化しない。
		//  - そのため、変化のたびに差分だけ更新する永続的なバケツ構造（Unity の
		//    sortingLayer/sortingOrderやGodotのz_indexが内部的に行っているような、
		//    dirty-flagによる再挿入方式）も検討したが、これは数千オブジェクト規模の
		//    汎用エンジン内部実装で正当化される複雑さであり、このゲームの物量
		//    （数百オブジェクト）や、addGameObject()がGUIスレッドとネットワークスレッドの
		//    両方から呼ばれる（ConcurrentHashMapで保護されている）という並行性を考えると、
		//    実装・保守コストに見合わないと判断した。
		//  - 実際、この描画ディスパッチの方式差はいずれも1フレームあたり数マイクロ秒の
		//    オーダーで、60FPSのフレーム予算(16.6ms)の0.1%にも満たない。各オブジェクトの
		//    draw()自体（画像のアフィン変換・blit）の方が遥かに支配的なため、この部分は
		//    「測って一番単純かつ速かったもの」を採用するのが合理的という結論に至った。
		for (RenderLayer layer : RenderLayer.values()) {
			for (GameObject object : objects.values()) {
				if (object.getRenderLayer() != layer) continue;
				object.draw(graphics);
			}
		}
	}

	/**
	 * ステージ上の<code>GameObject</code>のフレーム更新をおこなう。
	 * 他にも、衝突を判定し該当のオブジェクトに通知を送ったり、削除可能なオブジェクトをメモリから削除したりする。
	 */
	public void update() {

		// ゲームオブジェクトのフレーム更新
		for (GameObject object : objects.values()) {
			object.update();
		}

		// 削除可能なオブジェクトがあれば削除
		removeExpiredObjects();

		// 衝突判定。衝突があれば該当オブジェクトに通知を送る
		checkCollision();

		// ステージ演出アニメーション用の変数をインクリメント
		outerStageAnimationFrame++;
	}

	/**
	 * <code>GameObject</code>同士の衝突判定をおこない、衝突があれば該当のオブジェクトに通知を送る。
	 * 衝突判定は両方ののオブジェクトが<code>hasRigidBody()</code>が<code>true</code>を返した場合のみ行います。
	 * 「衝突判定のの通知」は、該当オブジェクトの<code>onCollision()</code>を呼ぶことで送られます。
	 */
	private void checkCollision() {
		ArrayList<GameObject> objectList = new ArrayList<>(objects.values());
		for (int i = 0; i < objectList.size(); i++) {
			for (int j = i + 1; j < objectList.size(); j++) {

				// オブジェクトの組（総組み合わせ）を取得
				GameObject o1 = objectList.get(i);
				GameObject o2 = objectList.get(j);
				stage.Shape shape1 = o1.getShape();
				stage.Shape shape2 = o2.getShape();

				// 同一のオブジェクト同士では衝突判定を行わない
				if (o1 == o2) continue;

				// 衝突判定は、両方がRigidBodyじゃないと行わない。
				if (!o1.hasRigidBody() || !o2.hasRigidBody()) continue;


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
					Circle circle1 = (Circle) shape1;
					Circle circle2 = (Circle) shape2;

					// 衝突判定
					double collisionRange = circle1.radius + circle2.radius;
					double distance = circle1.center.distance(circle2.center);
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

					// 円の中心座標を、長方形を原点としたローカル座標系に変換
					Point2D.Double circleCenterInRectLocal = Util.subtract(circle.center, rectangle.center);

					// 長方形の境界内に、円の中心座標をクランプ（射影）する
					//    clampedX/Yは、長方形上で円の中心に最も近い点の座標（ローカル座標）
					double halfW = rectangle.width / 2.0;
					double halfH = rectangle.height / 2.0;
					double clampedX = Math.max(-halfW, Math.min(halfW, circleCenterInRectLocal.x));
					double clampedY = Math.max(-halfH, Math.min(halfH, circleCenterInRectLocal.y));

					// クランプした点と、円の中心（ローカル座標）との距離を計算
					Point2D.Double clampedPoint = new Point2D.Double(clampedX, clampedY);
					double distance = circleCenterInRectLocal.distance(clampedPoint);

					// 距離が円の半径より小さければ衝突している
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

	/**
	 * 削除可能なオブジェクト(<code>GameObject</code>)をメモリから削除する。
	 * 具体的には、<code>isExpired()</code>が<code>true</code>を<code>this.objects</code>から削除する。
	 */
	private void removeExpiredObjects() {
		Iterator<GameObject> iterator = this.objects.values().iterator();
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

	/**
	 * オブジェクトIDを返す。
	 * <code>getObject()</code>に与えることができる。
	 *
	 * @return オブジェクトID
	 */
	private int getNextPrivateObjectID() {
		synchronized (this) {
			int id = nextPrivateObjectID;
			nextPrivateObjectID++;
			return id;
		}
	}
	// ============================= StageInfoインターフェースのメソッド =============================

	/**
	 * 赤チームの基地のHPを取得します。
	 * @return 赤チームの基地の現在のHP
	 */
	@Override
	public int getRedBaseHP() {
		return this.redBase.getHP();
	}

	/**
	 * 青チームの基地のHPを取得します。
	 * @return 青チームの基地の現在のHP
	 */
	@Override
	public int getBlueBaseHP() {
		return this.blueBase.getHP();
	}

	/**
	 * 赤チームの基地の状態を取得します。
	 * @return 赤チームの基地の現在の状態
	 */
	@Override
	public Base.State getRedBaseState() {
		return this.redBase.getState();
	}

	/**
	 * 青チームの基地の状態を取得します。
	 * @return 青チームの基地の現在の状態
	 */
	@Override
	public Base.State getBlueBaseState() {
		return this.blueBase.getState();
	}

	/**
	 * 赤チームの残りの戦車数を取得します。
	 * @return 赤チームの生存している戦車の数
	 */
	@Override
	public int getRedTank() {
		int count = 0;
		synchronized (this.objects) {
			for (GameObject object : this.objects.values()) {
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

	/**
	 * 青チームの残りの戦車数を取得します。
	 * @return 青チームの生存している戦車の数
	 */
	@Override
	public int getBlueTank() {
		int count = 0;
		synchronized (this.objects) {
			for (GameObject object : this.objects.values()) {
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

	@Override
	public boolean hasFinished() {
		boolean redAllDead = redBase.isRuins() && getRedTank() == 0;
		boolean blueAllDead = blueBase.isRuins() && getBlueTank() == 0;
		return redAllDead || blueAllDead;
	}

	@Override
	public Team getWinner() {
		if (!hasFinished()) return null;
		boolean redWon = getRedTank() > getBlueTank();
		return redWon ? RED : BLUE;
	}

}
