package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;
import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;

/**
 * The predictive mapper assigns a benchmark to an execution device (CPU,GPU) for 
 * a specific server architecture. Assignments depend on the benchmark's workload and
 * the benchmarks openCL kernel.
 * 
 * The assignment decision is based on a model built for the server architecture which
 * was trained using openCL kernel features. It is able to predict the most energy efficient
 * and runtime efficient execution device for the corresponding architecture.
 * 
 * For demo purposes, the predictive mapper does not execute live predictions in this version.
 * Instead it selects the optimal benchmark to server device mapping from a list of predictions
 * made offline ahead of time.
 * 
 * @author vseeker
 */
public class PredictiveMapper implements WorkloadMapper {
	
	// TODO read prediction depending on server architecture
	private final Hashtable<Benchmark, Hashtable<DataSetSize, ExecutionDevice>> preDeviceMapping;
	
	public PredictiveMapper(PredictiveMapperConfig config) {
		preDeviceMapping = OclBenchMapperCsvHandler.parsePrecomputedMapping(config.mappingPrecompute);
	}

	@Override
	public Hashtable<String, CmdToDeviceMapping> mapWorkload(String[] serverAdresses, UserCommand cmd) {
		if(serverAdresses == null || serverAdresses.length == 0)
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<String, CmdToDeviceMapping> map = new Hashtable<>();
		
		for(String s : serverAdresses) {
			ExecutionDevice device = null;
			
			if(cmd.getType() == CmdType.RUNBENCH) {
				device = getExecutionDevice((RunBenchCmd) cmd, s);
			} else {
				device = OclMapperServer.DEFAULT_SEVER_EXECUTION_DEVICE;
			}
			
			map.put(s, new CmdToDeviceMapping(cmd, device));
		}
		
		return map;
	}
	

	private ExecutionDevice getExecutionDevice(RunBenchCmd originalCommand, String serverId) {
		// TODO include the server id to pick the predictions for the corresponding architecture
		
		Hashtable<DataSetSize, ExecutionDevice> dataMap = preDeviceMapping.get(originalCommand.getBinaryName());
		if(dataMap == null) throw new RuntimeException("No data set size mappings found for benchmark: " + originalCommand.getBinaryName());
		
		ExecutionDevice device = dataMap.get(originalCommand.getDataSetSize());
		if(device == null) throw new RuntimeException("No device mappings found for benchmark: " 
				+ originalCommand.getBinaryName() + " with dataset size " + originalCommand.getDataSetSize());
		
		return device;
	}



}
