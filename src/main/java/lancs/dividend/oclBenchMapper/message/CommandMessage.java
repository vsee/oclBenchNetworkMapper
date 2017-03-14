package lancs.dividend.oclBenchMapper.message;

import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public class CommandMessage extends Message {

	private static final long serialVersionUID = 7497056677386506925L;
	
	private final UserCommand cmd;
	private final ExecutionDevice device;
	
	public CommandMessage(UserCommand cmd, ExecutionDevice device) {
		this.cmd = cmd;
		this.device = device;
	}
	
	public UserCommand getCommand() { return cmd; }
	public ExecutionDevice getExecutionDevice() { return device; }
	
	@Override
	public String toString() {
		return cmd.toString() + " - " + device;
	}
}
