package lancs.dividend.oclBenchMapper.server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import lancs.dividend.oclBenchMapper.energy.EnergyLog;
import lancs.dividend.oclBenchMapper.energy.OCLEnergyMonitor;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.utils.ShellCmdExecutor;

public class RodiniaRunner {

	public enum RodiniaBin { KMEANS }
	public enum DataSetSize { SMALL, MEDIUM, LARGE }
	
	private final Path rodiniaHome;
	
	public RodiniaRunner(Path rodiniaHome) {
		this.rodiniaHome = rodiniaHome;
	}

	public ResponseMessage run(RodiniaBin binary, DataSetSize dsetSize, ExecutionDevice device, boolean monitorEnergy) {
		
		ResponseMessage response = null;
		
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
		
		switch(binary) {
			case KMEANS:
				response = executeKmeans(execPrefix, dsetSize, device);
				break;
			default:
				response = new ErrorResponseMessage("Rodinia benchmark binary not handled by server: " + binary.name());
				break;
		}
		
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

	
	private static final String KMEANS_SMALL_DSET = "../../data/kmeans/100";
	private static final String KMEANS_MEDIUM_DSET = "../../data/kmeans/204800.txt";
	private static final String KMEANS_LARGE_DSET = "../../data/kmeans/819200.txt";
	
	private static final String KMEANS_HOME_CPU = "opencl/kmeans_cpu";
	private static final String KMEANS_HOME_GPU = "opencl/kmeans_gpu";
	
	private ResponseMessage executeKmeans(String execPrefix, DataSetSize dsetSize, ExecutionDevice device) {
		StringBuilder cmdBld = new StringBuilder();
		
		// enter benchmark directory
		Path kmeansDir;
		switch(device) {
			case CPU: kmeansDir = rodiniaHome.resolve(KMEANS_HOME_CPU); break;
			case GPU: kmeansDir = rodiniaHome.resolve(KMEANS_HOME_GPU); break;
			default: return new ErrorResponseMessage("Unknown execution device: " + device);
		}
		cmdBld.append("cd ").append(kmeansDir).append(";");
		
		// energy profiling if activated
		cmdBld.append(execPrefix);
		
		String dataset;
		switch(dsetSize) {
			case SMALL: dataset = KMEANS_SMALL_DSET; break;
			case MEDIUM: dataset = KMEANS_MEDIUM_DSET; break;
			case LARGE: dataset = KMEANS_LARGE_DSET; break;
			default: throw new IllegalArgumentException("Invalid dataset size for kmeans benchmark: " + dsetSize);
		}
		
		// execute command
		cmdBld.append("./kmeans -o -i ").append(dataset);
		
		String stdout = ShellCmdExecutor.executeCmd(cmdBld.toString(), true);
		System.out.println(stdout);
		
		if(stdout.contains("ERROR"))
			return new ErrorResponseMessage("Benchmark execution failed.\n" + stdout);
		
		return new BenchStatsResponseMessage(stdout);
	}

}
