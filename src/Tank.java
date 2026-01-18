import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Tank {

	// 戦車の特徴
	private double velocity = 3.0;
	private Team team;

	private double x, y; // オブジェクトの中心の座標
	private double chassisAngle; // ラジアン
	private double gunAngle; // ラジアン

	private int hp;

	private BufferedImage chassisImage, gunImage;
	private static BufferedImage redChassisImage, redGunImage;
	private static BufferedImage blueChassisImage, blueGunImage;

	static {
		try {
			redChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red.png")));
			redGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red.png")));
			blueChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue.png")));
			blueGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Tank(int x, int y, Team team) {
		switch (team) {
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
			default: assert false;
		}
		this.x = x;
		this.y = y;
	}

	/**
	 * 座標x, yの方に砲塔を向ける。
	 * @param x x座標
	 * @param y y座標
	 */
	public void aimAt(int x, int y) {
		double dx = x - this.x;
		double dy = y - this.y;
		this.gunAngle = Math.atan2(dy, dx) + Math.PI / 2.0;
		System.out.println("dx: " + dx + ", dy: " + dy + ", gunAngle: " + gunAngle);
	}

	/**
	 * 戦車を与えられベクトルを正規化して、その方向に移動させる。
	 * @param x
	 * @param y
	 */
	public void move(double x, double y) {
		double norm = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		if (norm == 0) return;
		double dx = x / norm * this.velocity;
		double dy = y / norm * this.velocity;
		this.x += dx;
		this.y += dy;
	}

	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * 現在の砲塔の方向に指定された弾丸を発射する。
	 * @param bulletType 将来的に必要
	 */
	public void fire(int bulletType) {

	}

	/**
	 * フレームを更新する際に呼び出されます。
	 */
	public void update() {
	}

	public void onHit() {

	}

	public void onCollision() {

	}

	public void damaged(int damage) {
		int newHp = this.hp - damage;
		if (newHp <= 0) {
			died();
		}
	}

	public void died() {

	}

	public void draw(Graphics2D graphics2D) {
		// 台車の描画
		AffineTransform chassisTransform = new AffineTransform();
		chassisTransform.translate(this.x, this.y);
		chassisTransform.rotate(this.chassisAngle);
		chassisTransform.translate(-this.chassisImage.getWidth() / 2.0, -this.chassisImage.getHeight() / 2.0);
		graphics2D.drawImage(this.chassisImage, chassisTransform, null);

		// 砲塔のの描画
		AffineTransform gunTransform = new AffineTransform();
		gunTransform.translate(this.x, this.y);
		gunTransform.rotate(this.gunAngle);
		gunTransform.translate(-this.gunImage.getWidth() / 2.0, -this.gunImage.getHeight() / 2.0);
		graphics2D.drawImage(this.gunImage, gunTransform, null);
	}


	// ============================= ゲッター・セッター =============================
	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
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

	// ============================= ゲッター・セッター(テスト用) =============================
	public void setChassisAngle(double theta) {
		this.chassisAngle = theta;
	}

	public void setGunAngle(double theta) {
		this.gunAngle = theta;
	}
}
