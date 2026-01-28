import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

public class Bullet implements GameObject, DangerGameObject {

	// 特徴
	private static final int COLLISION_RADIUS = 5;
	private static final double VELOCITY = 30;
	private static final int LIFE_TIME = GamePanel.FPS * 6; // 生存フレーム数
	private static final int DAMAGE_ABILITY = 10;

	// 状態（クライアント間の同期に必要)
	private Tank tank;
	private final Point2D.Double position; // 弾丸オブジェクトの中心座標
	private final double dx, dy;
	private int lifeFrame = LIFE_TIME;

	// 演出用定数
	private final int DEBRIS_LIFE_FRAME = GamePanel.FPS / 4;

	// 演出用変数（クライアント間の同期は必要ない）
	private double renderScale = 1.0;
	private int debrisLifeFrame = 0;

	// 効果音
	private final SoundManager sound = new SoundManager();

	// 画像リソース（共有）
	private static BufferedImage blueNormalBulletImage, redNormalBulletImage, blueBulletDebris, redBulletDebris, noneImage;

	protected enum Status {
		NORMAL, DEBRIS, SHOULD_REMOVE
	}

	static {
		try {
			noneImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/none_image.png")));
			blueNormalBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("assets/bullet_blue.png")));
			redNormalBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("assets/bullet_red_normal.png")));
			blueBulletDebris = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("assets/bullet_blue_debris.png")));
			redBulletDebris = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("assets/bullet_red_debris.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Bullet(Point2D.Double tankPosition, double angle, Tank tank) {
		double x = tankPosition.x + (tank.getBulletReleaseRadius() + this.getCollisionRadius()) * 1.3 * Math.cos(tank.getGunAngle());
		double y = tankPosition.y + (tank.getBulletReleaseRadius() + this.getCollisionRadius()) * 1.3 * Math.sin(tank.getGunAngle());
		this.position = new Point2D.Double(x, y);
		this.tank = tank;

		// 角度から速度ベクトルを計算
		this.dx = Math.cos(angle) * getVelocity();
		this.dy = Math.sin(angle) * getVelocity();
	}

	// ============================= Bulletクラス独自のメソッド =============================

	protected void explode() {
		sound.bulletExplosion();
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
		this.lifeFrame = 0;
	}

	protected double getCollisionRadius() {
		return COLLISION_RADIUS;
	}

	protected double getVelocity() {
		return VELOCITY;
	}

	protected Team getTeam() {
		return this.tank.getTeam();
	}

	protected BufferedImage getImage() {
		boolean isRed = this.getTeam() == Team.RED;
		switch (getStatus()) {
			case NORMAL:
				return isRed ? redNormalBulletImage : blueNormalBulletImage;
			case DEBRIS:
				return isRed ? redBulletDebris : blueBulletDebris;
			case SHOULD_REMOVE:
				return noneImage;
			default:
				throw new RuntimeException();
		}
	}

	protected Status getStatus() {
		if (debrisLifeFrame > 0) return Status.DEBRIS;
		if (lifeFrame <= 0) return Status.SHOULD_REMOVE;
		return Status.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================


	@Override
	public void update() {
		lifeFrame--;
		if (lifeFrame == 0) {
			explode();
			return;
		}
		switch (getStatus()) {
			case NORMAL: {
				this.position.x += dx;
				this.position.y += dy;
				break;
			}
			case DEBRIS: {
				debrisLifeFrame--;
				renderScale += (GamePanel.FPS / 120.0) * debrisLifeFrame / 100.0;
				break;
			}
		}
	}


	@Override
	public void draw(Graphics2D graphics) {
		BufferedImage image = getImage();
		AffineTransform trans = new AffineTransform();
		trans.translate(this.position.x, this.position.y);
		trans.rotate(Math.atan2(dy, dx));
		trans.scale(renderScale, renderScale);
		trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		graphics.drawImage(image, trans, null);
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
		return getStatus() == Status.SHOULD_REMOVE;
	}

	@Override
	public boolean isTangible() {
		return getStatus() == Status.NORMAL;
	}

	@Override
	public RenderLayer getRenderLayer() {
		switch (getStatus()) {
			case NORMAL:
				return RenderLayer.PROJECTILE;
			case DEBRIS:
			case SHOULD_REMOVE:
				return RenderLayer.DEBRIS;
			default:
				throw new RuntimeException();
		}
	}

	@Override
	public Shape getShape() {
		return new Circle(this.position, this.getCollisionRadius());
	}

	@Override
	public Point2D.Double getPosition() {
		return (Point2D.Double) this.position.clone();
	}

	@Override
	public void setPosition(double x, double y) {
		this.position.setLocation(x, y);
	}

	@Override
	public double getHP() {
		return 10;
	}


	// ============================= DangerGameObjectインタフェースのメソッド =============================

	public int getDamageAbility() {
		return DAMAGE_ABILITY;
	}

}