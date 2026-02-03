package client.ui;

import java.awt.*;

public class HowToPlaySlideShow implements GameUI {

	UIContent content;

	public HowToPlaySlideShow() {
		String[] slide = new String[10];
		slide[0] = "▼が付いているのが自分の洗車です。";
		slide[1] = "WASDで移動ができます。";
		slide[2] = "マウスの左クリックで弾を発射できます。";
		slide[3] = "マウスポインターのある方へ飛んでいきます。";
		slide[4] = "弾を発射して相手の戦車を破壊することができます。";
		slide[5] = "しかし破壊された戦車は\nスポーン地点から復活してしまいます。";
		slide[6] = "相手を復活させないためには\n相手のスポーン地点を破壊する必要があります。";
		slide[7] = "逆にゲームに勝つためには\n自分のスポーン地点を守る必要があります。";
		slide[8] = "マウスの右クックでブロックを配置できます。";
		slide[9] = "ブロックをたくさん置いて\nスポーン地点を守りましょう。";
		this.content = new SlideShowText(slide, 0.5, 0.2, 240, new Color(233, 133, 50));
	}

	@Override
	public void update() {
		content.update();
	}

	@Override
	public void draw(Graphics2D graphics2D, int windowWidth, int windowHeight) {
		content.draw(graphics2D, windowWidth, windowHeight);
	}
}
