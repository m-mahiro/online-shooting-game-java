package util;

import javax.imageio.ImageIO;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * 画像リソースの読み込みを行うユーティリティクラス。
 *
 * {@link ImageIO#read}が返す{@link BufferedImage}は、ディスプレイのGraphicsConfigurationとは
 * 無関係なピクセル形式（多くはTYPE_INT_ARGB）で生成される。これをそのまま描画に使うと、
 * Direct3D/OpenGLなどのハードウェアアクセラレーションされたパイプライン上では、描画の都度
 * フォーマット変換が発生してしまい、GPUによる高速な転送（VRAM上でのキャッシュや高速blit）が
 * 活かせない。
 * ここでは読み込み時に一度だけ、現在のGraphicsConfigurationに適合したBufferedImageへ変換する
 * ことで、以降の描画をアクセラレーションしやすい状態にしておく。
 */
public class ImageUtil {

	/**
	 * 画像リソースを読み込み、ディスプレイのGraphicsConfigurationに適合した形式に変換して返す。
	 *
	 * @param resource 画像リソースのURL
	 * @return 読み込まれ、変換されたBufferedImage
	 * @throws IOException 画像の読み込みに失敗した場合
	 */
	public static BufferedImage load(URL resource) throws IOException {
		BufferedImage source = ImageIO.read(resource);
		return toCompatibleImage(source);
	}

	/**
	 * 与えられたBufferedImageを、ディスプレイのGraphicsConfigurationに適合した形式へ変換する。
	 *
	 * @param source 変換元のBufferedImage
	 * @return 変換されたBufferedImage
	 */
	public static BufferedImage toCompatibleImage(BufferedImage source) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage compatible = gc.createCompatibleImage(source.getWidth(), source.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g = compatible.createGraphics();
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return compatible;
	}

	/**
	 * 画像を指定サイズへ拡大・縮小する。結果はGraphicsConfigurationに適合した形式で返されるため、
	 * {@link java.awt.Image#getScaledInstance}を使う場合と異なり、以降の描画をアクセラレーションしやすい。
	 *
	 * @param source 元の画像
	 * @param width 拡大・縮小後の幅
	 * @param height 拡大・縮小後の高さ
	 * @return 拡大・縮小されたBufferedImage
	 */
	public static BufferedImage scale(BufferedImage source, int width, int height) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage scaled = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
		Graphics2D g = scaled.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(source, 0, 0, width, height, null);
		g.dispose();
		return scaled;
	}
}
