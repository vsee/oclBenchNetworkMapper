package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage.CmdType;

/**
 * The simple mapper assigns the entire workload to the
 * first available server.
 * 
 * @author vseeker
 */
public class SimpleMapper extends WorkloadMapper {

	@Override
	public Hashtable<ServerConnection, CommandMessage> mapWorkload(
			List<ServerConnection> servers, CommandMessage cmd) {
		
		Hashtable<ServerConnection, CommandMessage> mapping = new Hashtable<>();
		
		boolean assigned = false;
		for(ServerConnection s : servers) {
			// assign only once to first server in list
			// except for exit methods, they are send to all servers
			if(!assigned || cmd.getType() == CmdType.EXIT) {
				mapping.put(s, cmd);
				assigned = true;
			}
		}
		return mapping;
	}



}
