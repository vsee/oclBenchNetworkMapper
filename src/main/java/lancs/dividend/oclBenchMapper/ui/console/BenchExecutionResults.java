package lancs.dividend.oclBenchMapper.ui.console;

public class BenchExecutionResults {
	public final double energyJ;
	public final double runtimeMS;
	public final double tradeoff; // the smaller the better
	
	public BenchExecutionResults(double e, double r) {
		energyJ = e;
		runtimeMS = r;
		tradeoff = energyJ * runtimeMS;
	}
}