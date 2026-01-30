package stage;

import java.awt.geom.Point2D;

public class Circle implements Shape {

	public Point2D.Double center;
	public double radius;

	public Circle(Point2D.Double center, double radius) {
		this.center = center;
		this.radius = radius;
	}
}
