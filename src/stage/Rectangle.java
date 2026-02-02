package stage;

import java.awt.geom.Point2D;

public class Rectangle implements Shape {

	public Point2D.Double center;
	public double width;
	public double height;

	    /**
	     * 新しい長方形オブジェクトを生成します。
	     * @param center 長方形の中心座標
	     * @param width 長方形の幅
	     * @param height 長方形の高さ
	     */
	    public Rectangle(Point2D.Double center, double width, double height) {		this.center = center;
		this.width = width;
		this.height = height;
	}
}
