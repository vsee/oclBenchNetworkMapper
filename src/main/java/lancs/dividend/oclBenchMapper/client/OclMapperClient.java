package lancs.dividend.oclBenchMapper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.ui.UserInterface;

public class OclMapperClient {
	
	private final List<ServerConnection> servers;
	private final WorkloadMapper wlMap;
	private final UserInterface ui;

	
	public OclMapperClient(List<String> serverAddresses, WorkloadMapper mapper, UserInterface ui) throws IOException {
		if(serverAddresses == null || serverAddresses.size() == 0)
			throw new IllegalArgumentException("Given server address list must not be empty.");
		if(mapper == null)
			throw new IllegalArgumentException("Given mapper must not be null.");
		if(ui == null)
			throw new IllegalArgumentException("Given ui must not be null.");
		
		wlMap = mapper;
		this.ui = ui;
		
		servers = new ArrayList<>(serverAddresses.size());
		connectToServers(serverAddresses);
	}
	
	private void connectToServers(List<String> serverAddresses) throws IOException {
		for(String addr : serverAddresses) {

			System.out.print("Connecting client with " + addr + " ...");

			ServerConnection s = new ServerConnection(addr);
			if(s.isConnected()) {
				servers.add(s);
				System.out.println(" connected!");
				System.out.println("Server architecture: " + s.getServerDescription().architecture);
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

		ui.run(new ClientConnectionHandler(servers), wlMap);
	}
}
