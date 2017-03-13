package lancs.dividend.oclBenchMapper.ui;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;

public interface UserInterface {

	public void run(ClientConnectionHandler cmdHandler, WorkloadMapper mapper);
	
}
