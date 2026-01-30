package client.ui;

import client.SoundManager;
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
		BaseHP redBase = new BaseHP(Team.RED, info);
		BaseHP blueBase = new BaseHP(Team.BLUE, info);
		contents.add(redBase);
		contents.add(blueBase);
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
