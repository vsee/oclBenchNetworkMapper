package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import lancs.dividend.oclBenchMapper.connection.ClientConnection;
import lancs.dividend.oclBenchMapper.energy.OCLEnergyMonitor;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

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
	private final ClientConnection client;
	
	public OclMapperServer(int port, Path rodiniaHome) throws IOException {
		rodinia = new RodiniaRunner(rodiniaHome, true);
		client = new ClientConnection(port);
		System.out.println("Starting server at port " + port);
	}

	public void runServer() {
		try {
			while(true) {
				System.out.println("Waiting for client ...");
				if(client.establishConnection())  {
					System.out.println("Connection established with client.");
					handleMessages();
				}
			}
		} finally {
            try {
                client.shutDown();
            } catch (IOException e) {
            	System.out.println("ERROR: Shutting down server failed: " + e);
            	e.printStackTrace();
            }
		}
	}
	
	private void handleMessages() {
		boolean closeConnection = false;
		
		while (!closeConnection) {
			
            UserCommand cmd = null;
            ResponseMessage response = null;
			cmd = client.waitForCmd().getCommand();
			
            if (cmd.getType() == CmdType.EXIT) {
                closeConnection = true;
                System.out.println("Closing connection with client.");
            } else if(cmd.getType() == CmdType.RUNBENCH) {
                response = executeCmd((RunBenchCmd) cmd);
            } else {
            	System.err.println("ERROR: Unknown command type: " + cmd.getType().name());
				response = new ErrorResponseMessage("Unknown command type: " + cmd.getType().name());
            }
				
            if(response != null) {
            	try {
					client.sendMessage(response);
				} catch (IOException e) {
					System.err.println("ERROR: sending response failed: " + e);
					e.printStackTrace();
					closeConnection = true;
				}
            }
        }
		
		client.closeConnection();
	}

	
	private ResponseMessage executeCmd(RunBenchCmd cmd) {
		switch (cmd.getType()) {
			case RUNBENCH:
				System.out.println("Executing: " + cmd.getBinaryName() + " " + cmd.getArgs());
				
				ResponseMessage result = null;
				try {
					OCLEnergyMonitor.getInstance().startMonitoring();
				
					result = rodinia.run(cmd.getBinaryName(), cmd.getArgs());
				
					OCLEnergyMonitor.getInstance().endMonitoring();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				
				// TODO retrieve energy monitoring results
				
				System.out.println("Execution finished. Returning results.");
				return result;
			default:
				System.err.println("Unhandled command type: " + cmd.getType());
				return new ErrorResponseMessage("Unable to execute command: " + cmd);
		}
	}
}
