package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage.CmdType;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.TextResponseMessage;
import lancs.dividend.oclBenchMapper.ui.ClientConsoleInterface;
import lancs.dividend.oclBenchMapper.ui.UserInterface;

public class OclMapperClient {
	
	private final List<ServerConnection> servers;
	
	public OclMapperClient(List<String> serverAddresses) throws IOException {
		if(serverAddresses == null || serverAddresses.size() == 0)
			throw new IllegalArgumentException("Given server address list must not be empty.");
		
		servers = new ArrayList<>(serverAddresses.size());
		connectToClients(serverAddresses);
	}
	
	private void connectToClients(List<String> serverAddresses) throws IOException {
		for(String addr : serverAddresses) {
			String[] addrParts = addr.split(":");
			if(addrParts.length != 2)  throw new IllegalArgumentException("Given address invalid: " + addr);
			
			System.out.print("Connecting client with " + addr + " ...");
			
			int port;
			try {
				port = Integer.parseInt(addrParts[1]);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Given address port invalid: " + addr);
			}
			
			ServerConnection s = new ServerConnection(port, addrParts[0]);
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
		
		handleMessages();
	}

	private void handleMessages() {
		// XXX sending and receiving messages currently only 
		// implemented for the first client - server connection in the list
		
		// TODO differentiate between user command and command message
		
		boolean waitingForResponse = false;
		UserInterface ui = new ClientConsoleInterface();

		while(true) {
        	if(waitingForResponse) {
        		ResponseMessage response;
                try {
                	response = servers.get(0).waitForCmdResponse();
                } catch (IOException e) {
                	System.err.println(e.getMessage());
                	e.printStackTrace();
                	break;
                }
                
            	processResponse(response);
            	waitingForResponse = false;
        	} else {
        		CommandMessage cmd = ui.parseCommand();

        		try {
					servers.get(0).sendMessage(cmd);
				} catch (IOException e) {
					System.err.println("ERROR: sending command failed: " + e);
					e.printStackTrace();
					break;
				}
                
        		if(cmd.getType() == CmdType.EXIT) break;
                
                waitingForResponse = true;
        	}
		}
	
		ui.exit();
		for(ServerConnection s : servers) s.closeConnection();
		servers.clear();
	}

	private void processResponse(ResponseMessage response) {

		switch (response.getType()) {
		case TEXT:
			System.out.println(((TextResponseMessage) response).getText());
			break;
		case BENCHSTATS:
			BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
			System.out.println(br.getEnergy() + " - " + br.getRuntime() + "ms");
			break;
		case ERROR:
			System.err.println("ERROR: " + ((ErrorResponseMessage) response).getText());
			break;
		default:
			System.err.println("Unknown response type: " + response.getType());
			break;
		}
	}

}
