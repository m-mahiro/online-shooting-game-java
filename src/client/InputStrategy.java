package client;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * ゲームモードに応じた入力処理の戦略を定義するインターフェース。
 * 通常モードと練習モードで異なる入力制限を実現するために使用します。
 */
public interface InputStrategy {

    /**
     * プレイヤーの移動ベクトルを取得します。
     *
     * @param canvasTransform キャンバスの座標変換
     * @return 移動ベクトル
     */
    Point2D.Double getMotionDirection(AffineTransform canvasTransform);

    /**
     * プレイヤーの照準座標を取得します。
     *
     * @param canvasTransform キャンバスの座標変換
     * @return 照準座標
     */
    Point2D.Double getAimedCoordinate(AffineTransform canvasTransform);

    /**
     * 弾を発射するかどうかを判定します。
     *
     * @return 発射する場合true
     */
    boolean shootBullet();

    /**
     * エネルギーチャージを開始するかどうかを判定します。
     *
     * @return チャージを開始する場合true
     */
    boolean startEnergyCharge();

    /**
     * エネルギーチャージを終了するかどうかを判定します。
     *
     * @return チャージを終了する場合true
     */
    boolean finishEnergyCharge();

    /**
     * ブロック生成が可能かどうかを判定します。
     *
     * @return ブロック生成が可能な場合true
     */
    boolean createBlock();

    /**
     * カメラのズーム量を取得します。
     *
     * @return ズーム量
     */
    int getZoomAmount();

    /**
     * フレーム更新時の処理を行います。
     */
    void onFrameUpdate();
}
