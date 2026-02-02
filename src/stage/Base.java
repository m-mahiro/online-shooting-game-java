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

public class Base implements GameObject {

    // 定数
    private static final int INIT_HP = 1000;

    // 状態
    private int hp = INIT_HP;
    private final Point2D.Double position;
    private final Team team;

    // 定数(演出用)
    private static final int DAMAGE_FLUSH_FRAME = (int) (GameEngine.FPS * 1.5);
    private static final int DEBRIS_LIFE_FRAME = GameEngine.FPS / 4;

    // 状態(演出用)
    private double debrisScale = 1.0;
    private double ringRotation = 0;
    private int damageFlushFrame = 0;
    private int debrisLifeFrame = 0;
    private boolean isBroken = false;
    private final SoundManager sound = new SoundManager();

    // 画像リソース
    private static BufferedImage normalRedBaseImage, brokenRedBaseImage, redBaseRuinsImage;
    private static BufferedImage normalBlueBaseImage, brokenBlueBaseImage, blueBaseRuinsImage;
    private static BufferedImage normalRingImage;
    private static BufferedImage noneImage;

    public enum State {
        NORMAL, BROKEN, RUINS
    }

    static {
        try {
            // red base
            normalRedBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/base_red_normal.png")));
            brokenRedBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/base_red_broken.png")));
            redBaseRuinsImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/base_red_ruins.png")));

            // blue base
            normalBlueBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/base_blue_normal.png")));
            brokenBlueBaseImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/base_blue_broken.png")));
            blueBaseRuinsImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/base_blue_ruins.png")));

            // ring
            normalRingImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/ring_normal.png")));

            // 透明
            noneImage = ImageIO.read(Objects.requireNonNull(Base.class.getResource("/client/assets/none_image.png")));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 新しいBaseオブジェクトを生成します。
     *
     * @param x    基地の中心X座標
     * @param y    基地の中心Y座標
     * @param team 基地が所属するチーム
     */
    public Base(double x, double y, Team team) {
        this.position = new Point2D.Double(x, y);
        this.team = team;
    }

    // ============================= Baseクラス固有のメソッド =============================

    /**
     * 基地にダメージを与えます。HPが0以下になった場合、onDie()を呼び出します。
     * ダメージを受けた際の点滅演出のためのフレームも設定します。
     *
     * @param damage 与えるダメージ量
     */
    private void damage(int damage) {
        this.damageFlushFrame = DAMAGE_FLUSH_FRAME;
        hp -= damage;
        if (hp <= 0) onDie();
    }

    /**
     * 基地のHPが0以下になったときに呼び出され、基地の破壊処理（爆発音再生など）を行います。
     */
    private void onDie() {        // 爆発する
        debrisLifeFrame = DEBRIS_LIFE_FRAME;
        sound.objectExplosion();
    }

    /**
     * この基地が所属するチームを取得します。
     *
     * @return この基地のチーム
     */
    public Team getTeam() {
        return this.team;
    }

    /**
     * 基地が廃墟状態であるかどうかを判定します。
     *
     * @return 基地が廃墟状態であればtrue、そうでなければfalse
     */
    public boolean isRuins() {
        return getState() == State.RUINS;
    }

    /**
     * 基地の現在の状態とチームに応じた画像を取得します。
     * ダメージを受けた直後は点滅演出のため透明な画像が返されることがあります。
     *
     * @return 基地の画像
     */
    private BufferedImage getBaseImage() {
        boolean isRed = this.team == RED;
        boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
        switch (getState()) {
            case NORMAL:
                if (isFlushing) return noneImage;
                return isRed ? normalRedBaseImage : normalBlueBaseImage;
            case BROKEN:
                if (isFlushing) return noneImage;
                return isRed ? brokenRedBaseImage : brokenBlueBaseImage;
            case RUINS:
                return isRed ? redBaseRuinsImage : blueBaseRuinsImage;
        }
        return normalRedBaseImage;
    }

    /**
     * 基地の現在の状態に応じたリング画像を取得します。
     * ダメージを受けた直後は点滅演出のため透明な画像が返されることがあります。
     *
     * @return 基地のリング画像
     */
    private BufferedImage getRingImage() {
        boolean isFlushing = (damageFlushFrame > 0) && damageFlushFrame % 20 == 0;
        switch (getState()) {
            case NORMAL:
            case BROKEN:
                if (isFlushing) return noneImage;
                return normalRingImage;
            case RUINS:
                return noneImage;
            default:
                throw new RuntimeException();
        }

    }

    /**
     * 基地の現在の状態（NORMAL, BROKEN, RUINS）を取得します。
     * HPに基づいて状態が決定されます。
     *
     * @return 基地の現在の状態
     */
    public State getState() {
        if (hp <= 0) return State.RUINS;
        if (hp <= INIT_HP / 2) return State.BROKEN;
        return State.NORMAL;
    }


    // ============================= GameObjectインタフェースのメソッド =============================

    /**
     * 基地の状態を更新します。リングの回転アニメーション、ダメージ点滅フレーム、残骸の寿命、破壊演出のスケールなどを処理します。
     */
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
        if (getState() == State.RUINS) debrisScale += (GameEngine.FPS / 10.0) * debrisLifeFrame / 100.0;

    }

    /**
     * 基地自身を描画します。基地の画像とリング画像を現在の状態と位置に基づいて描画します。
     *
     * @param graphics 描画に使用するGraphics2Dオブジェクト
     */
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

    /**
     * 他のGameObjectとの衝突時に呼び出されます。
     * 現在、基地に直接衝突することによる特殊な処理はありません。
     *
     * @param other 衝突したGameObject
     */
    @Override
    public void onCollision(GameObject other) {
    }

    /**
     * プロジェクタイルが基地に衝突した際に呼び出されます。
     * プロジェクタイルの持つダメージ能力に基づいて基地にダメージを与えます。
     *
     * @param other 基地に衝突したプロジェクタイル
     */
    @Override
    public void onHitBy(Projectile other) {
        damage(other.getDamageAbility());
    }

    /**
     * オブジェクトがステージから削除されるべきかを判定します。
     * 基地はゲーム終了までステージに残り続けるため、常にfalseを返します。
     *
     * @return 常にfalse
     */
    @Override
    public boolean isExpired() {
        return false;
    }

    /**
     * オブジェクトが剛体として扱われるべきか、つまり衝突判定の対象となるべきかを判定します。
     * 基地は通常状態または破損状態では剛体ですが、廃墟状態では剛体ではありません。
     *
     * @return 基地が剛体であればtrue、そうでなければfalse
     */
    @Override
    public boolean hasRigidBody() {
        switch (getState()) {
            case NORMAL:
            case BROKEN:
                return true;
            case RUINS:
                return false;
            default:
                throw new RuntimeException();
        }
    }

    /**
     * オブジェクトの描画レイヤーを取得します。
     * 基地は残骸レイヤーとして描画されます。
     *
     * @return 描画レイヤー
     */
    @Override
    public RenderLayer getRenderLayer() {
        return RenderLayer.DEBRIS;
    }

    /**
     * オブジェクトの衝突判定に使用される形状を取得します。
     * 基地は円形の形状を持ちます。破壊状態に応じてスケールが変化します。
     *
     * @return 基地の形状を表すShapeオブジェクト
     */
    @Override
    public Shape getShape() {
        double width = getBaseImage().getWidth() * debrisScale;
        return new Circle(this.position, width / 2.0);
    }

    /**
     * 基地の中心座標を取得します。
     *
     * @return 基地の中心座標のPoint2D.Doubleオブジェクト
     */
    @Override
    public Point2D.Double getPosition() {
        return (Point2D.Double) this.position.clone();
    }

    /**
     * 基地の中心座標を設定します。
     *
     * @param position 設定する新しい中心座標のPoint2D.Doubleオブジェクト
     */
    @Override
    public void setPosition(Point2D.Double position) {
        this.position.setLocation(position);
    }

    /**
     * 基地の現在のHPを取得します。
     *
     * @return 基地の現在のHP
     */
    @Override
    public int getHP() {
        return hp;
    }
}
