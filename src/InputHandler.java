import java.awt.geom.AffineTransform;

public interface InputHandler {

	double[] getMoveVector(AffineTransform canvasTransform);

	double[] getAimedCoordinate(AffineTransform canvasTransform);

	boolean gunButtonPressed();

	int getZoomAmount();
}
