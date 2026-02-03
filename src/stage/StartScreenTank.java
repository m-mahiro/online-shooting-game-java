package stage;

import java.awt.geom.Point2D;

import static stage.Team.RED;

public class StartScreenTank extends Tank {

    private final Base base;
    private final double travelDistanceLimit;

    /**
     * 新しい戦車オブジェクトを生成します。
     * 戦車は指定された基地の位置にスポーンします。
     *
     * @param base 戦車が所属し、スポーンする基地
     */
    public StartScreenTank(Base base, double x, double y, double travelDistanceLimit) {
        super(base);
        this.base = base;
        this.travelDistanceLimit = travelDistanceLimit;
        this.setPosition(new Point2D.Double(x, y));
    }

    @Override
    public void update() {
        super.update();
        Point2D.Double moveVector = this.getTeam() == RED ? new Point2D.Double(-1, 0) : new Point2D.Double(1, 0);
        this.move(moveVector);
        if (getTravelDistance() > this.travelDistanceLimit) this.respawn();
    }

    private double getTravelDistance() {
        return this.getPosition().distance(this.base.getPosition());
    }
}
