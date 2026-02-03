package client.ui;

import java.awt.*;

public class SlideShowText implements UIContent {
	private final String[] texts;
	private final double xRate;
	private final double yRate;
	private final int durationPerText;
	private final Color fillColor;

	private int currentIndex = 0;
	private int frameCount = 0;

	public SlideShowText(String[] texts, double xRate, double yRate,
	                     int durationPerText, Color fillColor) {
		this.texts = texts;
		this.xRate = xRate;
		this.yRate = yRate;
		this.durationPerText = durationPerText;
		this.fillColor = fillColor;
	}

	@Override
	public void update() {
		frameCount++;
		if (frameCount >= durationPerText) {
			frameCount = 0;
			currentIndex = (currentIndex + 1) % texts.length;
		}
	}

	@Override
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {
		String currentText = texts[currentIndex];

		// フォントサイズを6倍にする
		Font originalFont = graphics.getFont();
		Font largerFont = originalFont.deriveFont(originalFont.getSize() * 6.0f);
		graphics.setFont(largerFont);

		FontMetrics fm = graphics.getFontMetrics();
		int textWidth = fm.stringWidth(currentText);
		int textHeight = fm.getHeight();

		int x = (int) (windowWidth * xRate - textWidth / 2);
		int y = (int) (windowHeight * yRate + textHeight / 2 - fm.getDescent());

		graphics.setColor(fillColor);
		graphics.drawString(currentText, x, y);

		// フォントを元に戻す
		graphics.setFont(originalFont);
	}

	@Override
	public boolean isExpired() {
		return false;
	}
}
