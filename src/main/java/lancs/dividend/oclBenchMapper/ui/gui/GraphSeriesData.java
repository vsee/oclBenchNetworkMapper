package lancs.dividend.oclBenchMapper.ui.gui;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.server.ExecutionDevice;

public class GraphSeriesData {
	
	public final String name;
	public final List<Double> energyData = new ArrayList<>();
	public final List<Double> performanceData = new ArrayList<>();
	/** Specifies server addresses if an execution device is fixed for this server. */
	public final Hashtable<String, ExecutionDevice> fixedMapping;
	
	private double totalEnergyJ;
	private double totalRuntimeMS;
	
	private boolean initialValue;
	
	public GraphSeriesData(String name) {
		if(name == null) throw new IllegalArgumentException("Given seires name must not be null.");

		this.name = name;
		fixedMapping = new Hashtable<>();
		
		energyData.add(0.0);
		performanceData.add(0.0);
		initialValue = true;
	}

	public void addFixedMapping(String serverAddress, ExecutionDevice device) {
		if(serverAddress == null) throw new IllegalArgumentException("Given server address must not be null.");
		if(device == null) throw new IllegalArgumentException("Given execution device must not be null.");
		
		fixedMapping.put(serverAddress, device);
	}

	public void addData(GraphUpdate supdate) {
		if(supdate == null) throw new IllegalArgumentException("Given GraphUpdate must not be null.");
		if(initialValue) {
			energyData.clear();
			performanceData.clear();
			initialValue = false;
		}
		
		energyData.add(supdate.getNormalisedEnergy());
		totalEnergyJ += supdate.getTotalEnergyJ();
		if(energyData.size() > GuiModel.MAX_ITERATION_DISPLAY)
			energyData.remove(0);
		
		performanceData.add(supdate.getNormalisedRuntime());
		totalRuntimeMS += supdate.getTotalRuntimeMS();
		if(performanceData.size() > GuiModel.MAX_ITERATION_DISPLAY)
			performanceData.remove(0);
	}
	
	public double getTotalEnergyJ() { return totalEnergyJ; }
	public double getTotalRuntimeMS() { return totalRuntimeMS; }

}