package lancs.dividend.oclBenchMapper.ui;

import java.util.Scanner;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.ExitCmdMessage;
import lancs.dividend.oclBenchMapper.message.cmd.RunBenchCmdMessage;

public class ClientConsoleInterface implements UserInterface {

	private final String EXIT_CMD = "exit";
	private final String BENCHMARK_LIST;
	private final String BENCHMARK_TOP_MENU;
	
	private final Scanner cmdIn;

	public ClientConsoleInterface() {
        cmdIn = new Scanner(System.in);

		// Generate menu strings
		StringJoiner join = new StringJoiner(",","{","}");
		for (RodiniaBin b : RodiniaBin.values()) join.add(b.name());
		BENCHMARK_LIST = join.toString();
		
		BENCHMARK_TOP_MENU = 
		"\n1. Enter Benchmark name from selection: " + BENCHMARK_LIST + "\n" +
		"2. Enter '" + EXIT_CMD + "' to shut down client.\n>> ";
	}
	
	@Override
	public CommandMessage parseCommand() {

		while(true) {
			System.out.print(BENCHMARK_TOP_MENU);
			String line = cmdIn.nextLine();
			
			RodiniaBin rbin = null;
			if(line.trim().equals(EXIT_CMD)) {
				return new ExitCmdMessage();
			}
			else if((rbin = isBenchmarkBin(line.trim())) != null) {
				System.out.println("Benchmark: " + rbin + " selected. Running on server ...");
				return new RunBenchCmdMessage(rbin, "");
			} else {
				System.err.println("ERROR: Invalid input.");
			}
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
