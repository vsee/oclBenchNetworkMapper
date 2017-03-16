package lancs.dividend.oclBenchMapper.ui.console;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.benchmark.BenchFullExecutionResults;
import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.client.ExecutionItem;
import lancs.dividend.oclBenchMapper.energy.EnergyLog;
import lancs.dividend.oclBenchMapper.mapping.CmdToDeviceMapping;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.server.ServerDescription;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.userCmd.ExitCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;

public class ClientNiConsoleUi implements UserInterface {

	private static final String EXECUTION_STATS_CSV = "executionStats.csv";
	private static final String OPTIMAL_MAPPING_CSV = "optimalMapping.csv";
	private static final String EXECUTION_FULLSTATS_DAT = "executionFullStats.dat";
	
	private final List<CmdToDeviceMapping> execCmds;
	private final List<BenchFullExecutionResults> results;
	private final Hashtable<Benchmark, Hashtable<DataSetSize, 
								Hashtable<ExecutionDevice, 
									List<BenchFullExecutionResults>>>> bestMappingStats;
	
	private final Path statOutputDir;
	private boolean exitClient;
	private final boolean saveFullStats;
	
	public ClientNiConsoleUi(NiConsoleConfig conf) {
		if(conf == null) throw new IllegalArgumentException("Given configuration must not be null.");
		
		execCmds = OclBenchMapperCsvHandler.parseUserCommands(conf.cmdInputFile);
		execCmds.add(new CmdToDeviceMapping(new ExitCmd(), OclMapperServer.DEFAULT_SEVER_EXECUTION_DEVICE));
		
		bestMappingStats = new Hashtable<>();
		
		statOutputDir = conf.statOutputDir;
		saveFullStats = conf.saveFullStats;
		results = new ArrayList<>();
	}
	
	@Override
	public void run(ClientConnectionHandler cmdHandler, WorkloadMapper mapper) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		if(mapper == null)
			throw new IllegalArgumentException("Given workload mapper must not be null.");
		if(cmdHandler.getServerDescriptions().length != 1) 
			throw new RuntimeException("Expected a single server for a non-interactive run.");
		
		ServerDescription serverDescr = cmdHandler.getServerDescriptions()[0];
		
		System.out.println();
		
		int current = 1;
		for (CmdToDeviceMapping cmdMapping : execCmds) {
			if(exitClient) break;
			System.out.println("## Executing command " + current++ + "/" + execCmds.size() + ": " + 
					cmdMapping.userCmd + " on " + cmdMapping.execDev);
			
			Hashtable<String, List<ExecutionItem>> execItems = new Hashtable<>();
			List<ExecutionItem> items = new ArrayList<>();
			items.add(new ExecutionItem(cmdMapping.userCmd, cmdMapping.execDev, serverDescr.address));
			execItems.put(serverDescr.address, items);
			
			cmdHandler.executeCommands(cmdMapping.userCmd, execItems);
			processServerResponse(execItems, cmdMapping.userCmd);
		}
		
		saveStatsAndBestMapping(serverDescr.architecture);
		
		cmdHandler.closeConnections();
	}
	
	private void saveStatsAndBestMapping(String architecture) {
		
		List<String[]> mappingRecords = new ArrayList<>();
		List<String[]> statsRecords = new ArrayList<>();
		
		for(Benchmark bin : bestMappingStats.keySet()) {
			for(DataSetSize data : bestMappingStats.get(bin).keySet()) {
				
				ExecutionDevice bestDevice = null;
				double lowestAvgTradeoff = Double.MAX_VALUE;
				for(ExecutionDevice device : bestMappingStats.get(bin).get(data).keySet()) {
					List<BenchFullExecutionResults> mappings = bestMappingStats.get(bin).get(data).get(device);
					
					double avg_tradeoff = 0;
					double avg_energyJ = 0;
					double avg_runtimeMS = 0;
					for(BenchFullExecutionResults map : mappings) {
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
					
					statsRecords.add(new String[] { architecture, bin.name(), data.name(), device.name(), 
							Double.toString(avg_energyJ), Double.toString(avg_runtimeMS) });
				}
				
				mappingRecords.add(new String[] { architecture, bin.name(), data.name(), bestDevice.name() });
			}
		}
		
		OclBenchMapperCsvHandler.writePrecomputedMapping(statOutputDir.resolve(OPTIMAL_MAPPING_CSV), mappingRecords);
		OclBenchMapperCsvHandler.writeExecutionStats(statOutputDir.resolve(EXECUTION_STATS_CSV), statsRecords);
		if(saveFullStats)
			OclBenchMapperCsvHandler.writeFullExecutionStats(statOutputDir.resolve(EXECUTION_FULLSTATS_DAT), results);
	}

	public void processServerResponse(Hashtable<String, List<ExecutionItem>> execMapping, UserCommand cmd) {
		if(execMapping == null || execMapping.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");
		
		
		for (String serverAdr : execMapping.keySet()) {

			System.out.println("\n## Execution result of server " + serverAdr);

			for(ExecutionItem item : execMapping.get(serverAdr)) {
				
				System.out.println("\n# Command: " + item.getCmd());
				System.out.println("Execution Device " + item.getExecDevice());
				
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
							if(!br.hasEnergyLog()) {
								System.out.println("ERROR: No energy results received!");
								break;
							}
							RunBenchCmd bcmd = (RunBenchCmd) cmd;
							EnergyLog elog = br.getEnergyLog();
							BenchFullExecutionResults execRes = 
									new BenchFullExecutionResults(bcmd.getBinaryName(), bcmd.getDataSetSize(), 
											item.getExecDevice(), elog.getEnergyJ(), elog.getRuntimeMS(), br.getStdOut());
							
							results.add(execRes);
							
							if(!bestMappingStats.containsKey(bcmd.getBinaryName()))
								bestMappingStats.put(bcmd.getBinaryName(), new Hashtable<>());
							if(!bestMappingStats.get(bcmd.getBinaryName()).containsKey(bcmd.getDataSetSize()))
								bestMappingStats.get(bcmd.getBinaryName()).put(bcmd.getDataSetSize(), new Hashtable<>());
							if(!bestMappingStats.get(bcmd.getBinaryName()).get(bcmd.getDataSetSize()).containsKey(item.getExecDevice()))
								bestMappingStats.get(bcmd.getBinaryName()).get(bcmd.getDataSetSize()).put(item.getExecDevice(), new ArrayList<>());
							
							bestMappingStats.get(bcmd.getBinaryName()).get(bcmd.getDataSetSize()).get(item.getExecDevice()).add(execRes);
							
							System.out.println("Energy: " + elog.getEnergyJ() + " J - Runtime: " + elog.getRuntimeMS() + " ms");
							break;
						default: throw new RuntimeException("Unknown user command type: " + cmd.getType());
					}
				}
			}
		}
	}

}
