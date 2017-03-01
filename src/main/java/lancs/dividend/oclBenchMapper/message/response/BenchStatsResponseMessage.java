package lancs.dividend.oclBenchMapper.message.response;

import java.util.Optional;

import lancs.dividend.oclBenchMapper.energy.EnergyLog;

public class BenchStatsResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 5588662656879162203L;
	
	private final String executionStdOut;
	private Optional<EnergyLog> elog = Optional.empty();
	
	public BenchStatsResponseMessage(String stdOut) {
		super(ResponseType.BENCHSTATS);
		
		if(stdOut == null) 
			throw new IllegalArgumentException("Given standart output of benchmark execution must not be null.");
		
		executionStdOut = stdOut;
	}
	
	public String getStdOut() { return executionStdOut; }
	
	public void setEnergyLog(EnergyLog energyLog) {
		if(energyLog == null) 
			throw new IllegalArgumentException("Given energy log must not be null");
		
		elog = Optional.of(energyLog);
	}
	
	public Optional<EnergyLog> getEnergyLog() {
		return elog;
	}
	
	@Override
	public String toString() {
		return executionStdOut;
	}
}
