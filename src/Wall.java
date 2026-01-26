import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Wall implements GameObject {

	// 特徴
	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;

	// 状態（クライアント間の同期に必要)
	private final Point2D.Double translate;

	// 画像リソース
	private static final Image wallImage;

	static {
		try {
			wallImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/wall.png"))).getScaledInstance(WIDTH + 1, HEIGHT + 1, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Wall(Point2D.Double coordinate) {
		this.translate = (Point2D.Double) coordinate.clone();
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	@Override
	public void update() {

	}

	@Override
	public void draw(Graphics2D graphics) {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.translate.x, this.translate.y);
		trans.translate(-WIDTH, -HEIGHT);
		graphics.drawImage(wallImage, trans, null);
	}

	@Override
	public void onCollision(GameObject other) {

	}

	@Override
	public void onHitBy(DangerGameObject other) {

	}

	@Override
	public boolean shouldRemove() {
		return false;
	}

	@Override
	public boolean isTangible() {
		return true;
	}

	@Override
	public RenderLayer getRenderLayer() {
		return RenderLayer.TANGIBLE_OBJECT;
	}

	@Override
	public Shape getShape() {
		return new Rectangle(this.translate, WIDTH, HEIGHT);
	}

	@Override
	public Point2D.Double getTranslate() {
		return (Point2D.Double) this.translate.clone();
	}

	@Override
	public void setTranslate(double x, double y) {
		this.translate.x = x;
		this.translate.y = y;
	}
}
