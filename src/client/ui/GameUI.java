package client.ui;

import client.SoundManager;
import stage.StageInfo;
import stage.Team;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import static stage.Base.State.*;
import static stage.Team.*;

public class GameUI {

	// UIコンテンツ
	private final ArrayList<UIContent> contents = new ArrayList<>();

	// ステージ情報
	StageInfo info;

	// 効果音
	SoundManager sound = new SoundManager();

	private boolean hasDisplayedWinner = false;

	public GameUI(StageInfo info, Team myTeam) {
		this.info = info;
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
			Iterator<UIContent> iterator = this.contents.iterator();
			while(iterator.hasNext()) {
				UIContent content = iterator.next();
				if (content.isExpired()) iterator.remove();
				content.update();
			}
		}

		if (hasDisplayedWinner) return;
		boolean redAllDead = info.getRedBaseState() == RUINS && info.getRemainRedTank() == 0;
		boolean blueAllDead = info.getBlueBaseState() == RUINS && info.getRemainBlueTank() == 0;
		if (redAllDead || blueAllDead) {
			Team winner = redAllDead ? BLUE : RED;
			WinnerInfo winnerInfo = new WinnerInfo(winner);
			contents.add(winnerInfo);
			hasDisplayedWinner = true;

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
