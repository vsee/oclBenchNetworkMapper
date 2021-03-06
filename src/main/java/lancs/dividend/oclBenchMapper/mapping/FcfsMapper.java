package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.server.ServerDescription;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

/**
 * The first come first served mapper assigns the entire workload to the
 * first available server.
 * 
 * @author vseeker
 */
public class FcfsMapper implements WorkloadMapper {

	@Override
	public Hashtable<String, CmdToDeviceMapping> mapWorkload(ServerDescription[] servers, UserCommand cmd) {
		if(servers == null || servers.length == 0)
			throw new IllegalArgumentException("Given server descriptions must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<String, CmdToDeviceMapping> map = new Hashtable<>();
		
		boolean assigned = false;
		for(ServerDescription descr : servers) {
			// assign only once to first server in list
			// except for exit methods, they are send to all servers
			if(!assigned || cmd.getType() == CmdType.EXIT) {
				map.put(descr.address, new CmdToDeviceMapping(cmd, OclMapperServer.DEFAULT_SEVER_EXECUTION_DEVICE));
				assigned = true;
			}
		}
		
		return map;
	}

}
