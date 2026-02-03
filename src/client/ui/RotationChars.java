package client.ui;

import stage.ScreenObject;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class RotationChars implements ScreenObject {

    public static final int size = 300;

    private final String text;
    private final Point2D.Double position;
    private final double animationOffset;
    private int frameCount = 0;

    // アニメーション設定
    private static final int PAUSE_FRAMES = 180;      // 静止時間
    private static final int ROTATION_FRAMES = 60;  // 一周にかかる時間

    // 色設定
    private Color fillColor = Color.WHITE;

    public RotationChars(String text, Point2D.Double position, double animationOffset, Color color) {
        this.text = text;
        this.position = position;
        this.animationOffset = animationOffset;
    }

    @Override
    public void update() {
        frameCount++;
    }

    @Override
    public void draw(Graphics2D graphics) {

        // ============================= テキストを描画 =============================

        // アニメーションの計算
        // 遅延→回転→停止→回転→停止→... の繰り返し
        int adjustedFrame = frameCount - (int) animationOffset;
        double scaleX = 1.0;
        double angle = 0.0;
        boolean isRotating = false;

        if (adjustedFrame >= 0) {
            // 最初の遅延後のフレーム数
            int cycleFrame = adjustedFrame % (ROTATION_FRAMES + PAUSE_FRAMES);

            // 回転フェーズ
            if (cycleFrame < ROTATION_FRAMES) {
                angle = cycleFrame * (2.0 * Math.PI / ROTATION_FRAMES);
                scaleX = Math.cos(angle);
                isRotating = true;
            }
            // それ以外は停止フェーズ (scaleX = 1.0のまま)
        }

        // 色の設定 (角度に応じて変更)
        if (isRotating) {
            double normalizedAngle = angle % (2.0 * Math.PI);

            // 裏を向いているとき (90度〜270度)
            if (normalizedAngle > Math.PI / 2 && normalizedAngle < 3 * Math.PI / 2) {
                fillColor = Color.LIGHT_GRAY;
            } else {
                // 表を向いているとき
                fillColor = Color.BLACK;
            }
        } else {
            // 静止時
            fillColor = Color.BLACK;
        }

        AffineTransform trans = new AffineTransform();

        // 1. フォントの設定 (サイズを大きく、太字に)
        Font font = new Font("Arial", Font.BOLD, size);
        FontRenderContext frc = graphics.getFontRenderContext();

        // 2. 文字列を形状(Shape)に変換
        GlyphVector gv = font.createGlyphVector(frc, this.text);
        Shape textShape = gv.getOutline();

        // 3. 位置合わせ（センタリング）
        // 文字の「幅」と「高さ」を取得して、指定座標(textX, textY)が「文字の中心」に来るように調整
        Rectangle bounds = textShape.getBounds();
        double xOffset = -bounds.getWidth() / 2.0;
        double yOffset = bounds.getHeight() / 2.0; // ベースライン(足元)からの高さ調整

        // 4. アフィン変換の適用
        trans.translate(position.x, position.y);     // 指定位置へ移動
        trans.scale(scaleX, 1.0);                    // X方向のスケール変化で回転を再現
        trans.translate(xOffset, yOffset);           // 中心補正

        // 変換適用済みのShapeを作成
        Shape finalShape = trans.createTransformedShape(textShape);

        // 5. 描画設定 (レンダリング品質を上げる)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // --- 本体: 塗りつぶし ---
        graphics.setColor(fillColor);
        graphics.fill(finalShape);


    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public Point2D.Double getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(Point2D.Double position) {
        this.position.setLocation(position);
    }
}
