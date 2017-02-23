package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.CommandMessage;
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
	public Hashtable<ServerConnection, ExecutionItem> mapWorkload(
			List<ServerConnection> servers, UserCommand cmd) {
		
		if(servers == null || servers.size() == 0)
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<ServerConnection, ExecutionItem> mapping = new Hashtable<>();
		
		boolean assigned = false;
		for(ServerConnection s : servers) {
			// assign only once to first server in list
			// except for exit methods, they are send to all servers
			if(!assigned || cmd.getType() == CmdType.EXIT) {
				mapping.put(s, new ExecutionItem(new CommandMessage(cmd)));
				assigned = true;
			}
		}
		return mapping;
	}



}
