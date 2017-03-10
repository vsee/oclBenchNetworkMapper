package lancs.dividend.oclBenchMapper.server;

public class BenchExecArgs {
	public final String binDir;
	public final String execCmd;
	
	public BenchExecArgs(String binDir, String execCmd) {
		this.binDir = binDir;
		this.execCmd = execCmd;
	}
}