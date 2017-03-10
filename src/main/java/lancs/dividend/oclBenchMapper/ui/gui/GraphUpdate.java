package lancs.dividend.oclBenchMapper.ui.gui;

import java.util.ArrayList;
import java.util.List;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;

public class GraphUpdate {
	
	public final Benchmark bin;
	public final DataSetSize dset;
	public final ExecutionDevice dev;
	
	private final double energyJNorm;
	private final double runtimeMSNorm;
	
	private final List<Double> energyUpdates = new ArrayList<Double>();
	private final List<Double> runtimeUpdates = new ArrayList<Double>();


	public GraphUpdate(Benchmark bin, DataSetSize dset, ExecutionDevice dev, double energyJNorm, double runtimeMSNorm) {
		this.bin = bin;
		this.dset = dset;
		this.dev = dev;
		this.energyJNorm = energyJNorm;
		this.runtimeMSNorm = runtimeMSNorm;
	}
	
	public void addStatUpdate(double energyJ, double runtimeMS) {
		energyUpdates.add(energyJ);
		runtimeUpdates.add(runtimeMS);
	}
	
	public double getNormalisedEnergy() {
		return normalise(energyJNorm, energyUpdates);
	}
	
	public double getNormalisedRuntime() {
		return normalise(runtimeMSNorm, runtimeUpdates);
	}

	private double normalise(double baseLine, List<Double> updates) {
		double sum = updates.stream().mapToDouble(d -> d).sum();
		return sum / (updates.size() * baseLine);
	}
}