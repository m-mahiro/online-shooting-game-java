package stage;

import client.GameEngine;
import client.SoundManager;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import javax.imageio.ImageIO;

import static stage.Team.*;

public class Bullet implements GameObject, Projectile {

	// 特徴
	private static final int COLLISION_RADIUS = 5;
	private static final double VELOCITY = 30;
	private static final int LIFE_TIME = GameEngine.FPS * 6; // 生存フレーム数
	private static final int DAMAGE_ABILITY = 10;

	// 状態（クライアント間の同期に必要)
	private Tank shooter;
	private final Point2D.Double position; // 弾丸オブジェクトの中心座標
	private final double dx, dy;
	private int lifeFrame = LIFE_TIME;

	// 演出用定数
	private final int DEBRIS_LIFE_FRAME = GameEngine.FPS / 4;

	// 演出用変数（クライアント間の同期は必要ない）
	private double renderScale = 1.0;
	private int debrisLifeFrame = 0;

	// 効果音
	private static final SoundManager sound = new SoundManager();

	// 画像リソース（共有）
	private static BufferedImage blueNormalBulletImage, redNormalBulletImage, blueBulletDebris, redBulletDebris, noneImage;

	private enum State {
		NORMAL, DEBRIS, SHOULD_REMOVE
	}

	static {
		try {
			noneImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/none_image.png")));
			blueNormalBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_blue.png")));
			redNormalBulletImage = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_red_normal.png")));
			blueBulletDebris = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_blue_debris.png")));
			redBulletDebris = ImageIO.read(Objects.requireNonNull(Bullet.class.getResource("/client/assets/bullet_red_debris.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	    /**
	     * 新しい弾丸オブジェクトを生成します。
	     * 弾丸はシューターの現在位置と向きに基づいて初期化されます。
	     * @param shooter 弾丸を発射した戦車
	     */
	    public Bullet(Tank shooter) {		double angle = shooter.getGunAngle();
		Point2D.Double tankPosition = shooter.getPosition();
		double x = tankPosition.x + (shooter.getBulletReleaseRadius() + this.getCollisionRadius()) * Math.cos(shooter.getGunAngle());
		double y = tankPosition.y + (shooter.getBulletReleaseRadius() + this.getCollisionRadius()) * Math.sin(shooter.getGunAngle());
		this.position = new Point2D.Double(x, y);
		this.shooter = shooter;

		// 角度から速度ベクトルを計算
		this.dx = Math.cos(angle) * getVelocity();
		this.dy = Math.sin(angle) * getVelocity();
	}

	// ============================= Bulletクラス独自のメソッド =============================

	    /**
	     * 弾丸が爆発する際の処理を実行します。
	     * 爆発音を再生し、弾丸を破片状態にし、寿命を終了させます。
	     */
	    private void explode() {		sound.bulletExplosion();
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
		this.lifeFrame = 0;
	}

	    /**
	     * 弾丸の衝突半径を取得します。
	     * @return 弾丸の衝突半径
	     */
	    private double getCollisionRadius() {		return COLLISION_RADIUS;
	}

	    /**
	     * 弾丸の速度を取得します。
	     * @return 弾丸の速度
	     */
	    private double getVelocity() {		return VELOCITY;
	}

	    /**
	     * 弾丸を発射したシューターのチームを取得します。
	     * @return 弾丸が所属するチーム
	     */
	    public Team getTeam() {		return this.shooter.getTeam();
	}

	    /**
	     * 弾丸の現在の状態とチームに応じた画像を取得します。
	     * @return 弾丸の現在の状態に対応するBufferedImage
	     */
	    private BufferedImage getImage() {		boolean isRed = this.getTeam() == RED;
		switch (getState()) {
			case NORMAL:
				return isRed ? redNormalBulletImage : blueNormalBulletImage;
			case DEBRIS:
				return isRed ? redBulletDebris : blueBulletDebris;
			case SHOULD_REMOVE:
				return noneImage;
			default:
				throw new RuntimeException();
		}
	}

	    /**
	     * 弾丸の現在の状態を取得します。
	     * @return 弾丸の現在の状態 (NORMAL, DEBRIS, SHOULD_REMOVE)
	     */
	    private State getState() {		if (debrisLifeFrame > 0) return State.DEBRIS;
		if (lifeFrame <= 0) return State.SHOULD_REMOVE;
		return State.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================


	    /**
	     * 弾丸の状態を更新します。
	     * 弾丸の寿命カウントダウン、移動、破片状態の演出などを処理します。
	     */
	    @Override
	    public void update() {		lifeFrame--;
		if (lifeFrame == 0) {
			explode();
			return;
		}
		switch (getState()) {
			case NORMAL: {
				this.position.x += dx;
				this.position.y += dy;
				break;
			}
			case DEBRIS: {
				debrisLifeFrame--;
				renderScale += (GameEngine.FPS / 120.0) * debrisLifeFrame / 100.0;
				break;
			}
		}
	}


	    /**
	     * 弾丸自身を描画します。現在の状態、位置、速度に応じた画像を描画します。
	     * @param graphics 描画に使用するGraphics2Dオブジェクト
	     */
	    @Override
	    public void draw(Graphics2D graphics) {		BufferedImage image = getImage();
		AffineTransform trans = new AffineTransform();
		trans.translate(this.position.x, this.position.y);
		trans.rotate(Math.atan2(dy, dx));
		trans.scale(renderScale, renderScale);
		trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
		graphics.drawImage(image, trans, null);
	}

	    /**
	     * 他のGameObjectとの衝突時に呼び出されます。
	     * 衝突したオブジェクトが自身のチームと異なる場合、そのオブジェクトに被弾通知を送り、弾丸は爆発します。
	     * @param other 衝突したGameObject
	     */
	    @Override
	    public void onCollision(GameObject other) {
		// 衝突が相手のオブジェクトなら、被弾通知をおくる。
		if (other.getTeam() != this.getTeam()) {
			other.onHitBy(this);
			explode();
		}
	}

	    /**
	     * 他のプロジェクタイルから被弾した際に呼び出されます。
	     * 弾丸が他のプロジェクタイルに被弾した場合の特別な処理はありません。
	     * @param other 衝突したプロジェクタイル
	     */
	    @Override
	    public void onHitBy(Projectile other) {
	}

	    /**
	     * オブジェクトがステージから削除されるべきかを判定します。
	     * 弾丸がSHOULD_REMOVE状態であればtrueを返します。
	     * @return 弾丸が削除されるべきであればtrue、そうでなければfalse
	     */
	    @Override
	    public boolean isExpired() {		return getState() == State.SHOULD_REMOVE;
	}

	    /**
	     * オブジェクトが剛体として扱われるべきか、つまり衝突判定の対象となるべきかを判定します。
	     * 弾丸はNORMAL状態でのみ剛体として扱われます。
	     * @return 弾丸が剛体であればtrue、そうでなければfalse
	     */
	    @Override
	    public boolean hasRigidBody() {		return getState() == State.NORMAL;
	}

	    /**
	     * オブジェクトの描画レイヤーを取得します。
	     * 状態によってPROJECTILEレイヤーまたはDEBRISレイヤーを返します。
	     * @return 描画レイヤー
	     */
	    @Override
	    public RenderLayer getRenderLayer() {		switch (getState()) {
			case NORMAL:
				return RenderLayer.PROJECTILE;
			case DEBRIS:
			case SHOULD_REMOVE:
				return RenderLayer.DEBRIS;
			default:
				throw new RuntimeException();
		}
	}

	    /**
	     * オブジェクトの衝突判定に使用される形状を取得します。
	     * 弾丸は円形の形状を持ちます。
	     * @return 弾丸の形状を表すShapeオブジェクト
	     */
	    @Override
	    public Shape getShape() {		return new Circle(this.position, this.getCollisionRadius());
	}

	    /**
	     * 弾丸の中心座標を取得します。
	     * @return 弾丸の中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public Point2D.Double getPosition() {		return (Point2D.Double) this.position.clone();
	}

	    /**
	     * 弾丸の中心座標を設定します。
	     * @param position 設定する新しい中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public void setPosition(Point2D.Double position) {		this.position.setLocation(position);
	}

	    /**
	     * 弾丸のHPを取得します。
	     * @return 弾丸のHP。現在は常に10を返します。
	     */
	    @Override
	    public int getHP() {		return 10;
	}


	// ============================= Projectileインタフェースのメソッド =============================

	    /**
	     * 弾丸の与えるダメージ能力を取得します。
	     * @return 弾丸のダメージ量
	     */
	    public int getDamageAbility() {		return DAMAGE_ABILITY;
	}

}
