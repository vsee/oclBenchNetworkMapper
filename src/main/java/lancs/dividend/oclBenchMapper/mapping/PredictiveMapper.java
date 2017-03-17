package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.server.ServerDescription;
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
	
	private final Hashtable<String, Hashtable<Benchmark, Hashtable<String, ExecutionDevice>>> preDeviceMapping;
	
	public PredictiveMapper(PredictiveMapperConfig config) {
		preDeviceMapping = OclBenchMapperCsvHandler.parsePrecomputedMapping(config.mappingPrecompute);
	}

	@Override
	public Hashtable<String, CmdToDeviceMapping> mapWorkload(ServerDescription[] servers, UserCommand cmd) 
			throws WorkloadDistributionException {
		
		if(servers == null || servers.length == 0)
			throw new IllegalArgumentException("Given server descriptions must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<String, CmdToDeviceMapping> map = new Hashtable<>();
		
		for(ServerDescription descr : servers) {
			ExecutionDevice device = null;
			
			if(cmd.getType() == CmdType.RUNBENCH) {
				device = getExecutionDevice((RunBenchCmd) cmd, descr);
			} else {
				device = OclMapperServer.DEFAULT_SEVER_EXECUTION_DEVICE;
			}
			
			map.put(descr.address, new CmdToDeviceMapping(cmd, device));
		}
		
		return map;
	}
	

	private ExecutionDevice getExecutionDevice(RunBenchCmd originalCommand, ServerDescription descr) throws WorkloadDistributionException {

		Hashtable<Benchmark, Hashtable<String, ExecutionDevice>> benchmarkMap = preDeviceMapping.get(descr.architecture);
		if(benchmarkMap == null) throw new WorkloadDistributionException("No mapping available for server architecture: " + descr.architecture);
		
		Hashtable<String, ExecutionDevice> dataMap = benchmarkMap.get(originalCommand.getBinaryName());
		if(dataMap == null) throw new WorkloadDistributionException("No data set size mappings found for benchmark: " + originalCommand.getBinaryName());
		
		ExecutionDevice device = dataMap.get(originalCommand.getDataSetSize());
		if(device == null) throw new WorkloadDistributionException("No device mappings found for benchmark: " 
				+ originalCommand.getBinaryName() + " with dataset size " + originalCommand.getDataSetSize());
		
		return device;
	}



}
