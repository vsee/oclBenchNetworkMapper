package lancs.dividend.oclBenchMapper.userCmd;

import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.RodiniaRunner.RodiniaBin;

public class RunBenchCmd extends UserCommand {

	private static final long serialVersionUID = -8705036194082222085L;
	
	private final RodiniaBin bin;
	private final String args;
	
	public RunBenchCmd(RodiniaBin benchBinary, String benchArgs) {
		super(CmdType.RUNBENCH);
		bin = benchBinary;
		args = benchArgs;
	}
	
	public RodiniaBin getBinaryName() { return bin; }
	public String getArgs() { return args; }
	
	@Override
	public String toString() {
		return new StringJoiner(",")
			.add(type.name())
			.add(bin.name())
			.add(args)
			.toString();
	}
}
