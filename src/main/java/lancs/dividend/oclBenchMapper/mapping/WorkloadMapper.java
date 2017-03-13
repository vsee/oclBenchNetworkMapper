package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public interface WorkloadMapper {

	/**
	 * Maps the workload for the given benchmark command to available servers.
	 * The mapping is returned as a mapping of server address to a list of single execution items.
	 * 
	 * @param serverAdresses list of available servers
	 * @param cmd benchmark command issued by the user
	 * @return a mapping of server addresses to execution items
	 */
	public abstract Hashtable<String, List<ExecutionItem>> mapWorkload(String[] serverAdresses, UserCommand cmd);

}
