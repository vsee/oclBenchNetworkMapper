package lancs.dividend.oclBenchMapper.ui;

import java.util.Hashtable;
import java.util.Scanner;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
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

	public ClientConsoleUi() {
        cmdIn = new Scanner(System.in);

		// Generate menu strings
		StringJoiner join = new StringJoiner(",","{","}");
		for (RodiniaBin b : RodiniaBin.values()) join.add(b.name());
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
	public void run(ClientConnectionHandler cmdHandler) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		
		while(true) {
			UserCommand cmd = receiveCommand();
			Hashtable<ServerConnection, ExecutionItem> executionMap = new Hashtable<>();

			if(!cmdHandler.handleUserCommand(cmd, executionMap)) break;
			
			processServerResponse(executionMap, cmd);
		}
		
		cmdIn.close();
		cmdHandler.closeConnections();
	}
	
	public UserCommand receiveCommand() {

		while(true) {
			System.out.print(BENCHMARK_TOP_MENU);
			String line = cmdIn.nextLine();
			
			RodiniaBin rbin = null;
			if(line.trim().equals(EXIT_CMD)) {
				return new ExitCmd();
			}
			else if((rbin = isBenchmarkBin(line.trim())) != null) {
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

	private RodiniaBin isBenchmarkBin(String rodiniaBin) {
		try {
			return RodiniaBin.valueOf(rodiniaBin);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
	
	public void processServerResponse(Hashtable<ServerConnection, ExecutionItem> executionMap, UserCommand cmd) {
		if(executionMap == null || executionMap.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");
		
		System.out.println("###############################################");
		System.out.println("Original User Command:\n\t" + cmd);
		
		for (ServerConnection s : executionMap.keySet()) {
			ExecutionItem item = executionMap.get(s);

			System.out.println("\nExecution result of server " + s);
			System.out.println("Command:\n\t" + item.getCommand());
			System.out.println("Response:");
			
			if(!item.resultsAvailable())
				System.out.println("ERROR: No results received!");
			else {
				ResponseMessage response = item.getResponse();
				
				switch (response.getType()) {
					case BENCHSTATS:
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
					case ERROR:
						System.err.println("\tERROR: " + ((ErrorResponseMessage) response).getText());
						break;
					default:
						System.err.println("\tUnknown response type: " + response.getType());
						break;
				}
			}
		}
		
		System.out.println("###############################################");
	}

}
