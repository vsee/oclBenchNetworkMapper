package lancs.dividend.oclBenchMapper.client;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

public class ClientConnectionHandler {
	
	private final List<ServerConnection> servers;
	private final WorkloadMapper wlMap;
	
	public ClientConnectionHandler(List<ServerConnection> servers, WorkloadMapper wlMap) {
		if(servers == null || servers.isEmpty())
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(wlMap == null)
			throw new IllegalArgumentException("Given workload mapper must not be null.");
		
		this.wlMap = wlMap;
		this.servers = servers;		
	}
	
	public String[] getServerAdresses() {
		String[] res = new String[servers.size()];
		for(int i = 0; i < servers.size(); i++) {
			res[i] = servers.get(i).getAddress();
		}
		return res;
	}

	/**
	 * Handles workload mapping and execution for a given user command.
	 * NOTE: The given execution map will be cleared and refilled by this method.
	 * 
	 * @param cmd The command to be executed on the servers.
	 * @param executionMap This map is used to map the workload to available servers and filled with execution results.
	 * @return true if execution was successful.
	 */
	public boolean handleUserCommand(UserCommand cmd, Hashtable<ServerConnection, ExecutionItem> executionMap) {
		if(cmd == null)
			throw new IllegalArgumentException("Given user command must not be null or empty.");
		if(executionMap == null)
			throw new IllegalArgumentException("Given execution map must not be null.");
		
		wlMap.mapWorkload(servers, cmd, executionMap);
		
		if(!sendExecutionCommands(executionMap) || cmd.getType() == CmdType.EXIT) 
			return false;

		if(!receiveExecutionResults(executionMap)) return false;

		return true;
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

	public void closeConnections() {
		System.out.println("Closing server connections ...");
		for(ServerConnection s : servers) s.closeConnection();
		servers.clear();
	}	
}