package client.ui;

import stage.StageInfo;

import java.awt.*;
import java.util.ArrayList;

import static stage.Team.*;

public class GameUI {

	// UIコンテンツ
	private final ArrayList<UIContent> contents = new ArrayList<>();

	// ステージ情報
	StageInfo info;

	public GameUI(StageInfo info) {
		TeamInfoCard redCard = new TeamInfoCard(RED, info);
		TeamInfoCard blueCard = new TeamInfoCard(BLUE, info);
		TeamInfoIcon redIcon = new TeamInfoIcon(RED, info);
		TeamInfoIcon blueIcon = new TeamInfoIcon(BLUE, info);
		TeamInfoText redText = new TeamInfoText(RED, info);
		TeamInfoText blueText = new TeamInfoText(BLUE, info);
		contents.add(redCard);
		contents.add(blueCard);
		contents.add(redIcon);
		contents.add(blueIcon);
		contents.add(redText);
		contents.add(blueText);
	}

	public void update() {
		synchronized (this.contents) {
			for (UIContent content : this.contents) {
				content.update();
			}
		}

	}

	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {
		synchronized (this.contents) {
			for (UIContent content : this.contents) {
				content.draw(graphics, windowWidth, windowHeight);
			}
		}
	}
}
