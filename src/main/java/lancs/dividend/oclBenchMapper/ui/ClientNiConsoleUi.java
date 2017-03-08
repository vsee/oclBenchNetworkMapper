package lancs.dividend.oclBenchMapper.ui;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.energy.EnergyLog;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.userCmd.ExitCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.utils.CSVResourceTools;

public class ClientNiConsoleUi implements UserInterface {

	private class BenchExecutionResults {
		public final double energyJ;
		public final double runtimeMS;
		public final RunBenchCmd cmd;
		
		public BenchExecutionResults(double e, double r, RunBenchCmd c) {
			energyJ = e;
			runtimeMS = r;
			cmd = c;
		}
	}

	private static final String EXECUTION_STATS_CSV = "executionStats.csv";
	private static final String[] STATS_HEADER = new String[] {"bin","data","device","energyJ","runtimeMS"};
	
	private final List<UserCommand> execCmds;
	private final List<BenchExecutionResults> results;
	
	private final Path statOutputDir;
	
	public ClientNiConsoleUi(NiConsoleConfig conf) {
		if(conf == null) throw new IllegalArgumentException("Given configuration must not be null.");
		
		execCmds = parseCmds(conf.cmdInputFile);
		execCmds.add(new ExitCmd());
		
		statOutputDir = conf.statOutputDir;
		
		results = new ArrayList<>();
	}

	private List<UserCommand> parseCmds(Path cmdInputFile) {
		// TODO Auto-generated method stub
		// sysout that many cmds parsed
		
		List<UserCommand> res = new ArrayList<>();
		res.add(new RunBenchCmd(RodiniaBin.KMEANS, DataSetSize.SMALL));
		res.add(new RunBenchCmd(RodiniaBin.KMEANS, DataSetSize.MEDIUM));
		res.add(new RunBenchCmd(RodiniaBin.KMEANS, DataSetSize.LARGE));

		System.out.println("Successfully parsed " + res.size() + " execution commands from " + cmdInputFile);
		return res;
	}

	@Override
	public void run(ClientConnectionHandler cmdHandler) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		
		System.out.println();
		
		int current = 1;
		for (UserCommand cmd : execCmds) {
			Hashtable<ServerConnection, ExecutionItem> executionMap = new Hashtable<>();

			System.out.println("## Executing command " + current++ + "/" + execCmds.size() + ": " + cmd);
			if(!cmdHandler.handleUserCommand(cmd, executionMap)) break;
			
			processServerResponse(executionMap, cmd);
		}
		
		saveExecutionStatisticsToFile();
		// TODO calculate best mapping and save to file
		
		cmdHandler.closeConnections();
	}

	private void saveExecutionStatisticsToFile() {
		Path statOutput = statOutputDir.resolve(EXECUTION_STATS_CSV);
		List<String[]> records = new ArrayList<>(results.size());
		for (BenchExecutionResults res : results) {
			records.add(new String[] { 
					res.cmd.getBinaryName().name(),
					res.cmd.getDataSetSize().name(),
					res.cmd.getExecutionDevice().name(),
					Double.toString(res.energyJ),
					Double.toString(res.runtimeMS)
					});
		}
		CSVResourceTools.writeCSVFile(statOutput, STATS_HEADER, records);
		System.out.println("Execution statistics written to: " + statOutput);
	}

	private void processServerResponse(
			Hashtable<ServerConnection, ExecutionItem> executionMap,
			UserCommand cmd) {

		if(executionMap == null || executionMap.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");
		
		for (ServerConnection s : executionMap.keySet()) {
			ExecutionItem item = executionMap.get(s);

			if(!item.resultsAvailable())
				System.out.println("ERROR: No results received!");
			else {
				ResponseMessage response = item.getResponse();
				
				switch (response.getType()) {
					case BENCHSTATS:
						BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
						if(!br.hasEnergyLog()) {
							System.out.println("ERROR: No energy results received!");
							break;
						}
						EnergyLog elog = br.getEnergyLog();
						results.add(new BenchExecutionResults(elog.getEnergyJ(), 
								elog.getRuntimeMS(), (RunBenchCmd) cmd));
						System.out.println("Energy: " + elog.getEnergyJ() + " J - Runtime: " + elog.getRuntimeMS() + " ms");
						break;
					case ERROR:
						System.err.println("ERROR: " + ((ErrorResponseMessage) response).getText());
						break;
					default:
						System.err.println("ERROR: Unknown response type: " + response.getType());
						break;
				}
			}
		}
	}

}
