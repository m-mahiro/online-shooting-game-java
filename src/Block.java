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
	private final Point2D.Double translate;
	private int hp = INITIAL_HP;

	// 演出用（クライアント間の同期は必要ない）
	private final int DEBRIS_LIFE_FRAME = GamePanel.FPS / 4;
	private final int BABY_BLOCK_LIFE_FRAME = GamePanel.FPS;
	private int debrisLifeFrame = 0;
	private int babyBlockLifeFrame = 0;
	private double blockScale = 1.0;
	private boolean isBroken = false;

	private SoundManager sound = new SoundManager();

	// 画像リソース
	private static BufferedImage normalBlockImage, brokenBlockImage, blockDebrisImage, transparentBlockImage;

	private enum Status {
		BABY, NORMAL, BROKEN, DEBRIS, SHOULD_REMOVE
	}

	static {
		try {
			normalBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_normal.png")));
			brokenBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_broken.png")));
			blockDebrisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_debris.png")));
			transparentBlockImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("assets/block_trans.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Block(double x, double y, boolean isBaby) {
		this.translate = new Point2D.Double(x, y);
		if (isBaby) babyBlockLifeFrame = BABY_BLOCK_LIFE_FRAME;
	}

	// ============================= Blockクラス独自のメソッド =============================

	public void OnDie() {
		sound.objectExplosion();
		this.hp = 0;
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
	}

	private BufferedImage getBlockImage() {
		switch (getStatus()) {
			case NORMAL:
				return normalBlockImage;
			case BROKEN:
				return brokenBlockImage;
			case BABY:
				return transparentBlockImage;
			case DEBRIS:
			case SHOULD_REMOVE:
				return blockDebrisImage;
			default:
				throw new RuntimeException("未実装のImageStatusです。");
		}
	}

	private Status getStatus() {
		if (debrisLifeFrame <= 0 && hp <= 0) return Status.SHOULD_REMOVE;
		if (babyBlockLifeFrame > 0) return Status.BABY;
		if (hp <= 0) return Status.DEBRIS;
		if (this.hp < INITIAL_HP / 2.0) return Status.BROKEN;
		return Status.NORMAL;
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
		if (getStatus() == Status.DEBRIS) {
			blockScale += (GamePanel.FPS / 60.0) * debrisLifeFrame / 100.0;
		}
	}

	@Override
	public void draw(Graphics2D graphics) {
		BufferedImage image = getBlockImage();
		AffineTransform trans = new AffineTransform();
		trans.translate(this.translate.x, this.translate.y);
		trans.scale(blockScale, blockScale);
		trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		graphics.drawImage(getBlockImage(), trans, null);
	}

	@Override
	public void onCollision(GameObject other) {
	}

	@Override
	public void onHitBy(DangerGameObject other) {
		hp -= other.getDamageAbility();
		if (hp <= 0) this.OnDie();
	}

	@Override
	public boolean shouldRemove() {
		return getStatus() == Status.SHOULD_REMOVE;
	}

	@Override
	public boolean isTangible() {
		Status s = getStatus();
		return s == Status.NORMAL || s == Status.BROKEN;
	}

	@Override
	public RenderLayer getRenderLayer() {
		switch (getStatus()) {
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
		return new Rectangle(this.translate, this.getWidth(), this.getHeight());
	}

	@Override
	public Point2D.Double getTranslate() {
		return (Point2D.Double) this.translate.clone();
	}

	@Override
	public void setTranslate(double x, double y) {
		this.translate.setLocation(x, y);
	}

	// ============================= ゲッターセッター =============================
	public double getWidth() {
		return getBlockImage().getWidth() * blockScale;
	}

	public double getHeight() {
		return getBlockImage().getHeight() * blockScale;
	}
}
