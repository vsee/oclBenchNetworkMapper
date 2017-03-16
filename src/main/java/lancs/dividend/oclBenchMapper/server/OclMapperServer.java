package lancs.dividend.oclBenchMapper.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner;
import lancs.dividend.oclBenchMapper.connection.ClientConnection;
import lancs.dividend.oclBenchMapper.energy.EnergyLog;
import lancs.dividend.oclBenchMapper.message.CommandMessage;
import lancs.dividend.oclBenchMapper.message.response.ArchResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
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

	public static final ExecutionDevice DEFAULT_SEVER_EXECUTION_DEVICE = ExecutionDevice.CPU;
	private static final Path DUMMY_ENERGY_LOG = Paths.get("src/main/resources/ocleniMONITOR_sample.csv");
	
	private final BenchmarkRunner benchRunner;
	private final ClientConnection clientConnection;
	
	/** If this flag is true, the server does not execute benchmark but merely sends back
	 * dummy energy and performance statistics. This mode is meant for test and development purposes. */
	private final boolean isDummyServer;

	private final String archDescr;
	
	public OclMapperServer(int port, Path benchExecConf, Path benchDataConf, String archDescr, boolean isDummy) throws IOException {
		if(port <= 0) throw new IllegalArgumentException("Invalid server port: " + port);
		if(benchExecConf == null) throw new IllegalArgumentException("Benchmark execution configuration must not be null.");
		if(benchDataConf == null) throw new IllegalArgumentException("Benchmark data configuration must not be null.");
		
		benchRunner = new BenchmarkRunner(benchExecConf, benchDataConf);
		clientConnection = new ClientConnection(port);
		
		this.archDescr = archDescr;
		isDummyServer = isDummy;
		System.out.println("Starting server at port " + port);
		if(isDummy) System.out.println("Running server as dummy!");
	}

	public void runServer() {
		try {
			while(true) {
				System.out.println("Waiting for client ...");
				if(clientConnection.establishConnection())  {
					System.out.println("Connection established with client.");
					
					System.out.println("Sending architecture description: " + archDescr);
					try {
						clientConnection.sendMessage(new ArchResponseMessage(archDescr));
					} catch(IOException e) {
						System.err.println("ERROR: Sending architecture description to client failed. Disconnecting ...");
						clientConnection.closeConnection();
						continue;
					}
					
					handleMessages();
				}
			}
		} finally {
            try {
                clientConnection.shutDown();
            } catch (IOException e) {
            	System.out.println("ERROR: Shutting down server failed: " + e);
            	e.printStackTrace();
            }
		}
	}
	
	private void handleMessages() {
		boolean closeConnection = false;
		
		while (!closeConnection) {
			
            ResponseMessage response = null;
			CommandMessage cmdMsg = clientConnection.waitForCmd();
			
			UserCommand cmd = cmdMsg.getCommand();
			ExecutionDevice device = cmdMsg.getExecutionDevice();
			
            if (cmd.getType() == CmdType.EXIT) {
                closeConnection = true;
                System.out.println("Closing connection with client.");
            } else if(cmd.getType() == CmdType.RUNBENCH) {
                response = executeCmd((RunBenchCmd) cmd, device);
            } else {
            	System.err.println("ERROR: Unknown command type: " + cmd.getType().name());
				response = new ErrorResponseMessage("Unknown command type: " + cmd.getType().name());
            }
				
            if(response != null) {
            	try {
					clientConnection.sendMessage(response);
				} catch (IOException e) {
					System.err.println("ERROR: sending response failed: " + e);
					e.printStackTrace();
					closeConnection = true;
				}
            }
        }
		
		clientConnection.closeConnection();
	}

	
	private ResponseMessage executeCmd(RunBenchCmd cmd, ExecutionDevice device) {
		switch (cmd.getType()) {
			case RUNBENCH:
				System.out.println("Executing: " + cmd.getBinaryName() + " " + 
						cmd.getDataSetSize() + " on " + device);
				
				ResponseMessage result;
				
				if(!isDummyServer) {
					result = benchRunner.run(cmd.getBinaryName(), cmd.getDataSetSize(), device, true);
				}
				else {
					result = new BenchStatsResponseMessage("DUMMY EXECUTION");
					((BenchStatsResponseMessage) result).setEnergyLog(new EnergyLog(DUMMY_ENERGY_LOG));
				}
				
				System.out.println("Execution finished. Returning results.");
				return result;
			default:
				System.err.println("Unhandled command type: " + cmd.getType());
				return new ErrorResponseMessage("Unable to execute command: " + cmd);
		}
	}
}
