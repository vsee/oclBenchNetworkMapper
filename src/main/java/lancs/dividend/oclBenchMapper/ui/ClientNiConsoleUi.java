package lancs.dividend.oclBenchMapper.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.energy.EnergyLog;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.mapping.PredictiveMapper;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.userCmd.ExitCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.utils.CSVResourceTools;

public class ClientNiConsoleUi implements UserInterface {


	private class BenchExecutionResults {
		public final double energyJ;
		public final double runtimeMS;
		public final double tradeoff; // the smaller the better
		public final RunBenchCmd cmd;
		
		public BenchExecutionResults(double e, double r, RunBenchCmd c) {
			energyJ = e;
			runtimeMS = r;
			cmd = c;
			tradeoff = energyJ * runtimeMS;
		}
	}

	private static final String EXECUTION_STATS_CSV = "executionStats.csv";
	private static final String OPTIMAL_MAPPING_CSV = "optimalMapping.csv";

	private static final String[] STATS_HEADER = new String[] {"bin","data","device","energyJ","runtimeMS"};
	private static final String EXPECTED_CMD_HEADER = "bin,data,device,iterations";
	private static final int HEADER_BIN_IDX = 0;
	private static final int HEADER_DATA_IDX = 1;
	private static final int HEADER_DEVICE_IDX = 2;
	private static final int HEADER_ITER_IDX = 3;
	
	private final List<UserCommand> execCmds;
	private final List<BenchExecutionResults> results;
	private final Hashtable<RodiniaBin, Hashtable<DataSetSize, 
								Hashtable<ExecutionDevice, 
									List<BenchExecutionResults>>>> bestMappingStats;
	
	private final Path statOutputDir;
	
	public ClientNiConsoleUi(NiConsoleConfig conf) {
		if(conf == null) throw new IllegalArgumentException("Given configuration must not be null.");
		
		execCmds = parseCmds(conf.cmdInputFile);
		execCmds.add(new ExitCmd());
		
		bestMappingStats = new Hashtable<>();
		
		statOutputDir = conf.statOutputDir;
		
		results = new ArrayList<>();
	}

	private List<UserCommand> parseCmds(Path cmdInputFile) {
		List<UserCommand> res = new ArrayList<>();
		checkHeader(cmdInputFile);
		
		List<List<String>> recs = null;
		try {
			recs = CSVResourceTools.readRecords(cmdInputFile);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		for (List<String> record : recs) {
			RodiniaBin rbin = RodiniaBin.valueOf(record.get(HEADER_BIN_IDX));
			DataSetSize data = DataSetSize.valueOf(record.get(HEADER_DATA_IDX));
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(HEADER_DEVICE_IDX));
			int iterations = Integer.valueOf(record.get(HEADER_ITER_IDX));
			
			for(int i = 0; i < iterations; i++) {
				res.add(new RunBenchCmd(rbin, data, dev));
			}
		}
		
		System.out.println("Successfully parsed " + res.size() + " execution commands from " + cmdInputFile);
		return res;
	}

	private void checkHeader(Path cmdInputFile) {
		List<String> header = null;
		try {
			header = CSVResourceTools.readHeader(cmdInputFile);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		StringJoiner join = new StringJoiner(Character.toString(CSVResourceTools.DEFAULT_SEPARATOR));
		for (String column : header) join.add(column);
		if(!join.toString().equals(EXPECTED_CMD_HEADER))
			throw new RuntimeException("Unexpected header format in precomputation file: " + join);
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
		calculateAndSaveBestMapingToFile();
		
		cmdHandler.closeConnections();
	}

	private void calculateAndSaveBestMapingToFile() {
		Path mappingOutput = statOutputDir.resolve(OPTIMAL_MAPPING_CSV);
		String[] header = PredictiveMapper.EXPECTED_HEADER.split(
				Character.toString(CSVResourceTools.DEFAULT_SEPARATOR));

		List<String[]> records = new ArrayList<>(results.size());
		for(RodiniaBin bin : bestMappingStats.keySet()) {
			for(DataSetSize data : bestMappingStats.get(bin).keySet()) {
				
				ExecutionDevice bestDevice = null;
				double lowestAvgTradeoff = Double.MAX_VALUE;
				for(ExecutionDevice device : bestMappingStats.get(bin).get(data).keySet()) {
					List<BenchExecutionResults> mappings = bestMappingStats.get(bin).get(data).get(device);
					
					double avg_tradeoff = mappings.stream().mapToDouble(r -> r.tradeoff).sum() / mappings.size();
					if(bestDevice == null || avg_tradeoff < lowestAvgTradeoff) {
						bestDevice = device;
						lowestAvgTradeoff = avg_tradeoff;
					}
				}
				
				records.add(new String[] { bin.name(), data.name(), bestDevice.name() });
			}
		}
		
		CSVResourceTools.writeCSVFile(mappingOutput, header, records);
		System.out.println("Optimal mapping written to: " + mappingOutput);
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
						RunBenchCmd bcmd = (RunBenchCmd) cmd;
						EnergyLog elog = br.getEnergyLog();
						BenchExecutionResults execRes = new BenchExecutionResults(elog.getEnergyJ(), elog.getRuntimeMS(), bcmd);
						
						results.add(execRes);
						
						if(!bestMappingStats.containsKey(bcmd.getBinaryName()))
							bestMappingStats.put(bcmd.getBinaryName(), new Hashtable<>());
						if(!bestMappingStats.get(bcmd.getBinaryName()).containsKey(bcmd.getDataSetSize()))
							bestMappingStats.get(bcmd.getBinaryName()).put(bcmd.getDataSetSize(), new Hashtable<>());
						if(!bestMappingStats.get(bcmd.getBinaryName()).get(bcmd.getDataSetSize()).containsKey(bcmd.getExecutionDevice()))
							bestMappingStats.get(bcmd.getBinaryName()).get(bcmd.getDataSetSize()).put(bcmd.getExecutionDevice(), new ArrayList<>());
						
						bestMappingStats.get(bcmd.getBinaryName()).get(bcmd.getDataSetSize()).get(bcmd.getExecutionDevice()).add(execRes);
						
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
