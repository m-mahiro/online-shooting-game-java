import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Missile implements GameObject, DangerGameObject {

	// 定数
	private static final double VELOCITY = 50;
	private static final double COLLISION_RADIUS_IN_CHARGING = 10;
	private static final double COLLISION_RADIUS_IN_FLYING = 30;
	private static final int CHARGE_FRAME = GamePanel.FPS * 2;

	// 状態
	private Tank shooter;
	private Point2D.Double position;
	private double angle;
	private Status state = Status.CHARGING;
	private int damageAbility = 500;
	private int chargeFrame = CHARGE_FRAME;
	private double objectScale = 1.0;

	// 演出用定数
	private static final int CANCEL_ANIMATION_FRAME = (int) (GamePanel.FPS * 0.5);

	// 演出用変数
	private int cancelAnimationFrame = 0;

	// 画像リソース
	private static BufferedImage blueNormalMissileImage, redNormalMissileImage, blueMissileDebris, redMissileDebris, noneImage;

	static {
		try {
			noneImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/none_image.png")));
			blueNormalMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/missile_blue_normal.png")));
			redNormalMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/missile_blue_normal.png")));
			blueMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/bullet_blue_debris.png")));
			redMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/bullet_red_debris.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 状態管理
	private enum Status {
		CHARGING, CANCELLED, IS_READY, FLYING, DEBRIS, SHOULD_REMOVE
	}


	public Missile(Tank shooter) {
		this.shooter = shooter;
		this.position = new Point2D.Double();
		setPositionBaseOn(shooter);
	}

	// ============================= Missileクラス独自のメソッド =============================

	public boolean finishEnergyCharge() {
		switch (this.state) {
			case CHARGING:
				this.state = Status.CANCELLED;
				return false;
			case IS_READY:
				launch();
				return true;
			default:
				return false;
		}
	}

	public void decreaseDamageAbility(int damage) {
		damageAbility -= damage;
		if (damageAbility <= 0) onDie();
	}

	public void onDie() {
		explode();
	}

	public void explode() {
		this.state = Status.DEBRIS;
	}

	private void setPositionBaseOn(Tank shooter) {
		this.angle = shooter.getGunAngle();
		double shooterRadius = shooter.getBulletReleaseRadius();
		double missileRadius = this.getCollisionRadius();
		Point2D.Double tankPosition = (Point2D.Double) shooter.getPosition().clone();
		double x = tankPosition.x + (shooterRadius + missileRadius) * Math.cos(this.angle);
		double y = tankPosition.y + (shooterRadius + missileRadius) * Math.sin(this.angle);
		this.setPosition(x, y);
	}

	private void launch() {
		this.state = Status.FLYING;
	}

	private Team getTeam() {
		return this.shooter.getTeam();
	}

	private double getCollisionRadius() {
		switch (this.state) {
			case CHARGING:
			case IS_READY:
				return COLLISION_RADIUS_IN_CHARGING;
			case FLYING:
				return COLLISION_RADIUS_IN_FLYING;
			case CANCELLED:
			case DEBRIS:
			case SHOULD_REMOVE:
				return 0;
			default:
				throw new IllegalStateException("Unexpected value: " + this.state);
		}
	}

	private BufferedImage getImage() {
		boolean isRed = (getTeam() == Team.RED);
		switch (this.state) {
			case CHARGING:
			case CANCELLED:
			case IS_READY:
			case FLYING:
				return isRed ? redNormalMissileImage : blueNormalMissileImage;
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
		if (chargeFrame > 0) chargeFrame--;
		if (cancelAnimationFrame > 0) cancelAnimationFrame--;

		// 状態管理
		if (state == Status.CHARGING && chargeFrame <= 0) state = Status.IS_READY;

	}

	@Override
	public void draw(Graphics2D graphics) {
		BufferedImage image = getImage();
		AffineTransform trans = new AffineTransform();
		trans.translate(position.x, position.y);
		trans.rotate(angle);
		trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		trans.scale(objectScale, objectScale);
		graphics.drawImage(image, trans, null);
	}

	@Override
	public void onCollision(GameObject other) {
		if (other instanceof Tank) {
			Tank tank = (Tank) other;
			if (tank.getTeam() == shooter.getTeam()) return;
		}

		// 相手に被弾を知らせる
		if (this.state == Status.FLYING) other.onHitBy(this);

		// 相手のHPの分だけ自分の殺傷能力をを削る
		this.decreaseDamageAbility(other.getHP());
	}

	@Override
	public void onHitBy(DangerGameObject other) {
		decreaseDamageAbility(other.getDamageAbility());
	}

	@Override
	public boolean shouldRemove() {
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
			case IS_READY:
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
			case IS_READY:
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
		return damageAbility;
	}


	// ============================= DangerObjectインターフェースのメソッド =============================

	@Override
	public int getDamageAbility() {
		if (this.state == Status.FLYING) {
			return damageAbility;
		} else {
			return 0;
		}
	}
}