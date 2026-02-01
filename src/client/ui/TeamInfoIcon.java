package client.ui;

import stage.Base;
import stage.StageInfo;
import stage.Team;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Objects;

import static stage.Base.State.RUINS;
import static stage.Team.RED;

public class TeamInfoIcon implements UIContent {

	// どちらのチームの情報を表示するのか?
	private Team team;

	// 表示する情報の提供元
	private StageInfo info;

	// 表示サイズ調整用
	private double baseScale = 0.6;
	private double tankScale = 1.3;

	// 画像リソース
	private static Image normalRedBaseImage, brokenRedBaseImage, redBaseRuinsImage;
	private static Image normalBlueBaseImage, brokenBlueBaseImage, blueBaseRuinsImage;
	private static Image redTankImage, blueTankImage;

	static {
		try {
			// REDチームの基地の画像
			normalRedBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/hp_base_red_normal.png")));
			brokenRedBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/hp_base_red_broken.png")));
			redBaseRuinsImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/hp_base_red_ruins.png")));

			// BLUEチームの基地の画像
			normalBlueBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/hp_base_blue_normal.png")));
			brokenBlueBaseImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/hp_base_blue_broken.png")));
			blueBaseRuinsImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/hp_base_blue_ruins.png")));

			// 戦車の画像
			redTankImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/tank_red.png")));
			blueTankImage = ImageIO.read(Objects.requireNonNull(TeamInfoIcon.class.getResource("../assets/tank_blue.png")));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public TeamInfoIcon(Team team, StageInfo info) {
		this.info = info;
		this.team = team;
	}

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

	private boolean isRuins() {
		return getBaseState() == RUINS;
	}

	private Base.State getBaseState() {
		boolean isRed = this.team == RED;
		return isRed ? info.getRedBaseState() : info.getBlueBaseState();
	}

	// ============================= UIContentインターフェースのメソッド =============================

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {

		boolean isRed = this.team == RED;

		// 画像を用意
		Image image = this.getImage();

		// 画像の位置を計算
		double scale, x, y;
		if (isRuins()) {
			// 戦車を表示する
			scale = tankScale;
			x = isRed ? windowWidth - scale * image.getWidth(null) - 10 : 10;
			y = windowHeight - scale * image.getHeight(null) - 10;
		} else {
			// 基地を表示する
			scale = baseScale;
			x = isRed ? windowWidth - scale * image.getWidth(null) - 10 : 10;
			y = windowHeight - scale * image.getHeight(null);
		}

		// 画像の描画
		AffineTransform baseTrans = new AffineTransform();
		baseTrans.translate(x, y);
		baseTrans.scale(scale, scale);
		graphics.drawImage(image, baseTrans, null);
	}
}
