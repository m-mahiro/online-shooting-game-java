package benchmark;

/**
 * 1つの計測結果（どのベンチマークの、どのシナリオの結果か、という識別情報付き）。
 * CSVへの出力や最終的なサマリー表示のために使う。
 */
public final class BenchResult {
	public final String benchmark; // 例: "1-background-texture"
	public final String scenario;  // 例: "legacy" / "current"
	public final BenchUtil.Stats stats;

	public BenchResult(String benchmark, String scenario, BenchUtil.Stats stats) {
		this.benchmark = benchmark;
		this.scenario = scenario;
		this.stats = stats;
	}
}
