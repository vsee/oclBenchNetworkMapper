package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public interface WorkloadMapper {

	/**
	 * Maps the workload for the given benchmark command to available servers.
	 * The mapping is saved in the given execution map.
	 * NOTE: The given map hashtable will be cleared and refilled with the determined mapping.
	 * 
	 * @param servers list of available execution servers.
	 * @param cmd a user command specifiying benchmark and workload size.
	 * @param executionMap execution map filled with the determined workload distribution.
	 */
	public abstract void mapWorkload(List<ServerConnection> servers, UserCommand cmd, 
			Hashtable<ServerConnection, ExecutionItem> executionMap);

}
