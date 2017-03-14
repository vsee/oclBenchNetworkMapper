package lancs.dividend.oclBenchMapper.benchmark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Optional;

import lancs.dividend.oclBenchMapper.energy.EnergyLog;
import lancs.dividend.oclBenchMapper.energy.OCLEnergyMonitor;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;
import lancs.dividend.oclBenchMapper.utils.ShellCmdExecutor;

public class BenchmarkRunner {

	public enum DataSetSize { SMALL, MEDIUM, LARGE }
	
	private final Hashtable<Benchmark, Hashtable<ExecutionDevice, BenchExecArgs>> execArgConfig;
	private final Hashtable<Benchmark, Hashtable<DataSetSize, String>> dataArgConfig;
	
	public BenchmarkRunner(Path execConfigCsv, Path dataConfigCsv) {
		if(execConfigCsv == null) throw new IllegalArgumentException("Benchmark execution configuration must not be null.");
		if(dataConfigCsv == null) throw new IllegalArgumentException("Benchmark data configuration must not be null.");
		
		execArgConfig = OclBenchMapperCsvHandler.parseBenchmarkExecConfig(execConfigCsv);
		dataArgConfig = OclBenchMapperCsvHandler.parseBenchmarkDataConfig(dataConfigCsv);
	}

	public ResponseMessage run(Benchmark binary, DataSetSize dsetSize, ExecutionDevice device, boolean monitorEnergy) {
		String execPrefix = "";
		if(monitorEnergy) {
			try {
				execPrefix = OCLEnergyMonitor.getInstance().startMonitoring();
			} catch (IOException e) {
				System.err.println("ERROR: Starting energy monitor failed: " + e);
				e.printStackTrace();
				OCLEnergyMonitor.getInstance().resetMonitor();
				return new ErrorResponseMessage("Starting energy monitor failed: " + e.getMessage());
			}
		}
		
		ResponseMessage response = executeBenchmark(execPrefix, binary, dsetSize, device);
		assert response != null : "Response message must be set at this point.";
		
		if(monitorEnergy) {
			try {
				OCLEnergyMonitor.getInstance().endMonitoring();
				if(response.getType() == ResponseType.BENCHSTATS) {
					Optional<EnergyLog> elog = OCLEnergyMonitor.getInstance().getEnergyLog();
					if(elog.isPresent())
						((BenchStatsResponseMessage) response).setEnergyLog(elog.get());
				}
			} catch (IOException e) {
				System.err.println("ERROR: Ending energy monitor failed: " + e);
				e.printStackTrace();
				OCLEnergyMonitor.getInstance().resetMonitor();
				return new ErrorResponseMessage("Ending energy monitor failed: " + e.getMessage());
			}
		}
		
		return response;
	}
	
	private ResponseMessage executeBenchmark(String execPrefix, Benchmark bin, DataSetSize dsetSize, ExecutionDevice device) {
		StringBuilder cmdBld = new StringBuilder();
		
		// enter benchmark directory
		String kmeansDir = execArgConfig.get(bin).get(device).binDir;
		cmdBld.append("cd ").append(kmeansDir).append(";");
		
		// energy profiling if activated
		cmdBld.append(execPrefix);
		
		// get correct data file
		String dataset = dataArgConfig.get(bin).get(dsetSize);
		
		// execute command
		String execCmd = execArgConfig.get(bin).get(device).execCmd;
		cmdBld.append(execCmd).append(" ").append(dataset);
		
		String stdout = ShellCmdExecutor.executeCmd(cmdBld.toString(), true);
		System.out.println(stdout);
		
		if(stdout.contains("ERROR"))
			return new ErrorResponseMessage("Benchmark execution failed.\n" + stdout);
		
		return new BenchStatsResponseMessage(stdout);
	}

}
