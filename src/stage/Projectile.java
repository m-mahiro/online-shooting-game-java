package stage;

public interface Projectile {

	/**
	 * プロジェクタイルが与えるダメージ能力を取得します。
	 * @return プロジェクタイルのダメージ量
	 */
	int getDamageAbility();

	/**
	 * プロジェクタイルが所属するチームを取得します。
	 * @return プロジェクタイルのチーム
	 */
	Team getTeam();

}
