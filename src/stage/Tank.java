package stage;

import client.GameEngine;
import client.SoundManager;
import util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import static stage.Team.*;

public class Tank implements GameObject {

	// 特徴（定数）
	public static final double VELOCITY = 20;
	private static final int INITIAL_HP = 50;

	// 状態（クライアント間の同期に必要)
	private final Base base;
	private final Point2D.Double position; // オブジェクトの中心の座標
	private double gunAngle; // ラジアン
	private int hp = INITIAL_HP;
	private Missile holdingMissile;

	// 演出用定数
	private final static int DAMAGE_FLUSH_FRAME = (int) (GameEngine.FPS * 1.5);
	private final static int DEBRIS_LIFE_FRAME = (int) (GameEngine.FPS * 0.25);
	private final static int RESPAWN_LAG_FRAME = GameEngine.FPS * 3;
	private final static int RESPAWN_ANIMATE_FRAME = GameEngine.FPS;

	// 演出用変数（クライアント間の同期は必要ない）
	private int damageFlushFrame = 0;
	private int debrisLifeFrame = 0;
	private int respawnLagFrame = 0;
	private int respawnAnimateFrame = 0;
	private boolean hadBroken = false;
	private boolean isOnBase = false;

	// 効果音
	private static final SoundManager sound = new SoundManager();

	// 画像リソース
	private static BufferedImage redNormalChassisImage, redBrokenChassisImage, redTransparentChassisImage;
	private static BufferedImage blueNormalChassisImage, blueBrokenChassisImage, blueTransparentChassisImage;
	private static BufferedImage redNormalGunImage, redBrokenGunImage, redTransparentGunImage;
	private static BufferedImage blueNormalGunImage, blueBrokenGunImage, blueTransparentGunImage;
	private static BufferedImage redTankDebris;
	private static BufferedImage blueTankDebris;
	private static BufferedImage noneImage;

	private enum State {
		RESPAWNING, NORMAL, BROKEN, DEBRIS, NONE
	}

	static {
		try {
			// 本当の本当に透明
			noneImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/none_image.png")));

			// debris
			redTankDebris = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/tank_red_debris.png")));
			blueTankDebris = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/tank_blue_debris.png")));

			// red chassis
			redNormalChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/chassis_red_normal.png")));
			redBrokenChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/chassis_red_broken.png")));
			redTransparentChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/chassis_red_trans.png")));

			// blue chassis
			blueNormalChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/chassis_blue_normal.png")));
			blueBrokenChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/chassis_blue_broken.png")));
			blueTransparentChassisImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/chassis_blue_trans.png")));

			// red gun
			redNormalGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/gun_red_normal.png")));
			redBrokenGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/gun_red_broken.png")));
			redTransparentGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/gun_red_trans.png")));

			// blue gun
			blueNormalGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/gun_blue_normal.png")));
			blueBrokenGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/gun_blue_broken.png")));
			blueTransparentGunImage = ImageIO.read(Objects.requireNonNull(Tank.class.getResource("/client/assets/gun_blue_trans.png")));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	    /**
	     * 新しい戦車オブジェクトを生成します。
	     * 戦車は指定された基地の位置にスポーンします。
	     * @param base 戦車が所属し、スポーンする基地
	     */
	    public Tank(Base base) {		this.position = base.getPosition();
		this.base = base;
	}

	// ============================= Tankクラス独自のメソッド =============================

	    /**
	     * 戦車の砲塔を指定された座標に向かせます。
	     * ミサイルをチャージ中の場合は砲塔を動かせません。
	     * @param coordinate 砲塔を向けるターゲットの座標
	     */
	    public void aimAt(Point2D.Double coordinate) {		if (holdingMissile != null) return;
		this.gunAngle = Math.atan2(coordinate.y - this.position.y, coordinate.x - this.position.x);
	}

	    /**
	     * 指定されたベクトル方向に戦車を移動させます。
	     * ミサイルをチャージ中の場合は移動できません。
	     * @param vector 移動方向と強さを表すベクトル
	     */
	    public void move(Point2D.Double vector) {		if (holdingMissile != null) return;
		Point2D.Double p = new Point2D.Double(vector.x, vector.y);
		p = Util.normalize(p);
		p = Util.multiple(p, this.VELOCITY);
		p = Util.addition(this.position, p);
		this.position.setLocation(p);
	}

	    /**
	     * 戦車から弾丸を発射します。
	     * @return 発射されたBulletオブジェクト
	     */
	    public Bullet shootBullet() {		sound.shootGun();
		return new Bullet(this);
	}

	    /**
	     * エネルギーチャージを開始し、新しいミサイルを生成して保持します。
	     * @return 生成されたMissileオブジェクト
	     */
	    public Missile startEnergyCharge() {		Missile missile = new Missile(this);
		this.holdingMissile = missile;
		return missile;
	}

	    /**
	     * エネルギーチャージを終了し、保持しているミサイルを発射します。
	     * ミサイルが保持されていない場合は何もしません。
	     */
	    public void finishEnergyCharge() {		if (this.holdingMissile == null) return;
		this.holdingMissile.launch();
		this.holdingMissile = null;
	}

	    /**
	     * 現在位置にブロックを生成します。
	     * 基地の上にいる場合はブロックを生成できません。
	     * @return 生成されたBlockオブジェクト、または基地の上にいる場合はnull
	     */
	    public Block createBlock() {		if (isOnBase) return null;
		sound.createBlock();
		return new Block(this.position.x, this.position.y, true);
	}

	    /**
	     * 戦車にダメージを与えます。
	     * HPが0以下になった場合、onDie()を呼び出します。
	     * ダメージを受けた際の点滅演出のためのフレームも設定します。
	     * @param damage 与えるダメージ量
	     */
	    public void damage(int damage) {		this.damageFlushFrame = DAMAGE_FLUSH_FRAME;
		this.hp -= damage;
		if (this.hp <= 0) {
			onDie();
		}
	}

	    /**
	     * 戦車のHPが0以下になったときに呼び出され、戦車の破壊処理（爆発音再生、残骸表示、リスポーン遅延設定）を行います。
	     */
	    public void onDie() {		sound.objectExplosion();
		this.hp = 0;
		this.damageFlushFrame = 0;
		this.debrisLifeFrame = DEBRIS_LIFE_FRAME;
		this.respawnLagFrame = RESPAWN_LAG_FRAME;
	}

	    /**
	     * 戦車をリスポーンさせます。
	     * 基地が廃墟状態の場合はリスポーンできません。HPを初期値に戻し、リスポーンアニメーションを開始し、基地の位置に戦車を移動させます。
	     */
	    public void respawn() {		if (base.isRuins()) return;
		this.hp = INITIAL_HP;
		this.respawnAnimateFrame = RESPAWN_ANIMATE_FRAME;
		Point2D.Double spawnPoint = base.getPosition();
		this.setPosition(spawnPoint);
	}

	    /**
	     * 戦車の描画スケールを取得します。
	     * 戦車の現在の状態（リスポーン中、通常、破壊など）に応じてスケールが異なります。
	     * @return 戦車の描画スケール
	     */
	    private double getObjectScale() {		switch (getState()) {
			case RESPAWNING:
				return 1.0 - respawnAnimateFrame / (double) RESPAWN_ANIMATE_FRAME;
			case NORMAL:
			case BROKEN:
			case NONE:
				return 1.0;
			case DEBRIS:
				return (GameEngine.FPS / 240.0) * Math.pow(debrisLifeFrame, 2) / 100.0;
			default:
				throw new RuntimeException();
		}
	}

	    /**
	     * 戦車のシャシーの現在の状態とチームに応じた画像を取得します。
	     * ダメージを受けた直後は点滅演出のため透明な画像が返されることがあります。
	     * @return シャシーの画像
	     */
	    private BufferedImage getChassisImage() {
		boolean isRed = (base.getTeam() == RED);
		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
		switch (getState()) {
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
				throw new IllegalStateException("Unexpected value: " + getState());
		}
	}

	    /**
	     * 戦車の砲塔の現在の状態とチームに応じた画像を取得します。
	     * ダメージを受けた直後は点滅演出のため透明な画像が返されることがあります。
	     * @return 砲塔の画像
	     */
	    private BufferedImage getGunImage() {		boolean isRed = (base.getTeam() == RED);
		boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
		switch (getState()) {
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
				throw new IllegalStateException("Unexpected value: " + getState());
		}
	}

	    /**
	     * 戦車が現在行動不能（死亡または消滅）状態であるかどうかを判定します。
	     * @return 戦車が行動不能であればtrue、そうでなければfalse
	     */
	    public boolean isDead() {		switch (this.getState()) {
			case RESPAWNING:
			case NORMAL:
			case BROKEN:
				return false;
			case DEBRIS:
			case NONE:
				return true;
			default:
				throw new IllegalStateException("Unexpected value: " + this.getState());
		}
	}

	    /**
	     * 戦車の現在の状態（RESPAWNING, NORMAL, BROKEN, DEBRIS, NONE）を取得します。
	     * @return 戦車の現在の状態
	     */
	    private State getState() {
		if (respawnAnimateFrame > 0) return State.RESPAWNING;

		// 残骸は一定時間経過後画面から消える
		if (debrisLifeFrame <= 0 && hp <= 0) return State.NONE;

		// 破壊されたときに、画面に残骸が表示される
		if (hp <= 0) return State.DEBRIS;

		// ダメージが蓄積されると戦車にひびが入る演出
		if (hp < INITIAL_HP / 2.0) return State.BROKEN;

		return State.NORMAL;
	}

	// ============================= GameObjectインタフェースのメソッド =============================

	    /**
	     * 戦車の状態を更新します。
	     * リスポーンアニメーション、ダメージ点滅、残骸の寿命、リスポーン遅延などのカウントダウンを処理します。
	     */
	    @Override
	    public void update() {
		isOnBase = false;

		if (getState() == State.BROKEN && !hadBroken) {
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


	    /**
	     * 戦車自身を描画します。シャシーと砲塔を現在の位置、角度、スケールに基づいて描画します。
	     * @param graphics 描画に使用するGraphics2Dオブジェクト
	     */
	    @Override
	    public void draw(Graphics2D graphics) {		BufferedImage chassisImage = this.getChassisImage();
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

	    /**
	     * 他のGameObjectとの衝突時に呼び出されます。
	     * 自身のチームの弾丸や基地との衝突は無視されます。
	     * それ以外のオブジェクトとの衝突では、戦車がめり込まないように適切な方向に押し戻されます。
	     * @param other 衝突したGameObject
	     */
	    @Override
	    public void onCollision(GameObject other) {
		// 自チームの弾はすり抜ける
		if (other instanceof Projectile) {
			Projectile projectile = (Projectile) other;
			if (projectile.getTeam() == this.getTeam()) return;
		}

		// 自チームの基地の上には乗れる。
		if (other instanceof Base && other.getTeam() == this.getTeam()) {
			isOnBase = true;
			return;
		}

		// ============================= オブジェクトがのめりこまないように、適切な方向に逃げる =============================

		// 相対的な位置関係を取得 (相手 - 自分)
		Point2D.Double vector = Util.subtract(other.getPosition(), this.getPosition());

		// 全く同じ位置だった場合、少しだけずらす
		if (vector.x == 0 && vector.y == 0) {
			Random random = new Random();
			vector.x = random.nextDouble() - 0.5;
			vector.y = random.nextDouble() - 0.5;
		}

		// 相手のサイズを取得
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

	    /**
	     * プロジェクタイルから被弾した際に呼び出されます。
	     * 戦車が死亡状態でない場合、プロジェクタイルのダメージ能力に基づいてダメージを受けます。
	     * @param bullet 衝突したプロジェクタイル
	     */
	    @Override
	    public void onHitBy(Projectile bullet) {		if (isDead()) return;
		this.damage(bullet.getDamageAbility());
	}

	    /**
	     * オブジェクトがステージから削除されるべきかを判定します。
	     * 戦車はゲーム終了までステージに存在するため、常にfalseを返します。
	     * @return 常にfalse
	     */
	    @Override
	    public boolean isExpired() {		return false;
	}

	    /**
	     * オブジェクトが剛体として扱われるべきか、つまり衝突判定の対象となるべきかを判定します。
	     * 戦車はNORMAL状態またはBROKEN状態でのみ剛体として扱われます。
	     * @return 戦車が剛体であればtrue、そうでなければfalse
	     */
	    @Override
	    public boolean hasRigidBody() {		switch (getState()) {
			case NORMAL:
			case BROKEN:
				return true;
			case RESPAWNING:
			case DEBRIS:
			case NONE:
				return false;
			default:
				throw new RuntimeException();
		}
	}

	    /**
	     * オブジェクトの描画レイヤーを取得します。
	     * 戦車の状態に応じてTANGIBLE_OBJECTレイヤーまたはDEBRISレイヤーを返します。
	     * @return 描画レイヤー
	     */
	    @Override
	    public RenderLayer getRenderLayer() {		switch (getState()) {
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

	    /**
	     * オブジェクトの衝突判定に使用される形状を取得します。
	     * 戦車は長方形の形状を持ちます。
	     * @return 戦車の形状を表すShapeオブジェクト
	     */
	    @Override
	    public Shape getShape() {		return new Rectangle(this.position, getWidth(), getHeight());
	}

	    /**
	     * 戦車の中心座標を取得します。
	     * @return 戦車の中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public Point2D.Double getPosition() {		return (Point2D.Double) this.position.clone();
	}

	    /**
	     * 戦車の中心座標を設定します。
	     * @param position 設定する新しい中心座標のPoint2D.Doubleオブジェクト
	     */
	    @Override
	    public void setPosition(Point2D.Double position) {		this.position.setLocation(position);
	}

	    /**
	     * 戦車の現在のHPを取得します。
	     * @return 戦車の現在のHP
	     */
	    @Override
	    public int getHP() {		return this.hp;
	}

	// ============================= ゲッター・セッター =============================

	    /**
	     * 戦車の砲塔の角度をラジアンで取得します。
	     * @return 砲塔の現在の角度（ラジアン）
	     */
	    public double getGunAngle() {		return this.gunAngle;
	}

	    /**
	     * 戦車が所属するチームを取得します。
	     * これは戦車が紐付けられている基地のチームです。
	     * @return 戦車のチーム
	     */
	    public Team getTeam() {		return this.base.getTeam();
	}

	    /**
	     * 戦車の高さを取得します。オブジェクトのスケールを考慮します。
	     * @return 戦車の現在の高さ
	     */
	    public double getHeight() {		return getChassisImage().getHeight() * getObjectScale();
	}

	    /**
	     * 戦車の幅を取得します。オブジェクトのスケールを考慮します。
	     * @return 戦車の現在の幅
	     */
	    public double getWidth() {		return getChassisImage().getWidth() * getObjectScale();
	}

	    /**
	     * 弾丸が戦車から放出される際の半径を取得します。
	     * これは砲身の長さと戦車の半径を考慮して計算されます。
	     * @return 弾丸の放出半径
	     */
	    public double getBulletReleaseRadius() {		double gunLength = getGunImage().getWidth() / 2.0;
		double chassisRadius = Math.max(getWidth(), getHeight()) / 2.0;
		return Math.max(gunLength, chassisRadius);
	}
}
