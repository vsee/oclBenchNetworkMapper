package lancs.dividend.oclBenchMapper.ui;

import java.util.Scanner;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.userCmd.ExitCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public class ClientConsoleInterface implements UserInterface {

	private final String EXIT_CMD = "exit";
	private final String BENCHMARK_LIST;
	private final String BENCHMARK_TOP_MENU;
	
	private final String MENU_UP = "back";
	private final String DSET_SIZE_LIST;
	private final String BENCHMARK_DSET_SIZE_MENU;
	
	private final Scanner cmdIn;

	public ClientConsoleInterface() {
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
	
	@Override
	public void exit() {
		cmdIn.close();
	}


}
