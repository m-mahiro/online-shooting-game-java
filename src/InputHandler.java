import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public interface InputHandler {

	Point2D.Double getMoveVector(AffineTransform canvasTransform);

	Point2D.Double getAimedCoordinate(AffineTransform canvasTransform);

	boolean shootBullet();

	boolean chargeButtonPressed();

	boolean createBlock();

	int getZoomAmount();

	void onFrameUpdate();
}
