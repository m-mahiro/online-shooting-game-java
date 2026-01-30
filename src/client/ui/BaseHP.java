package client.ui;

import client.GamePanel;
import stage.Base;
import stage.StageInfo;
import stage.Team;
import sun.java2d.pipe.TextRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class BaseHP implements UIContent {


	private Team team;
	private StageInfo info;

	// 演出用定数
	private static final int HP_CHANGE_ANIMATION_FRAME = GamePanel.FPS / 5;

	// 演出用変数
	private int hpChangeAnimationFrame = 0;
	private int previousHP = 0;

	// 画像リソース
	private static BufferedImage redTableImage, blueTableImage;
	private static BufferedImage normalRedBaseImage, brokenRedBaseImage, redBaseRuinsImage;
	private static BufferedImage normalBlueBaseImage, brokenBlueBaseImage, blueBaseRuinsImage;

	static {
		try {
			// HUD用に表示される台の画像
			redTableImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_table_red.png")));
			blueTableImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_table_blue.png")));

			// REDチームの基地の画像
			normalRedBaseImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_base_red_normal.png")));
			brokenRedBaseImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_base_red_broken.png")));
			redBaseRuinsImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_base_red_ruins.png")));

			// BLUEチームの基地の画像
			normalBlueBaseImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_base_blue_normal.png")));
			brokenBlueBaseImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_base_blue_broken.png")));
			blueBaseRuinsImage = ImageIO.read(Objects.requireNonNull(BaseHP.class.getResource("../assets/hp_base_blue_ruins.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public BaseHP(Team team, StageInfo info) {
		this.info = info;
		this.team = team;
	}



	private BufferedImage getBaseImage() {
		boolean isRed = this.team == Team.RED;
		switch (getBaseState()) {
			case NORMAL:
				return isRed ? normalRedBaseImage : normalBlueBaseImage;
			case BROKEN:
				return isRed ? brokenRedBaseImage : brokenBlueBaseImage;
			case RUINS:
				return isRed ? redBaseRuinsImage : blueBaseRuinsImage;
		}
		return normalRedBaseImage;
	}

	private BufferedImage getTableImage() {
		return this.team == Team.RED ? redTableImage : blueTableImage;
	}

	private Base.State getBaseState() {
		return Base.State.NORMAL;
	}

	// ============================= UIContentインターフェースのメソッド =============================

	@Override
	public void update() {
		if (hpChangeAnimationFrame > 0) hpChangeAnimationFrame--;
	}

	@Override
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {

		// ============================= 描画するコンテンツを用意 =============================

		// それぞれの画像を用意
		BufferedImage table = this.getTableImage();
		BufferedImage base = this.getBaseImage();

		// テキストを用意
		int hp = this.team == Team.RED ? info.getRedBaseHP() : info.getBlueBaseHP();
		String text = Integer.toString(hp);


		// ============================= コンテンツを描画する位置を計算 =============================

		// 台の位置を計算
		double tableX, tableY;
		if (this.team == Team.RED) {
			// ウィンドウの右下
			tableX = windowWidth - table.getWidth() * 4.0 / 5.0;
			tableY = windowHeight - table.getHeight() + 10;
		} else if (this.team == Team.BLUE) {
			// ウィンドウの左下
			tableX = -table.getWidth() / 5.0;
			tableY = windowHeight - table.getHeight() + 10;
		} else {
			throw new RuntimeException();
		}

		// 基地の位置を計算
		double baseX, baseY;
		if (this.team == Team.RED) {
			baseX = tableX - table.getWidth() / 3.0;
			baseY = tableY - table.getHeight() * 2.0 / 3.0;
		} else if (this.team == Team.BLUE) {
			baseX = tableX + table.getWidth() * 2.0 / 3.0;
			baseY = tableY + table.getHeight() * 2.0 / 3.0;
		} else {
			throw new RuntimeException();
		}

		// HPの文字を表示する位置を計算
		double textX, textY;
		if (this.team == Team.RED) {
			textX = baseX - 50;
			textY = baseY;
		} else if (this.team == Team.BLUE) {
			textX = baseX + 50;
			textY = baseY;
		} else {
			throw new RuntimeException();
		}

		// ============================= 基地と台の描画 =============================

		// 基地の描画
		AffineTransform baseTrans = new AffineTransform();
		baseTrans.translate(baseX, baseY);
		graphics.drawImage(base, baseTrans, null);

		// 台の描画
		AffineTransform tableTrans = new AffineTransform();
		tableTrans.translate(tableX, tableY);
		graphics.drawImage(table, tableTrans, null);


		// ============================= テキスト(基地のHP)の描画 =============================

		AffineTransform textTrans = new AffineTransform();

		// 1. フォントの設定 (サイズを大きく、太字に)
		Font font = new Font("Arial", Font.BOLD, 40);
		FontRenderContext frc = graphics.getFontRenderContext();

		// 2. 文字列を形状(Shape)に変換
		// String text は上で定義済み (Integer.toString(hp))
		GlyphVector gv = font.createGlyphVector(frc, text);
		Shape textShape = gv.getOutline();

		// 3. 位置合わせ（センタリング）
		// 文字の「幅」と「高さ」を取得して、指定座標(textX, textY)が「文字の中心」に来るように調整
		Rectangle bounds = textShape.getBounds();
		double xOffset = -bounds.getWidth() / 2.0;
		double yOffset = bounds.getHeight() / 2.0; // ベースライン(足元)からの高さ調整

		// 4. アフィン変換の適用
		textTrans.translate(textX, textY);     // 指定位置へ移動
		textTrans.translate(xOffset, yOffset); // 中心補正

		// 変換適用済みのShapeを作成
		Shape finalShape = textTrans.createTransformedShape(textShape);

		// 5. 描画設定 (レンダリング品質を上げる)
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		// --- 装飾: 縁取り (黒色) ---
		graphics.setColor(Color.BLACK);
		// BasicStroke(太さ, 端の形, 接合部の形)
		graphics.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.draw(finalShape);

		// --- 本体: 塗りつぶし (白色) ---
		graphics.setColor(Color.WHITE);
		graphics.fill(finalShape);	}
}
