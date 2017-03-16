package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.server.ServerDescription;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public interface WorkloadMapper {

	/**
	 * Maps the workload for the given benchmark command to available servers.
	 * The mapping is returned as a mapping of server address to a list of single execution items.
	 * 
	 * @param servers list with descriptions of available servers
	 * @param cmd benchmark command issued by the user
	 * @return a mapping of server addresses to command and execution device.
	 */
	public Hashtable<String, CmdToDeviceMapping> 
		mapWorkload(ServerDescription[] servers, UserCommand cmd) throws WorkloadDistributionException;

}
