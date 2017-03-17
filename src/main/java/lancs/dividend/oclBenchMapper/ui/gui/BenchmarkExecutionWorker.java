package lancs.dividend.oclBenchMapper.ui.gui;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import lancs.dividend.oclBenchMapper.benchmark.BenchExecutionResults;
import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkData;
import lancs.dividend.oclBenchMapper.client.ExecutionItem;
import lancs.dividend.oclBenchMapper.mapping.CmdToDeviceMapping;
import lancs.dividend.oclBenchMapper.mapping.WorkloadDistributionException;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.server.ServerDescription;
import lancs.dividend.oclBenchMapper.ui.gui.GuiModel.ExecutionMode;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdate;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdateError;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdateExit;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdateGraph;
import lancs.dividend.oclBenchMapper.ui.gui.update.GuiUpdateMessage;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

/**
 * @author vseeker
 *
 */
public class BenchmarkExecutionWorker extends SwingWorker<Integer, List<GuiUpdate>> {
	
	private static final String MSG_INSET = "   ";
	private static final String RESULT_FORMAT = "%.3f";
	
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
		String dsetSize;
		if(!randomDataSize) dsetSize = (String) gui.datacbox.getSelectedItem();
		else {
			String[] dsetSizeList = BenchmarkData.getAvailableDSetSizes(rbin);
			assert dsetSizeList != null && dsetSizeList.length > 0 : "No data set sizes assigned to " + rbin;
			dsetSize = dsetSizeList[rnd.nextInt(dsetSizeList.length)];
		}
			
		return new RunBenchCmd(rbin, dsetSize);
	}
	
	private void handleUserCmdGuiEffects(UserCommand cmd) {
		assert gui.cmdHandler != null && gui.wlMap != null : "Command handler and mapper must not be null at this point.";
		List<GuiUpdate> updates = new ArrayList<>();

		Hashtable<String, CmdToDeviceMapping> execMapping;
		try {
			execMapping = gui.wlMap.mapWorkload(gui.cmdHandler.getServerDescriptions(), cmd);
		} catch (WorkloadDistributionException e) {
			System.err.println("ERROR: Workload mapping failed: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		updates.add(printExecutionStart(cmd, execMapping));
		publish(updates);
		
		Hashtable<String, List<ExecutionItem>> execItems = 
				generateExecutionItems(execMapping);
				
		gui.cmdHandler.executeCommands(cmd, execItems);
		
		updates.clear();
		updates = processServerResponse(execMapping, execItems, cmd);
		publish(updates);
	}

	private GuiUpdateMessage printExecutionStart(UserCommand cmd, Hashtable<String, CmdToDeviceMapping> execMapping) {
		StringBuilder execMsgBld = new StringBuilder();
		
		switch(cmd.getType()) {
			case EXIT:
				execMsgBld.append("Disconnecting and shutting down client ...\n");
				break;
			case RUNBENCH:
				RunBenchCmd bcmd = (RunBenchCmd) cmd;
				execMsgBld.append("### Executing benchmark: ").append(bcmd.getBinaryName())
					.append(" with ").append(bcmd.getDataSetSize()).append(" workload.\n");

				execMsgBld.append("Execution device mapping:");
				for (String addr : execMapping.keySet()) {
					execMsgBld.append("\n").append(MSG_INSET).append(addr).append(": ")
						.append(execMapping.get(addr).execDev);
				}
				break;
		}
		
		return new GuiUpdateMessage(execMsgBld.toString());
	}
	
	private GuiUpdateMessage printExecutionEnd(GraphUpdate update) {
		StringBuilder execMsgBld = new StringBuilder();
		
		execMsgBld.append("# Execution finished.\n")
			.append(MSG_INSET).append("Energy Consumption:\t")
				.append(String.format(RESULT_FORMAT, update.getTotalEnergyJ())).append(" J\n")
			.append(MSG_INSET).append("Execution Runtime:\t")
				.append(String.format(RESULT_FORMAT, update.getTotalRuntimeMS())).append(" ms\n");
		
		return new GuiUpdateMessage(execMsgBld.toString());
	}
	
	private Hashtable<String, List<ExecutionItem>> generateExecutionItems(
			Hashtable<String, CmdToDeviceMapping> execMapping) {

		Hashtable<String, List<ExecutionItem>> execItems = new Hashtable<>();
		for (String serverAddr : execMapping.keySet()) {

			CmdToDeviceMapping mapping = execMapping.get(serverAddr);
			if(mapping == null) continue; // no workload assigned

			List<ExecutionItem> items = new ArrayList<>();
			items.add(new ExecutionItem(mapping.userCmd, mapping.execDev, serverAddr));
			
			// Make sure the selected workload is executed on all available execution devices on this server
			if(gui.alterChkBox.isSelected()) {
				ExecutionDevice alterDevice = null;
				switch (mapping.execDev) {
					case CPU:
						alterDevice = ExecutionDevice.GPU;
						break;
					case GPU:
						alterDevice = ExecutionDevice.CPU;
						break;
	
					default: throw new RuntimeException("Unknown execution device: " + mapping.execDev);
				}
				items.add(new ExecutionItem(mapping.userCmd, alterDevice, serverAddr));
			}
			
			execItems.put(serverAddr, items);
		}
		
		return execItems;
	}
	
	public List<GuiUpdate> processServerResponse(Hashtable<String, CmdToDeviceMapping> execMapping, 
			Hashtable<String, List<ExecutionItem>> execItems, UserCommand cmd) {
		
		if(execMapping == null || execMapping.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");

		List<GuiUpdate> gUpdates = new ArrayList<>();
		Hashtable<String, Hashtable<ExecutionDevice, BenchExecutionResults>> execStats = new Hashtable<>();
		
		for (String serverAdr : execItems.keySet()) {	
			for(ExecutionItem item : execItems.get(serverAdr)) {
				
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
					return gUpdates;
					
				} else {					
					switch(cmd.getType()) {
						case EXIT:
							gUpdates.add(new GuiUpdateExit());
							break;
						case RUNBENCH:
							if(!execStats.containsKey(serverAdr))
								execStats.put(serverAdr, new Hashtable<>());

							assert !execStats.get(serverAdr).containsKey(item.getExecDevice()) : 
								"Execution device results already received for server " + serverAdr;
							
							ResponseMessage response = item.getResponse();
							assert response != null : "Response message must not be null if error flag is not set.";
							assert response.getType() == ResponseType.BENCHSTATS : "Invalid response type at this point: " + response.getType();
							
							BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
							if(!br.hasEnergyLog()) {
								gUpdates.add(new GuiUpdateError("ERROR: Execution response from server " + 
										serverAdr + " has no energy log."));
								return gUpdates;
								
							} else {
								BenchExecutionResults stats = new BenchExecutionResults(br.getEnergyLog().getEnergyJ(), 
										br.getEnergyLog().getRuntimeMS());
								execStats.get(serverAdr).put(item.getExecDevice(), stats);
							}
							break;
						default: throw new RuntimeException("Unknown user command type: " + cmd.getType());
					}
				}
			}
		}
		
		// only update graphs if all executions went successfully
		if(cmd.getType() == CmdType.RUNBENCH) {
			assert execStats.size() > 0 : "Execution statistics should have been present here.";
			
			if(!gui.alterChkBox.isSelected()) {
				addPrecalculatedStats(execStats, (RunBenchCmd) cmd);
			}
			Hashtable<String, ExecutionDevice> bestMapping = getBestMapping(execStats);
			Hashtable<String, GraphUpdate> seriesUpdates = 
					processExecutionStatistics(execMapping, execStats, bestMapping, (RunBenchCmd) cmd);
			gUpdates.add(new GuiUpdateGraph(seriesUpdates));
			
			gUpdates.add(printExecutionEnd(seriesUpdates.get(GuiModel.GRAPH_SERIES_NAME_MAPPER)));
		}
		
		return gUpdates;
	}
	
	private void addPrecalculatedStats(
			Hashtable<String, Hashtable<ExecutionDevice, BenchExecutionResults>> execStats, RunBenchCmd bcmd) {
		
		for (String serverAdr : execStats.keySet()) {
			Hashtable<ExecutionDevice, BenchExecutionResults> serverStats = execStats.get(serverAdr);
			ServerDescription descr = gui.cmdHandler.getServerDescription(serverAdr);
			assert descr != null : "No known server description for address: " + serverAdr;
			
			for (ExecutionDevice device : ExecutionDevice.values()) {
				if(!serverStats.containsKey(device)) {
					
					BenchExecutionResults precalcStats = gui.serverExecStats
							.get(descr.architecture)
							.get(bcmd.getBinaryName())
							.get(bcmd.getDataSetSize())
							.get(device);
					serverStats.put(device, precalcStats);
				}
			}
		}
		
	}
	
	private Hashtable<String, ExecutionDevice> getBestMapping(
			Hashtable<String, Hashtable<ExecutionDevice, BenchExecutionResults>> execStats) {

		Hashtable<String, ExecutionDevice> bestMapping = new Hashtable<>();

		for (String serverAdr : execStats.keySet()) {
			double bestTradeoff = Double.MAX_VALUE;
			ExecutionDevice bestDevice = null;
			
			for(ExecutionDevice device : execStats.get(serverAdr).keySet()) {
				if(execStats.get(serverAdr).get(device).tradeoff < bestTradeoff) {
					bestTradeoff = execStats.get(serverAdr).get(device).tradeoff;
					bestDevice = device;
				}
			}
			
			bestMapping.put(serverAdr, bestDevice);
		}

		return bestMapping;
	}

	private Hashtable<String, GraphUpdate> processExecutionStatistics(
			Hashtable<String, CmdToDeviceMapping> execMapping,
			Hashtable<String, Hashtable<ExecutionDevice, BenchExecutionResults>> execStats,
			Hashtable<String, ExecutionDevice> bestMapping,
			RunBenchCmd bcmd) {

		Hashtable<String, GraphUpdate> seriesUpdates = new Hashtable<>();
		for(String name : gui.series.keySet()) { // update each series
			GraphSeriesData gData = gui.series.get(name);
			GraphUpdate gUpdate = new GraphUpdate();
			seriesUpdates.put(name, gUpdate);
			
			// for each server
			for(String serverAdr : execStats.keySet()) {
			
				// if current series belongs to mapper, take exec device from original mapping
				// otherwise a fixed mapping must be specified in corresponding GraphSeriesData
				ExecutionDevice dev = null;
				if(name.equals(GuiModel.GRAPH_SERIES_NAME_MAPPER)) {
					dev = execMapping.get(serverAdr).execDev;
				} else {
					dev = gData.fixedMapping.get(serverAdr);
				}
				assert dev != null : "Something went wrong retrieving the execution device.";
				
				BenchExecutionResults stats = execStats.get(serverAdr).get(dev);
				BenchExecutionResults bestStats = execStats.get(serverAdr).get(bestMapping.get(serverAdr));
				assert stats != null : "Something went wrong retrieving the execution statistics.";

				gUpdate.addStatUpdate(stats.energyJ, bestStats.energyJ, stats.runtimeMS, bestStats.runtimeMS);
			}
		}
		
		return seriesUpdates;
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
					case MESSAGE:
						GuiUpdateMessage uMsg = (GuiUpdateMessage) update;
						gui.msgOutTextArea.append("\n");
						gui.msgOutTextArea.append(uMsg.message);
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
						
						if(gui.initialIteration) {
							gui.iterationData.clear();
							gui.initialIteration = false;
						}
						gui.iterationData.add((double)gui.iteration++);
						if(gui.iterationData.size() > GuiModel.MAX_ITERATION_DISPLAY)
							gui.iterationData.remove(0);
						
						// update all series registered in the charts with presented data
						for (String	seriesName : uGraph.seriesUpdates.keySet()) {
							GraphSeriesData sdata = gui.series.get(seriesName);
							GraphUpdate supdate = uGraph.seriesUpdates.get(seriesName);
							sdata.addData(supdate);
							
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
				gui.resTable.repaint();
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
