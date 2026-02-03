package client;

import java.awt.geom.Point2D;

/**
 * ネットワーク通信の戦略を定義するインターフェース。
 * ゲームの入力情報をサーバに送信する処理を抽象化します。
 */
public interface NetworkStrategy {

    /**
     * 照準座標をサーバに送信します。
     *
     * @param tankID 戦車のID
     * @param coordinate 照準座標
     */
    void aimAt(int tankID, Point2D.Double coordinate);

    /**
     * 銃の発射をサーバに送信します。
     *
     * @param tankID 戦車のID
     */
    void shootGun(int tankID);

    /**
     * エネルギーチャージの開始をサーバに送信します。
     *
     * @param tankID 戦車のID
     */
    void startCharge(int tankID);

    /**
     * エネルギーチャージの終了をサーバに送信します。
     *
     * @param tankID 戦車のID
     */
    void finishCharge(int tankID);

    /**
     * ブロック生成をサーバに送信します。
     *
     * @param tankID 戦車のID
     */
    void createBlock(int tankID);
}
