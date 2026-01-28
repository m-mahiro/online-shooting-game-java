import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Base implements GameObject {

	// 定数
	private static final int INIT_HP = 1000;

	// 状態
	private int hp = INIT_HP;
	private final Point2D.Double position;
	private Team team;

	// 定数(演出用)
	private static final int EXPLOSION_FRAME = GamePanel.FPS / 2;
	private static final int DAMAGE_FLUSH_FRAME = (int) (GamePanel.FPS * 1.5);
	private static final int DEBRIS_LIFE_FRAME = GamePanel.FPS / 4;

	// 状態(演出用)
	private int explosionFrame = 0;
	private double debrisScale = 1.0;
	private double ringRotation = 0;
	private int damageFlushFrame = 0;
	private int debrisLifeFrame = 0;
	private boolean isBroken = false;
	private SoundManager sound = new SoundManager();

	private static BufferedImage normalRedBaseImage, brokenRedBaseImage, redBaseRuinsImage;
	private static BufferedImage normalBlueBaseImage, brokenBlueBaseImage, blueBaseRuinsImage;
	private static BufferedImage normalRingImage;
	private static BufferedImage redBaseDebrisImage, blueBaseDebrisImage;
	private static BufferedImage noneImage;

	private enum State {
		NORMAL, BROKEN, DEBRIS
	}

	static {
		try {
			// red base
			normalRedBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/base_red_normal.png")));
			brokenRedBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/base_red_broken.png")));
			redBaseRuinsImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/base_red_debris.png")));

			// blue base
			normalBlueBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/base_blue_normal.png")));
			brokenBlueBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/base_blue_broken.png")));
			blueBaseRuinsImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/base_blue_debris.png")));

			// ring
			normalRingImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/ring_normal.png")));

			// debris
			redBaseDebrisImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/tank_red_debris.png")));
			blueBaseDebrisImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("assets/tank_blue_debris.png")));

			// 透明
			noneImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/none_image.png")));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Base(double x, double y, Team team) {
		this.position = new Point2D.Double(x, y);
		this.team = team;
	}

	// ============================= Baseクラス固有のメソッド =============================

	private void damage(int damage) {
		this.damageFlushFrame = DAMAGE_FLUSH_FRAME;
		hp -= damage;
		if (hp <= 0) onDie();
	}

	private void onDie() {
		// 爆発する
		debrisLifeFrame = DEBRIS_LIFE_FRAME;
		sound.objectExplosion();
	}

	public Team getTeam() {
		return this.team;
	}

	public boolean isDebris() {
		return getState() == State.DEBRIS;
	}

	private BufferedImage getBaseImage() {
		boolean isRed = this.team == Team.RED;
		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
		switch (getState()) {
			case NORMAL:
				if (isFlushing) return noneImage;
				return isRed ? normalRedBaseImage : normalBlueBaseImage;
			case BROKEN:
				if (isFlushing) return noneImage;
				return isRed ? brokenRedBaseImage : brokenBlueBaseImage;
			case DEBRIS:
				return isRed ? redBaseRuinsImage : blueBaseRuinsImage;
		}
		return normalRedBaseImage;
	}

	private BufferedImage getRingImage() {
		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
		switch (getState()) {
			case NORMAL:
			case BROKEN:
				if (isFlushing) return noneImage;
				return normalRingImage;
			case DEBRIS:
				return noneImage;
			default:
				throw new RuntimeException();
		}

	}

	private State getState() {
		if (hp <= 0) return State.DEBRIS;
		if (hp <= INIT_HP / 2) return State.BROKEN;
		return State.NORMAL;
	}


	// ============================= GameObjectインタフェースのメソッド =============================

	@Override
	public void update() {

		// リングの回転
		switch (getState()) {
			case NORMAL:
				ringRotation += 0.1;
				break;
			case BROKEN:
				ringRotation += 0.05;
				break;
		}

		// ひびが入った時のサウンド
		if (hp < INIT_HP / 2 && !isBroken) {
			sound.objectBreak();
			isBroken = true;
		}

		// ダメージ受けた直後の点滅用にフレームをカウント
		if (damageFlushFrame > 0) damageFlushFrame--;

		// 破壊されああと残骸を数フレーム画面に残すためのカウント
		if (debrisLifeFrame > 0) debrisLifeFrame--;

		// 破壊されたときの残骸が飛び散る演出用に、オブジェクトのスケールを二次関数的に増加させる。
		if (getState() == State.DEBRIS) debrisScale += (GamePanel.FPS / 10.0) * debrisLifeFrame / 100.0;
	}

	@Override
	public void draw(Graphics2D graphics) {
		AffineTransform baseTrans = new AffineTransform();
		BufferedImage baseImage = getBaseImage();
		baseTrans.translate(position.x, position.y);
		baseTrans.translate(-baseImage.getWidth() / 2.0, -baseImage.getHeight() / 2.0);
		graphics.drawImage(baseImage, baseTrans, null);

		AffineTransform ringTrans = new AffineTransform();
		BufferedImage ringImage = getRingImage();
		ringTrans.translate(position.x, position.y);
		ringTrans.rotate(ringRotation);
		ringTrans.translate(-debrisScale * ringImage.getWidth() / 2.0, -debrisScale * ringImage.getHeight() / 2.0);
		ringTrans.scale(debrisScale, debrisScale);
		graphics.drawImage(ringImage, ringTrans, null);
	}

	@Override
	public void onCollision(GameObject other) {
		// 何もしない
	}

	@Override
	public void onHitBy(DangerGameObject other) {
		damage(other.getDamageAbility());
	}

	@Override
	public boolean shouldRemove() {
		return false;
	}

	@Override
	public boolean isTangible() {
		switch (getState()) {
			case NORMAL:
			case BROKEN:
				return true;
			case DEBRIS:
				return false;
			default:
				throw new RuntimeException();
		}
	}

	@Override
	public RenderLayer getRenderLayer() {
		return RenderLayer.DEBRIS;
	}

	@Override
	public Shape getShape() {
		double width = getBaseImage().getWidth() * debrisScale;
		return new Circle(this.position, 0.8 * width / 2.0);
	}

	@Override
	public Point2D.Double getPosition() {
		return (Point2D.Double) this.position.clone();
	}

	@Override
	public void setPosition(double x, double y) {
		this.position.setLocation(x, y);
	}
}
