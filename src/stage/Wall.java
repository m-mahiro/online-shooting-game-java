package stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

public class Wall implements GameObject {

	// 特徴
	public static final int WIDTH = 100;
	public static final int HEIGHT = 100;

	// 状態（クライアント間の同期に必要)
	private final Point2D.Double position;

	// 画像リソース
	private static final Image wallImage;

	static {
		try {
			wallImage = ImageIO.read(Objects.requireNonNull(Wall.class.getResource("/client/assets/wall.png"))).getScaledInstance(WIDTH + 1, HEIGHT + 1, Image.SCALE_SMOOTH);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 新しい壁オブジェクトを生成します。
	 * 壁は固定サイズで破壊不可能なオブジェクトとして初期化されます。
	 */
	public Wall(Point2D.Double coordinate) {
		this.position = (Point2D.Double) coordinate.clone();
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	/**
	 * {@inheritDoc}
	 * 壁は静的なオブジェクトであるため、このメソッドは何も処理しません。
	 */
	@Override
	public void update() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void draw(Graphics2D graphics) {
		AffineTransform trans = new AffineTransform();
		trans.translate(this.position.x, this.position.y);
		trans.translate(-WIDTH / 2.0, -HEIGHT / 2.0);
		graphics.drawImage(wallImage, trans, null);
	}

	/**
	 * {@inheritDoc}
	 * 壁は衝突によって特別な状態変化を起こさないため、このメソッドは何も処理しません。
	 */
	@Override
	public void onCollision(GameObject other) {
	}

	/**
	 * {@inheritDoc}
	 * 壁はダメージを受けないため、このメソッドは何も処理しません。
	 */
	@Override
	public void onHitBy(Projectile other) {
	}

	/**
	 * {@inheritDoc}
	 * 壁はゲーム終了までステージに残り続けるため、常にfalseを返します。
	 */
	@Override
	public boolean isExpired() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 壁は常に剛体として扱われます。
	 */
	@Override
	public boolean hasRigidBody() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 壁は常にTANGIBLE_OBJECTレイヤーとして描画されます。
	 */
	@Override
	public RenderLayer getRenderLayer() {
		return RenderLayer.TANGIBLE_OBJECT;
	}

	/**
	 * {@inheritDoc}
	 * 壁は長方形の形状を持ちます。
	 */
	@Override
	public Shape getShape() {
		return new Rectangle(this.position, WIDTH, HEIGHT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Point2D.Double getPosition() {
		return (Point2D.Double) this.position.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPosition(Point2D.Double position) {
		this.position.setLocation(position);
	}

	/**
	 * {@inheritDoc}
	 * 壁は破壊されないため、常にInteger.MAX_VALUEを返します。
	 */
	@Override
	public int getHP() {
		return Integer.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 * 壁は常にOBSTACLEチームに所属します。
	 */
	@Override
	public Team getTeam() {
		return OBSTACLE;
	}
}
