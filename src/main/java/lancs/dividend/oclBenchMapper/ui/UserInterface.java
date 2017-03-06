package lancs.dividend.oclBenchMapper.ui;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public interface UserInterface {

	public void run(ClientConnectionHandler cmdHandler);
	
	public UserCommand receiveCommand();
	
	public void updateDisplay(Hashtable<ServerConnection, ExecutionItem> executionMap, UserCommand cmd);
	
}
