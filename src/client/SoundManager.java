package client;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {

	public static Clip bulletExplosion, createBlock, objectBreak, objectExplosion, shot;

	static {
		bulletExplosion = load("assets/sounds/bullet_explosion.wav");
		createBlock = load("assets/sounds/create_block.wav");
		objectBreak = load("assets/sounds/object_break.wav");
		objectExplosion = load("assets/sounds/object_explosion.wav");
		shot = load("assets/sounds/shot.wav");
	}

	private static Clip load(String path) {
		try {
			URL url = SoundManager.class.getResource(path);
			if (url == null) {
				System.err.println("音声ファイルが見つかりません: " + path);
				return null;
			}

			AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);
			AudioFormat format = audioStream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);

			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(audioStream);
			return clip;

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static void play(Clip clip) {
		if (clip != null) {
			// 連続再生のために停止＆巻き戻し
			if (clip.isRunning()) {
				clip.stop();
			}
			clip.setFramePosition(0);
			clip.start();
		}
	}

	public void bulletExplosion() {
		play(bulletExplosion);
	}

	public void createBlock() {
		play(createBlock);
	}

	public void objectBreak() {
		play(objectBreak);
	}

	public void objectExplosion() {
		play(objectExplosion);
	}

	public void shootGun() {
		play(shot);
	}


}