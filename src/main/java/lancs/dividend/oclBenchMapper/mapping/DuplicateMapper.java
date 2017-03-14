package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

/**
 * The duplicate mapper assigns the entire workload
 * to each available server.
 * 
 * @author vseeker
 */
public class DuplicateMapper implements WorkloadMapper {

	@Override
	public Hashtable<String, CmdToDeviceMapping> mapWorkload(String[] serverAdresses, UserCommand cmd) {
		if(serverAdresses == null || serverAdresses.length == 0)
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<String, CmdToDeviceMapping> map = new Hashtable<>();
		
		for(String s : serverAdresses) {
			map.put(s, new CmdToDeviceMapping(cmd, OclMapperServer.DEFAULT_SEVER_EXECUTION_DEVICE));
		}
		
		return map;
	}



}
