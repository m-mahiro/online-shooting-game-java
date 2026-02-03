package stage;

/**
 * ステージの現在の状態情報を提供するインターフェース。
 * 各チームの基地HP、戦車数、ゲーム終了状態、勝者などの情報を取得できる。
 */
public interface StageInfo {

	/**
	 * 赤チームの基地のヒットポイントを取得する。
	 *
	 * @return 赤チームの基地の現在のHP
	 */
	int getRedBaseHP();

	/**
	 * 青チームの基地のヒットポイントを取得する。
	 *
	 * @return 青チームの基地の現在のHP
	 */
	int getBlueBaseHP();

	/**
	 * 赤チームの基地の状態を取得する。
	 *
	 * @return 赤チームの基地の現在の状態
	 */
	Base.State getRedBaseState();

	/**
	 * 青チームの基地の状態を取得する。
	 *
	 * @return 青チームの基地の現在の状態
	 */
	Base.State getBlueBaseState();

	/**
	 * 赤チームの戦車数を取得する。
	 *
	 * @return 赤チームの現在の戦車数
	 */
	int getRedTank();

	/**
	 * 青チームの戦車数を取得する。
	 *
	 * @return 青チームの現在の戦車数
	 */
	int getBlueTank();

	/**
	 * ゲームが終了したかどうかを判定する。
	 *
	 * @return ゲームが終了している場合はtrue、継続中の場合はfalse
	 */
	boolean hasFinished();

	/**
	 * ゲームの勝者チームを取得する。
	 * ゲームが終了していない場合の動作は実装に依存する。
	 *
	 * @return 勝利したチーム
	 */
	Team getWinner();

}
