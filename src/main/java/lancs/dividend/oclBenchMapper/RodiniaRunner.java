package lancs.dividend.oclBenchMapper;

import java.nio.file.Path;

import lancs.dividend.oclBenchMapper.OclMapperClient.RodiniaBins;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.utils.ShellCmdExecutor;

public class RodiniaRunner {

	private final Path rodiniaHome;
	
	public RodiniaRunner(Path rodiniaHome) {
		this.rodiniaHome = rodiniaHome;
	}

	public ResponseMessage run(RodiniaBins binary, String args) {
		
		switch(binary) {
			case KMEANS:
				StringBuilder cmdBld = new StringBuilder();
				
				// enter benchmark directory
				Path kmeansDir = rodiniaHome.resolve("opencl/kmeans");
				cmdBld.append("cd ").append(kmeansDir).append(";");
				
				// execute command
				// TODO include args
				cmdBld.append("./run");
				
				String result = ShellCmdExecutor.executeCmd(cmdBld.toString(), true);
				System.out.println(result);
				
				// TODO evaluate results and send proper stats
				return new BenchStatsResponseMessage(0.0, 0.0);
			default:
				return new ErrorResponseMessage("Unhandled rodinia benchmark binary: " + binary.name());
		}
	}

}
