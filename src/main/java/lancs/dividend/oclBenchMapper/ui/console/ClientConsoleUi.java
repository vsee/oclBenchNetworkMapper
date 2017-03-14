package lancs.dividend.oclBenchMapper.ui.console;

import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.userCmd.ExitCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

/**
 * A console based user interface allows the user to control
 * client and server interactions using console input. Results
 * are displayed as messages in the console as well.
 * 
 * @author vseeker
 *
 */
public class ClientConsoleUi implements UserInterface {

	private final String EXIT_CMD = "exit";
	private final String BENCHMARK_LIST;
	private final String BENCHMARK_TOP_MENU;
	
	private final String MENU_UP = "back";
	private final String DSET_SIZE_LIST;
	private final String BENCHMARK_DSET_SIZE_MENU;
	
	private final Scanner cmdIn;
	private boolean exitClient;

	public ClientConsoleUi() {
        cmdIn = new Scanner(System.in);
        
		// Generate menu strings
		StringJoiner join = new StringJoiner(",","{","}");
		for (Benchmark b : Benchmark.values()) join.add(b.name());
		BENCHMARK_LIST = join.toString();
		
		BENCHMARK_TOP_MENU = 
		"\n1. Enter benchmark name from selection: " + BENCHMARK_LIST + "\n" +
		"2. Enter '" + EXIT_CMD + "' to shut down client.\n>> ";

		join = new StringJoiner(",","{","}");
		for (DataSetSize d : DataSetSize.values()) join.add(d.name());
		DSET_SIZE_LIST = join.toString();
		BENCHMARK_DSET_SIZE_MENU = 
		"\n1. Enter dataset size from selection: " + DSET_SIZE_LIST + "\n" +
		"2. Enter '" + MENU_UP + "' to return to benchmark selection.\n>> ";
	}
	
	@Override
	public void run(ClientConnectionHandler cmdHandler, WorkloadMapper mapper) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		if(mapper == null)
			throw new IllegalArgumentException("Given workload mapper must not be null.");
		
		while(!exitClient) {
			UserCommand cmd = receiveCommand();
			Hashtable<String, List<ExecutionItem>> execMapping = 
					mapper.mapWorkload(cmdHandler.getServerAdresses(), cmd);
			cmdHandler.executeCommands(cmd, execMapping);
			processServerResponse(execMapping, cmd);
		}
		
		cmdIn.close();
		cmdHandler.closeConnections();
	}
	
	public UserCommand receiveCommand() {

		while(true) {
			System.out.print(BENCHMARK_TOP_MENU);
			String line = cmdIn.nextLine();
			
			Benchmark rbin = null;
			if(line.trim().equals(EXIT_CMD)) {
				return new ExitCmd();
			}
			else if((rbin = isBenchmark(line.trim())) != null) {
				DataSetSize dsetSize = displayDataSetSizeMenu();
				if(dsetSize != null) {
					System.out.println("Benchmark: " + rbin + " selected with a " + dsetSize + 
							" data set.\nRunning on server ...");
					return new RunBenchCmd(rbin, dsetSize);
				}
			} else {
				System.err.println("ERROR: Invalid input.");
			}
		}
		
	}
	
	private DataSetSize displayDataSetSizeMenu() {
		while(true) {
			System.out.print(BENCHMARK_DSET_SIZE_MENU);
			String line = cmdIn.nextLine();
			
			DataSetSize dsetSize = null;
			if(line.trim().equals(MENU_UP)) {
				System.out.println();
				return null;
			}
			else if((dsetSize = isDsetSize(line.trim())) != null) {
				return dsetSize;
			} else {
				System.err.println("ERROR: Invalid input.");
			}
		}
	}

	private DataSetSize isDsetSize(String dsetSize) {
		try {
			return DataSetSize.valueOf(dsetSize);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}

	private Benchmark isBenchmark(String bin) {
		try {
			return Benchmark.valueOf(bin);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	public void processServerResponse(Hashtable<String, List<ExecutionItem>> execMapping, UserCommand cmd) {
		if(execMapping == null || execMapping.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");
		
		System.out.println("###############################################");
		System.out.println("Original User Command:\n\t" + cmd);
		
		for (String serverAdr : execMapping.keySet()) {
			System.out.println("\n###############################################");
			System.out.println("## Execution result of server " + serverAdr);

			for(ExecutionItem item : execMapping.get(serverAdr)) {
				
				System.out.println("\n# Command: " + item.getCmdMsg());
				
				if(item.hasError()) {
					System.out.println("# Execution Error:");
					System.out.println(item.getErrorMsg());
					Exception e = item.getErrorException();
					if(e != null) {
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
				} else {
					switch(cmd.getType()) {
						case EXIT:
							exitClient = true;
							break;
						case RUNBENCH:
							System.out.println("# Result:");

							ResponseMessage response = item.getResponse();
							assert response != null : "Response message must not be null if error flag is not set.";
							assert response.getType() == ResponseType.BENCHSTATS : "Invalid response type at this point: " + response.getType();
							
							BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
							System.out.println("### Execution standard output:\n" + br.getStdOut());
							System.out.println("### Has Energy Log: " + br.hasEnergyLog());
							
							if(br.hasEnergyLog()) {
								System.out.println("### Energy Log:");
								System.out.println(br.getEnergyLog().getLogRecords().size() + " log entries found.");
								System.out.println("### Energy: " + br.getEnergyLog().getEnergyJ() + " J");
								System.out.println("### Runtime: " + br.getEnergyLog().getRuntimeMS() + " ms");
							}
							break;
						default: throw new RuntimeException("Unknown user command type: " + cmd.getType());
					}
				}
			}
		}
		
		System.out.println("###############################################");
	}

}
