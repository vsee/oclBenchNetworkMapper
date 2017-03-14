package lancs.dividend.oclBenchMapper.mapping;

import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public class CmdToDeviceMapping {

	public final UserCommand userCmd;
	public final ExecutionDevice execDev;
	
	public CmdToDeviceMapping(UserCommand cmd, ExecutionDevice dev) {
		userCmd = cmd;
		execDev = dev;
	}
	
}
