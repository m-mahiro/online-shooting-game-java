import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Tank implements GameObject {

	// 戦車の特徴
	private double velocity = 10;
	private Team team;

	public Point2D.Double translate = new Point2D.Double(0, 0); // オブジェクトの中心の座標
	private double chassisAngle; // ラジアン
	private double gunAngle; // ラジアン
	private double hp = 100.0;
	private boolean alive = true;
	private int damageFlushCounter = 0;
	private final int DAMAGE_FLUSH_FRAME = 70;

	private BufferedImage chassisImage, gunImage;
	private static BufferedImage redChassisImage, redGunImage, waterRedChassisImage, waterRedGunImage;
	private static BufferedImage blueChassisImage, blueGunImage, waterBlueChassisImage, waterBlueGunImage;

	static {
		try {
			redChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red.png")));
			redGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red.png")));
			waterRedChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_water_red.png")));
			waterRedGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_water_red.png")));

			blueChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue.png")));
			blueGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue.png")));
			waterBlueChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_water_blue.png")));
			waterBlueGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_water_blue.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Tank(double x, double y, Team team) {
		this.translate.setLocation(x, y);
		this.team = team;
		switch (this.team) {
			case RED: {
				this.chassisImage = redChassisImage;
				this.gunImage = redGunImage;
				break;
			}
			case BLUE: {
				this.chassisImage = blueChassisImage;
				this.gunImage = blueGunImage;
				break;
			}
			default:
				assert false;
		}
	}

	/**
	 * 座標x, yの方に砲塔を向ける。
	 *
	 * @param x x座標
	 * @param y y座標
	 */
	public void aimAt(double x, double y) {
		double dx = x - translate.x;
		double dy = y - translate.y;
		this.gunAngle = Math.atan2(dy, dx);
	}

	/**
	 * 戦車を与えられベクトルを正規化して、その方向に移動させる。
	 *
	 * @param x
	 * @param y
	 */
	public void moveFor(double x, double y) {
		Point2D.Double p = new Point2D.Double(x, y);
		p = Util.normalize(p);
		p = Util.multiple(p, this.velocity);
		p = Util.addition(this.translate, p);
		this.translate.setLocation(p);
	}

	public void setPosition(double x, double y) {
		this.translate.setLocation(x, y);
	}

	/**
	 * 現在の砲塔の方向に指定された弾丸を発射する。
	 */
	public Bullet shootBullet() {
		if (!alive) return null; // smell
		double initX = translate.x + (this.getRadius() + Bullet.OBJECT_RADIUS) * 1.2 * Math.cos(this.gunAngle);
		double initY = translate.y + (this.getRadius() + Bullet.OBJECT_RADIUS) * 1.2 * Math.sin(this.gunAngle);
		return new Bullet(initX, initY, this.gunAngle, this.team);
	}

	/**
	 * フレームを更新する際に呼び出されます。
	 */
	public void update() {
		damageFlushCounter--;
		if (damageFlushCounter > 0) {
			boolean flag = damageFlushCounter % 10 == 0;
			switch (this.team) {
				case RED: {
					this.chassisImage = flag ? waterRedChassisImage : redChassisImage;
					this.gunImage = flag ? waterRedGunImage : redGunImage;
					break;
				}
				case BLUE: {
					this.chassisImage = flag ? waterBlueChassisImage : blueChassisImage;
					this.gunImage = flag ? waterBlueGunImage : blueGunImage;
					break;
				}
				default:
					assert false;
			}
		}
	}

	public void onHit(Bullet bullet) {
		if (!this.alive) return;
		this.damage(bullet.getDamageAbility());
	}

	public void damage(double damage) {
		this.damageFlushCounter = DAMAGE_FLUSH_FRAME;
		this.hp -= damage;
		if (this.hp <= 0) {
			die();
		}
	}

	public void die() {
		this.alive = false;
		this.damageFlushCounter = 0;
		switch (this.team) {
			case RED: {
				this.chassisImage = waterRedChassisImage;
				this.gunImage = waterRedGunImage;
				break;
			}
			case BLUE: {
				this.chassisImage = waterBlueChassisImage;
				this.gunImage = waterBlueGunImage;
				break;
			}
			default:
				assert false;
		}

	}

	@Override
	public void onCollision(GameObject other) {
		if(!this.alive) return;
		System.out.println("衝突が起きました");
		Point2D.Double vector = Util.subtract(this.translate, other.getTranslate());
		vector = Util.normalize(vector);
		this.moveFor(vector.x, vector.y);
	}

	@Override
	public boolean shouldRemove() {
		return false;
	}

	@Override
	public boolean isTangible() {
		return this.alive;
	}

	public void draw(Graphics2D graphics2D) {
		// 台車の描画
		AffineTransform chassisTransform = new AffineTransform();
		chassisTransform.translate(translate.x, translate.y);
		chassisTransform.rotate(this.chassisAngle);
		chassisTransform.translate(-this.chassisImage.getWidth() / 2.0, -this.chassisImage.getHeight() / 2.0);
		graphics2D.drawImage(this.chassisImage, chassisTransform, null);

		// 砲塔のの描画
		AffineTransform gunTransform = new AffineTransform();
		gunTransform.translate(translate.x, translate.y);
		gunTransform.rotate(this.gunAngle);
		gunTransform.translate(-this.gunImage.getWidth() / 2.0, -this.gunImage.getHeight() / 2.0);
		graphics2D.drawImage(this.gunImage, gunTransform, null);
	}


	// ============================= ゲッター・セッター =============================
	public double getX() {
		return translate.x;
	}

	public double getY() {
		return translate.y;
	}

	public double getChassisAngle() {
		return this.chassisAngle;
	}

	public double getGunAngle() {
		return this.gunAngle;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getRadius() {
		double chassis_a = this.chassisImage.getHeight() / 2.0;
		double chassis_b = this.chassisImage.getWidth() / 2.0;
		double chassisRadiusSqr = Math.pow(chassis_a, 2) + Math.pow(chassis_b, 2);
		return Math.sqrt(chassisRadiusSqr);
	}

	@Override
	public Point2D.Double getTranslate() {
		return this.translate;
	}

	public Team getTeam() {
		return this.team;
	}

	// ============================= ゲッター・セッター(テスト用) =============================
	public void setChassisAngle(double theta) {
		this.chassisAngle = theta;
	}

	public void setGunAngle(double theta) {
		this.gunAngle = theta;
	}
}
