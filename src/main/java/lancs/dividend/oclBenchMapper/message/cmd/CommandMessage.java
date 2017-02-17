package lancs.dividend.oclBenchMapper.message.cmd;

import lancs.dividend.oclBenchMapper.message.Message;

public class CommandMessage extends Message {

	public enum CmdType { 
		/** Exit client and server connection */
		EXIT,
		/** Run the specified benchmark on server side and return results */
		RUNBENCH,
		/** Execute a specified console command directly and return stdout and stderr. */
		CONSOLE 
	}

	private static final long serialVersionUID = 7497056677386506925L;
	
	protected CmdType type;
	
	public CommandMessage(CmdType t) {
		type = t;
	}
	
	public CmdType getType() { return type; }
}
