package lancs.dividend.oclBenchMapper.energy;

import java.nio.file.Path;

public class EnergySimulationLog extends EnergyLog {

	private static final long serialVersionUID = -128553005752783047L;

	private final double simEnergyJ;
	private final double simRuntimeMS;
	
	public EnergySimulationLog(Path logFile, double energyJ, double runtimeMS) {
		super(logFile);
		
		this.simEnergyJ = energyJ;
		this.simRuntimeMS = runtimeMS;
	}
	
	@Override
	public double getEnergyJ() { return simEnergyJ; }
	@Override
	public double getRuntimeMS() { return simRuntimeMS; }
}
