package lancs.dividend.oclBenchMapper.ui.gui;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;

public class GraphSeriesData {
	
	public String name;
	public List<Double> energyData = new ArrayList<>();
	public List<Double> performanceData = new ArrayList<>();
	/** Specifies server addresses if an execution device is fixed for this server. */
	public final Hashtable<String, ExecutionDevice> fixedMapping;
	
	public GraphSeriesData(String name) {
		if(name == null) throw new IllegalArgumentException("Given seires name must not be null.");

		this.name = name;
		fixedMapping = new Hashtable<>();
	}

	public void addFixedMapping(String serverAddress, ExecutionDevice device) {
		if(serverAddress == null) throw new IllegalArgumentException("Given server address must not be null.");
		if(device == null) throw new IllegalArgumentException("Given execution device must not be null.");
		
		fixedMapping.put(serverAddress, device);
	}
	
	public void addData(double eJ, double rMS) {
		energyData.add(eJ);
		if(energyData.size() > GuiModel.MAX_ITERATION_DISPLAY)
			energyData.remove(0);
		
		performanceData.add(rMS);
		if(performanceData.size() > GuiModel.MAX_ITERATION_DISPLAY)
			performanceData.remove(0);
	}
}