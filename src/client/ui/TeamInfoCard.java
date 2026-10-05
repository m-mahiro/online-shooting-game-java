package client.ui;

import stage.StageInfo;
import stage.Team;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

/**
 * チーム情報カードを表示するクラス。
 * 画面下部の左右にチームカラーの背景画像を表示する。
 */
public class TeamInfoCard implements UIContent {


	// どちらのチーム用の画面を表示すれば良いか
	private final Team team;

	// 表示する情報の提供元
	private StageInfo info;

	// 画像リソース
	private double imageScale = 0.5;
	private final BufferedImage cardImage;

	// 画像リソース（他のクラスと同様、起動時に一度だけ読み込んで共有する）
	private static final BufferedImage redCardImage, blueCardImage;

	static {
		try {
			redCardImage = util.ImageUtil.load(Objects.requireNonNull(TeamInfoCard.class.getResource("../assets/side_card_red.png")));
			blueCardImage = util.ImageUtil.load(Objects.requireNonNull(TeamInfoCard.class.getResource("../assets/side_card_blue.png")));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * TeamInfoCardのコンストラクタ。
	 * 指定されたチームのカード画像を読み込む。
	 *
	 * @param team 表示するチーム
	 * @param info ステージ情報
	 */
	public TeamInfoCard(Team team, StageInfo info) {
		this.team = team;
		this.info = info;
		switch (team) {
			case RED:
				cardImage = redCardImage;
				break;
			case BLUE:
				cardImage = blueCardImage;
				break;
			default:
				throw new RuntimeException();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update() {

	}

	/**
	 * {@inheritDoc}
	 * チーム情報カードを画面下部の左右に描画する。
	 */
	@Override
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {

		// カードの位置を計算
		boolean isRed = this.team == RED;
		double cardX = isRed ? windowWidth - imageScale * cardImage.getWidth(null) : 0;
		double cardY = windowHeight - imageScale * cardImage.getHeight(null);

		// カードを描画
		AffineTransform tableTrans = new AffineTransform();
		tableTrans.translate(cardX, cardY);
		tableTrans.scale(imageScale, imageScale);
		graphics.drawImage(this.cardImage, tableTrans, null);

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
