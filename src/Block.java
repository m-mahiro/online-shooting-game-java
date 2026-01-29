import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Block implements GameObject {

	// 特徴
	private static final int INITIAL_HP = 50;

	// 状態（クライアント間の同期に必要)
	private final Point2D.Double position;
	private int hp = INITIAL_HP;

	// 演出用定数
	private static final int DAMAGE_FLUSH_FRAME = (int)(GamePanel.FPS * 1.5);
	private static final int DEBRIS_LIFE_FRAME = GamePanel.FPS / 4;
	private static final int BABY_BLOCK_LIFE_FRAME = GamePanel.FPS;

	// 演出用変数（クライアント間の同期は必要ない）
	private int debrisLifeFrame = 0;
	private int damageFlushFrame = 0;
	private int babyBlockLifeFrame = 0;
	private double objectScale = 1.0;
	private boolean isBroken = false;

	private SoundManager sound = new SoundManager();

	// 画像リソース
	private static BufferedImage normalBlockImage, brokenBlockImage, blockDebrisImage, transparentBlockImage;
	private static BufferedImage noneImage;

	private enum State {
		BABY, NORMAL, BROKEN, DEBRIS, SHOULD_REMOVE
	}

	static {
		try {
			normalBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_normal.png")));
			brokenBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_broken.png")));
			blockDebrisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_debris.png")));
			transparentBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_trans.png")));
			noneImage = ImageIO.read(Objects.requireNonNull(Block.class.getResource("assets/none_image.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Block(double x, double y, boolean isBaby) {
		this.position = new Point2D.Double(x, y);
		if (isBaby) babyBlockLifeFrame = BABY_BLOCK_LIFE_FRAME;
	}

	// ============================= Blockクラス独自のメソッド =============================

	public void OnDie() {
		sound.objectExplosion();
		this.hp = 0;
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
	}

	public void damage(int damage) {
		damageFlushFrame = DAMAGE_FLUSH_FRAME;
		hp -= damage;
		if (hp <= 0) this.OnDie();
	}

	private BufferedImage getImage() {
		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
		switch (getState()) {
			case NORMAL:
				if (isFlushing) return noneImage;
				return normalBlockImage;
			case BROKEN:
				if (isFlushing) return noneImage;
				return brokenBlockImage;
			case BABY:
				return transparentBlockImage;
			case DEBRIS:
			case SHOULD_REMOVE:
				return blockDebrisImage;
			default:
				throw new IllegalStateException("Unexpected value: " + getState());
		}
	}

	private State getState() {
		if (debrisLifeFrame <= 0 && hp <= 0) return State.SHOULD_REMOVE;
		if (babyBlockLifeFrame > 0) return State.BABY;
		if (hp <= 0) return State.DEBRIS;
		if (this.hp < INITIAL_HP / 2.0) return State.BROKEN;
		return State.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	@Override
	public void update() {
		if (hp < INITIAL_HP / 2 && !isBroken) {
			sound.objectBreak();
			isBroken = true;
		}
		if (debrisLifeFrame > 0) debrisLifeFrame--;
		if (babyBlockLifeFrame > 0) babyBlockLifeFrame--;

		if (damageFlushFrame > 0) damageFlushFrame--;

		if (getState() == State.DEBRIS) {
			objectScale += (GamePanel.FPS / 60.0) * debrisLifeFrame / 100.0;
		}
	}

	@Override
	public void draw(Graphics2D graphics) {
		BufferedImage image = getImage();
		AffineTransform trans = new AffineTransform();
		trans.translate(this.position.x, this.position.y);
		trans.scale(objectScale, objectScale);
		trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		graphics.drawImage(getImage(), trans, null);
	}

	@Override
	public void onCollision(GameObject other) {
	}

	@Override
	public void onHitBy(DangerGameObject other) {
		damage(other.getDamageAbility());
	}

	@Override
	public boolean shouldRemove() {
		return getState() == State.SHOULD_REMOVE;
	}

	@Override
	public boolean isTangible() {
		State s = getState();
		return s == State.NORMAL || s == State.BROKEN;
	}

	@Override
	public RenderLayer getRenderLayer() {
		switch (getState()) {
			case BABY:
			case DEBRIS:
			case SHOULD_REMOVE:
				return RenderLayer.DEBRIS;
			case NORMAL:
			case BROKEN:
				return RenderLayer.TANGIBLE_OBJECT;
			default: throw new RuntimeException();
		}
	}

	@Override
	public Shape getShape() {
		return new Rectangle(this.position, this.getWidth(), this.getHeight());
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
		return this.hp;
	}

	// ============================= ゲッターセッター =============================
	public double getWidth() {
		return getImage().getWidth() * objectScale;
	}

	public double getHeight() {
		return getImage().getHeight() * objectScale;
	}
}
