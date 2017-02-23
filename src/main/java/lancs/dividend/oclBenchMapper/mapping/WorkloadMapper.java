package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;

public abstract class WorkloadMapper {

	public abstract Hashtable<ServerConnection, CommandMessage> 
			mapWorkload(List<ServerConnection> servers, CommandMessage cmd);

}
