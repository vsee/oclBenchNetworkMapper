package lancs.dividend.oclBenchMapper.message;

import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public class CommandMessage extends Message {

	private static final long serialVersionUID = 7497056677386506925L;
	
	private final UserCommand cmd;
	
	public CommandMessage(UserCommand cmd) {
		this.cmd = cmd;
	}
	
	public UserCommand getCommand() { return cmd; }
	
	@Override
	public String toString() {
		return cmd.toString();
	}
}
