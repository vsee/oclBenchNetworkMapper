package lancs.dividend.oclBenchMapper;

import java.nio.file.Path;

import lancs.dividend.oclBenchMapper.energy.OCLEnergyMonitor;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.utils.ShellCmdExecutor;

public class RodiniaRunner {

	public enum RodiniaBin { KMEANS, LUD }
	
	private final Path rodiniaHome;
	private final String monitoringPrefix;
	
	public RodiniaRunner(Path rodiniaHome, boolean monitorEnergy) {
		this.rodiniaHome = rodiniaHome;
		
		if(monitorEnergy)
			monitoringPrefix = OCLEnergyMonitor.getInstance().getExecutionPrefix();
		else
			monitoringPrefix = "";
	}

	public ResponseMessage run(RodiniaBin binary, String args) {
		
		switch(binary) {
			case KMEANS:
				StringBuilder cmdBld = new StringBuilder();
				
				// enter benchmark directory
				Path kmeansDir = rodiniaHome.resolve("opencl/kmeans");
				cmdBld.append("cd ").append(kmeansDir).append(";");
				
				// energy profiling if activated
				cmdBld.append(monitoringPrefix);
				
				// execute command
				// TODO include args
				cmdBld.append("./kmeans -o -i ../../data/kmeans/kdd_cup");
				
				String result = ShellCmdExecutor.executeCmd(cmdBld.toString(), true);
				System.out.println(result);
				
				// TODO evaluate results and send proper stats
				return new BenchStatsResponseMessage(0.0, 0.0);
			default:
				return new ErrorResponseMessage("Rodinia benchmark binary not handled by server: " + binary.name());
		}
	}

}
