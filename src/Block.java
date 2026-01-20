import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Block implements GameObject {

	private static final int WIDTH = 200;
	private static final int HEIGHT = 200;

	private final Point2D.Double translate = new Point2D.Double(0, 0);

	// 画像
	private Image blockImage;
	private static Image normalBlockImage;
	static {
		try {
			normalBlockImage = ImageIO.read(
					Objects.requireNonNull(Tank.class.getResource("assets/grey_block.png"))
			).getScaledInstance(WIDTH, HEIGHT, BufferedImage.SCALE_SMOOTH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Block(double x, double y) {
		this.blockImage = Block.normalBlockImage;
		this.translate.setLocation(x, y);
	}

	// ============================= GameObjectの実装 =============================

	@Override
	public void draw(Graphics2D graphics2D) {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.translate.x, this.translate.y);
		trans.translate(-WIDTH / 2.0, -HEIGHT / 2.0);
		graphics2D.drawImage(this.blockImage, trans, null);
	}

	@Override
	public void update() {

	}

	@Override
	public double getRadius() {
		return Math.sqrt(Math.pow(WIDTH / 2.0, 2) + Math.pow(HEIGHT / 2.0, 2));
	}

	@Override
	public Point2D.Double getTranslate() {
		return this.translate;
	}

	@Override
	public void onCollision(GameObject other) {

	}

	@Override
	public boolean shouldRemove() {
		return false;
	}

	@Override
	public boolean isTangible() {
		return true;
	}

	// =============================  =============================
	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}
}
