package lancs.dividend.oclBenchMapper.message.cmd;

import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.OclMapperClient.RodiniaBins;

public class RunBenchCmdMessage extends CommandMessage {

	private static final long serialVersionUID = -7281212996643722226L;
	
	private final RodiniaBins bin;
	private final String args;
	
	public RunBenchCmdMessage(RodiniaBins benchBinary, String benchArgs) {
		super(CmdType.RUNBENCH);
		bin = benchBinary;
		args = benchArgs;
	}
	
	public RodiniaBins getBinaryName() { return bin; }
	public String getArgs() { return args; }
	
	@Override
	public String toString() {
		return new StringJoiner(CMD_SEP)
			.add(type.name())
			.add(bin.name())
			.add(args)
			.toString();
	}
}
