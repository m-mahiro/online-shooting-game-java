package client.ui;

import stage.StageInfo;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

import static stage.Base.State.*;

import stage.Team;
import stage.Base;
import static stage.Team.*;

public class TeamInfoText implements UIContent {

	// どちらのチーム用の画面を表示すれば良いか
	private final Team team;

	// 表示する情報の提供元
	private StageInfo info;

	public TeamInfoText(Team team, StageInfo info) {
		this.team = team;
		this.info = info;
	}

	// ============================= UIContentインターフェースのメソッド =============================

	@Override
	public void update() {

	}

	@Override
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {


		// ============================= 表示するテキストを用意 =============================

		boolean isRed = this.team == RED;

		// このクラスでは、基地のHPか、戦車の残り代数のどちらかを表示する
		int baseHp = isRed ? info.getRedBaseHP() : info.getBlueBaseHP();
		int tankCount = isRed ? info.getRemainRedTank() : info.getRemainBlueTank();
		String tankCountText = Integer.toString(tankCount);
		String baseHpText = Integer.toString(baseHp);

		// 通常は基地のHP、基地が破壊されたら戦車の残り台数
		Base.State baseState = isRed ? info.getRedBaseState() : info.getBlueBaseState();
		boolean isRuins = baseState == RUINS;
		String text = isRuins ? tankCountText : baseHpText;


		// ============================= テキストを描画 =============================

		// テキストの位置を計算
		double textX = isRed ? windowWidth - 400 : 400;
		double textY = windowHeight - 80;

		AffineTransform textTrans = new AffineTransform();

		// 1. フォントの設定 (サイズを大きく、太字に)
		Font font = new Font("Arial", Font.BOLD, 100);
		FontRenderContext frc = graphics.getFontRenderContext();

		// 2. 文字列を形状(Shape)に変換
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
		graphics.fill(finalShape);

	}

	@Override
	public boolean isExpired() {
		return false;
	}
}
