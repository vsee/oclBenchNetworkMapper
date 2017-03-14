package lancs.dividend.oclBenchMapper.ui.gui;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.ui.console.BenchExecutionResults;
import lancs.dividend.oclBenchMapper.ui.gui.GuiModel.ExecutionMode;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdate;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdateError;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdateExit;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdateGraph;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public class BenchmarkExecutionWorker extends SwingWorker<Integer, List<GuiUpdate>> {

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
		
		Benchmark rbin = (Benchmark) gui.benchcbox.getSelectedItem();
		DataSetSize dsetSize;
		if(!randomDataSize) dsetSize = (DataSetSize) gui.datacbox.getSelectedItem();
		else dsetSize = DataSetSize.values()[rnd.nextInt(DataSetSize.values().length)];
			
		return new RunBenchCmd(rbin, dsetSize);
	}
	
	private void handleUserCmdGuiEffects(UserCommand cmd) {
		assert gui.cmdHandler != null && gui.wlMap != null : "Command handler and mapper must not be null at this point.";

		Hashtable<String, List<ExecutionItem>> execMapping = 
				gui.wlMap.mapWorkload(gui.cmdHandler.getServerAdresses(), cmd);
		
		// TODO add additional execution commands depending on gui option box
		
		gui.cmdHandler.executeCommands(cmd, execMapping);
		List<GuiUpdate> updates = processServerResponse(execMapping, cmd);
		publish(updates);
	}
	
	public List<GuiUpdate> processServerResponse(Hashtable<String, List<ExecutionItem>> execMapping, UserCommand cmd) {
		if(execMapping == null || execMapping.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");

		List<GuiUpdate> gUpdates = new ArrayList<>();
		Hashtable<String, GraphUpdate> seriesUpdates = new Hashtable<>();
		
		for (String serverAdr : execMapping.keySet()) {

			for(ExecutionItem item : execMapping.get(serverAdr)) {
				
				if(item.hasError()) {
					StringBuilder errorBld = new StringBuilder("Execution Error: ");
					errorBld.append(item.getErrorMsg());
					Exception e = item.getErrorException();
					if(e != null) {
						errorBld.append(e.getMessage());
						System.out.println(e.getMessage());
						e.printStackTrace();
					}
					
					gUpdates.add(new GuiUpdateError(errorBld.toString()));
				} else {
					switch(cmd.getType()) {
						case EXIT:
							gUpdates.add(new GuiUpdateExit());
							break;
						case RUNBENCH:
							ResponseMessage response = item.getResponse();
							assert response != null : "Response message must not be null if error flag is not set.";
							assert response.getType() == ResponseType.BENCHSTATS : "Invalid response type at this point: " + response.getType();
							
							RunBenchCmd bcmd = (RunBenchCmd) cmd;
							BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
							
							// save energy and runtime results for each graph series
							// consider measured results for the mapper series
							// and precomputations for the alternative ones
							for(String name : gui.series.keySet()) {
								if(!seriesUpdates.containsKey(name)) {
									Hashtable<ExecutionDevice, BenchExecutionResults> res = 
											gui.serverExecStats.get(bcmd.getBinaryName()).get(bcmd.getDataSetSize());
									GraphUpdate up = new GraphUpdate(bcmd.getBinaryName(), 
											bcmd.getDataSetSize(), bcmd.getExecutionDevice(),
											Math.min(res.get(ExecutionDevice.CPU).energyJ, res.get(ExecutionDevice.GPU).energyJ),
											Math.min(res.get(ExecutionDevice.CPU).runtimeMS, res.get(ExecutionDevice.GPU).runtimeMS));
									seriesUpdates.put(name, up);
								}
								GraphUpdate update = seriesUpdates.get(name);
								
								if(name.equals(GuiModel.GRAPH_SERIES_NAME_MAPPER)) {
									update.addStatUpdate(br.getEnergyLog().getEnergyJ(), 
											br.getEnergyLog().getRuntimeMS());
								} else {
									// get fixed mapping
									ExecutionDevice device = gui.series.get(name).fixedMapping.get(serverAdr);
									if(device != null) {
										BenchExecutionResults execRes = gui.serverExecStats.get(bcmd.getBinaryName())
												.get(bcmd.getDataSetSize()).get(device);
										
										update.addStatUpdate(execRes.energyJ, execRes.runtimeMS);
									}
								}
							}
							break;
						default: throw new RuntimeException("Unknown user command type: " + cmd.getType());
					}
				}
			}
		}
		
		if(!seriesUpdates.isEmpty())
			gUpdates.add(new GuiUpdateGraph(seriesUpdates));
		
		return gUpdates;
	}
	
	@Override
	protected void process(List<List<GuiUpdate>> chunks) {
		
		for (List<GuiUpdate> gUpdates : chunks) {
			
			for (GuiUpdate update : gUpdates) {
				
				switch(update.getType()) {
					case EXIT:
						JOptionPane.showMessageDialog(null, "Shutting down client.", 
								"Exit", JOptionPane.INFORMATION_MESSAGE);
						gui.frame.dispatchEvent(new WindowEvent(gui.frame, WindowEvent.WINDOW_CLOSING));
						break;
					case ERROR:
						GuiUpdateError uErr = (GuiUpdateError) update;
						gui.msgOutTextArea.append("\n");
						gui.msgOutTextArea.append(uErr.errorMessage);
						
						if(gui.activeMode == ExecutionMode.AUTOMATIC) {
							gui.msgOutTextArea.append("\nStopping Automatic ...");
							stopAutomatic();
						}
						break;
					case GRAPH:
						GuiUpdateGraph uGraph = (GuiUpdateGraph) update;
						
						gui.iterationData.add(gui.iteration++);
						if(gui.iterationData.size() > GuiModel.MAX_ITERATION_DISPLAY)
							gui.iterationData.remove(0);
						
						// update all series registered in the charts with presented data
						for (String	seriesName : uGraph.seriesUpdates.keySet()) {
							GraphSeriesData sdata = gui.series.get(seriesName);
							GraphUpdate supdate = uGraph.seriesUpdates.get(seriesName);
							sdata.addData(supdate.getNormalisedEnergy(), supdate.getNormalisedRuntime());
							
							gui.energyChart.updateXYSeries(seriesName, gui.iterationData, sdata.energyData, null);
							gui.performanceChart.updateXYSeries(seriesName, gui.iterationData, sdata.performanceData, null);
						}

						gui.eChartPanel.revalidate();
						gui.eChartPanel.repaint();
						gui.pChartPanel.revalidate();
						gui.pChartPanel.repaint();
						break;
					default: throw new RuntimeException("Unknown gui update type: " + update.getType());
				}
				
				gui.msgOutTextArea.update(gui.msgOutTextArea.getGraphics());
			}
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
