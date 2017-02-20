package lancs.dividend.oclBenchMapper;

import java.io.IOException;

import lancs.dividend.oclBenchMapper.connection.ConnectionServer;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.IllegalCommandMessageException;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage.CmdType;
import lancs.dividend.oclBenchMapper.message.cmd.RunBenchCmdMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.TextResponseMessage;
import lancs.dividend.oclBenchMapper.utils.ShellCmdExecutor;

/**
 * 
 * @author vseeker
 *
 * listens for client commands
 * executes commands blocking further client communication until execution results are present
 * returns results and waits for new commands
 *
 */
public class OclMapperServer {

	// TODO add graceful server shutdown
	
	private final ConnectionServer server;
	
	public OclMapperServer(int port) throws IOException {
		server = new ConnectionServer(port);
	}

	public void runServer() {
		try {
			while(true) {
				System.out.println("Waiting for client ...");
				if(server.establishConnection())  {
					System.out.println("Connection established with client.");
					handleMessages();
				}
			}
		} finally {
            try {
                server.shutDown();
            } catch (IOException e) {
            	System.out.println("ERROR: Shutting down server failed: " + e);
            	e.printStackTrace();
            }
		}
	}
	
	private void handleMessages() {
		boolean closeConnection = false;
		
		while (!closeConnection) {
			
            CommandMessage cmd = null;
            ResponseMessage response = null;
			try {
				cmd = server.waitForCmd();
				
	            if (cmd.getType() == CmdType.EXIT) {
	                closeConnection = true;
	            } else if(cmd.getType() == CmdType.RUNBENCH) {
	                response = executeCmd((RunBenchCmdMessage) cmd);
	            } else {
	            	String error = "ERROR: Unknown command type: " + cmd.getType().name();
	            	System.err.println(error);
	            	throw new IllegalCommandMessageException(error);
	            }
				
			} catch (IllegalCommandMessageException e) {
				response = new ErrorResponseMessage(e.getMessage());
			}
            
            if(response != null) {
            	try {
					server.sendMessage(response);
				} catch (IOException e) {
					System.err.println("ERROR: sending response failed: " + e);
					e.printStackTrace();
					closeConnection = true;
				}
            }
        }
		
		server.closeConnection();
	}

	
	private ResponseMessage executeCmd(CommandMessage cmd) {
		switch (cmd.getType()) {
			case RUNBENCH:
				RunBenchCmdMessage rbMsg = (RunBenchCmdMessage) cmd;
				System.out.println("Executing: " + rbMsg.getName() + " " + rbMsg.getArgs());
				String result = ShellCmdExecutor.executeCmd(
						rbMsg.getName() + " " + rbMsg.getArgs(), true);
				
				return new TextResponseMessage(result);
			default:
				System.err.println("Unhandled command type: " + cmd.getType());
				return new TextResponseMessage("Unable to execute command: " + cmd);
		}
	}
}
