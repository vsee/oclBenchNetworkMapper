package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public interface WorkloadMapper {

	public abstract Hashtable<ServerConnection, ExecutionItem> mapWorkload(
			List<ServerConnection> servers, UserCommand cmd);

}
