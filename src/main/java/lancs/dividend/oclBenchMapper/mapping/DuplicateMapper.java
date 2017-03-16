package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.server.ServerDescription;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

/**
 * The duplicate mapper assigns the entire workload
 * to each available server.
 * 
 * @author vseeker
 */
public class DuplicateMapper implements WorkloadMapper {

	@Override
	public Hashtable<String, CmdToDeviceMapping> mapWorkload(ServerDescription[] servers, UserCommand cmd) {
		if(servers == null || servers.length == 0)
			throw new IllegalArgumentException("Given server descriptions must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<String, CmdToDeviceMapping> map = new Hashtable<>();
		
		for(ServerDescription descr : servers) {
			map.put(descr.address, new CmdToDeviceMapping(cmd, OclMapperServer.DEFAULT_SEVER_EXECUTION_DEVICE));
		}
		
		return map;
	}



}
