import java.awt.*;
import java.awt.geom.Point2D;

public interface GameObject {

	void update();

	void draw(Graphics2D graphics);

	void onCollision(GameObject other);

	void onHitBy(DangerGameObject other);

	boolean shouldRemove();

	boolean isTangible();

	RenderLayer getRenderLayer();

	Shape getShape();

	Point2D.Double getTranslate();

	void setTranslate(double x, double y);
}
