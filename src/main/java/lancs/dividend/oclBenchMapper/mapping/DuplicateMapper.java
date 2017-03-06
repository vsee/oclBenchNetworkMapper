package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.CommandMessage;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

/**
 * The duplicate mapper assigns the entire workload
 * to each available server.
 * 
 * @author vseeker
 */
public class DuplicateMapper implements WorkloadMapper {

	@Override
	public void mapWorkload(List<ServerConnection> servers, UserCommand cmd,
			Hashtable<ServerConnection, ExecutionItem> executionMap) {
		
		if(servers == null || servers.isEmpty())
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		if(executionMap == null)
			throw new IllegalArgumentException("Given execution map must not be null.");
		
		executionMap.clear();
		
		for(ServerConnection s : servers) 
			executionMap.put(s, new ExecutionItem(new CommandMessage(cmd)));
	}



}
