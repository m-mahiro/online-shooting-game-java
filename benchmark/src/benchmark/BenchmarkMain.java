package benchmark;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 描画最適化（変更点1〜3）のビフォーアフターをまとめて計測するエントリーポイント。
 *
 * 実行方法は benchmark/README.md を参照。基本的には
 * benchmark/run_benchmark.sh（Linux/macOS）または benchmark/run_benchmark.bat（Windows）
 * から実行する。
 *
 * 引数: [projectRoot] [warmupIterations] [measuredIterations]
 *  - projectRoot: リポジトリのルートディレクトリ（省略時は現在のディレクトリ）
 *  - warmupIterations: ウォームアップ回数（省略時は200）
 *  - measuredIterations: 計測回数（省略時は2000）
 *
 * 結果は以下に出力される:
 *  - benchmark/results/&lt;timestamp&gt;-&lt;os&gt;.txt   : 1回分の実行結果（人間が読む用）
 *  - benchmark/results/history.csv              : 全実行結果を1行ずつ積み重ねたもの（Linux/Windows比較用）
 */
public class BenchmarkMain {

	public static void main(String[] args) throws Exception {
		File projectRoot = args.length > 0 ? new File(args[0]) : new File(".");
		int warmup = args.length > 1 ? Integer.parseInt(args[1]) : 200;
		int iterations = args.length > 2 ? Integer.parseInt(args[2]) : 2000;

		File resultsDir = new File(projectRoot, "benchmark/results");
		resultsDir.mkdirs();

		String osTag = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? "windows" : "linux";
		String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC).format(Instant.now());
		File resultFile = new File(resultsDir, timestamp + "-" + osTag + ".txt");
		File historyFile = new File(resultsDir, "history.csv");

		List<BenchResult> allResults = new ArrayList<>();

		try (FileOutputStream fileStream = new FileOutputStream(resultFile);
				PrintStream tee = new PrintStream(new TeeOutputStream(System.out, fileStream), true, "UTF-8")) {

			BenchUtil.printEnvironmentInfo(tee);
			tee.println("warmup=" + warmup + " iterations=" + iterations);
			tee.println();

			allResults.addAll(new BackgroundTextureBenchmark(projectRoot).run(warmup, iterations, tee));
			tee.println();

			allResults.addAll(new LayerBucketBenchmark().run(warmup, iterations, tee));
			tee.println();

			allResults.addAll(new GlyphCacheBenchmark().run(warmup, iterations, tee));
			tee.println();

			tee.println("結果を書き出しました: " + resultFile.getAbsolutePath());
			tee.println("履歴に追記しました:   " + historyFile.getAbsolutePath());
		}

		appendHistoryCsv(historyFile, timestamp, osTag, allResults);
	}

	private static void appendHistoryCsv(File historyFile, String timestamp, String osTag, List<BenchResult> results)
			throws IOException {
		boolean writeHeader = !historyFile.exists();

		try (Writer w = new FileWriter(historyFile, true)) {
			if (writeHeader) {
				w.write("timestamp_utc,os,os_name,os_version,os_arch,java_version,git_commit,"
						+ "benchmark,scenario,iterations,mean_ms,median_ms,p95_ms,min_ms,max_ms\n");
			}

			String osName = System.getProperty("os.name");
			String osVersion = System.getProperty("os.version");
			String osArch = System.getProperty("os.arch");
			String javaVersion = System.getProperty("java.version");
			String gitCommit = BenchUtil.gitCommit();

			for (BenchResult r : results) {
				BenchUtil.Stats s = r.stats;
				w.write(String.format(
						Locale.ROOT,
						"%s,%s,%s,%s,%s,%s,%s,%s,%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f\n",
						timestamp, osTag, csvSafe(osName), csvSafe(osVersion), csvSafe(osArch), csvSafe(javaVersion),
						csvSafe(gitCommit), r.benchmark, r.scenario, s.iterations, s.meanMs, s.medianMs, s.p95Ms,
						s.minMs, s.maxMs));
			}
		}
	}

	private static String csvSafe(String s) {
		if (s == null) return "";
		return s.replace(",", ";");
	}
}
