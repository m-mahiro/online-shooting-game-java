import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Tank {

	// 戦車の特徴
	private double speed = 3.0;
	private BufferedImage baseImage, gunImage;

	private double x, y;

	private double bodyAngle;
	private double gunAngle;


	Tank(BufferedImage baseImage, BufferedImage gunImage) {
		this.baseImage = baseImage;
		this.gunImage = gunImage;
	}



	/**
	 * 座標x, yの方に砲塔を向ける。
	 * @param x x座標
	 * @param y y座標
	 */
	public void aimAt(int x, int y) {
		double dx = x - this.x;
		double dy = y - this.y;
		this.gunAngle = Math.atan2(dy, dx);
	}

	/**
	 * 戦車を与えられベクトルを正規化して、その方向に移動させる。
	 * @param x
	 * @param y
	 */
	public void move(double x, double y) {
		double norm = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		double dx = x / norm * this.speed;
		double dy = y / norm * this.speed;
		this.x += dx;
		this.y += dy;
	}

	public void update() {

	}

	/**
	 * 現在の砲塔の方向に指定された弾丸を発射する。
	 * @param bulletType 将来的に必要
	 */
	public void fire(int bulletType) {

	}


	// ============================= ゲッター・セッター =============================
	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public double getBodyAngle() {
		return this.bodyAngle;
	}

	public double getGunAngle() {
		return this.gunAngle;
	}


}
