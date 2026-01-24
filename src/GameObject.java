import java.awt.*;
import java.awt.geom.Point2D;

public interface GameObject {

	void draw(Graphics2D graphics);

	void update();

	double getCollisionRadius();

	Point2D.Double getTranslate();

	void setTranslate(double x, double y);

	void onCollision(GameObject other);

	boolean shouldRemove();

	boolean isTangible();
}
