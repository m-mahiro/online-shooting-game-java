package client;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * ゲームの入力を処理するインターフェース。
 * プレイヤーの操作を検知し、ゲームエンジンに提供する。
 */
public interface InputHandler {

	/**
	 * プレイヤーの移動ベクトルを取得する。
	 *
	 * @param canvasTransform キャンバスの変換行列
	 * @return 移動ベクトル（-1.0 ～ 1.0の範囲）
	 */
	Point2D.Double getMoveVector(AffineTransform canvasTransform);

	/**
	 * プレイヤーが照準を合わせている座標を取得する。
	 *
	 * @param canvasTransform キャンバスの変換行列
	 * @return 照準座標（ゲーム世界座標系）
	 */
	Point2D.Double getAimedCoordinate(AffineTransform canvasTransform);

	/**
	 * 弾丸を発射するかどうかを判定する。
	 *
	 * @return 発射する場合はtrue、しない場合はfalse
	 */
	boolean shootBullet();

	/**
	 * エネルギーチャージを開始するかどうかを判定する。
	 *
	 * @return チャージを開始する場合はtrue、しない場合はfalse
	 */
	boolean startEnergyCharge();

	/**
	 * エネルギーチャージを完了するかどうかを判定する。
	 *
	 * @return チャージを完了する場合はtrue、しない場合はfalse
	 */
	boolean finishEnergyCharge();

	/**
	 * ブロックを生成するかどうかを判定する。
	 *
	 * @return ブロックを生成する場合はtrue、しない場合はfalse
	 */
	boolean createBlock();

	/**
	 * ズーム量を取得する。
	 *
	 * @return ズーム量（正の値でズームイン、負の値でズームアウト）
	 */
	int getZoomAmount();

	/**
	 * フレーム更新時に呼ばれる。
	 * 入力状態の更新処理を行う。
	 */
	void onFrameUpdate();

}
