package lancs.dividend.oclBenchMapper.message.response;

import lancs.dividend.oclBenchMapper.energy.EnergyLog;

public class BenchStatsResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 5588662656879162203L;
	
	private final String executionStdOut;
	private EnergyLog elog;
	private final long bechRuntimeMS;
	
	public BenchStatsResponseMessage(String stdOut, long benchmarkRuntimeMS) {
		super(ResponseType.BENCHSTATS);
		
		if(stdOut == null) 
			throw new IllegalArgumentException("Given standart output of benchmark execution must not be null.");
		
		executionStdOut = stdOut;
		bechRuntimeMS = benchmarkRuntimeMS;
	}
	
	public String getStdOut() { return executionStdOut; }
	public long getBenchmarkExecutionTimeMS() { return bechRuntimeMS; }
	
	public boolean hasEnergyLog() { return elog != null; }
	
	public void setEnergyLog(EnergyLog energyLog) {
		if(energyLog == null) 
			throw new IllegalArgumentException("Given energy log must not be null");
		
		elog = energyLog;
	}
	
	public EnergyLog getEnergyLog() {
		return elog;
	}
	
	@Override
	public String toString() {
		return executionStdOut;
	}
}
