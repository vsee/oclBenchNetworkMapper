package lancs.dividend.oclBenchMapper.userCmd;

import java.io.Serializable;

public abstract class UserCommand implements Serializable {
	
	private static final long serialVersionUID = -2267953757874513185L;

	public enum CmdType { 
		/** Exit client and server connection */
		EXIT,
		/** Run the specified benchmark on server side and return results */
		RUNBENCH
	}

	protected CmdType type;

	public UserCommand(CmdType type) {
		this.type = type;
	}
	
	public CmdType getType() { return type; }

	@Override
	public String toString() {
		return type.name();
	}
}
