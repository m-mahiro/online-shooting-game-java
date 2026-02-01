package stage;

import client.GamePanel;

import java.awt.geom.Point2D;
import java.util.Random;


public class AutoTank extends Tank {

    private final Random random = new Random();
    private final GameStage gameStage;

    // 移動関連
    private static final int DIRECTION_CHANGE_FRAME = GamePanel.FPS * 2; // 2秒ごとに方向転換
    private int directionChangeFrame = 0;
    private Point2D.Double currentDirection = new Point2D.Double(0, 0);

    // 射撃関連
    private static final int SHOOT_COOLDOWN = GamePanel.FPS * 3; // 3秒ごとに連射開始
    private static final int BURST_COUNT = 5; // 5発連射
    private static final int BURST_INTERVAL = GamePanel.FPS / 5; // 0.2秒間隔
    private int shootCooldown = SHOOT_COOLDOWN;
    private int burstCount = 0;
    private int burstInterval = 0;

    // ブロック設置関連
    private static final int BLOCK_COOLDOWN = GamePanel.FPS * 10; // 10秒ごとに設置開始
    private static final int PLACING_BLOCK_COUNT = 8; // 8個連続設置
    private static final int BLOCK_PLACEMENT_INTERVAL = GamePanel.FPS / 4; // ブロック生成間隔 (0.25秒)
    private int blockCooldown = BLOCK_COOLDOWN;
    private int placingBlockCount = 0;
    private int blockPlacementInterval = 0; // 次のブロックを置くまでのタイマー


    public AutoTank(Base spawnBase, GameStage gameStage) {
        super(spawnBase);
        this.gameStage = gameStage;
        changeDirection();
    }

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

    private void handleShooting() {
        // ブロック設置中は射撃しない
        if (placingBlockCount > 0) {
            return;
        }

        // 連射中
        if (burstCount > 0) {
            burstInterval--;
            if (burstInterval <= 0) {
                gameStage.addStageObject(shootBullet());
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

    private void handleBlockPlacement() {
        // ブロック設置中
        if (placingBlockCount > 0) {
            blockPlacementInterval--;
            if (blockPlacementInterval <= 0) {
                gameStage.addStageObject(createBlock());
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
                directionChangeFrame = PLACING_BLOCK_COUNT * BLOCK_PLACEMENT_INTERVAL + GamePanel.FPS; // 設置時間+α
                blockCooldown = BLOCK_COOLDOWN;
            }
        }
    }


    @Override
    public void onCollision(GameObject other) {
        super.onCollision(other);
        if (other instanceof Wall) {
            changeDirection();
        }
    }

    private void changeDirection() {
        directionChangeFrame = DIRECTION_CHANGE_FRAME;
        int angleDeg = random.nextInt(8) * 45; // 0, 45, 90, ..., 315
        double angleRad = Math.toRadians(angleDeg);
        currentDirection.x = Math.cos(angleRad);
        currentDirection.y = Math.sin(angleRad);
    }
}
