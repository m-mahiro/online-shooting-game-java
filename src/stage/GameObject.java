package stage;

import java.awt.*;
import java.awt.geom.Point2D;

public interface GameObject {

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
	 * 他のGameObjectとの衝突時に呼び出されます。
	 * @param other 衝突した他のGameObject
	 */
	void onCollision(GameObject other);

	/**
	 * プロジェクタイルから被弾した際に呼び出されます。
	 * @param other 衝突したプロジェクタイル
	 */
	void onHitBy(Projectile other);

	/**
	 * オブジェクトがステージから削除されるべきかを判定します。
	 * @return オブジェクトが削除されるべきであればtrue、そうでなければfalse
	 */
	boolean isExpired();

	/**
	 * オブジェクトが剛体として扱われるべきか（衝突判定の対象となるか）を判定します。
	 * @return オブジェクトが剛体であればtrue、そうでなければfalse
	 */
	boolean hasRigidBody();

	/**
	 * オブジェクトの描画レイヤーを取得します。
	 * @return オブジェクトの描画レイヤー
	 */
	RenderLayer getRenderLayer();

	/**
	 * オブジェクトの衝突判定に使用される形状を取得します。
	 * @return オブジェクトの形状を表すShapeオブジェクト
	 */
	Shape getShape();

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

	/**
	 * オブジェクトの現在のHPを取得します。
	 * @return オブジェクトの現在のHP
	 */
	int getHP();

	/**
	 * オブジェクトが所属するチームを取得します。
	 * @return オブジェクトのチーム
	 */
	Team getTeam();
}
