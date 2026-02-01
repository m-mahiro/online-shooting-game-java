package client.ui;

import stage.StageInfo;
import stage.Team;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

public class TeamInfoCard implements UIContent {


	// どちらのチーム用の画面を表示すれば良いか
	private final Team team;

	// 表示する情報の提供元
	private StageInfo info;

	// 画像リソース
	private double imageScale = 0.5;
	private BufferedImage cardImage;

	public TeamInfoCard(Team team, StageInfo info) {
		this.team = team;
		this.info = info;
		try {
			switch (team) {
				case RED:
					cardImage = ImageIO.read(Objects.requireNonNull(TeamInfoCard.class.getResource("../assets/side_card_red.png")));
					break;
				case BLUE:
					cardImage = ImageIO.read(Objects.requireNonNull(TeamInfoCard.class.getResource("../assets/side_card_blue.png")));
					break;
				default:
					throw new RuntimeException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update() {

	}

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
}
