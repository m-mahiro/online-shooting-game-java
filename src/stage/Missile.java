package stage;

import client.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

public class Missile implements GameObject, Projectile {

	// 定数
	private static final double VELOCITY = 50;
	private static final double MAX_OBJECT_SCALE = 5.0;
	private static final int MAX_DAMAGE_ABILITY = 200;

	// 状態
	private Tank shooter;
	private Point2D.Double position;
	private double angle;
	private Status state = Status.CHARGING;
	private int chargeCount = 0;
	private int damageTotal = 0;

	// 演出用定数
	private static final int CANCEL_ANIMATION_FRAME = (int) (GamePanel.FPS * 0.5);

	// 演出用変数
	private int cancelAnimationFrame = 0;

	// 画像リソース
	private static BufferedImage blueChargingMissileImage, redChargingMissileImage;
	private static BufferedImage blueReadyMissileImage, redReadyMissileImage;
	private static BufferedImage blueMissileDebris, redMissileDebris;
	private static BufferedImage noneImage;

	// 状態管理
	private enum Status {
		CHARGING, CANCELLED, FLYING, DEBRIS, SHOULD_REMOVE
	}

	static {
		try {
			noneImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/none_image.png")));

			blueChargingMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_charging.png")));
			redChargingMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_charging.png")));

			blueReadyMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_ready.png")));
			redReadyMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_ready.png")));

			blueMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/bullet_blue_debris.png")));
			redMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/bullet_red_debris.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Missile(Tank shooter) {
		this.shooter = shooter;
		this.position = new Point2D.Double();
		setPositionBaseOn(shooter);
	}

	// ============================= Missileクラス独自のメソッド =============================

	public void decreaseDamageAbility(int damage) {
		damageTotal += damage;
		if (this.getHP() <= 0) explode();
	}

	public void explode() {
		this.state = Status.DEBRIS;
	}

	private void setPositionBaseOn(Tank shooter) {
		this.angle = shooter.getGunAngle();
		double shooterRadius = shooter.getBulletReleaseRadius();
		double missileRadius = this.getCollisionRadius();
		Point2D.Double bulletPosition = (Point2D.Double) shooter.getPosition().clone();
		double x = bulletPosition.x + (shooterRadius + missileRadius) * Math.cos(this.angle);
		double y = bulletPosition.y + (shooterRadius + missileRadius) * Math.sin(this.angle);
		this.setPosition(x, y);
	}

	public void launch() {
		this.state = Status.FLYING;
	}

	public Team getTeam() {
		return this.shooter.getTeam();
	}

	private double getObjectScale() {
		switch (this.state) {
			case DEBRIS:
			case CANCELLED:
			case SHOULD_REMOVE:
				return 1.0;
			case CHARGING:
			case FLYING:
				return Math.max(MAX_OBJECT_SCALE, getDamageAbility() / 100.0);
			default:
				throw new IllegalStateException("Unexpected value: " + this.state);
		}
	}

	private double getCollisionRadius() {
		return getImage().getWidth() / 2.0 * getObjectScale();
	}

	private BufferedImage getImage() {
		boolean isRed = (getTeam() == RED);
		switch (this.state) {
			case CHARGING:
			case CANCELLED:
				return isRed ? redChargingMissileImage : blueChargingMissileImage;
			case FLYING:
				return isRed ? redReadyMissileImage : blueReadyMissileImage;
			case DEBRIS:
				return isRed ? redMissileDebris : blueMissileDebris;
			case SHOULD_REMOVE:
				return noneImage;
			default:
				throw new RuntimeException("Unknown Status: " + state);
		}
	}

	// ============================= GameObjectインターフェースのメソッド =============================

	@Override
	public void update() {

		// チャージ中は戦車の位置や砲塔の向きに合わせてミサイルの位置を変える
		switch (this.state) {
			case CHARGING:
				setPositionBaseOn(shooter);
				break;
			case FLYING:
				double dx = VELOCITY * Math.cos(this.angle);
				double dy = VELOCITY * Math.sin(this.angle);
				this.position.x += dx;
				this.position.y += dy;
				break;
		}

		// フレームカウントダウン
		if (cancelAnimationFrame > 0) cancelAnimationFrame--;

		// フレームカウントアップ
		if (state == Status.CHARGING) chargeCount++;
	}

	@Override
	public void draw(Graphics2D graphics) {
		BufferedImage image = getImage();
		double objectScale = this.getObjectScale();
		AffineTransform trans = new AffineTransform();
		trans.translate(position.x, position.y);
		trans.rotate(angle);
		trans.scale(objectScale, objectScale);
		trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		graphics.drawImage(image, trans, null);
	}

	@Override
	public void onCollision(GameObject other) {

		if(this.state == Status.CHARGING) explode();

		// 衝突が相手のオブジェクトなら、被弾通知をおくる。
		if (other.getTeam() != this.getTeam()) {
			other.onHitBy(this);
			// 相手のHPの分だけ自分の殺傷能力をを削る
			this.decreaseDamageAbility(other.getHP());
		}

	}

	@Override
	public void onHitBy(Projectile other) {
		decreaseDamageAbility(other.getDamageAbility());
	}

	@Override
	public boolean isExpired() {
		switch (this.state) {
			case CANCELLED:
				return cancelAnimationFrame <= 0;
			case SHOULD_REMOVE:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean isTangible() {
		switch (this.state) {
			case CHARGING:
			case FLYING:
				return true;
			case CANCELLED:
			case DEBRIS:
			case SHOULD_REMOVE:
				return false;
			default:
				throw new IllegalStateException("Unexpected value: " + this.state);
		}
	}

	@Override
	public RenderLayer getRenderLayer() {
		switch (this.state) {
			case CHARGING:
			case CANCELLED:
			case FLYING:
				return RenderLayer.PROJECTILE;
			case DEBRIS:
			case SHOULD_REMOVE:
				return RenderLayer.DEBRIS;
			default:
				throw new IllegalStateException("Unexpected value: " + this.state);
		}
	}

	@Override
	public Shape getShape() {
		return new Circle(this.position, getCollisionRadius());
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
	public int getHP() {
		return getDamageAbility();
	}


	// ============================= Projectileインターフェースのメソッド =============================

	@Override
	public int getDamageAbility() {
		if (this.state == Status.FLYING) {
			int damageAbility = Math.min(chargeCount / GamePanel.FPS * 10, MAX_DAMAGE_ABILITY) - damageTotal;
			return Math.max(damageAbility, 0);
		}
		return 0;
	}
}