package benchmark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 変更点3: TeamInfoText / RotationChars が毎フレームFontからGlyphVector（文字の輪郭）を
 * 再計算していた問題を切り出して計測する。
 *
 * 比較対象:
 *  - legacy : 毎フレーム GlyphVector を生成し直す（修正前の実装を再現）
 *  - current: 表示内容（テキスト）が変わらない限りShapeをキャッシュする（修正後の実装）
 *
 * 2つのシナリオで計測する:
 *  - stable  : 表示する文字列が変化しない（HPが変化しない通常時を想定。キャッシュが最大限効く）
 *  - changing: 毎フレーム文字列が変化する（キャッシュが毎回ミスする最悪ケース。
 *              legacyとほぼ同じ時間になるはず = 回帰（キャッシュ判定自体のコストで遅くなっていないか）の確認）
 */
public class GlyphCacheBenchmark {

	private static final Font FONT = new Font("Arial", Font.BOLD, 100);

	// LayerBucketBenchmarkと同じ理由（JITの定常状態に達するまでの最低回数の確保）。
	private static final int MIN_WARMUP = 5000;
	private static final int MIN_ITERATIONS = 5000;

	public List<BenchResult> run(int warmup, int iterations, PrintStream out) {
		warmup = Math.max(warmup, MIN_WARMUP);
		iterations = Math.max(iterations, MIN_ITERATIONS);

		BufferedImage target = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = target.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		out.println("[3. GlyphVector Cache]");

		List<BenchResult> results = new ArrayList<>();
		results.addAll(runScenario(out, "stable", g, warmup, iterations, frame -> "50"));
		results.addAll(runScenario(out, "changing", g, warmup, iterations, frame -> Integer.toString(frame % 50)));

		g.dispose();
		return results;
	}

	private interface TextSource {
		String text(int frame);
	}

	private List<BenchResult> runScenario(
			PrintStream out, String scenarioName, Graphics2D g, int warmup, int iterations, TextSource source) {

		out.println("  -- scenario: " + scenarioName + " --");

		int[] frameLegacy = {0};
		BenchUtil.Stats legacyStats = BenchUtil.measure(warmup, iterations, () -> {
			String text = source.text(frameLegacy[0]++);
			drawLegacy(g, text);
		});

		// current実装用のキャッシュ状態（ベンチ関数はRunnableなのでラムダ外に状態を持つ）
		String[] cachedText = {null};
		Shape[] cachedShape = {null};
		int[] frameCurrent = {0};
		BenchUtil.Stats currentStats = BenchUtil.measure(warmup, iterations, () -> {
			String text = source.text(frameCurrent[0]++);
			cachedShape[0] = drawCurrent(g, text, cachedText[0], cachedShape[0]);
			cachedText[0] = text;
		});

		out.println("    legacy  : " + legacyStats);
		out.println("    current : " + currentStats);
		out.printf("    speedup (mean): %.1fx%n", legacyStats.meanMs / currentStats.meanMs);

		List<BenchResult> results = new ArrayList<>();
		results.add(new BenchResult("3-glyph-cache-" + scenarioName, "legacy", legacyStats));
		results.add(new BenchResult("3-glyph-cache-" + scenarioName, "current", currentStats));
		return results;
	}

	/** 修正前のアルゴリズム（TeamInfoText.java 旧実装の再現）。 */
	private void drawLegacy(Graphics2D graphics, String text) {
		double textX = 400, textY = 600;

		FontRenderContext frc = graphics.getFontRenderContext();
		GlyphVector gv = FONT.createGlyphVector(frc, text);
		Shape textShape = gv.getOutline();

		java.awt.Rectangle bounds = textShape.getBounds();
		double xOffset = -bounds.getWidth() / 2.0;
		double yOffset = bounds.getHeight() / 2.0;

		AffineTransform textTrans = new AffineTransform();
		textTrans.translate(textX, textY);
		textTrans.translate(xOffset, yOffset);
		Shape finalShape = textTrans.createTransformedShape(textShape);

		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.draw(finalShape);
		graphics.setColor(Color.WHITE);
		graphics.fill(finalShape);
	}

	/**
	 * 修正後のアルゴリズム（TeamInfoText.java 現行実装の再現）。
	 * @return 今回使った変換済みShape（次フレームのキャッシュ判定に使う）
	 */
	private Shape drawCurrent(Graphics2D graphics, String text, String cachedText, Shape cachedShapeIn) {
		double textX = 400, textY = 600;

		Shape finalShape;
		if (cachedShapeIn != null && text.equals(cachedText)) {
			finalShape = cachedShapeIn;
		} else {
			FontRenderContext frc = graphics.getFontRenderContext();
			GlyphVector gv = FONT.createGlyphVector(frc, text);
			Shape textShape = gv.getOutline();

			java.awt.Rectangle bounds = textShape.getBounds();
			double xOffset = -bounds.getWidth() / 2.0;
			double yOffset = bounds.getHeight() / 2.0;

			AffineTransform textTrans = new AffineTransform();
			textTrans.translate(textX, textY);
			textTrans.translate(xOffset, yOffset);
			finalShape = textTrans.createTransformedShape(textShape);
		}

		graphics.setColor(Color.BLACK);
		graphics.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.draw(finalShape);
		graphics.setColor(Color.WHITE);
		graphics.fill(finalShape);

		return finalShape;
	}
}
