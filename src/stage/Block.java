package stage;

import client.GameEngine;
import client.SoundManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

public class Block implements GameObject {

	// 特徴
	private static final int INITIAL_HP = 50;

	// 状態（クライアント間の同期に必要)
	private final Point2D.Double position;
	private int hp = INITIAL_HP;

	// 演出用定数
	private static final int DAMAGE_FLUSH_FRAME = (int)(GameEngine.FPS * 1.5);
	private static final int DEBRIS_LIFE_FRAME = GameEngine.FPS / 4;
	private static final int BABY_BLOCK_LIFE_FRAME = GameEngine.FPS;

	// 演出用変数（クライアント間の同期は必要ない）
	private int debrisLifeFrame = 0;
	private int damageFlushFrame = 0;
	private int babyBlockLifeFrame = 0;
	private double objectScale = 1.0;
	private boolean isBroken = false;

	// 効果音
	private static final SoundManager sound = new SoundManager();

	// 画像リソース
	private static BufferedImage normalBlockImage, brokenBlockImage, blockDebrisImage, transparentBlockImage;
	private static BufferedImage noneImage;

	private enum State {
		BABY, NORMAL, BROKEN, DEBRIS, SHOULD_REMOVE
	}

	static {
		try {
			normalBlockImage = ImageIO.read(Objects.requireNonNull(Block.class.getResource("/client/assets/block_normal.png")));
			brokenBlockImage = ImageIO.read(Objects.requireNonNull(Block.class.getResource("/client/assets/block_broken.png")));
			blockDebrisImage = ImageIO.read(Objects.requireNonNull(Block.class.getResource("/client/assets/block_debris.png")));
			transparentBlockImage = ImageIO.read(Objects.requireNonNull(Block.class.getResource("/client/assets/block_trans.png")));
			noneImage = ImageIO.read(Objects.requireNonNull(Block.class.getResource("/client/assets/none_image.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	    /**
	     * 新しいブロックオブジェクトを生成します。
	     * @param x ブロックの中心X座標
	     * @param y ブロックの中心Y座標
	     * @param isBaby 生成されたばかりの「ベビー」ブロックであるかどうかのフラグ
	     */
	    public Block(double x, double y, boolean isBaby) {		this.position = new Point2D.Double(x, y);
		if (isBaby) babyBlockLifeFrame = BABY_BLOCK_LIFE_FRAME;
	}

	// ============================= Blockクラス独自のメソッド =============================

	    /**
	     * ブロックが破壊されたときに呼び出される処理。
	     * 爆発音を再生し、HPを0にし、残骸ライフフレームを設定します。
	     */
	    public void OnDie() {		sound.objectExplosion();
		this.hp = 0;
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
	}

	    /**
	     * ブロックにダメージを与えます。
	     * HPが0以下になった場合、OnDie()を呼び出して破壊処理を実行します。
	     * @param damage 与えるダメージ量
	     */
	    public void damage(int damage) {		damageFlushFrame = DAMAGE_FLUSH_FRAME;
		hp -= damage;
		if (hp <= 0) this.OnDie();
	}

	    /**
	     * ブロックの現在の状態に応じた画像を取得します。
	     * ダメージを受けた直後は点滅演出のため透明な画像が返されることがあります。
	     * @return ブロックの現在の状態に対応するBufferedImage
	     */
	    private BufferedImage getImage() {		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
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

	    /**
	     * ブロックの現在の状態を取得します。
	     * @return ブロックの現在の状態 (BABY, NORMAL, BROKEN, DEBRIS, SHOULD_REMOVE)
	     */
	    private State getState() {		if (debrisLifeFrame <= 0 && hp <= 0) return State.SHOULD_REMOVE;
		if (babyBlockLifeFrame > 0) return State.BABY;
		if (hp <= 0) return State.DEBRIS;
		if (this.hp < INITIAL_HP / 2.0) return State.BROKEN;
		return State.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	    /**
	     * ブロックの状態を更新します。
	     * HPによる状態変化、ダメージ点滅、残骸の寿命、スケールアニメーションなどを処理します。
	     */
	    @Override
	    public void update() {		if (hp < INITIAL_HP / 2 && !isBroken) {
			sound.objectBreak();
			isBroken = true;
		}
		if (debrisLifeFrame > 0) debrisLifeFrame--;
		if (babyBlockLifeFrame > 0) babyBlockLifeFrame--;

		if (damageFlushFrame > 0) damageFlushFrame--;

		if (getState() == State.DEBRIS) {
			objectScale += (GameEngine.FPS / 60.0) * debrisLifeFrame / 100.0;
		}
	}

	    /**
	     * ブロック自身を描画します。現在の状態と位置、スケールに基づいて画像を描画します。
	     * @param graphics 描画に使用するGraphics2Dオブジェクト
	     */
	    @Override
	    public void draw(Graphics2D graphics) {		BufferedImage image = getImage();
		AffineTransform trans = new AffineTransform();
		trans.translate(this.position.x, this.position.y);
		trans.scale(objectScale, objectScale);
		trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		graphics.drawImage(getImage(), trans, null);
	}

	    /**
	     * 他のGameObjectとの衝突時に呼び出されます。
	     * ブロックに直接衝突することによる特殊な処理はありません。
	     * @param other 衝突したGameObject
	     */
	    @Override
	    public void onCollision(GameObject other) {	}

	    /**
	     * プロジェクタイルがブロックに衝突した際に呼び出されます。
	     * プロジェクタイルの持つダメージ能力に基づいてブロックにダメージを与えます。
	     * @param other ブロックに衝突したプロジェクタイル
	     */
	    @Override
	    public void onHitBy(Projectile other) {		damage(other.getDamageAbility());
	}

	    /**
	     * オブジェクトがステージから削除されるべきかを判定します。
	     * ブロックがSHOULD_REMOVE状態であればtrueを返します。
	     * @return ブロックが削除されるべきであればtrue、そうでなければfalse
	     */
	    @Override
	    public boolean isExpired() {		return getState() == State.SHOULD_REMOVE;
	}

	    /**
	     * オブジェクトが剛体として扱われるべきか、つまり衝突判定の対象となるべきかを判定します。
	     * ブロックはNORMAL状態またはBROKEN状態では剛体です。
	     * @return ブロックが剛体であればtrue、そうでなければfalse
	     */
	    @Override
	    public boolean hasRigidBody() {		State s = getState();
		return s == State.NORMAL || s == State.BROKEN;
	}

	    /**
	     * オブジェクトの描画レイヤーを取得します。
	     * 状態によってDEBRISレイヤーまたはTANGIBLE_OBJECTレイヤーを返します。
	     * @return 描画レイヤー
	     */
	    @Override
	    public RenderLayer getRenderLayer() {		switch (getState()) {
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

	    /**
	     * オブジェクトの衝突判定に使用される形状を取得します。
	     * ブロックは長方形の形状を持ちます。
	     * @return ブロックの形状を表すShapeオブジェクト
	     */
	    @Override
	    public Shape getShape() {		return new Rectangle(this.position, this.getWidth(), this.getHeight());
	}

	    /**
	     * ブロックの中心座標を取得します。
	     * @return ブロックの中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public Point2D.Double getPosition() {		return (Point2D.Double) this.position.clone();
	}

	    /**
	     * ブロックの中心座標を設定します。
	     * @param position 設定する新しい中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public void setPosition(Point2D.Double position) {		this.position.setLocation(position);
	}

	    /**
	     * ブロックの現在のHPを取得します。
	     * @return ブロックの現在のHP
	     */
	    @Override
	    public int getHP() {		return this.hp;
	}

	    /**
	     * ブロックが所属するチームを取得します。
	     * ブロックは常にOBSTACLEチームに所属します。
	     * @return 常にOBSTACLE
	     */
	    @Override
	    public Team getTeam() {		return OBSTACLE;
	}

	// ============================= ゲッターセッター =============================
	    /**
	     * ブロックの幅を取得します。オブジェクトのスケールを考慮します。
	     * @return ブロックの現在の幅
	     */
	    public double getWidth() {		return getImage().getWidth() * objectScale;
	}

	    /**
	     * ブロックの高さを取得します。オブジェクトのスケールを考慮します。
	     * @return ブロックの現在の高さ
	     */
	    public double getHeight() {		return getImage().getHeight() * objectScale;
	}
}
