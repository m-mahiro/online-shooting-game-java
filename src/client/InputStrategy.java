package client;

import stage.GameStage;
import stage.Tank;

import java.awt.geom.AffineTransform;

/**
 * ゲームモードに応じた入力処理の戦略を定義するインターフェース。
 * 通常モードと練習モードで異なる入力制限を実現するために使用します。
 */
public interface InputStrategy {

    /**
     * 入力を処理し、戦車を操作します。
     * 場面（JPanel）によって受け付ける入力と受け付けない入力を切り替えることができます。
     *
     * @param myTank 操作対象の戦車
     * @param canvasTransform キャンバスの座標変換
     * @param stage ゲームステージ
     */
    void handleInput(Tank myTank, AffineTransform canvasTransform, GameStage stage);

    /**
     * フレーム更新時の処理を行います。
     */
    void onFrameUpdate();
}
