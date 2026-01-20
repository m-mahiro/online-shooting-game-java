import java.awt.*;
import java.awt.geom.Point2D;

public interface GameObject {

	void draw(Graphics2D graphics2D);

	void update();

	double getRadius();

	Point2D.Double getTranslate();

	void onCollision(GameObject other);

	boolean shouldRemove();

	boolean isTangible();
}
