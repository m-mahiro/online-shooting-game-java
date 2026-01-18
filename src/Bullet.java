import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

public class Bullet implements GameObject {

	// --- 定数 ---
	private final double VELOCITY = 8.0;
	private final int LIFE_TIME = 400; // 生存フレーム数（約6.6秒）

	// --- 状態 ---
	private double x, y; // 弾丸オブジェクトの中心座標
	private double dx, dy; // 1フレームあたりの移動量
	private int lifeCount; // 経過フレーム数
	private boolean alive; // 生存フラグ

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
	 * @param x 初期X座標
	 * @param y 初期Y座標
	 * @param angle 発射角度（ラジアン）
	 */
	public Bullet(double x, double y, double angle, Team team) {
		this.x = x;
		this.y = y;
		this.alive = true;
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

	public void update() {
		if (!alive) return;

		// 移動
		x += dx;
		y += dy;

		// 寿命チェック
		lifeCount++;
		if (lifeCount > LIFE_TIME) {
			alive = false;
		}

		// 画面外チェック（GamePanelのサイズ定数を使うか、引数で貰う）
		if (x < 0 || x > GamePanel.WIDTH || y < 0 || y > GamePanel.HEIGHT) {
			alive = false;
		}
	}

	public void draw(Graphics2D g2d) {
		if (!alive) return;

		AffineTransform trans = new AffineTransform();
		trans.translate(x, y);
		trans.rotate(Math.atan2(dy, dx));
		trans.translate(-this.bulletImage.getWidth() / 2.0, -this.bulletImage.getHeight() / 2.0);
		g2d.drawImage(this.bulletImage, trans, null);
	}

	// --- ゲッター ---
	public boolean isAlive() { return alive; }
	public double getX() { return x; }
	public double getY() { return y; }

	// 衝突時に弾を消すためのメソッド
	public void destroy() {
		this.alive = false;
	}
}