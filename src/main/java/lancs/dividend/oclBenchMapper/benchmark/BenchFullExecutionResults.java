package lancs.dividend.oclBenchMapper.benchmark;

public class BenchFullExecutionResults extends BenchExecutionResults {
	
	private static final long serialVersionUID = -2934741438342792956L;
	
	public final String stdOut;
	
	public BenchFullExecutionResults(double e, double r, String stdOut) {
		super(e, r);
		this.stdOut = stdOut;
	}
}