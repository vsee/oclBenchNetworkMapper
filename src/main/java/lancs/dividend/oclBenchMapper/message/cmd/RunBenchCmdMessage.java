package lancs.dividend.oclBenchMapper.message.cmd;

import java.util.StringJoiner;

public class RunBenchCmdMessage extends CommandMessage {

	private static final long serialVersionUID = -7281212996643722226L;
	
	private final String name;
	private final String args;
	
	public RunBenchCmdMessage(String n, String a) {
		super(CmdType.RUNBENCH);
		name = n;
		args = a;
	}
	
	public String getName() { return name; }
	public String getArgs() { return args; }
	
	@Override
	public String toString() {
		return new StringJoiner(CMD_SEP)
			.add(type.name())
			.add(name)
			.add(args)
			.toString();
	}
}
