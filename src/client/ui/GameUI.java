package client.ui;

import stage.StageInfo;
import stage.Team;

import java.awt.*;
import java.util.ArrayList;

public class GameUI {

	// UIコンテンツ
	private final ArrayList<UIContent> contents = new ArrayList<>();

	// ステージ情報
	StageInfo info;

	public GameUI(StageInfo info) {
		TeamInfoCard redCard = new TeamInfoCard(Team.RED, info);
		TeamInfoCard blueCard = new TeamInfoCard(Team.BLUE, info);
		TeamInfoIcon redIcon = new TeamInfoIcon(Team.RED, info);
		TeamInfoIcon blueIcon = new TeamInfoIcon(Team.BLUE, info);
		TeamInfoText redText = new TeamInfoText(Team.RED, info);
		TeamInfoText blueText = new TeamInfoText(Team.BLUE, info);
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
