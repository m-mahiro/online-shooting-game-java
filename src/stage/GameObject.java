package stage;

import java.awt.*;
import java.awt.geom.Point2D;

public interface GameObject {

	void update();

	void draw(Graphics2D graphics);

	void onCollision(GameObject other);

	void onHitBy(Projectile other);

	boolean isExpired();

	boolean isTangible();

	RenderLayer getRenderLayer();

	Shape getShape();

	Point2D.Double getPosition();

	void setPosition(Point2D.Double position);

	int getHP();

	Team getTeam();
}
