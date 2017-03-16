package lancs.dividend.oclBenchMapper.benchmark;

import java.io.Serializable;

public class BenchExecutionResults implements Serializable {
	private static final long serialVersionUID = -1546465187190799926L;
	
	public final double energyJ;
	public final double runtimeMS;
	public final double tradeoff; // the smaller the better
	
	public BenchExecutionResults(double e, double r) {
		energyJ = e;
		runtimeMS = r;
		tradeoff = energyJ * runtimeMS;
	}
}