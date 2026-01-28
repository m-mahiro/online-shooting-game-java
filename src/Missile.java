import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Missile extends Bullet {

	// 特徴
	private static final double VELOCITY = 50;
	private static final double COLLISION_RADIUS = 10;
	private static final int CHARGE_FRAME = GamePanel.FPS * 2;

	// 状態
	private boolean isFlying = false;
	private boolean isCanceled = false;
	private int damageAbility = 500;
	private int chargeFrame = 0;

	// 画像リソース
	private static BufferedImage blueNormalMissileImage, redNormalMissileImage, blueMissileDebris, redMissileDebris, noneImage;

	static {
		try {
			noneImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/none_image.png")));
			blueNormalMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/missile_blue_normal.png")));
			redNormalMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/missile_blue_normal.png")));
			blueMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/missile_blue_normal.png")));
			redMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("assets/missile_blue_normal.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Missile(Point2D.Double tankPosition, double angle, Tank tank) {
		super(tankPosition, angle, tank);
	}

	// ============================= Missileクラス独自のメソッド =============================

	public boolean launch() {
		if (chargeFrame > 0) {
			// チャージが足りなかった
			isCanceled = true;
		}
	}


	// ============================= Bulletクラスからのオーバーライド =============================

	@Override
	protected BufferedImage getImage() {
		boolean isRed = this.getTeam() == Team.RED;
		switch (getStatus()) {
			case NORMAL:
				return isRed ? redNormalMissileImage : blueNormalMissileImage;
			case DEBRIS:
				return isRed ? redMissileDebris : blueMissileDebris;
			case SHOULD_REMOVE:
				return noneImage;
			default:
				throw new RuntimeException("Unknown Status: " + getStatus());
		}
	}

	@Override
	protected double getCollisionRadius() {
		return COLLISION_RADIUS;
	}

	@Override
	protected double getVelocity() {
		return VELOCITY;
	}

	@Override
	public void update() {
		if (chargeFrame > 0) chargeFrame--;
		if (isFlying) {
			super.update();
		}
	}

	@Override
	public int getDamageAbility() {
		return damageAbility;
	}

	@Override
	public void onCollision(GameObject other) {
		other.onHitBy(this); // 相手に被弾を通知する
		this.damageAbility -= (int) other.getHP();
		if (getStatus() == Status.NORMAL) {
			if (damageAbility <= 0) explode();
			if (other instanceof Wall) explode();

		}
	}
}
