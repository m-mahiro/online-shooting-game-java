import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Tank implements GameObject {

	// 特徴
	public static final double velocity = 20;
	private Team team;
	private double MAX_HP = 100.0;

	// 状態
	public Point2D.Double translate = new Point2D.Double(0, 0); // オブジェクトの中心の座標
	private double gunAngle; // ラジアン
	private double hp = MAX_HP;
	private boolean alive = true;

	// 演出用
	private final int DAMAGE_FLUSH_FRAME = 30;
	private int damageFlushCounter = 0;
	private double tankScale = 1.0;

	// 画像リソース
	private static BufferedImage redNormalChassisImage, redBrokenChassisImage, redBrokenGunImage, redNormalGunImage, redTransparentChassisImage, redTransparentGunImage;
	private static BufferedImage blueChassisImage, blueBrokenChassisImage, blueBrokenGunImage, blueGunImage, blueTransparentChassisImage, blueTransparentGunImage;
	static {
		try {
			// red chassis
			redNormalChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red_normal.png")));
			redBrokenChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red_broken.png")));
			redTransparentChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red_trans.png")));

			// blue chassis
			blueChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue_normal.png")));
			blueBrokenChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue_broken.png")));
			blueTransparentChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue_trans.png")));

			// red gun
			redNormalGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red_normal.png")));
			redBrokenGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red_broken.png")));
			redTransparentGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red_trans.png")));

			// blue gun
			blueGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue_normal.png")));
			blueBrokenGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue_broken.png")));
			blueTransparentGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue_trans.png")));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private enum TankImageType {
		NORMAL, BROKEN, TRANSPARENT, NONE
	}

	Tank(double x, double y, Team team) {
		this.translate.setLocation(x, y);
		this.team = team;
	}

	// ============================= Tankクラス独自のメソッド =============================

	public void aimAt(Point2D.Double coordinate) {
		this.gunAngle = Math.atan2(coordinate.y - this.translate.y, coordinate.x - this.translate.x);
	}

	public void move(Point2D.Double vector) {
		Point2D.Double p = new Point2D.Double(vector.x, vector.y);
		p = Util.normalize(p);
		p = Util.multiple(p, this.velocity);
		p = Util.addition(this.translate, p);
		this.translate.setLocation(p);
	}

	public Bullet shotBullet() {
		if (!alive) return null; // smell
		double initY = translate.y + (this.getGunLength() + Bullet.OBJECT_RADIUS) * 1.2 * Math.sin(this.gunAngle);
		double initX = translate.x + (this.getGunLength() + Bullet.OBJECT_RADIUS) * 1.2 * Math.cos(this.gunAngle);
		return new Bullet(initX, initY, this.gunAngle, this);
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
	}

	private BufferedImage getChassisImage() {

		boolean isRed = (team == Team.RED);
		switch (getImageType()) {
			case NORMAL: return isRed ? redNormalChassisImage : blueChassisImage;
			case BROKEN: return isRed ? redBrokenChassisImage : blueBrokenChassisImage;
			case TRANSPARENT: return isRed ? redTransparentChassisImage : blueTransparentChassisImage;
			case NONE: return null;
			default: throw new RuntimeException("未実装のTankImageType");
		}
	}

	private BufferedImage getGunImage() {
		boolean isRed = (team == Team.RED);
		switch (getImageType()) {
			case NORMAL: return isRed ? redNormalGunImage : blueGunImage;
			case BROKEN: return isRed ? redBrokenGunImage : blueBrokenGunImage;
			case TRANSPARENT: return isRed ? redTransparentGunImage : blueTransparentGunImage;
			case NONE: return null;
			default: throw new RuntimeException("未実装のTankImageType");
		}
	}

	private TankImageType getImageType() {

		// 被弾演出
		if (damageFlushCounter > 0 && damageFlushCounter % 10 == 0) return TankImageType.NONE;

		// 死亡演出
		if (hp <= 0) return TankImageType.TRANSPARENT;

		return TankImageType.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	@Override
	public void update() {
		damageFlushCounter--;
		if (damageFlushCounter < 0) damageFlushCounter = 0;
	}


	@Override
	public void draw(Graphics2D graphics) {
		// 台車の描画
		AffineTransform chassisTransform = new AffineTransform();
		chassisTransform.translate(translate.x, translate.y);
		chassisTransform.translate(-getChassisImage().getWidth(null) / 2.0, -this.chassisImage.getHeight(null) / 2.0);
		graphics.drawImage(this.chassisImage, chassisTransform, null);

		// 砲塔のの描画
		AffineTransform gunTransform = new AffineTransform();
		gunTransform.translate(translate.x, translate.y);
		gunTransform.rotate(this.gunAngle);
		gunTransform.translate(-this.gunImage.getWidth(null) / 2.0, -this.gunImage.getHeight(null) / 2.0);
		graphics.drawImage(this.gunImage, gunTransform, null);
	}

	@Override
	public void onCollision(GameObject other) {
		// ============================= オブジェクトがのめりこまないように、適切な方向に逃げる =============================

		// 相対的な位置関係を取得 (相手 - 自分)
		Point2D.Double vector = Util.subtract(other.getTranslate(), this.getTranslate());

		// 全く同じ位置だった場合、少しだけずらす
		if (vector.x == 0 && vector.y == 0) {
			Random random = new Random();
			vector.x = random.nextDouble() - 0.5; // 0.0~1.0だと正の方向に偏るので -0.5
			vector.y = random.nextDouble() - 0.5;
		}

		// 相手のサイズを取得（これが抜けていました）
		double otherWidth, otherHeight;
		if (other.getShape() instanceof Rectangle) {
			Rectangle rect = (Rectangle) other.getShape();
			otherWidth = rect.width;
			otherHeight = rect.height;
		} else {
			assert other.getShape() instanceof Circle;
			Circle circle = (Circle) other.getShape();
			otherWidth = otherHeight = circle.radius * 2; // 半径x2 = 直径(幅)
		}

		// 衝突判定に使う「合体した矩形」のサイズ
		double totalWidth = this.getWidth() + otherWidth;
		double totalHeight = this.getHeight() + otherHeight;

		// ベクトルの絶対値
		double absX = Math.abs(vector.x);
		double absY = Math.abs(vector.y);

		// 逃げる方向（自分から相手への逆 = 自分自身の移動方向）
		vector = Util.multiple(vector, -1);

		// 対角線判定: (absY / absX) < (totalHeight / totalWidth)
		// 式変形して割り算をなくすと: absY * totalWidth < absX * totalHeight
		// これが成り立つなら「横長」の領域にいるため、横（左右）からの衝突
		if (absY * totalWidth < absX * totalHeight) {
			// 横方向 (左右) からの衝突 -> Y成分を0にして、純粋にX方向に逃げる
			vector.y = 0;
		} else {
			// 縦方向 (上下) からの衝突 -> X成分を0にして、純粋にY方向に逃げる
			vector.x = 0;
		}

		// 移動実行
		move(vector);
	}

	@Override
	public void onHitBy(DangerGameObject bullet) {
		if (!this.alive) return;
		this.damage(bullet.getDamageAbility());
	}

	@Override
	public boolean shouldRemove() {
		return false;
	}

	@Override
	public boolean isTangible() {
		return this.alive;
	}

	@Override
	public Shape getShape() {
		return new Rectangle(this.translate, getWidth(), getHeight());
	}

	@Override
	public Point2D.Double getTranslate() {
		return (Point2D.Double) this.translate.clone();
	}

	@Override
	public void setTranslate(double x, double y) {
		this.translate.setLocation(x, y);
	}

	// ============================= ゲッター・セッター =============================
	public double getX() {
		return translate.x;
	}

	public double getY() {
		return translate.y;
	}

	public double getGunAngle() {
		return this.gunAngle;
	}

	public void setVelocity(double velocity) {
		velocity = velocity;
	}

	public Team getTeam() {
		return this.team;
	}

	public double getHeight() {
		return chassisImage.getHeight() * tankScale;
	}

	public double getWidth() {
		return chassisImage.getWidth() + tankScale;
	}

	public double getGunLength() {
		return gunImage.getWidth() / 2.0;
	}
}
