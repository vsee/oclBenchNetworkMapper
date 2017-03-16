package lancs.dividend.oclBenchMapper.benchmark;

import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;

public class BenchFullExecutionResults extends BenchExecutionResults {
	
	private static final long serialVersionUID = -2934741438342792956L;
	
	public final Benchmark bin;
	public final DataSetSize dset;
	public final ExecutionDevice dev;
	public final String stdOut;
	
	public BenchFullExecutionResults(Benchmark bin, DataSetSize dset, ExecutionDevice dev, double e, double r, String stdOut) {
		super(e, r);
		this.stdOut = stdOut;
		this.bin = bin;
		this.dset = dset;
		this.dev = dev;
	}
}