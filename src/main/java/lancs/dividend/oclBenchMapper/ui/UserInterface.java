package lancs.dividend.oclBenchMapper.ui;

import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;

public interface UserInterface {

	public CommandMessage parseCommand();
	
	public void exit();

	
}
