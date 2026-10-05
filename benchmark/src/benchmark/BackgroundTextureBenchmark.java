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
 * ディスクから再読込していた問題、および追ってTexturePaintによるfillRect()自体が
 * 遅いと判明した問題を切り出して計測する。
 *
 * 比較対象:
 *  - legacy              : 毎フレーム ImageIO.read() を2回呼ぶ + TexturePaintでfillRect()
 *                           （最初の修正前の実装を再現）
 *  - current-texturepaint: 起動時に1回だけ読み込み、以降は同じBufferedImage参照を再利用するが、
 *                           描画そのものは依然TexturePaint+fillRect()（1回目の修正後の実装を再現）
 *  - current-tiled       : current-texturepaintと同じ画像を使うが、TexturePaintの代わりに
 *                           drawImage()でタイル状に繰り返し描く（2回目の修正=現行実装を再現）
 *
 * current-texturepointを計測して初めて、「ディスク再読込をやめても背景描画はほとんど速くならない」
 * ことが判明した（TexturePaint自体がJava2Dのアクセラレーションパイプラインの対象外で、
 * 常にソフトウェアで塗られるため）。current-tiledはその発見を受けての対策。
 *
 * 意図的にutil.ImageUtil（GraphicsConfiguration互換化）は使っていない。
 * 「ディスクI/O・PNGデコードが毎フレーム発生していた問題」「TexturePaint自体の重さ」を
 * 切り分けて計測したいため。GraphicsConfiguration互換化（変更点4）の効果は、実際のGPUパイプラインに
 * 依存するためこのLinux/Xvfb環境では意味のある計測ができず、別途Windows実機での計測が必要になる。
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
		// BufferedImage.createGraphics()直後はクリップが設定されておらずgetClipBounds()がnullを返すため
		// （実際のSwingのpaintComponent()では自動的にコンポーネントの描画範囲がクリップとして
		// 設定されるが、ここでは明示的に模擬する必要がある）、current-tiledのタイル範囲計算
		// （GamePanel.drawTiled()と同じくgetClipBounds()を使う）が正しく動くように設定しておく。
		g.setClip(0, 0, target.getWidth(), target.getHeight());

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

		// ---- current-texturepaint: 1回目の修正後の実装を再現（起動時に1回だけ読み込むが、描画はTexturePaintのまま） ----
		BufferedImage cachedFloor = ImageIO.read(floorFile);
		BufferedImage cachedOcean = ImageIO.read(oceanFile);
		Rectangle2D floorAnchor = new Rectangle2D.Double(0, 0, cachedFloor.getWidth(), cachedFloor.getHeight());
		TexturePaint floorPaint = new TexturePaint(cachedFloor, floorAnchor); // フローリングは不変なので1回だけ生成

		int[] frame2 = {0};
		BenchUtil.Stats texturePaintStats = BenchUtil.measure(warmup, iterations, () -> {
			double textureSize = 1000;
			double translate = (frame2[0]++ * 10) % textureSize;
			// アニメーションでアンカーが動くのでTexturePaintだけは毎フレーム作り直す（画像の再読込は発生しない）
			Rectangle2D oceanAnchor = new Rectangle2D.Double(translate, translate, textureSize, textureSize);
			g.setPaint(new TexturePaint(cachedOcean, oceanAnchor));
			g.fillRect(-3000, -3000, 6000, 6000);

			g.setPaint(floorPaint);
			g.fillRect(-3000, -3000, 6000, 6000);
		});

		// ---- current-tiled: 現行実装(GamePanel.drawTiled)を再現。TexturePaintをやめ、drawImage()でタイルを繰り返す ----
		BufferedImage oceanTile = scale(cachedOcean, 1000, 1000); // GamePanelと同様、起動時に1回だけ1000x1000へ揃える
		Rectangle2D visibleArea = g.getClipBounds(); // GamePanelと同じくgetClipBounds()を「可視範囲」として使う

		int[] frame3 = {0};
		BenchUtil.Stats tiledStats = BenchUtil.measure(warmup, iterations, () -> {
			double textureSize = oceanTile.getWidth();
			double translate = (frame3[0]++ * 10) % textureSize;
			drawTiled(g, oceanTile, translate, translate, visibleArea);
			drawTiled(g, cachedFloor, 0, 0, visibleArea);
		});

		g.dispose();

		out.println("[1. Background Texture Reload]");
		out.println("  legacy              (毎フレームImageIO.read + TexturePaint): " + legacyStats);
		out.println("  current-texturepaint(キャッシュ済み + TexturePaint)        : " + texturePaintStats);
		out.println("  current-tiled       (キャッシュ済み + drawImageでタイル描画)  : " + tiledStats);
		out.printf("  speedup (mean) current-texturepaint vs legacy: %.1fx%n",
				legacyStats.meanMs / texturePaintStats.meanMs);
		out.printf("  speedup (mean) current-tiled        vs legacy: %.1fx%n",
				legacyStats.meanMs / tiledStats.meanMs);
		out.printf("  speedup (mean) current-tiled vs current-texturepaint: %.1fx%n",
				texturePaintStats.meanMs / tiledStats.meanMs);

		List<BenchResult> results = new ArrayList<>();
		results.add(new BenchResult("1-background-texture", "legacy", legacyStats));
		results.add(new BenchResult("1-background-texture", "current-texturepaint", texturePaintStats));
		results.add(new BenchResult("1-background-texture", "current-tiled", tiledStats));
		return results;
	}

	/**
	 * GamePanel.drawTiled()と同じアルゴリズム。TexturePaintの代わりにdrawImage()を繰り返して
	 * タイル状に敷き詰める。詳細な理由はGamePanel.javaのdrawTiled()のコメントを参照。
	 */
	private static void drawTiled(Graphics2D graphics, BufferedImage tile, double anchorX, double anchorY, Rectangle2D area) {
		if (area == null || area.isEmpty()) return;

		double tileWidth = tile.getWidth();
		double tileHeight = tile.getHeight();

		int firstX = (int) Math.floor((area.getMinX() - anchorX) / tileWidth);
		int lastX = (int) Math.ceil((area.getMaxX() - anchorX) / tileWidth);
		int firstY = (int) Math.floor((area.getMinY() - anchorY) / tileHeight);
		int lastY = (int) Math.ceil((area.getMaxY() - anchorY) / tileHeight);

		for (int ty = firstY; ty < lastY; ty++) {
			int y = (int) Math.round(anchorY + ty * tileHeight);
			for (int tx = firstX; tx < lastX; tx++) {
				int x = (int) Math.round(anchorX + tx * tileWidth);
				graphics.drawImage(tile, x, y, null);
			}
		}
	}

	/** util.ImageUtil.scale()と同じ処理（ベンチマークは意図的にutil.ImageUtilに依存しないようにしている）。 */
	private static BufferedImage scale(BufferedImage source, int width, int height) {
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(source, 0, 0, width, height, null);
		g.dispose();
		return scaled;
	}
}
