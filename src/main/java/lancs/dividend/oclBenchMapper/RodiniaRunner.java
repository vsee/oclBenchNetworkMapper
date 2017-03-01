package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import lancs.dividend.oclBenchMapper.energy.EnergyLog;
import lancs.dividend.oclBenchMapper.energy.OCLEnergyMonitor;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.utils.ShellCmdExecutor;

public class RodiniaRunner {

	public enum RodiniaBin { KMEANS, LUD }
	public enum DataSetSize { SMALL, MEDIUM, LARGE }
	
	private final Path rodiniaHome;
	
	public RodiniaRunner(Path rodiniaHome) {
		this.rodiniaHome = rodiniaHome;
	}

	public ResponseMessage run(RodiniaBin binary, DataSetSize dsetSize, boolean monitorEnergy) {
		
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
				response = executeKmeans(execPrefix, dsetSize);
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

	private ResponseMessage executeKmeans(String execPrefix, DataSetSize dsetSize) {
		StringBuilder cmdBld = new StringBuilder();
		
		// enter benchmark directory
		Path kmeansDir = rodiniaHome.resolve("opencl/kmeans");
		cmdBld.append("cd ").append(kmeansDir).append(";");
		
		// energy profiling if activated
		cmdBld.append(execPrefix);
		
		// execute command
		// TODO include dataset size
		cmdBld.append("./kmeans -o -i ../../data/kmeans/kdd_cup");
		
		String stdout = ShellCmdExecutor.executeCmd(cmdBld.toString(), true);
		System.out.println(stdout);
		
		return new BenchStatsResponseMessage(stdout);
	}

}
