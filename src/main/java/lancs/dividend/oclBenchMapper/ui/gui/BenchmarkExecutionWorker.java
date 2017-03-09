package lancs.dividend.oclBenchMapper.ui.gui;

import java.awt.event.WindowEvent;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.ui.gui.GuiModel.ExecutionMode;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

public class BenchmarkExecutionWorker extends SwingWorker<Integer, Hashtable<String, GraphUpdate>> {

	private AtomicBoolean endAutoMode;
	private GuiModel gui;
	
	public BenchmarkExecutionWorker(GuiModel gui) {
		if(gui == null) throw new IllegalArgumentException("Given gui model must not be null.");
		this.gui = gui;
		endAutoMode = new AtomicBoolean(false);
	}
	
	public void stopAutomatic() {
		if(gui.activeMode == ExecutionMode.AUTOMATIC && !endAutoMode.get()) {
			gui.btnAutomatic.setText(GuiModel.AUTOMATIC_BTN_STOPPING_TEXT);
			gui.btnAutomatic.setEnabled(false);;
			endAutoMode.set(true);
		}
	}
	
	@Override
	protected Integer doInBackground() throws Exception {
		switch (gui.activeMode) {
			case MANUAL:
	    		handleUserCmdGuiEffects(receiveCommand(false)); 
				break;
			case AUTOMATIC:
				while(!endAutoMode.get()) {
		    		handleUserCmdGuiEffects(receiveCommand(true)); 
				}
				break;
			default:
				throw new RuntimeException("Unknown execution mode: " + gui.activeMode);
		}
		
		return null;
	}
	
	private UserCommand receiveCommand(boolean randomDataSize) {
		Random rnd = new Random();
		
		RodiniaBin rbin = (RodiniaBin) gui.benchcbox.getSelectedItem();
		DataSetSize dsetSize;
		if(!randomDataSize) dsetSize = (DataSetSize) gui.datacbox.getSelectedItem();
		else dsetSize = DataSetSize.values()[rnd.nextInt(DataSetSize.values().length)];
			
		return new RunBenchCmd(rbin, dsetSize);
	}
	
	private void handleUserCmdGuiEffects(UserCommand cmd) {
		assert gui.cmdHandler != null : "Command handler must not be null at this point.";
		Hashtable<ServerConnection, ExecutionItem> executionMap = new Hashtable<>();

		if(!gui.cmdHandler.handleUserCommand(cmd, executionMap)) {
			if(cmd.getType() == CmdType.EXIT) {
				JOptionPane.showMessageDialog(null, "Shutting down client.", 
						"Exit", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(null, "Server communication failed. Shutting Down client.", 
						"Communication Error", JOptionPane.ERROR_MESSAGE);
			}
			gui.frame.dispatchEvent(new WindowEvent(gui.frame, WindowEvent.WINDOW_CLOSING));
		}
		
		// TODO do something graphical about null returns
		 Hashtable<String, GraphUpdate> gupdates = processServerResponse(executionMap, cmd);
		if(gupdates != null) publish(gupdates);
	}
	
	public Hashtable<String, GraphUpdate> processServerResponse(
			Hashtable<ServerConnection, ExecutionItem> executionMap, UserCommand cmd) {
		if(executionMap == null || executionMap.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");
		
		boolean validResponse = true;
		Hashtable<String, GraphUpdate> seriesUpdates = new Hashtable<>();
		for(String name : gui.series.keySet()) seriesUpdates.put(name, new GraphUpdate(0, 0));
		
		for (ServerConnection s : executionMap.keySet()) {
			ExecutionItem item = executionMap.get(s);
			
			System.out.println("\nExecution result of server " + s);
			System.out.println("Command:\n\t" + item.getCommand());
			System.out.println("Response:");
			
			if(!item.resultsAvailable()) {
				System.out.println("ERROR: No results received!");
				validResponse = false;
			} else {
				ResponseMessage response = item.getResponse();
				
				switch (response.getType()) {
					case BENCHSTATS:
						RunBenchCmd bcmd = (RunBenchCmd) cmd;
						BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
						System.out.println("### Execution standard output:\n" + br.getStdOut());
						System.out.println("### Has Energy Log: " + br.hasEnergyLog());
						
						if(br.hasEnergyLog()) {
							System.out.println("### Energy Log:");
							System.out.println(br.getEnergyLog().getLogRecords().size() + " log entries found.");
							System.out.println("### Energy: " + br.getEnergyLog().getEnergyJ() + " J");
							System.out.println("### Runtime: " + br.getEnergyLog().getRuntimeMS() + " ms");
						}
						
						// save energy and runtime results for each graph series
						// consider measured results for the mapper series
						// and precomputations for the alternative ones
						for(String name : seriesUpdates.keySet()) {
							GraphUpdate update = seriesUpdates.get(name);
							
							if(name.equals(GuiModel.GRAPH_SERIES_NAME_MAPPER)) {
								update.energyJ += br.getEnergyLog().getEnergyJ();
								update.runtimeMS += br.getEnergyLog().getRuntimeMS();
							} else {
								// get fixed mapping
								ExecutionDevice device = gui.series.get(name).fixedMapping.get(s.getAddress());
								if(device != null) {
									update.energyJ += gui.serverExecStats.get(bcmd.getBinaryName())
											.get(bcmd.getDataSetSize()).get(device).energyJ;
									update.runtimeMS += gui.serverExecStats.get(bcmd.getBinaryName())
											.get(bcmd.getDataSetSize()).get(device).runtimeMS;
								}
							}
						}
						break;
					case ERROR:
						System.err.println("\tERROR: " + ((ErrorResponseMessage) response).getText());
						validResponse = false;
						break;
					default:
						System.err.println("\tUnknown response type: " + response.getType());
						validResponse = false;
						break;
				}
			}
		}

		if(validResponse) return seriesUpdates;
		else return null;
	}
	
	@Override
	protected void process(List<Hashtable<String, GraphUpdate>> chunks) {
		for (Hashtable<String, GraphUpdate> updates : chunks) {
			
			gui.iterationData.add(gui.iteration++);
			if(gui.iterationData.size() > GuiModel.MAX_ITERATION_DISPLAY)
				gui.iterationData.remove(0);
			
			// update all series registered in the charts with presented data
			for (String	seriesName : updates.keySet()) {
				GraphSeriesData sdata = gui.series.get(seriesName);
				GraphUpdate supdate = updates.get(seriesName);
				sdata.addData(supdate.energyJ, supdate.runtimeMS);
				
				gui.energyChart.updateXYSeries(seriesName, gui.iterationData, sdata.energyData, null);
				gui.performanceChart.updateXYSeries(seriesName, gui.iterationData, sdata.performanceData, null);
			}

			gui.eChartPanel.revalidate();
			gui.eChartPanel.repaint();
			gui.pChartPanel.revalidate();
			gui.pChartPanel.repaint();
		}
	}
	
	@Override
	protected void done() {
		switch (gui.activeMode) {
			case MANUAL:
				gui.btnRun.setEnabled(true);
				gui.btnRun.setText(GuiModel.RUN_BTN_RUN_TEXT);
				gui.btnAutomatic.setEnabled(true);
				break;
			case AUTOMATIC:
				gui.activeMode = ExecutionMode.MANUAL;
				gui.btnRun.setEnabled(true);
				gui.datacbox.setEnabled(true);
				gui.btnAutomatic.setText(GuiModel.AUTOMATIC_BTN_START_TEXT);
				gui.btnAutomatic.setEnabled(true);;
				break;
			default:
				throw new RuntimeException("Unknown execution mode: " + gui.activeMode);
		}
	}
}
