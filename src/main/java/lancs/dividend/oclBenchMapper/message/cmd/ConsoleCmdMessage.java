package lancs.dividend.oclBenchMapper.message.cmd;

import java.util.StringJoiner;

public class ConsoleCmdMessage extends CommandMessage {

	private static final long serialVersionUID = -6010720613735264352L;
	
	private final String consoleCmd;
	
	public ConsoleCmdMessage(String consoleCmd) {
		super(CmdType.CONSOLE);
		this.consoleCmd = consoleCmd;
	}
	
	public String getConsoleCmd() { return consoleCmd; }
	
	@Override
	public String toString() {
		return new StringJoiner(CMD_SEP).add(type.name()).add(consoleCmd).toString();
	}
}
