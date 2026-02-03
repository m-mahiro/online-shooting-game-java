package stage;

import java.awt.geom.Point2D;

public class Circle implements Shape {

	public Point2D.Double center;
	public double radius;

	/**
	 * 新しい円形オブジェクトを生成します。
	 * @param center 円の中心座標
	 * @param radius 円の半径
	 */
	public Circle(Point2D.Double center, double radius) {
		this.center = center;
		this.radius = radius;
	}
}
