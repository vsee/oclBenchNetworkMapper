package lancs.dividend.oclBenchMapper.ui.gui;

import java.util.ArrayList;
import java.util.List;

public class GraphUpdate {
	
	private final List<Double> energyUpdates = new ArrayList<Double>();
	private final List<Double> energyBaseLines = new ArrayList<Double>();

	private final List<Double> runtimeUpdates = new ArrayList<Double>();
	private final List<Double> runtimeBaseLines = new ArrayList<Double>();

	public void addStatUpdate(double energyJ, double energyBaselineJ, double runtimeMS, double runtimeBaselineMS) {
		energyUpdates.add(energyJ);
		energyBaseLines.add(energyBaselineJ);
		runtimeUpdates.add(runtimeMS);
		runtimeBaseLines.add(runtimeBaselineMS);
	}
	
	public double getNormalisedEnergy() {
		return normalise(energyBaseLines, energyUpdates);
	}
	
	public double getNormalisedRuntime() {
		return normalise(runtimeBaseLines, runtimeUpdates);
	}

	private double normalise(List<Double> baseLines, List<Double> updates) {
		assert baseLines.size() == updates.size() : "Baselines and updates do not match";
		
		double sum = updates.stream().mapToDouble(d -> d).sum();
		double baseline = baseLines.stream().mapToDouble(d -> d).sum();
		return sum / baseline;
	}
}
