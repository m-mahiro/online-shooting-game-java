package client.ui;

import stage.GameEngine;
import stage.Team;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static stage.Team.*;

public class WinnerInfo implements UIContent {

	private final Team team;
	private BufferedImage image;

	private int animationCounter = 0;
	private int vanishingPoint = 800;
	private double imageScale = 0.6;
	private boolean isExpired = false;

	private static final int MOTION_DURATION = GameEngine.FPS * 4;

	public WinnerInfo(Team team) {
		this.team = team;
		try {
			switch (team) {
				case BLUE:
					this.image = ImageIO.read(Objects.requireNonNull(WinnerInfo.class.getResource("../assets/winner_blue.png")));
					break;
				case RED:this.image = ImageIO.read(Objects.requireNonNull(WinnerInfo.class.getResource("../assets/winner_red.png")));
					break;
				default:
					throw new RuntimeException();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double getScale(double x) {
		double h1 = 639;   // 図形の左端の長さ
		double h2 = 3085;  // 図形の右端の長さ
		double w = 1041;   // 図形の下辺の長さ
		return (h2 - h1) / h2 * (-x / w) * imageScale;
	}


	private double getX(double windowWidth) {
		double passageTimeRate = (double) animationCounter / MOTION_DURATION;
		double coefficient = windowWidth * 4;
		return coefficient * Math.pow(passageTimeRate - 0.5, 3) - vanishingPoint;
	}

	@Override
	public void update() {
		animationCounter++;
		if (animationCounter > MOTION_DURATION) isExpired = true;
	}

	@Override
	public void draw(Graphics2D graphics, int windowWidth, int windowHeight) {
		boolean isRed = team == RED;
		double x = getX(windowWidth);
		double scale = getScale(x);
		double rotate = Math.PI / 10;

		if (scale < 0) {
			this.isExpired = true;
			return;
		}

		AffineTransform trans = new AffineTransform();
		trans.translate(windowWidth / 2.0, windowHeight / 3.0);
		trans.rotate(isRed ? rotate : -rotate);
		trans.translate(isRed ? vanishingPoint : -vanishingPoint, 0);
		trans.translate(isRed ? x : -x , 0);
		trans.scale(scale, scale);
		trans.translate(-this.image.getWidth() / 2.0, -this.image.getHeight() / 2.0);
		graphics.drawImage(image, trans, null);
	}

	@Override
	public boolean isExpired() {
		return isExpired;
	}
}
