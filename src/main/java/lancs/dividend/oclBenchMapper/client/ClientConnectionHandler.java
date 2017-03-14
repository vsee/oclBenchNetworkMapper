package lancs.dividend.oclBenchMapper.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

public class ClientConnectionHandler {
	
	private class ServerLoadHandler implements Runnable {

		private final List<ExecutionItem> executionLoad;
		private final ServerConnection server;
		
		public ServerLoadHandler(List<ExecutionItem> load, ServerConnection server) {
			executionLoad = load;
			this.server = server;
		}
		
		@Override
		public void run() {

			for (ExecutionItem item : executionLoad) {
				sendExecutionItem(item);
				if(!item.hasError() && item.getCmdMsg().getCommand().getType() != CmdType.EXIT)
					receiveExecutionResults(item);
			}
			
		}

		private void sendExecutionItem(ExecutionItem item) {
			try {
				server.sendMessage(item.getCmdMsg());
			} catch (IOException e) {
				item.setError("ERROR: sending command to server " + server.getAddress() + " failed.",e);
			}
		}
		
		private void receiveExecutionResults(ExecutionItem item) {
	        try {
	        	ResponseMessage response = server.waitForCmdResponse();
	        	item.setResponse(response);
	        	
	        	if(response.getType() == ResponseType.ERROR) {
					item.setError(((ErrorResponseMessage) response).getText());
	        	}
	        } catch (IOException e) {
				item.setError("ERROR: receiving command from " + server.getAddress() + " failed.",e);
	        }
		}
	}
	
	// # ------------------------------------------------------------------------------------ #
	
	private final Hashtable<String, ServerConnection> servers;
	private final String[] serverAdresses;
	
	public ClientConnectionHandler(List<ServerConnection> connections) {
		if(connections == null || connections.isEmpty())
			throw new IllegalArgumentException("Given server connections must not be null or empty.");

		this.servers = new Hashtable<>();
		for(ServerConnection sc : connections) {
			if(sc == null) throw new IllegalArgumentException("Given server connection must not be null.");
			servers.put(sc.getAddress(), sc);
		}

		serverAdresses = servers.keySet().toArray(new String[servers.keySet().size()]);
	}
	
	public String[] getServerAdresses() {
		return serverAdresses;
	}
		
	public void executeCommands(UserCommand cmd, Hashtable<String, List<ExecutionItem>> execMapping) {
		if(cmd == null)
			throw new IllegalArgumentException("Given user command must not be null or empty.");
		if(execMapping == null)
			throw new IllegalArgumentException("Given execution mapping must not be null.");
		
		// issue worker threads
		List<Thread> handlers = new ArrayList<>();
		for(String serverAddr : execMapping.keySet()) {
			List<ExecutionItem> execItems = execMapping.get(serverAddr);
			
			assert execItems != null : "Given execution items for server " + serverAddr + " must not be null.";
			
			Thread loadHandlerThread = new Thread(new ServerLoadHandler(execItems, servers.get(serverAddr)));
			loadHandlerThread.start();
			handlers.add(loadHandlerThread);
		}

		// wait for them to finish
		for (Thread loadHandlerThread : handlers) {
			try {
				loadHandlerThread.join();
			} catch (InterruptedException e) {
				System.out.println(e);
				e.printStackTrace();

				throw new RuntimeException("Benchmark execution worker thread was interrupted.");
			}
		}
	}
	

	public void closeConnections() {
		System.out.println("Closing server connections ...");
		for(ServerConnection s : servers.values()) s.closeConnection();
		servers.clear();
	}	
}