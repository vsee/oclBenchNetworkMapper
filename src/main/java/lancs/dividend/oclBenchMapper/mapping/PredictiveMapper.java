package lancs.dividend.oclBenchMapper.mapping;

import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.CommandMessage;
import lancs.dividend.oclBenchMapper.server.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.BenchmarkRunner.BenchmarkBin;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
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
	
	private final Hashtable<BenchmarkBin, Hashtable<DataSetSize, ExecutionDevice>> preDeviceMapping;
	
	public PredictiveMapper(PredictiveMapperConfig config) {
		preDeviceMapping = OclBenchMapperCsvHandler.parsePrecomputedMapping(config.mappingPrecompute);
	}

	@Override
	public void mapWorkload(List<ServerConnection> servers, UserCommand cmd,
			Hashtable<ServerConnection, ExecutionItem> executionMap) {
		
		if(servers == null || servers.isEmpty())
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		if(executionMap == null)
			throw new IllegalArgumentException("Given execution map must not be null.");
		
		executionMap.clear();
		
		if(cmd.getType() == CmdType.RUNBENCH) {
			setExecutionDevice((RunBenchCmd) cmd);
		}
		
		for(ServerConnection s : servers) {
			executionMap.put(s, new ExecutionItem(new CommandMessage(cmd)));
		}
	}

	private void setExecutionDevice(RunBenchCmd bcmd) {
		Hashtable<DataSetSize, ExecutionDevice> dataMap = preDeviceMapping.get(bcmd.getBinaryName());
		if(dataMap == null) throw new RuntimeException("No data set size mappings found for benchmark: " + bcmd.getBinaryName());
		
		ExecutionDevice device = dataMap.get(bcmd.getDataSetSize());
		if(device == null) throw new RuntimeException("No device mappings found for benchmark: " 
				+ bcmd.getBinaryName() + " with dataset size " + bcmd.getDataSetSize());
		
		bcmd.setExecutionDevice(device);
	}



}
