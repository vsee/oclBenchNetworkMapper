package lancs.dividend.oclBenchMapper.ui.console;

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
import lancs.dividend.oclBenchMapper.server.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.BenchmarkRunner.BenchmarkBin;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.userCmd.ExitCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;

public class ClientNiConsoleUi implements UserInterface {

	private static final String EXECUTION_STATS_CSV = "executionStats.csv";
	private static final String OPTIMAL_MAPPING_CSV = "optimalMapping.csv";
	
	private final List<UserCommand> execCmds;
	private final List<BenchExecutionResults> results;
	private final Hashtable<BenchmarkBin, Hashtable<DataSetSize, 
								Hashtable<ExecutionDevice, 
									List<BenchExecutionResults>>>> bestMappingStats;
	
	private final Path statOutputDir;
	
	public ClientNiConsoleUi(NiConsoleConfig conf) {
		if(conf == null) throw new IllegalArgumentException("Given configuration must not be null.");
		
		execCmds = OclBenchMapperCsvHandler.parseUserCommands(conf.cmdInputFile);
		execCmds.add(new ExitCmd());
		
		bestMappingStats = new Hashtable<>();
		
		statOutputDir = conf.statOutputDir;
		
		results = new ArrayList<>();
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
		
		saveStatsAndBestMapping();
		
		cmdHandler.closeConnections();
	}

	private void saveStatsAndBestMapping() {
		
		List<String[]> mappingRecords = new ArrayList<>();
		List<String[]> statsRecords = new ArrayList<>();
		
		for(BenchmarkBin bin : bestMappingStats.keySet()) {
			for(DataSetSize data : bestMappingStats.get(bin).keySet()) {
				
				ExecutionDevice bestDevice = null;
				double lowestAvgTradeoff = Double.MAX_VALUE;
				for(ExecutionDevice device : bestMappingStats.get(bin).get(data).keySet()) {
					List<BenchExecutionResults> mappings = bestMappingStats.get(bin).get(data).get(device);
					
					double avg_tradeoff = 0;
					double avg_energyJ = 0;
					double avg_runtimeMS = 0;
					for(BenchExecutionResults map : mappings) {
						avg_tradeoff += map.tradeoff;
						avg_energyJ += map.energyJ;
						avg_runtimeMS += map.runtimeMS;
					}
					avg_tradeoff /= mappings.size();
					avg_energyJ /= mappings.size();
					avg_runtimeMS /= mappings.size();
					
					// remember best execution device with lowest tradeoff
					if(bestDevice == null || avg_tradeoff < lowestAvgTradeoff) {
						bestDevice = device;
						lowestAvgTradeoff = avg_tradeoff;
					}
					
					statsRecords.add(new String[] { bin.name(), data.name(), device.name(), 
							Double.toString(avg_energyJ), Double.toString(avg_runtimeMS) });
				}
				
				mappingRecords.add(new String[] { bin.name(), data.name(), bestDevice.name() });
			}
		}
		
		OclBenchMapperCsvHandler.writePrecomputedMapping(statOutputDir.resolve(OPTIMAL_MAPPING_CSV), mappingRecords);
		OclBenchMapperCsvHandler.writeExecutionStats(statOutputDir.resolve(EXECUTION_STATS_CSV), statsRecords);
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
						BenchExecutionResults execRes = new BenchExecutionResults(elog.getEnergyJ(), elog.getRuntimeMS());
						
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
