package stage;

import java.awt.geom.Point2D;

public class Rectangle implements Shape {

	public Point2D.Double center;
	public double width;
	public double height;

	public Rectangle(Point2D.Double center, double width, double height) {
		this.center = center;
		this.width = width;
		this.height = height;
	}
}
