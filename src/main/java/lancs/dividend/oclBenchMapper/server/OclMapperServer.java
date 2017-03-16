package lancs.dividend.oclBenchMapper.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import lancs.dividend.oclBenchMapper.benchmark.BenchFullExecutionResults;
import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.connection.ClientConnection;
import lancs.dividend.oclBenchMapper.energy.EnergySimulationLog;
import lancs.dividend.oclBenchMapper.message.CommandMessage;
import lancs.dividend.oclBenchMapper.message.response.ArchResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;
import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;

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
	
	private final String archDescr;
	private final Hashtable<Benchmark, Hashtable<DataSetSize, 
					Hashtable<ExecutionDevice, List<BenchFullExecutionResults>>>> simulationData;
	
	public OclMapperServer(int port, Path benchExecConf, Path benchDataConf, String archDescr, Path simulationConf) throws IOException {
		if(port <= 0) throw new IllegalArgumentException("Invalid server port: " + port);
		if(benchExecConf == null) throw new IllegalArgumentException("Benchmark execution configuration must not be null.");
		if(benchDataConf == null) throw new IllegalArgumentException("Benchmark data configuration must not be null.");
		
		benchRunner = new BenchmarkRunner(benchExecConf, benchDataConf);
		clientConnection = new ClientConnection(port);
		
		this.archDescr = archDescr;
		System.out.println("Starting server at port " + port);
		
		if(simulationConf != null) {
			simulationData = OclBenchMapperCsvHandler.parseFullExecutionStats(simulationConf);
			System.out.println("Running server simulation.");
		} else {
			simulationData = null;
		}
	}
	
	private boolean isSimulation() { return simulationData == null; }

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
				
				if(!isSimulation()) {
					result = benchRunner.run(cmd.getBinaryName(), cmd.getDataSetSize(), device, true);
				}
				else {
					result = simulateBenchmark(cmd, device);
				}
				
				System.out.println("Execution finished. Returning results.");
				return result;
			default:
				System.err.println("Unhandled command type: " + cmd.getType());
				return new ErrorResponseMessage("Unable to execute command: " + cmd);
		}
	}

	private ResponseMessage simulateBenchmark(RunBenchCmd cmd, ExecutionDevice device) {
		ResponseMessage result;
		Random rnd = new Random();
		
		// TODO add some error handling
		List<BenchFullExecutionResults> execResults = 
				simulationData.get(cmd.getBinaryName()).get(cmd.getDataSetSize()).get(device);
		BenchFullExecutionResults res = execResults.get(rnd.nextInt(execResults.size()));
		
		result = new BenchStatsResponseMessage(res.stdOut);
		((BenchStatsResponseMessage) result).setEnergyLog(
				new EnergySimulationLog(DUMMY_ENERGY_LOG, res.energyJ, res.runtimeMS));

		try {
			Thread.sleep((long) res.runtimeMS);
		} catch (InterruptedException e) {
			System.err.println("Benchmark simulation interrupted: " + e.getMessage());
			e.printStackTrace();
		}
		
		System.out.println(res.stdOut);
		return result;
	}
}
