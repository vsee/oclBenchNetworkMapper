package lancs.dividend.oclBenchMapper.userCmd;

import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;

public class RunBenchCmd extends UserCommand {

	private static final long serialVersionUID = -8705036194082222085L;
	
	private final RodiniaBin bin;
	private final DataSetSize dsetSize;
	
	public RunBenchCmd(RodiniaBin benchBinary, DataSetSize datasetSize) {
		super(CmdType.RUNBENCH);
		bin = benchBinary;
		dsetSize = datasetSize;
	}
	
	public RodiniaBin getBinaryName() { return bin; }
	public DataSetSize getDataSetSize() { return dsetSize; }
	
	@Override
	public String toString() {
		return new StringJoiner(",")
			.add(type.name())
			.add(bin.name())
			.add(dsetSize.name())
			.toString();
	}
}
