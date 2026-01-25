import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

public class Bullet implements GameObject, DangerGameObject {

	// 特徴
	private final double VELOCITY = 30;
	private final int LIFE_TIME = GamePanel.FPS * 6; // 生存フレーム数
	public static final int OBJECT_RADIUS = 10;
	public double damageAbility = 10.0;
	private Tank tank;


	// 状態
	private Point2D.Double translate = new Point2D.Double(0, 0); // 弾丸オブジェクトの中心座標
	private double dx, dy; // 1フレームあたりの移動量
	private int lifeCount; // 経過フレーム数
	private boolean isExploded = false;

	// 画像リソース（共有）
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
	 *
	 * @param x     初期X座標s
	 * @param y     初期Y座標
	 * @param angle 発射角度（ラジアン）
	 */
	public Bullet(double x, double y, double angle, Tank tank) {
		this.translate.setLocation(x, y);
		this.lifeCount = 0;
		this.tank = tank;

		// 角度から速度ベクトルを計算
		this.dx = Math.cos(angle) * VELOCITY;
		this.dy = Math.sin(angle) * VELOCITY;

		switch (tank.getTeam()) {
			case RED: {
				this.bulletImage = redBulletImage;
				break;
			}
			case BLUE: {
				this.bulletImage = blueBulletImage;
				break;
			}
			default:
				assert false;
		}
	}

	// ============================= Bulletクラス独自のメソッド =============================

	private void explode() {
		this.isExploded = true;
	}

	public double getCollisionRadius() {
		return bulletImage.getWidth();
	}

	// ============================= GameObjectインタフェースのメソッド =============================


	@Override
	public void update() {
		if (!isExploded) {
			this.translate.x += dx;
			this.translate.y += dy;
			lifeCount++;
		}
	}


	@Override
	public void draw(Graphics2D graphics) {
		if (isExploded) return;
		AffineTransform trans = new AffineTransform();
		trans.translate(this.translate.x, this.translate.y);
		trans.rotate(Math.atan2(dy, dx));
		trans.translate(-this.bulletImage.getWidth() / 2.0, -this.bulletImage.getHeight() / 2.0);
		graphics.drawImage(this.bulletImage, trans, null);
	}

	@Override
	public void onCollision(GameObject other) {
		this.explode();     // とりあえず自身が爆発
		other.onHitBy(this); // 相手に被弾を通知する
	}

	@Override
	public void onHitBy(DangerGameObject other) {
		// 何もしない
	}

	@Override
	public boolean shouldRemove() {
		return lifeCount > LIFE_TIME || this.isExploded;
	}

	@Override
	public boolean isTangible() {
		return true;
	}

	@Override
	public Shape getShape() {
		return new Circle(this.translate, this.getCollisionRadius());
	}

	@Override
	public Point2D.Double getTranslate() {
		return (Point2D.Double) this.translate.clone();
	}

	@Override
	public void setTranslate(double x, double y) {
		this.translate.setLocation(x, y);
	}


	// ============================= DangerGameObjectインタフェースのメソッド =============================

	public double getDamageAbility() {
		return this.damageAbility;
	}

}