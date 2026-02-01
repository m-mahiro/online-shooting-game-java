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
	 * 指定された音声ファイルから、複数の {@link Clip} を生成し、
	 * 同一効果音を重ねて再生するための Clip プールを作成する。
	 *
	 * @param path クラスパス上の音声ファイルへのパス
	 * @return 生成された {@link Clip} のリスト（Clipプール）
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
	 * 指定された音声ファイルを読み込み、再生可能な {@link Clip} を生成する。
	 *
	 * @param path クラスパス上の音声ファイルへのパス
	 * @return 初期化済みの {@link Clip}。読み込みに失敗した場合は {@code null}
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
	 * Clipプールから再生可能な {@link Clip} を選択して再生する。
	 * すべての Clip が再生中の場合は、先頭の Clip を停止して再利用する。
	 *
	 * @param pool 再生対象となる {@link Clip} のプール
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

	public void playBell() {
		new Thread(() -> {
			try {
				int velocity = 90;

				// A4 = 440Hz を含むオクターブ
				int C5 = 72;
				int G5 = 79;

				midiChannel.noteOn(C5, velocity);
				Thread.sleep(60);
				midiChannel.noteOn(G5, velocity);
				Thread.sleep(400);
				midiChannel.noteOff(C5);
				midiChannel.noteOff(G5);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Plays a victory sound as a longer sequence of single notes
	 * that ascend to convey triumphant feeling.
	 */
	public void playVictorySound() {
		new Thread(() -> {
			try {
				int velocity = 90;

				int[] victoryNotes = {
						72, // C5
						74, // D5
						76, // E5
						79, // G5
						72, // C5
						74, // D5
						76, // E5
						79, // G5
						84  // C6
				};

				for (int note : victoryNotes) {
					midiChannel.noteOn(note, velocity);
					Thread.sleep(150); // note duration
					midiChannel.noteOff(note);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Plays a game over sound as a longer sequence of single notes
	 * that descend or feel denser to convey somber feeling.
	 */
	public void playGameOverSound() {
		new Thread(() -> {
			try {
				int velocity = 85;

				// 音階: A4, G4, E4, C4
				int[] gameOverNotes = {
						69, // A4
						67, // G4
						64, // E4
						60  // C4
				};

				for (int note : gameOverNotes) {
					midiChannel.noteOn(note, velocity);
					Thread.sleep(220); // note duration
					midiChannel.noteOff(note);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}


}
