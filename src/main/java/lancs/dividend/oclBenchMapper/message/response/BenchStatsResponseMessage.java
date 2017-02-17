package lancs.dividend.oclBenchMapper.message.response;

import java.util.StringJoiner;

public class BenchStatsResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 5588662656879162203L;
	
	private final double energy;
	private final double runtimeMS;
	
	public BenchStatsResponseMessage(double e, double t) {
		super(ResponseType.BENCHSTATS);
		energy = e;
		runtimeMS = t;
	}
	
	public double getEnergy() { return energy; }
	
	public double getRuntime() { return runtimeMS; }
	
	@Override
	public String toString() {
		return new StringJoiner(CMD_SEP).add(type.name())
				.add(Double.toString(energy))
				.add(Double.toString(runtimeMS)).toString();
	}
}
