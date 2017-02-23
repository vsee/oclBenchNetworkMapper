package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.mapping.MapperFactory;
import lancs.dividend.oclBenchMapper.mapping.MapperFactory.MapperType;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.resultDisplay.ResultDisplay;
import lancs.dividend.oclBenchMapper.resultDisplay.SimpleConsoleDisplay;
import lancs.dividend.oclBenchMapper.ui.ClientConsoleInterface;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

public class OclMapperClient {
	
	private final List<ServerConnection> servers;
	private final WorkloadMapper wlMap;
	
	public OclMapperClient(List<String> serverAddresses, MapperType mapper) throws IOException {
		if(serverAddresses == null || serverAddresses.size() == 0)
			throw new IllegalArgumentException("Given server address list must not be empty.");
		if(mapper == null)
			throw new IllegalArgumentException("Given mapper type must not be null.");
		
		wlMap = MapperFactory.createWorkloadMapper(mapper);
		
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
		
		handleMessages();
	}

	private void handleMessages() {
		UserInterface ui = new ClientConsoleInterface();
		ResultDisplay resDisp = new SimpleConsoleDisplay();

		UserCommand cmd = null;
		Hashtable<ServerConnection, ExecutionItem> executionMap = new Hashtable<>();
		boolean waitingForResponse = false;

		while(true) {
        	if(!waitingForResponse) {
        		
        		cmd = ui.receiveCommand();
        		executionMap = wlMap.mapWorkload(servers, cmd);
        		
        		if(!sendExecutionCommands(executionMap) || cmd.getType() == CmdType.EXIT) 
        			break;
        		
        		waitingForResponse = true;
        		
        	} else {
        		
        		if(!receiveExecutionResults(executionMap)) break;
                
        		resDisp.display(executionMap, cmd);
        		
            	waitingForResponse = false;
        	}
		}
	
		ui.exit();
		for(ServerConnection s : servers) s.closeConnection();
		servers.clear();
	}

	
	/**
	 * Waits for a response of all execution servers considered
	 * in the workload mapping and returns them. 
	 *  
	 * @param executionMap The mapping of benchmark workload to server currently being executed. 
	 * 
	 * @return A mapping of server to execution response or null if an error happened.
	 */
	private boolean receiveExecutionResults(Hashtable<ServerConnection, ExecutionItem> executionMap) {
		
		assert executionMap != null && executionMap.size() > 0 : "Invalid command to server mapping.";
		
		for (ServerConnection s : executionMap.keySet()) {
	        try {
	        	ResponseMessage response = s.waitForCmdResponse();
	        	ExecutionItem item = executionMap.get(s);
	        	item.setResponse(response);
	        } catch (IOException e) {
	        	System.err.println(e.getMessage());
	        	e.printStackTrace();
	        	return false;
	        }
		}
		
		return true;
	}

	/** 
	 * Takes a mapping of workload execution commands to servers and
	 * starts workload execution by sending the corresponding messages 
	 * to their paired servers. It gives up as soon as a single message
	 * could not be send successfully.
	 * 
	 * @param executionMap mapping of CommandMessages to ServerConnections
	 * @return true if all messages were send out successfully
	 */
	private boolean sendExecutionCommands(Hashtable<ServerConnection, ExecutionItem> executionMap) {

		assert executionMap != null && executionMap.size() > 0 : "Invalid command to server mapping.";
		
		for (ServerConnection s : executionMap.keySet()) {
			try {
				s.sendMessage(executionMap.get(s).getCommand());
			} catch (IOException e) {
				System.err.println("ERROR: sending command to server " + s +" failed: " + e);
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}
