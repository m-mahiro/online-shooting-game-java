package benchmark;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 変更点1: GamePanel.drawBackground()が毎フレーム(60回/秒)ImageIO.read()でPNGを
 * ディスクから再読込していた問題を切り出して計測する。
 *
 * 比較対象:
 *  - legacy : 毎フレーム ImageIO.read() を2回呼ぶ（修正前の実装を再現）
 *  - current: 起動時に1回だけ読み込み、以降は同じBufferedImage参照を再利用する（修正後の実装）
 *
 * 意図的にutil.ImageUtil（GraphicsConfiguration互換化）は使っていない。
 * 「ディスクI/O・PNGデコードが毎フレーム発生していた問題」を切り分けて計測したいため。
 * GraphicsConfiguration互換化（変更点4）の効果は、実際のGPUパイプラインに依存するため
 * このLinux/Xvfb環境では意味のある計測ができず、別途Windows実機での計測が必要になる。
 * そのため、ここでは plain ImageIO.read() だけを使い、どの環境でも同じ条件で測れるようにしている。
 */
public class BackgroundTextureBenchmark {

	private final File floorFile;
	private final File oceanFile;

	public BackgroundTextureBenchmark(File projectRoot) {
		this.floorFile = new File(projectRoot, "src/client/assets/floor_texture.png");
		this.oceanFile = new File(projectRoot, "src/client/assets/ocean_texture.png");
	}

	public List<BenchResult> run(int warmup, int iterations, PrintStream out) throws IOException {
		if (!floorFile.exists() || !oceanFile.exists()) {
			throw new IOException("アセットが見つかりません: " + floorFile + " / " + oceanFile
					+ " (projectRootの指定が正しいか確認してください)");
		}

		BufferedImage target = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = target.createGraphics();

		// ---- legacy: GamePanelの修正前の実装を再現 ----
		int[] frame = {0};
		BenchUtil.Stats legacyStats = BenchUtil.measure(warmup, iterations, () -> {
			try {
				BufferedImage floor = ImageIO.read(floorFile);
				BufferedImage ocean = ImageIO.read(oceanFile);

				double textureSize = 1000;
				double translate = (frame[0]++ * 10) % textureSize;
				Rectangle2D oceanAnchor = new Rectangle2D.Double(translate, translate, textureSize, textureSize);
				g.setPaint(new TexturePaint(ocean, oceanAnchor));
				g.fillRect(-3000, -3000, 6000, 6000);

				Rectangle2D floorAnchor = new Rectangle2D.Double(0, 0, floor.getWidth(), floor.getHeight());
				g.setPaint(new TexturePaint(floor, floorAnchor));
				g.fillRect(-3000, -3000, 6000, 6000);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		// ---- current: GamePanelの現行実装を再現（起動時に1回だけ読み込む） ----
		BufferedImage cachedFloor = ImageIO.read(floorFile);
		BufferedImage cachedOcean = ImageIO.read(oceanFile);
		Rectangle2D floorAnchor = new Rectangle2D.Double(0, 0, cachedFloor.getWidth(), cachedFloor.getHeight());
		TexturePaint floorPaint = new TexturePaint(cachedFloor, floorAnchor); // フローリングは不変なので1回だけ生成

		int[] frame2 = {0};
		BenchUtil.Stats currentStats = BenchUtil.measure(warmup, iterations, () -> {
			double textureSize = 1000;
			double translate = (frame2[0]++ * 10) % textureSize;
			// アニメーションでアンカーが動くのでTexturePaintだけは毎フレーム作り直す（画像の再読込は発生しない）
			Rectangle2D oceanAnchor = new Rectangle2D.Double(translate, translate, textureSize, textureSize);
			g.setPaint(new TexturePaint(cachedOcean, oceanAnchor));
			g.fillRect(-3000, -3000, 6000, 6000);

			g.setPaint(floorPaint);
			g.fillRect(-3000, -3000, 6000, 6000);
		});

		g.dispose();

		out.println("[1. Background Texture Reload]");
		out.println("  legacy  (毎フレームImageIO.read): " + legacyStats);
		out.println("  current (キャッシュ済み)        : " + currentStats);
		out.printf("  speedup (mean): %.1fx%n", legacyStats.meanMs / currentStats.meanMs);

		List<BenchResult> results = new ArrayList<>();
		results.add(new BenchResult("1-background-texture", "legacy", legacyStats));
		results.add(new BenchResult("1-background-texture", "current", currentStats));
		return results;
	}
}
