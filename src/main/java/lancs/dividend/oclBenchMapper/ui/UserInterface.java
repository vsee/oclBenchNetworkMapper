package lancs.dividend.oclBenchMapper.ui;

import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public interface UserInterface {

	public UserCommand receiveCommand();
	
	public void exit();

	
}
