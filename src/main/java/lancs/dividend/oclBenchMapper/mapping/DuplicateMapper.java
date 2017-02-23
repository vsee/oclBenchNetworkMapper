package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;

/**
 * The duplicate mapper assigns the entire workload
 * to each available server.
 * 
 * @author vseeker
 */
public class DuplicateMapper implements WorkloadMapper {

	@Override
	public Hashtable<ServerConnection, ExecutionItem> mapWorkload(
			List<ServerConnection> servers, CommandMessage cmd) {
		
		Hashtable<ServerConnection, ExecutionItem> mapping = new Hashtable<>();
		
		for(ServerConnection s : servers) 
			mapping.put(s, new ExecutionItem(cmd));

		return mapping;
	}



}
