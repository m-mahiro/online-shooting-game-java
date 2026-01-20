import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

public class Bullet implements GameObject {

	private final double VELOCITY = 15;
	private final int LIFE_TIME = 400; // 生存フレーム数
	public static final int OBJECT_RADIUS = 10;
	public double damageAbility = 10.0;

	// --- 状態 ---
	private Point2D.Double translate = new Point2D.Double(0, 0); // 弾丸オブジェクトの中心座標
	private double dx, dy; // 1フレームあたりの移動量
	private int lifeCount; // 経過フレーム数
	private boolean isExploded = false;

	// --- 画像リソース（共有） ---
	private BufferedImage bulletImage;
	private static BufferedImage blueBulletImage, redBulletImage;
	static {
		try {
			blueBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("assets/bullet_blue.png")));
			redBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("assets/bullet_red.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * コンストラクタ
	 * @param x 初期X座標s
	 * @param y 初期Y座標
	 * @param angle 発射角度（ラジアン）
	 */
	public Bullet(double x, double y, double angle, Team team) {
		this.translate.setLocation(x, y);
		this.lifeCount = 0;

		// 角度から速度ベクトルを計算
		this.dx = Math.cos(angle) * VELOCITY;
		this.dy = Math.sin(angle) * VELOCITY;

		switch (team) {
			case RED: {
				this.bulletImage = redBulletImage;
				break;
			}
			case BLUE: {
				this.bulletImage = blueBulletImage;
				break;
			}
			default: assert false;
		}
	}

	public double getDamageAbility() {
		return this.damageAbility;
	}


	// ============================= GameObjectインタフェースの実装 =============================

	@Override
	public double getRadius() {
		return OBJECT_RADIUS;
	}

	@Override
	public Point2D.Double getTranslate() {
		return this.translate;
	}

	@Override
	public void onCollision(GameObject other) {
		if (!other.isTangible()) return;
		this.explode();
		if (other instanceof Tank) {
			Tank tank = (Tank)other;
			tank.onHit(this);
		} else if (other instanceof Bullet) {
			Bullet bullet = (Bullet)other;
			bullet.explode();
		}
	}

	// ============================= 描画系処理 =============================

	public void draw(Graphics2D g2d) {
		if (isExploded) return;
		AffineTransform trans = new AffineTransform();
		trans.translate(this.translate.x, this.translate.y);
		trans.rotate(Math.atan2(dy, dx));
		trans.translate(-this.bulletImage.getWidth() / 2.0, -this.bulletImage.getHeight() / 2.0);
		g2d.drawImage(this.bulletImage, trans, null);
	}

	public void update() {
		if (!isExploded) {
			this.translate.x += dx;
			this.translate.y += dy;
			lifeCount++;
		}
	}
	public boolean shouldRemove() {
		return lifeCount > LIFE_TIME || this.isExploded;
	}

	@Override
	public boolean isTangible() {
		return true;
	}

	public void explode() {
		this.isExploded = true;
	}

}