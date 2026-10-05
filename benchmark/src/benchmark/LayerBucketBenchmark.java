package benchmark;

import stage.GameObject;
import stage.Circle;
import stage.Projectile;
import stage.RenderLayer;
import stage.Team;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * GameStage.draw()のレイヤー振り分け方式について、3つの実装候補を比較する。
 *
 * このベンチマークはちょっとした紆余曲折の記録でもある:
 *  1. 元々は「RenderLayerの数だけ全オブジェクトを毎回スキャンする」(= legacy)実装だった。
 *  2. 一度「1回の走査でレイヤーごとに振り分けてから描画する」(= current, 毎フレーム
 *     ArrayListを4つ新規確保)に変更したが、このベンチマークで計測したところ、
 *     legacyの方が速いという逆の結果が出た（ArrayList確保のコストが、減らした
 *     はずの走査コストを上回っていたため）。
 *  3. バッファを使い回す(= current-reused)案も試したが、legacyには僅かに届かなかった。
 *  4. さらに「GameObjectのレイヤーが実際いつ変わるか」をソースコードから調査したところ、
 *     壁・基地は生成後レイヤーが一切変わらず、戦車・弾・ミサイル・ブロックも
 *     死亡/リスポーン/着弾など特定のイベント時に高々1〜2回しか変わらないと判明。
 *     「永続的なバケツ構造を差分更新する」設計（Unity/Godotのsorting layer/z_index
 *     が内部的に行っているようなdirty-flag方式）も検討したが、数千オブジェクト規模の
 *     汎用エンジンでなければ正当化しづらい複雑さであり、addGameObject()がGUI/
 *     ネットワークの複数スレッドから呼ばれる並行性も考えると、実装・保守コストが
 *     見合わないと判断した。
 *  5. 結論として、GameStage.javaは1.のlegacy方式（最も単純、かつ実測でも最速）に戻した。
 *
 * このベンチマークは「試した結果、元の実装が最善だった」という結論に至った過程を
 * 再現・記録するために残している。
 *
 * 比較対象:
 *  - legacy       : RenderLayer.values()の数だけ全オブジェクトを走査する（現在のGameStage.javaの実装）
 *  - current       : 1回の走査でレイヤーごとに振り分けるが、ArrayListを毎回新規に4つ確保する（過去に試して不採用になった案）
 *  - current-reused: 同じく1回の走査で振り分けるが、ArrayListを再利用する（不採用になった案の改良版）
 *
 * 実際のステージに近い比率（壁多数・弾少数・残骸少数）のダミーGameObjectを用意して計測する。
 * 実際のGameStage.draw()はこれに加えてStageGenerator#drawBackground()も呼ぶが、
 * ここではレイヤー振り分けアルゴリズムの差だけを切り出して計測するため、背景描画は含めない。
 *
 * 注意: ダミーのdraw()は観測可能な副作用（staticカウンタの加算）を持たせている。
 * 完全に何もしないメソッドにすると、JITが「結果を誰も読まない呼び出し」全体を
 * デッドコードとして消してしまい、特にlegacy側（if文1つで済む単純な分岐）が
 * 不自然に高速（ほぼ0ms）に計測されてしまう問題が実際に発生したため。
 */
public class LayerBucketBenchmark {

	private static final class DummyObject implements GameObject {
		private final RenderLayer layer;

		// JITによるデッドコード除去を防ぐための、観測可能な副作用のあるカウンタ
		static long sink;

		DummyObject(RenderLayer layer) {
			this.layer = layer;
		}

		public void update() {}
		public void draw(Graphics2D g) { sink++; }
		public void onCollision(GameObject other) {}
		public void onHitBy(Projectile p) {}
		public boolean isExpired() { return false; }
		public boolean hasRigidBody() { return false; }
		public RenderLayer getRenderLayer() { return layer; }
		public stage.Shape getShape() { return new Circle(new Point2D.Double(0, 0), 1); }
		public Point2D.Double getPosition() { return new Point2D.Double(0, 0); }
		public void setPosition(Point2D.Double p) {}
		public int getHP() { return 1; }
		public Team getTeam() { return Team.OBSTACLE; }
	}

	// この処理は1回あたり数マイクロ秒程度と非常に軽いため、JITが十分に最適化を
	// 終えた「定常状態」に到達するにはBenchmarkMainのデフォルト(warmup=200)では足りず、
	// legacyとcurrentの大小関係が逆転して見えることがある（実際に発生した）。
	// そのため、呼び出し元の指定値に関わらず最低限このくらいは回すようにする。
	private static final int MIN_WARMUP = 5000;
	private static final int MIN_ITERATIONS = 5000;

	public List<BenchResult> run(int warmup, int iterations, PrintStream out) {
		warmup = Math.max(warmup, MIN_WARMUP);
		iterations = Math.max(iterations, MIN_ITERATIONS);

		// 実際の対戦ステージに近い比率:
		// 壁(外周) 約240 + 戦車・設置ブロック等 TANGIBLE_OBJECTで計300、弾・ミサイル30、残骸15、未使用のFLOURも5
		List<GameObject> objects = new ArrayList<>();
		addN(objects, RenderLayer.TANGIBLE_OBJECT, 300);
		addN(objects, RenderLayer.PROJECTILE, 30);
		addN(objects, RenderLayer.DEBRIS, 15);
		addN(objects, RenderLayer.FLOUR, 5);

		BufferedImage target = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = target.createGraphics();

		// 振り分け先のバケツを毎回新規に確保せず、clear()して再利用する版。
		// 「current」との差は、ArrayListをフレームごとに作り直すか再利用するかだけ。
		List<List<GameObject>> reusableBuckets = new ArrayList<>();
		for (int i = 0; i < RenderLayer.values().length; i++) reusableBuckets.add(new ArrayList<>());

		BenchUtil.Stats legacyStats = BenchUtil.measure(warmup, iterations, () -> legacyDraw(objects, g));
		BenchUtil.Stats currentStats = BenchUtil.measure(warmup, iterations, () -> currentDraw(objects, g));
		BenchUtil.Stats reusedStats =
				BenchUtil.measure(warmup, iterations, () -> currentDrawReusingBuckets(objects, g, reusableBuckets));

		g.dispose();

		out.println("[2. GameStage.draw() Layer Dispatch] objects=" + objects.size());
		out.println("  legacy        (レイヤー数分フルスキャン・現行実装)        : " + legacyStats);
		out.println("  current       (1回のスキャン、毎回ArrayListを新規確保・不採用): " + currentStats);
		out.println("  current-reused(1回のスキャン、ArrayListを再利用・不採用)     : " + reusedStats);
		out.printf("  speedup (mean) current        vs legacy: %.1fx%n", legacyStats.meanMs / currentStats.meanMs);
		out.printf("  speedup (mean) current-reused vs legacy: %.1fx%n", legacyStats.meanMs / reusedStats.meanMs);
		// DummyObject.sinkを実際に読み出して使うことで、JITがdraw()呼び出しごと
		// デッドコードとして消してしまわないようにする（benchmark内部値なので無視してよい）
		out.println("  (sink=" + DummyObject.sink + ")");

		List<BenchResult> results = new ArrayList<>();
		results.add(new BenchResult("2-layer-dispatch", "legacy", legacyStats));
		results.add(new BenchResult("2-layer-dispatch", "current", currentStats));
		results.add(new BenchResult("2-layer-dispatch", "current-reused", reusedStats));
		return results;
	}

	private void addN(List<GameObject> list, RenderLayer layer, int n) {
		for (int i = 0; i < n; i++) list.add(new DummyObject(layer));
	}

	/** 修正前のアルゴリズム（GameStage.java 旧実装の再現）。 */
	private void legacyDraw(List<GameObject> objects, Graphics2D graphics) {
		for (RenderLayer layer : RenderLayer.values()) {
			for (GameObject object : objects) {
				if (object.getRenderLayer() != layer) continue;
				object.draw(graphics);
			}
		}
	}

	/** 修正後のアルゴリズム（GameStage.java 現行実装の再現）。バケツは毎回新規に確保する。 */
	private void currentDraw(List<GameObject> objects, Graphics2D graphics) {
		List<List<GameObject>> objectsByLayer = new ArrayList<>();
		for (int i = 0; i < RenderLayer.values().length; i++) objectsByLayer.add(new ArrayList<>());
		for (GameObject object : objects) {
			objectsByLayer.get(object.getRenderLayer().ordinal()).add(object);
		}
		for (List<GameObject> layerObjects : objectsByLayer) {
			for (GameObject object : layerObjects) {
				object.draw(graphics);
			}
		}
	}

	/**
	 * 1回のスキャンで振り分ける点はcurrentと同じだが、バケツ(ArrayList)を毎フレーム
	 * 新規に確保せず、呼び出し元から渡されたものをclear()して再利用する版。
	 * GameStage.javaの現行実装はまだこの最適化をしていない（毎フレーム4つのArrayListを新規確保している）。
	 */
	private void currentDrawReusingBuckets(List<GameObject> objects, Graphics2D graphics, List<List<GameObject>> buckets) {
		for (List<GameObject> bucket : buckets) bucket.clear();
		for (GameObject object : objects) {
			buckets.get(object.getRenderLayer().ordinal()).add(object);
		}
		for (List<GameObject> bucket : buckets) {
			for (GameObject object : bucket) {
				object.draw(graphics);
			}
		}
	}
}
