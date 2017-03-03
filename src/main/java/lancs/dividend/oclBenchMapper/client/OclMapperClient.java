package lancs.dividend.oclBenchMapper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.MapperFactory;
import lancs.dividend.oclBenchMapper.mapping.MapperFactory.MapperType;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.ui.UserInterfaceFactory;
import lancs.dividend.oclBenchMapper.ui.UserInterfaceFactory.UserInterfaceType;

public class OclMapperClient {
	
	private final List<ServerConnection> servers;
	private final WorkloadMapper wlMap;
	private final UserInterface ui;

	
	public OclMapperClient(List<String> serverAddresses, MapperType mapper, UserInterfaceType uiType) throws IOException {
		if(serverAddresses == null || serverAddresses.size() == 0)
			throw new IllegalArgumentException("Given server address list must not be empty.");
		if(mapper == null)
			throw new IllegalArgumentException("Given mapper type must not be null.");
		
		wlMap = MapperFactory.createWorkloadMapper(mapper);
		ui = UserInterfaceFactory.createUserInterface(uiType);
		
		servers = new ArrayList<>(serverAddresses.size());
		connectToClients(serverAddresses);
	}
	
	private void connectToClients(List<String> serverAddresses) throws IOException {
		for(String addr : serverAddresses) {

			System.out.print("Connecting client with " + addr + " ...");

			ServerConnection s = new ServerConnection(addr);
			if(s.isConnected()) {
				servers.add(s);
				System.out.println(" connected!");
			} else {
				System.err.println(" failed!");
			}
		}
	}

	public void runClient() {
		if(servers.isEmpty()) {
			System.err.println("ERROR: Could not establish connection with server.");
			return;
		}

		ui.start(new ClientUserCommandHandler(servers, wlMap));
		// running UI ...
		
		// clean up upon return
		ui.exit();
		for(ServerConnection s : servers) s.closeConnection();
		servers.clear();
	}
}
