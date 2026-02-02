package client.ui;

import stage.Base;
import stage.StageInfo;
import stage.Team;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Objects;

import static stage.Base.State.*;
import static stage.Team.*;

/**
 * チーム情報アイコンを表示するクラス。
 * 基地の状態（通常、破損、廃墟）に応じたアイコンまたは戦車アイコンを表示する。
 */
public class TeamInfoIcon implements UIContent {

	// どちらのチームの情報を表示するのか?
	private Team team;

	// 表示する情報の提供元
	private StageInfo info;

	// 画像リソース
	private static Image normalRedBaseImage, brokenRedBaseImage;
	private static Image normalBlueBaseImage, brokenBlueBaseImage;
	private static Image redTankImage, blueTankImage;

	static {
		try {
			// REDチームの基地の画像
			normalRedBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/info_team_base_red_normal.png")));
			brokenRedBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/info_team_base_red_broken.png")));

			// BLUEチームの基地の画像
			normalBlueBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/info_team_base_blue_normal.png")));
			brokenBlueBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/info_team_base_blue_broken.png")));

			// 戦車の画像
			redTankImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/tank_red.png")));
			blueTankImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/tank_blue.png")));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * TeamInfoIconのコンストラクタ。
	 *
	 * @param team 表示するチーム
	 * @param info ステージ情報
	 */
	public TeamInfoIcon(Team team, StageInfo info) {
		this.info = info;
		this.team = team;
	}

	/**
	 * 基地の状態に応じた画像を取得する。
	 *
	 * @return 基地の状態に対応する画像（通常、破損、廃墟時は戦車）
	 */
	private Image getImage() {
		boolean isRed = this.team == RED;
		switch (getBaseState()) {
			case NORMAL:
				return isRed ? normalRedBaseImage : normalBlueBaseImage;
			case BROKEN:
				return isRed ? brokenRedBaseImage : brokenBlueBaseImage;
			case RUINS:
				return isRed ? redTankImage : blueTankImage;
			default:
				throw new IllegalStateException("Unexpected value: " + getBaseState());
		}
	}

	/**
	 * 基地が廃墟状態かどうかを判定する。
	 *
	 * @return 廃墟状態の場合true、そうでない場合false
	 */
	private boolean isRuins() {
		return getBaseState() == RUINS;
	}

	/**
	 * 基地の状態を取得する。
	 *
	 * @return 基地の状態
	 */
	private Base.State getBaseState() {
		boolean isRed = this.team == RED;
		return isRed ? info.getRedBaseState() : info.getBlueBaseState();
	}

	// ============================= UIContentインターフェースのメソッド =============================

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update() {
	}

	/**
	 * {@inheritDoc}
	 * 基地の状態に応じたアイコンを画面下部の左右に描画する。
	 */
	@Override
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {

		boolean isRed = this.team == RED;

		// 画像を用意
		Image image = this.getImage();

		// 画像の位置を計算
		double scale, x, y;
		if (isRuins()) {
			// 戦車を表示する
			scale = 1.3;
			x = isRed ? windowWidth - scale * image.getWidth(null) - 10 : 10;
			y = windowHeight - scale * image.getHeight(null) - 10;
		} else {
			// 基地を表示する
			// 表示サイズ調整用
			scale = 0.6;
			x = isRed ? windowWidth - scale * image.getWidth(null) - 10 : 10;
			y = windowHeight - scale * image.getHeight(null);
		}

		// 画像の描画
		AffineTransform baseTrans = new AffineTransform();
		baseTrans.translate(x, y);
		baseTrans.scale(scale, scale);
		graphics.drawImage(image, baseTrans, null);
	}

	/**
	 * {@inheritDoc}
	 * このUI要素は常に表示されるため、常にfalseを返す。
	 */
	@Override
	public boolean isExpired() {
		return false;
	}
}
