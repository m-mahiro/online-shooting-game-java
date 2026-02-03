package stage;

import client.GameEngine;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

public class Missile implements GameObject, Projectile {

    // 定数
    private static final double VELOCITY = 50;
    private static final double MAX_OBJECT_SCALE = 5.0;
    private static final int MAX_DAMAGE_ABILITY = 200;

    // 状態
    private Tank shooter;
    private Point2D.Double position;
    private double angle;
    private Status state = Status.CHARGING;
    private int chargeCount = 0;
    private int damageTotal = 0;

    // 演出用定数
    private static final int CANCEL_ANIMATION_FRAME = (int) (GameEngine.FPS * 0.5);

    // 演出用変数
    private int cancelAnimationFrame = 0;

    // 画像リソース
    private static BufferedImage blueChargingMissileImage, redChargingMissileImage;
    private static BufferedImage blueReadyMissileImage, redReadyMissileImage;
    private static BufferedImage blueMissileDebris, redMissileDebris;
    private static BufferedImage noneImage;

    // 状態管理
    private enum Status {
        CHARGING, CANCELLED, FLYING, DEBRIS, SHOULD_REMOVE
    }

    static {
        try {
            noneImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/none_image.png")));

            blueChargingMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_charging.png")));
            redChargingMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_charging.png")));

            blueReadyMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_ready.png")));
            redReadyMissileImage = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/missile_blue_ready.png")));

            blueMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/bullet_blue_debris.png")));
            redMissileDebris = ImageIO.read(Objects.requireNonNull(Missile.class.getResource("/client/assets/bullet_red_debris.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 新しいミサイルオブジェクトを生成します。
     * ミサイルは初期状態でチャージング状態であり、シューターの位置と向きに基づいて位置が設定されます。
     */
    public Missile(Tank shooter) {
        this.shooter = shooter;
        this.position = new Point2D.Double();
        setPositionBaseOn(shooter);
    }

    // ============================= Missileクラス独自のメソッド =============================

    /**
     * ミサイルのダメージ能力を減少させます。
     * ミサイルが何かに衝突してダメージを与えた際に、その対象のHP分だけミサイルのダメージ能力を減算します。
     * ダメージ能力が0以下になった場合、ミサイルは爆発します。
     */
    public void decreaseDamageAbility(int damage) {
        damageTotal += damage;
        if (this.getHP() <= 0) explode();
    }

    /**
     * ミサイルを爆発させ、状態をDEBRIS（破片）に変更します。
     */
    public void explode() {
        this.state = Status.DEBRIS;
    }

    /**
     * ミサイルの位置をシューター（戦車）に基づいて設定します。
     * 戦車の砲塔の向きと、弾丸の放出半径を考慮してミサイルが戦車の前に配置されます。
     */
    private void setPositionBaseOn(Tank shooter) {
        this.angle = shooter.getGunAngle();
        double shooterRadius = shooter.getBulletReleaseRadius();
        double missileRadius = this.getCollisionRadius();
        Point2D.Double bulletPosition = (Point2D.Double) shooter.getPosition().clone();
        double x = bulletPosition.x + (shooterRadius + missileRadius) * Math.cos(this.angle);
        double y = bulletPosition.y + (shooterRadius + missileRadius) * Math.sin(this.angle);
        this.setPosition(new Point2D.Double(x, y));
    }

    /**
     * ミサイルを発射し、状態をFLYING（飛行中）に変更します。
     */
    public void launch() {
        this.state = Status.FLYING;
    }

    /**
     * {@inheritDoc}
     */
    public Team getTeam() {
        return this.shooter.getTeam();
    }

    /**
     * ミサイルの描画スケールを取得します。
     * 状態やダメージ能力によってスケールが異なります。
     */
    private double getObjectScale() {
        switch (this.state) {
            case DEBRIS:
            case CANCELLED:
            case SHOULD_REMOVE:
                return 1.0;
            case CHARGING:
            case FLYING:
                return Math.max(MAX_OBJECT_SCALE, getDamageAbility() / 100.0);
            default:
                throw new IllegalStateException("Unexpected value: " + this.state);
        }
    }

    /**
     * ミサイルの衝突半径を取得します。
     * これはミサイルの画像サイズと現在のオブジェクトスケールに基づいて計算されます。
     */
    private double getCollisionRadius() {
        return getImage().getWidth() / 2.0 * getObjectScale();
    }

    /**
     * ミサイルの現在の状態とチームに応じた画像を取得します。
     */
    private BufferedImage getImage() {
        boolean isRed = (getTeam() == RED);
        switch (this.state) {
            case CHARGING:
            case CANCELLED:
                return isRed ? redChargingMissileImage : blueChargingMissileImage;
            case FLYING:
                return isRed ? redReadyMissileImage : blueReadyMissileImage;
            case DEBRIS:
                return isRed ? redMissileDebris : blueMissileDebris;
            case SHOULD_REMOVE:
                return noneImage;
            default:
                throw new RuntimeException("Unknown Status: " + state);
        }
    }

    // ============================= GameObjectインターフェースのメソッド =============================

    /**
     * {@inheritDoc}
     * チャージ中または飛行中のミサイルの位置更新、アニメーションフレームのカウントダウン/アップなどを処理します。
     */
    @Override
    public void update() {
        // チャージ中は戦車の位置や砲塔の向きに合わせてミサイルの位置を変える
        switch (this.state) {
            case CHARGING:
                setPositionBaseOn(shooter);
                break;
            case FLYING:
                double dx = VELOCITY * Math.cos(this.angle);
                double dy = VELOCITY * Math.sin(this.angle);
                this.position.x += dx;
                this.position.y += dy;
                break;
        }

        // フレームカウントダウン
        if (cancelAnimationFrame > 0) cancelAnimationFrame--;

        // フレームカウントアップ
        if (state == Status.CHARGING) chargeCount++;
    }

    /**
     * {@inheritDoc}
     * 現在の状態、位置、角度、スケールに基づいて画像を描画します。
     */
    @Override
    public void draw(Graphics2D graphics) {
        BufferedImage image = getImage();
        double objectScale = this.getObjectScale();
        AffineTransform trans = new AffineTransform();
        trans.translate(position.x, position.y);
        trans.rotate(angle);
        trans.scale(objectScale, objectScale);
        trans.translate(-image.getWidth() / 2.0, -image.getHeight() / 2.0);
        graphics.drawImage(image, trans, null);
    }

    /**
     * {@inheritDoc}
     * ミサイルがチャージ中に衝突した場合、爆発します。
     * 衝突したオブジェクトが自身のチームと異なる場合、そのオブジェクトに被弾通知を送り、ミサイルのダメージ能力を減少させます。
     */
    @Override
    public void onCollision(GameObject other) {
        if (this.state == Status.CHARGING) explode();

        // 衝突が相手のオブジェクトなら、被弾通知をおくる。
        if (other.getTeam() != this.getTeam()) {
            other.onHitBy(this);
            // 相手のHPの分だけ自分の殺傷能力をを削る
            this.decreaseDamageAbility(other.getHP());
        }

    }

    /**
     * {@inheritDoc}
     * 衝突したプロジェクタイルのダメージ能力分、ミサイルのダメージ能力を減少させます。
     */
    @Override
    public void onHitBy(Projectile other) {
        decreaseDamageAbility(other.getDamageAbility());
    }

    /**
     * {@inheritDoc}
     * キャンセル状態の場合、キャンセルアニメーションフレームが0以下であればtrueを返します。
     * SHOULD_REMOVE状態の場合は常にtrueを返します。
     */
    @Override
    public boolean isExpired() {
        switch (this.state) {
            case CANCELLED:
                return cancelAnimationFrame <= 0;
            case SHOULD_REMOVE:
                return true;
            default:
                return false;
        }
    }

    /**
     * {@inheritDoc}
     * ミサイルはCHARGING状態またはFLYING状態でのみ剛体として扱われます。
     */
    @Override
    public boolean hasRigidBody() {
        switch (this.state) {
            case CHARGING:
            case FLYING:
                return true;
            case CANCELLED:
            case DEBRIS:
            case SHOULD_REMOVE:
                return false;
            default:
                throw new IllegalStateException("Unexpected value: " + this.state);
        }
    }

    /**
     * {@inheritDoc}
     * 状態によってPROJECTILEレイヤーまたはDEBRISレイヤーを返します。
     */
    @Override
    public RenderLayer getRenderLayer() {
        switch (this.state) {
            case CHARGING:
            case CANCELLED:
            case FLYING:
                return RenderLayer.PROJECTILE;
            case DEBRIS:
            case SHOULD_REMOVE:
                return RenderLayer.DEBRIS;
            default:
                throw new IllegalStateException("Unexpected value: " + this.state);
        }
    }

    /**
     * {@inheritDoc}
     * ミサイルは円形の形状を持ちます。
     */
    @Override
    public Shape getShape() {
        return new Circle(this.position, getCollisionRadius());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D.Double getPosition() {
        return (Point2D.Double) this.position.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(Point2D.Double position) {
        this.position.setLocation(position);
    }

    /**
     * {@inheritDoc}
     * これはミサイルのダメージ能力に依存します。
     */
    @Override
    public int getHP() {
        return getDamageAbility();
    }


    // ============================= Projectileインターフェースのメソッド =============================

    /**
     * {@inheritDoc}
     * 飛行中のミサイルのチャージ時間と既に与えたダメージ量に基づいて計算されます。
     */
    @Override
    public int getDamageAbility() {
        if (this.state == Status.FLYING) {
            int damageAbility = Math.min(chargeCount / GameEngine.FPS * 10, MAX_DAMAGE_ABILITY) - damageTotal;
            return Math.max(damageAbility, 0);
        }
        return 0;
    }
}