package stage;

import java.awt.*;
import java.awt.geom.Point2D;

public interface UpperStageObject {

	void update();

	void draw(Graphics2D graphics);

	void isExpired();

	Point2D.Double getPosition();

	void setPosition(Point2D.Double position);

}
