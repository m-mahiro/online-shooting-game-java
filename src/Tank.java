import com.sun.istack.internal.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class Tank implements GameObject {

	// 特徴（定数）
	public static final double VELOCITY = 20;
	private static final int INITIAL_HP = 50;

	// 状態（クライアント間の同期に必要)
	private final Base base;
	private final Point2D.Double position; // オブジェクトの中心の座標
	private double gunAngle; // ラジアン
	private int hp = INITIAL_HP;
	@Nullable private Missile holdingMissile;

	// 演出用定数
	private final static int DAMAGE_FLUSH_FRAME = (int) (GamePanel.FPS * 1.5);
	private final static int DEBRIS_LIFE_FRAME = (int) (GamePanel.FPS * 0.25);
	private final static int RESPAWN_LAG_FRAME = GamePanel.FPS * 3;
	private final static int RESPAWN_ANIMATE_FRAME = GamePanel.FPS;

	// 演出用変数（クライアント間の同期は必要ない）
	private int damageFlushFrame = 0;
	private int debrisLifeFrame = 0;
	private int respawnLagFrame = 0;
	private int respawnAnimateFrame = 0;
	private boolean hadBroken = false;

	private SoundManager sound = new SoundManager();

	// 画像リソース
	private static BufferedImage redNormalChassisImage, redBrokenChassisImage, redTransparentChassisImage;
	private static BufferedImage blueNormalChassisImage, blueBrokenChassisImage, blueTransparentChassisImage;
	private static BufferedImage redNormalGunImage, redBrokenGunImage, redTransparentGunImage;
	private static BufferedImage blueNormalGunImage, blueBrokenGunImage, blueTransparentGunImage;
	private static BufferedImage redTankDebris;
	private static BufferedImage blueTankDebris;
	private static BufferedImage noneImage;

	private enum Status {
		RESPAWNING, NORMAL, BROKEN, DEBRIS, NONE
	}

	static {
		try {
			// 本当の本当に透明
			noneImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/none_image.png")));

			// debris
			redTankDebris = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/tank_red_debris.png")));
			blueTankDebris = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/tank_blue_debris.png")));

			// red chassis
			redNormalChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red_normal.png")));
			redBrokenChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red_broken.png")));
			redTransparentChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_red_trans.png")));

			// blue chassis
			blueNormalChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue_normal.png")));
			blueBrokenChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue_broken.png")));
			blueTransparentChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/chassis_blue_trans.png")));

			// red gun
			redNormalGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red_normal.png")));
			redBrokenGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red_broken.png")));
			redTransparentGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_red_trans.png")));

			// blue gun
			blueNormalGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue_normal.png")));
			blueBrokenGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue_broken.png")));
			blueTransparentGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/gun_blue_trans.png")));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	Tank(Base base) {
		this.position = base.getPosition();
		this.base = base;
	}

	// ============================= Tankクラス独自のメソッド =============================

	public void aimAt(Point2D.Double coordinate) {
		if (holdingMissile != null) return;
		this.gunAngle = Math.atan2(coordinate.y - this.position.y, coordinate.x - this.position.x);
	}

	public void move(Point2D.Double vector) {
		if (holdingMissile != null) return;
		Point2D.Double p = new Point2D.Double(vector.x, vector.y);
		p = Util.normalize(p);
		p = Util.multiple(p, this.VELOCITY);
		p = Util.addition(this.position, p);
		this.position.setLocation(p);
	}

	public Bullet shootBullet() {
		sound.shootGun();
		return new Bullet(this.getPosition(), this.gunAngle, this);
	}

	public Missile readyMissile() {
		Missile missile = new Missile(this.getPosition(), this.gunAngle, this);
		this.holdingMissile = missile;
		return missile;
	}

	public void launchMissile() {
		if (holdingMissile == null) return;
		this.holdingMissile.launch();
		this.holdingMissile = null;
	}

	public Block createBlock() {
		sound.createBlock();
		return new Block(this.position.x, this.position.y, true);
	}

	public void damage(int damage) {
		this.damageFlushFrame = DAMAGE_FLUSH_FRAME;
		this.hp -= damage;
		if (this.hp <= 0) {
			onDie();
		}
	}

	public void onDie() {
		sound.objectExplosion();
		this.hp = 0;
		this.damageFlushFrame = 0;
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
		this.respawnLagFrame = RESPAWN_LAG_FRAME;
	}

	public void respawn() {
		if (base.isDebris()) return;
		this.hp = INITIAL_HP;
		this.respawnAnimateFrame = RESPAWN_ANIMATE_FRAME;
		Point2D.Double spawnPoint = base.getPosition();
		this.setPosition(spawnPoint.x, spawnPoint.y);
	}

	public boolean isDead() {
		return this.hp <= 0;
	}

	private double getObjectScale() {
		switch (getStatus()) {
			case RESPAWNING:
				return 1.0 - respawnAnimateFrame / (double) RESPAWN_ANIMATE_FRAME;
			case NORMAL:
			case BROKEN:
			case NONE:
				return 1.0;
			case DEBRIS:
				return (GamePanel.FPS / 240.0) * Math.pow(debrisLifeFrame, 2) / 100.0;
			default:
				throw new RuntimeException();
		}
	}

	private BufferedImage getChassisImage() {

		boolean isRed = (base.getTeam() == Team.RED);
		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
		switch (getStatus()) {
			case RESPAWNING: return isRed ? redTransparentChassisImage : blueTransparentChassisImage;
//			case RESPAWNING: return isRed ? redNormalChassisImage : blueNormalChassisImage;
			case NORMAL:
				if (isFlushing) return noneImage;
				return isRed ? redNormalChassisImage : blueNormalChassisImage;
			case BROKEN:
				if (isFlushing) return noneImage;
				return isRed ? redBrokenChassisImage : blueBrokenChassisImage;
			case DEBRIS:
				return isRed ? redTankDebris : blueTankDebris;
			case NONE:
				return noneImage;
			default:
				throw new RuntimeException("未実装のTankImageStatus");
		}
	}

	private BufferedImage getGunImage() {
		boolean isRed = (base.getTeam() == Team.RED);
		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
		switch (getStatus()) {
			case RESPAWNING: return isRed ? redTransparentGunImage : blueTransparentGunImage;
//			case RESPAWNING: return isRed ? redNormalGunImage : blueNormalGunImage;
			case NORMAL:
				if (isFlushing) return noneImage;
				return isRed ? redNormalGunImage : blueNormalGunImage;
			case BROKEN:
				if (isFlushing) return noneImage;
				return isRed ? redBrokenGunImage : blueBrokenGunImage;
			case DEBRIS:
			case NONE:
				return noneImage;
			default:
				throw new RuntimeException("未実装のTankImageStatus");
		}
	}

	private Status getStatus() {

		if (respawnAnimateFrame > 0) return Status.RESPAWNING;

		// 残骸は一定時間経過後画面から消える
		if (debrisLifeFrame <= 0 && isDead()) return Status.NONE;

		// 破壊されたときに、画面に残骸が表示される
		if (isDead()) return Status.DEBRIS;

		// ダメージが蓄積されると戦車にひびが入る演出
		if (hp < INITIAL_HP / 2.0) return Status.BROKEN;

		return Status.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	@Override
	public void update() {


		if (getStatus() == Status.BROKEN && !hadBroken) {
			sound.objectBreak();
			hadBroken = true;
		}
		if (respawnAnimateFrame > 0) respawnAnimateFrame--;
		if (damageFlushFrame > 0) damageFlushFrame--;
		if (debrisLifeFrame > 0) debrisLifeFrame--;
		if (respawnLagFrame > 0) {
			respawnLagFrame--;
		} else {
			if (hp == 0) respawn();
		}
	}


	@Override
	public void draw(Graphics2D graphics) {
		BufferedImage chassisImage = this.getChassisImage();
		BufferedImage gunImage = this.getGunImage();
		double objectScale = getObjectScale();


		// 台車の描画
		AffineTransform chassisTransform = new AffineTransform();
		chassisTransform.translate(position.x, position.y);
		chassisTransform.scale(objectScale, objectScale);
		chassisTransform.translate(-chassisImage.getWidth() / 2.0, -chassisImage.getHeight() / 2.0);
		graphics.drawImage(chassisImage, chassisTransform, null);

		// 砲塔のの描画
		AffineTransform gunTransform = new AffineTransform();
		gunTransform.translate(position.x, position.y);
		gunTransform.rotate(this.gunAngle);
		gunTransform.scale(objectScale, objectScale);
		gunTransform.translate(-gunImage.getWidth() / 2.0, -gunImage.getHeight() / 2.0);
		graphics.drawImage(gunImage, gunTransform, null);
	}

	@Override
	public void onCollision(GameObject other) {

		if (other instanceof Base) return;

		// ============================= オブジェクトがのめりこまないように、適切な方向に逃げる =============================

		// 相対的な位置関係を取得 (相手 - 自分)
		Point2D.Double vector = Util.subtract(other.getPosition(), this.getPosition());

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
		if (isDead()) return;
		this.damage(bullet.getDamageAbility());
	}

	@Override
	public boolean shouldRemove() {
		return false;
	}

	@Override
	public boolean isTangible() {
		switch (getStatus()) {
			case RESPAWNING:
			case NORMAL:
			case BROKEN:
				return true;
			case DEBRIS:
			case NONE:
				return false;
			default:
				throw new RuntimeException();
		}
	}

	@Override
	public RenderLayer getRenderLayer() {
		switch (getStatus()) {
			case RESPAWNING:
			case NORMAL:
			case BROKEN:
				return RenderLayer.TANGIBLE_OBJECT;
			case DEBRIS:
			case NONE:
				return RenderLayer.DEBRIS;
			default:
				throw new RuntimeException();
		}
	}

	@Override
	public Shape getShape() {
		return new Rectangle(this.position, getWidth(), getHeight());
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
		return this.hp;
	}

	// ============================= ゲッター・セッター =============================
	public double getX() {
		return position.x;
	}

	public double getY() {
		return position.y;
	}

	public double getGunAngle() {
		return this.gunAngle;
	}

	public Team getTeam() {
		return this.base.getTeam();
	}

	public double getHeight() {
		return getChassisImage().getHeight() * getObjectScale();
	}

	public double getWidth() {
		return getChassisImage().getWidth() * getObjectScale();
	}

	public double getBulletReleaseRadius() {
		double gunLength = getGunImage().getWidth() / 2.0;
		double chassisRadius = Math.max(getWidth(), getHeight()) / 2.0;
		return Math.max(gunLength, chassisRadius);
	}
}
