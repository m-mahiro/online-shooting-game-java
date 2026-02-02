package stage;

import java.awt.*;
import java.awt.geom.Point2D;

public interface ScreenObject {

	/**
	 * オブジェクトの状態を更新します。
	 */
	void update();

	/**
	 * オブジェクト自身を描画します。
	 * @param graphics 描画に使用するGraphics2Dオブジェクト
	 */
	void draw(Graphics2D graphics);

	/**
	 * オブジェクトがステージから削除されるべきかを判定します。
	 * @return オブジェクトが削除されるべきであればtrue、そうでなければfalse
	 */
	boolean isExpired();

	/**
	 * オブジェクトの中心座標を取得します。
	 * @return オブジェクトの中心座標のPoint2D.Doubleオブジェクト
	 */
	Point2D.Double getPosition();

	/**
	 * オブジェクトの中心座標を設定します。
	 * @param position 設定する新しい中心座標のPoint2D.Doubleオブジェクト
	 */
	void setPosition(Point2D.Double position);

}
