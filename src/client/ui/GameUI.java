package client.ui;

import client.SoundManager;
import stage.StageInfo;
import stage.Team;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

import static stage.Base.State.*;
import static stage.Team.*;

/**
 * ゲーム画面のUI管理クラス。
 * チーム情報カード、アイコン、テキストなどのUI要素を管理し、画面への描画を制御する。
 */
public class GameUI {

	// UIコンテンツ
	private final ArrayList<UIContent> contents = new ArrayList<>();

	// ステージ情報
	StageInfo info;

	// 効果音
	SoundManager sound = new SoundManager();

	private boolean hasDisplayedWinner = false;

	/**
	 * GameUIのコンストラクタ。
	 * 両チームの情報表示用UI要素（カード、アイコン、テキスト）を初期化する。
	 *
	 * @param info ステージ情報
	 * @param myTeam 自分のチーム
	 */
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

	/**
	 * UI要素を更新する。
	 * 有効期限切れの要素を削除し、各UI要素の状態を更新する。
	 * ゲーム終了時には勝利チームの表示とホイッスル音を再生する。
	 */
	public void update() {
		synchronized (this.contents) {
			Iterator<UIContent> iterator = this.contents.iterator();
			while(iterator.hasNext()) {
				UIContent content = iterator.next();
				if (content.isExpired()) iterator.remove();
				content.update();
			}
		}

		// ゲーム終了演出
		if (!hasDisplayedWinner && info.hasFinished()) {
			Team winner = info.getWinner();

			// 勝利チームを画面に表示
			WinnerInfo winnerInfo = new WinnerInfo(winner);
			contents.add(winnerInfo);
			hasDisplayedWinner = true;

			// 終了のホイッスル
			sound.playWhistle();
		}
	}

	/**
	 * すべてのUI要素を画面に描画する。
	 *
	 * @param graphics 描画に使用するGraphics2Dオブジェクト
	 * @param windowWidth ウィンドウの幅
	 * @param windowHeight ウィンドウの高さ
	 */
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {
		synchronized (this.contents) {
			for (UIContent content : this.contents) {
				content.draw(graphics, windowWidth, windowHeight);
			}
		}
	}
}
