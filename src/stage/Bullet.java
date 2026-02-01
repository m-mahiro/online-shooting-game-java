package stage;

import client.GamePanel;
import client.SoundManager;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

import static stage.Team.*;

public class Bullet implements GameObject, Projectile {

	// 特徴
	private static final int COLLISION_RADIUS = 5;
	private static final double VELOCITY = 30;
	private static final int LIFE_TIME = GamePanel.FPS * 6; // 生存フレーム数
	private static final int DAMAGE_ABILITY = 10;

	// 状態（クライアント間の同期に必要)
	private Tank shooter;
	private final Point2D.Double position; // 弾丸オブジェクトの中心座標
	private final double dx, dy;
	private int lifeFrame = LIFE_TIME;

	// 演出用定数
	private final int DEBRIS_LIFE_FRAME = GamePanel.FPS / 4;

	// 演出用変数（クライアント間の同期は必要ない）
	private double renderScale = 1.0;
	private int debrisLifeFrame = 0;

	// 効果音
	private static final SoundManager sound = new SoundManager();

	// 画像リソース（共有）
	private static BufferedImage blueNormalBulletImage, redNormalBulletImage, blueBulletDebris, redBulletDebris, noneImage;

	private enum State {
		NORMAL, DEBRIS, SHOULD_REMOVE
	}

	static {
		try {
			noneImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/none_image.png")));
			blueNormalBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_blue.png")));
			redNormalBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_red_normal.png")));
			blueBulletDebris = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_blue_debris.png")));
			redBulletDebris = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_red_debris.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Bullet(Tank shooter) {
		double angle = shooter.getGunAngle();
		Point2D.Double tankPosition = shooter.getPosition();
		double x = tankPosition.x + (shooter.getBulletReleaseRadius() + this.getCollisionRadius()) * Math.cos(shooter.getGunAngle());
		double y = tankPosition.y + (shooter.getBulletReleaseRadius() + this.getCollisionRadius()) * Math.sin(shooter.getGunAngle());
		this.position = new Point2D.Double(x, y);
		this.shooter = shooter;

		// 角度から速度ベクトルを計算
		this.dx = Math.cos(angle) * getVelocity();
		this.dy = Math.sin(angle) * getVelocity();
	}

	// ============================= Bulletクラス独自のメソッド =============================

	private void explode() {
		sound.bulletExplosion();
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
		this.lifeFrame = 0;
	}

	private double getCollisionRadius() {
		return COLLISION_RADIUS;
	}

	private double getVelocity() {
		return VELOCITY;
	}

	public Team getTeam() {
		return this.shooter.getTeam();
	}

	private BufferedImage getImage() {
		boolean isRed = this.getTeam() == RED;
		switch (getState()) {
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

	private State getState() {
		if (debrisLifeFrame > 0) return State.DEBRIS;
		if (lifeFrame <= 0) return State.SHOULD_REMOVE;
		return State.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================


	@Override
	public void update() {
		lifeFrame--;
		if (lifeFrame == 0) {
			explode();
			return;
		}
		switch (getState()) {
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

		// 衝突が相手のオブジェクトなら、被弾通知をおくる。
		if (other.getTeam() != this.getTeam()) {
			other.onHitBy(this);
			explode();
		}
	}

	@Override
	public void onHitBy(Projectile other) {

	}

	@Override
	public boolean isExpired() {
		return getState() == State.SHOULD_REMOVE;
	}

	@Override
	public boolean isTangible() {
		return getState() == State.NORMAL;
	}

	@Override
	public RenderLayer getRenderLayer() {
		switch (getState()) {
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
	public void setPosition(Point2D.Double position) {
		this.position.setLocation(position);
	}

	@Override
	public int getHP() {
		return 10;
	}


	// ============================= Projectileインタフェースのメソッド =============================

	public int getDamageAbility() {
		return DAMAGE_ABILITY;
	}

}
