import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Block implements GameObject {

	private final Point2D.Double translate = new Point2D.Double(0, 0);

	// 画像
	private BufferedImage blockImage;
	private static BufferedImage normalBlockImage, transparentBlockImage;
	static {
		try {
			normalBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_grey.png")));
			transparentBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_water_grey.png")));
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
	public void draw(Graphics2D graphics) {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.translate.x, this.translate.y);
		trans.translate(-getWidth() / 2.0, -getHeight() / 2.0);
		graphics.drawImage(getBlockImage(), trans, null);
	}

	@Override
	public void update() {

	}

	@Override
	public double getCollisionRadius() {
		return Math.sqrt(Math.pow(getWidth() / 2.0, 2) + Math.pow(getWidth() / 2.0, 2));
	}

	@Override
	public Point2D.Double getTranslate() {
		return this.translate;
	}

	@Override
	public void setTranslate(double x, double y) {
		this.translate.setLocation(x, y);
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
		return blockImage.getWidth();
	}

	public int getHeight() {
		return blockImage.getHeight();
	}

	private BufferedImage getBlockImage() {
		return isTangible() ? normalBlockImage : transparentBlockImage;
	}
}
