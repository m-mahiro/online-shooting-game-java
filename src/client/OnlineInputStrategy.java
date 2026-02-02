package client;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * 通常モード（オンライン対戦）用の入力戦略。
 * すべての入力操作を許可します。
 */
public class OnlineInputStrategy implements InputStrategy {

    private final InputHandler inputHandler;

    /**
     * コンストラクタ。
     *
     * @param inputHandler ユーザー入力を処理するInputHandler
     */
    public OnlineInputStrategy(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    @Override
    public Point2D.Double getMoveVector(AffineTransform canvasTransform) {
        return inputHandler.getMoveVector(canvasTransform);
    }

    @Override
    public Point2D.Double getAimedCoordinate(AffineTransform canvasTransform) {
        return inputHandler.getAimedCoordinate(canvasTransform);
    }

    @Override
    public boolean shootBullet() {
        return inputHandler.shootBullet();
    }

    @Override
    public boolean startEnergyCharge() {
        return inputHandler.startEnergyCharge();
    }

    @Override
    public boolean finishEnergyCharge() {
        return inputHandler.finishEnergyCharge();
    }

    @Override
    public boolean createBlock() {
        return inputHandler.createBlock();
    }

    @Override
    public int getZoomAmount() {
        return inputHandler.getZoomAmount();
    }

    @Override
    public void onFrameUpdate() {
        inputHandler.onFrameUpdate();
    }
}
