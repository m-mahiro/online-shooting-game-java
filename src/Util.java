import java.awt.geom.Point2D;

public class Util {

	public static Point2D.Double subtract(Point2D.Double p1, Point2D.Double p2) {
		return new Point2D.Double(p1.x - p2.x, p1.y - p2.y);
	}

	public static Point2D.Double normalize(Point2D.Double p) {
		double x = p.x;
		double y = p.y;

		double norm = norm(p);
		if (norm == 0) return new Point2D.Double(0, 0);

		double x_dash = x / norm;
		double y_dash = y / norm;

		return new Point2D.Double(x_dash, y_dash);
	}

	public static Point2D.Double multiple(Point2D.Double p, double number) {
		return new Point2D.Double(p.x * number, p.y * number);
	}

	public static double norm(Point2D.Double p) {
		return Math.sqrt(Math.pow(p.x, 2) + Math.pow(p.y, 2));
	}

	public static Point2D.Double addition(Point2D.Double p1, Point2D.Double p2) {
		return new Point2D.Double(p1.x + p2.x, p1.y + p2.y);
	}

}
