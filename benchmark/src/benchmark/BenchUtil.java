package benchmark;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * ベンチマーク計測用のユーティリティ。
 * ウォームアップ付きで計測し、mean/median/p95などの統計値を算出する。
 */
public final class BenchUtil {

	private BenchUtil() {}

	/** 1回の計測セットの統計値（単位: ミリ秒）。 */
	public static final class Stats {
		public final double meanMs, medianMs, p95Ms, minMs, maxMs;
		public final int iterations;

		private Stats(double[] sortedMs) {
			this.iterations = sortedMs.length;
			this.minMs = sortedMs[0];
			this.maxMs = sortedMs[sortedMs.length - 1];

			double sum = 0;
			for (double v : sortedMs) sum += v;
			this.meanMs = sum / sortedMs.length;

			this.medianMs = percentile(sortedMs, 50);
			this.p95Ms = percentile(sortedMs, 95);
		}

		private static double percentile(double[] sorted, double p) {
			if (sorted.length == 1) return sorted[0];
			double idx = (p / 100.0) * (sorted.length - 1);
			int lo = (int) Math.floor(idx);
			int hi = (int) Math.ceil(idx);
			if (lo == hi) return sorted[lo];
			double frac = idx - lo;
			return sorted[lo] + (sorted[hi] - sorted[lo]) * frac;
		}

		@Override
		public String toString() {
			return String.format(
					"mean=%.4fms median=%.4fms p95=%.4fms min=%.4fms max=%.4fms (n=%d)",
					meanMs, medianMs, p95Ms, minMs, maxMs, iterations);
		}
	}

	/**
	 * taskをwarmupIterations回実行してJITを安定させた後、measuredIterations回計測する。
	 * 1回ごとの実行時間（ミリ秒）を集計し、{@link Stats}として返す。
	 */
	public static Stats measure(int warmupIterations, int measuredIterations, Runnable task) {
		for (int i = 0; i < warmupIterations; i++) task.run();

		double[] samples = new double[measuredIterations];
		for (int i = 0; i < measuredIterations; i++) {
			long start = System.nanoTime();
			task.run();
			long end = System.nanoTime();
			samples[i] = (end - start) / 1_000_000.0;
		}
		Arrays.sort(samples);
		return new Stats(samples);
	}

	/** 実行環境の情報を出力する。Linux/Windowsの違いを後で比較するために記録しておく。 */
	public static void printEnvironmentInfo(PrintStream out) {
		out.println("==== Environment ====");
		out.println("timestamp (UTC): " + java.time.Instant.now());
		out.println("os.name: " + System.getProperty("os.name"));
		out.println("os.version: " + System.getProperty("os.version"));
		out.println("os.arch: " + System.getProperty("os.arch"));
		out.println("java.version: " + System.getProperty("java.version"));
		out.println("java.vendor: " + System.getProperty("java.vendor"));
		out.println("available processors: " + Runtime.getRuntime().availableProcessors());
		out.println("max heap (MB): " + (Runtime.getRuntime().maxMemory() / 1024 / 1024));
		out.println("java.awt.headless: " + System.getProperty("java.awt.headless"));
		out.println("git commit: " + gitCommit());
		out.println("======================");
	}

	/** 可能ならgitの短縮コミットハッシュを返す。取得できない場合は"unknown"。 */
	public static String gitCommit() {
		try {
			Process p = new ProcessBuilder("git", "rev-parse", "--short", "HEAD")
					.redirectErrorStream(true)
					.start();
			try (java.io.BufferedReader reader = new java.io.BufferedReader(
					new java.io.InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
				String line = reader.readLine();
				p.waitFor();
				return (line == null || line.isEmpty()) ? "unknown" : line.trim();
			}
		} catch (Exception e) {
			return "unknown";
		}
	}
}
