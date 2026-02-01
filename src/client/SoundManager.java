package client;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SoundManager {

	// Clipプール
	private static final int POOL_SIZE = 5;
	private static List<Clip> bulletExplosionPool;
	private static List<Clip> createBlockPool;
	private static List<Clip> objectBreakPool;
	private static List<Clip> objectExplosionPool;
	private static List<Clip> shotPool;

	// MIDI
	private static Synthesizer synthesizer;
	private static MidiChannel midiChannel;

	static {
		bulletExplosionPool = loadPool("assets/sounds/bullet_explosion.wav");
		createBlockPool     = loadPool("assets/sounds/create_block.wav");
		objectBreakPool     = loadPool("assets/sounds/object_break.wav");
		objectExplosionPool = loadPool("assets/sounds/object_explosion.wav");
		shotPool            = loadPool("assets/sounds/shot.wav");

		try {
			synthesizer = MidiSystem.getSynthesizer();
			synthesizer.open();
			midiChannel = synthesizer.getChannels()[0];
			midiChannel.programChange(11); // Vibraphone
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private static List<Clip> loadPool(String path) {
		List<Clip> pool = new ArrayList<>();

		for (int i = 0; i < POOL_SIZE; i++) {
			Clip clip = loadClip(path);
			if (clip != null) {
				pool.add(clip);
			}
		}
		return pool;
	}

	/**
	 *
	 * @param path
	 * @return
	 */
	private static Clip loadClip(String path) {
		try {
			URL url = SoundManager.class.getResource(path);
			if (url == null) {
				System.err.println("音声ファイルが見つかりません: " + path);
				return null;
			}

			AudioInputStream stream = AudioSystem.getAudioInputStream(url);
			AudioFormat format = stream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);

			Clip clip = (Clip) AudioSystem.getLine(info);
			clip.open(stream);
			return clip;

		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
			return null;
		}
	}


	/**
	 *
 	 * @param pool
	 */
	private static void playFromPool(List<Clip> pool) {
		if (pool == null || pool.isEmpty()) {
			return;
		}

		for (Clip clip : pool) {
			if (!clip.isRunning()) {
				clip.setFramePosition(0);
				clip.start();
				return;
			}
		}

		// 全部再生中なら先頭を使い回す
		Clip clip = pool.get(0);
		clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}

	public void bulletExplosion() {
		playFromPool(bulletExplosionPool);
	}

	public void createBlock() {
		playFromPool(createBlockPool);
	}

	public void objectBreak() {
		playFromPool(objectBreakPool);
	}

	public void objectExplosion() {
		playFromPool(objectExplosionPool);
	}

	public void shootGun() {
		playFromPool(shotPool);
	}

	public void playClearChord() {
		new Thread(() -> {
			try {
				int velocity = 90;

				// A4 = 440Hz を含むオクターブ
				int C5 = 72;
				int G5 = 79;

				midiChannel.noteOn(C5, velocity);
				Thread.sleep(250);
				midiChannel.noteOff(C5);

				midiChannel.noteOn(G5, velocity);
				Thread.sleep(400);
				midiChannel.noteOff(G5);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}
}
