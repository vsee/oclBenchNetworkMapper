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
	public Hashtable<ServerConnection, ExecutionItem> mapWorkload(
			List<ServerConnection> servers, UserCommand cmd) {
		
		if(servers == null || servers.size() == 0)
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<ServerConnection, ExecutionItem> mapping = new Hashtable<>();
		
		for(ServerConnection s : servers) 
			mapping.put(s, new ExecutionItem(new CommandMessage(cmd)));

		return mapping;
	}



}
