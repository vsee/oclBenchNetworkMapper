package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.nio.file.Path;

import lancs.dividend.oclBenchMapper.connection.ConnectionServer;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage.CmdType;
import lancs.dividend.oclBenchMapper.message.cmd.RunBenchCmdMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;

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
	
	private final RodiniaRunner rodinia;
	private final ConnectionServer server;
	
	public OclMapperServer(int port, Path rodiniaHome) throws IOException {
		rodinia = new RodiniaRunner(rodiniaHome);
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
			cmd = server.waitForCmd();
			
            if (cmd.getType() == CmdType.EXIT) {
                closeConnection = true;
                System.out.println("Closing connection with client.");
            } else if(cmd.getType() == CmdType.RUNBENCH) {
                response = executeCmd((RunBenchCmdMessage) cmd);
            } else {
            	System.err.println("ERROR: Unknown command type: " + cmd.getType().name());
				response = new ErrorResponseMessage("Unknown command type: " + cmd.getType().name());
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
				System.out.println("Executing: " + rbMsg.getBinaryName() + " " + rbMsg.getArgs());
				
				ResponseMessage result = rodinia.run(rbMsg.getBinaryName(), rbMsg.getArgs());
							
				System.out.println("Execution finished. Returning results.");
				return result;
			default:
				System.err.println("Unhandled command type: " + cmd.getType());
				return new ErrorResponseMessage("Unable to execute command: " + cmd);
		}
	}
}
