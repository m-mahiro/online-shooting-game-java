package stage;

import java.awt.geom.Point2D;

import static stage.Team.RED;

public class StartScreenTank extends Tank {

    private final Base base;
    private final double velocity;
    private final double travelDistanceLimit;

    /**
     * 新しい戦車オブジェクトを生成します。
     * 戦車は指定された基地の位置にスポーンします。
     *
     * @param base 戦車が所属し、スポーンする基地
     */
    public StartScreenTank(Base base, double velocity, double travelDistanceLimit) {
        super(base);
        this.base = base;
        this.velocity = velocity;
        this.travelDistanceLimit = travelDistanceLimit;
    }

    /**
     * {@inheritDoc}
     * 戦車を更新し、チームに応じて左右に移動させます。
     * 移動距離が制限を超えた場合、戦車をリスポーンさせます。
     */
    @Override
    public void update() {
        super.update();
        Point2D.Double moveVector = this.getTeam() == RED ? new Point2D.Double(-1, 0) : new Point2D.Double(1, 0);
        this.move(moveVector);
        if (getTravelDistance() > this.travelDistanceLimit) this.respawn();
    }

    /**
     * 基地からの移動距離を計算します。
     */
    private double getTravelDistance() {
        return this.getPosition().distance(this.base.getPosition());
    }

    @Override
    public double getVelocity() {
        return this.velocity;
    }
}
