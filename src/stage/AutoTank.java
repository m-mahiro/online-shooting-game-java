package stage;

import client.GameEngine;

import java.awt.geom.Point2D;
import java.util.Random;

public class AutoTank extends Tank {

    private final Random random = new Random();
    private final GameStage gameStage;

    // 移動関連
    private static final int DIRECTION_CHANGE_FRAME = GameEngine.FPS * 2; // 2秒ごとに方向転換
    private int directionChangeFrame = 0;
    private Point2D.Double currentDirection = new Point2D.Double(0, 0);

    // 射撃関連
    private static final int SHOOT_COOLDOWN = GameEngine.FPS * 3; // 3秒ごとに連射開始
    private static final int BURST_COUNT = 5; // 5発連射
    private static final int BURST_INTERVAL = GameEngine.FPS / 5; // 0.2秒間隔
    private int shootCooldown = SHOOT_COOLDOWN;
    private int burstCount = 0;
    private int burstInterval = 0;

    // ブロック設置関連
    private static final int BLOCK_COOLDOWN = GameEngine.FPS * 10; // 10秒ごとに設置開始
    private static final int PLACING_BLOCK_COUNT = 8; // 8個連続設置
    private static final int BLOCK_PLACEMENT_INTERVAL = GameEngine.FPS / 4; // ブロック生成間隔 (0.25秒)
    private int blockCooldown = BLOCK_COOLDOWN;
    private int placingBlockCount = 0;
    private int blockPlacementInterval = 0; // 次のブロックを置くまでのタイマー


    /**
     * コンストラクタ。AutoTankを指定された基地にスポーンさせ、ゲームステージに登録します。
     * @param spawnBase スポーンする基地
     * @param gameStage ゲームステージ
     */
    public AutoTank(Base spawnBase, GameStage gameStage) {
        super(spawnBase);
        this.gameStage = gameStage;
        changeDirection();
    }

    /**
     * AutoTankの状態を更新します。これには、移動、射撃、ブロック設置の処理が含まれます。
     */
    @Override
    public void update() {
        super.update();

        if (isDead()) {
            return;
        }

        // 砲塔を移動方向に向ける (適当な照準)
        Point2D.Double target = new Point2D.Double(getPosition().x + currentDirection.x, getPosition().y + currentDirection.y);
        aimAt(target);

        // --- 行動処理 ---
        handleBlockPlacement();
        handleShooting();
        handleMovement();

        // 実際に移動
        move(currentDirection);
    }

    /**
     * AutoTankの移動ロジックを処理します。一定時間ごとにランダムな方向に進行方向を変更します。
     * ただし、ブロック設置中は方向転換しません。
     */
    private void handleMovement() {
        // ブロック設置中は方向転換しない
        if (placingBlockCount > 0) {
            return;
        }

        directionChangeFrame--;
        if (directionChangeFrame <= 0) {
            changeDirection();
        }
    }

    /**
     * AutoTankの射撃ロジックを処理します。クールダウン後、一定数の弾を連続して発射します。
     * ブロック設置中は射撃しません。
     */
    private void handleShooting() {
        // ブロック設置中は射撃しない
        if (placingBlockCount > 0) {
            return;
        }

        // 連射中
        if (burstCount > 0) {
            burstInterval--;
            if (burstInterval <= 0) {
                gameStage.addGameObject(shootBullet());
                burstCount--;
                burstInterval = BURST_INTERVAL;
            }
        }
        // 連射開始判定
        else {
            shootCooldown--;
            if (shootCooldown <= 0) {
                burstCount = BURST_COUNT;
                burstInterval = 0; // すぐに1発目を撃つ
                shootCooldown = SHOOT_COOLDOWN;
            }
        }
    }

    /**
     * AutoTankのブロック設置ロジックを処理します。クールダウン後、一定数のブロックを連続して設置します。
     */
    private void handleBlockPlacement() {
        // ブロック設置中
        if (placingBlockCount > 0) {
            blockPlacementInterval--;
            if (blockPlacementInterval <= 0) {
                gameStage.addGameObject(createBlock());
                placingBlockCount--;
                blockPlacementInterval = BLOCK_PLACEMENT_INTERVAL;
            }
        }
        // ブロック設置開始判定
        else {
            blockCooldown--;
            if (blockCooldown <= 0) {
                placingBlockCount = PLACING_BLOCK_COUNT;
                blockPlacementInterval = 0; // 最初のブロックはすぐに置く
                // 設置中は直進を続けるように方向転換タイマーをリセット
                directionChangeFrame = PLACING_BLOCK_COUNT * BLOCK_PLACEMENT_INTERVAL + GameEngine.FPS; // 設置時間+α
                blockCooldown = BLOCK_COOLDOWN;
            }
        }
    }


    /**
     * 他のゲームオブジェクトとの衝突時に呼び出されます。壁に衝突した場合、進行方向を変更します。
     * @param other 衝突した他のゲームオブジェクト
     */
    @Override
    public void onCollision(GameObject other) {
        super.onCollision(other);
        if (other instanceof Wall) {
            changeDirection();
        }
    }

    /**
     * AutoTankの進行方向をランダムな8方向のいずれかに変更します。
     */
    private void changeDirection() {
        directionChangeFrame = DIRECTION_CHANGE_FRAME;
        int angleDeg = random.nextInt(8) * 45; // 0, 45, 90, ..., 315
        double angleRad = Math.toRadians(angleDeg);
        currentDirection.x = Math.cos(angleRad);
        currentDirection.y = Math.sin(angleRad);
    }
}
