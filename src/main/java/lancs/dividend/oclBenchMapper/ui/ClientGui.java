package lancs.dividend.oclBenchMapper.ui;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public class ClientGui implements UserInterface {

	@Override
	public UserCommand receiveCommand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void display(
			Hashtable<ServerConnection, ExecutionItem> executionMap,
			UserCommand cmd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub

	}

}
