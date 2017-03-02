package lancs.dividend.oclBenchMapper.ui;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public interface UserInterface {

	public UserCommand receiveCommand();
	
	public void display(Hashtable<ServerConnection, ExecutionItem> executionMap, UserCommand cmd);
	
	public void exit();

	
	
}
