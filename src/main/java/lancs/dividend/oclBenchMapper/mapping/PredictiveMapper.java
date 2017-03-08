package lancs.dividend.oclBenchMapper.mapping;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.message.CommandMessage;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;
import lancs.dividend.oclBenchMapper.utils.CSVResourceTools;

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

	private static final String EXPECTED_HEADER = "RodiniaBin" + CSVResourceTools.DEFAULT_SEPARATOR
		+ "DataSetSize" + CSVResourceTools.DEFAULT_SEPARATOR + "ExecutionDevice";
	private static final int HEADER_BIN_IDX = 0;
	private static final int HEADER_DATA_IDX = 1;
	private static final int HEADER_DEVICE_IDX = 2;

	
	private final Hashtable<RodiniaBin, Hashtable<DataSetSize, ExecutionDevice>> preDeviceMapping;
	
	public PredictiveMapper(PredictiveMapperConfig config) {
		preDeviceMapping = parsePrecomputedMapping(config.mappingPrecompute);
	}
	
	private Hashtable<RodiniaBin, Hashtable<DataSetSize, ExecutionDevice>> parsePrecomputedMapping(
			Path mappingPrecomputation) {		
		if (mappingPrecomputation == null ||
			!mappingPrecomputation.toFile().exists() || 
			!mappingPrecomputation.toFile().isFile() || 
			!mappingPrecomputation.toFile().canRead())
				throw new IllegalArgumentException("Error file with precomputed mappings not found: " + mappingPrecomputation);
		
		Hashtable<RodiniaBin, Hashtable<DataSetSize, ExecutionDevice>> map =  new Hashtable<>();
		
		checkHeader(mappingPrecomputation);
		
		List<List<String>> recs = null;
		try {
			recs = CSVResourceTools.readRecords(mappingPrecomputation);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		for (List<String> record : recs) {
			RodiniaBin rbin = RodiniaBin.valueOf(record.get(HEADER_BIN_IDX));
			DataSetSize data = DataSetSize.valueOf(record.get(HEADER_DATA_IDX));
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(HEADER_DEVICE_IDX));
			
			if(!map.containsKey(rbin)) map.put(rbin, new Hashtable<>());
			map.get(rbin).put(data, dev);
		}
		
		return map;
	}

	private void checkHeader(Path mappingPrecomputation) {
		List<String> header = null;
		try {
			header = CSVResourceTools.readHeader(mappingPrecomputation);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		StringJoiner join = new StringJoiner(Character.toString(CSVResourceTools.DEFAULT_SEPARATOR));
		for (String column : header) join.add(column);
		if(!join.toString().equals(EXPECTED_HEADER))
			throw new RuntimeException("Unexpected header format in precomputation file: " + join);
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
