package lancs.dividend.oclBenchMapper.userCmd;

import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;

public class RunBenchCmd extends UserCommand {

	private static final long serialVersionUID = -8705036194082222085L;
	
	private final Benchmark bin;
	private final String dsetSize;
	
	public RunBenchCmd(Benchmark benchBinary, String datasetSize) {
		super(CmdType.RUNBENCH);
		bin = benchBinary;
		dsetSize = datasetSize;
	}
	
	public Benchmark getBinaryName() { return bin; }
	public String getDataSetSize() { return dsetSize; }
	
	@Override
	public String toString() {
		return new StringJoiner(",")
			.add(type.name())
			.add(bin.name())
			.add(dsetSize)
			.toString();
	}

}
