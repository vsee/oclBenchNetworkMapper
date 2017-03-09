package lancs.dividend.oclBenchMapper.ui.gui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.ui.console.ClientNiConsoleUi;
import lancs.dividend.oclBenchMapper.ui.gui.GuiModel.ExecutionMode;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.utils.CSVResourceTools;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;

public class ClientGui implements UserInterface {

	// TODO clean up file parsing
	// TODO add graceful shutdown upon window close
	
	private static final int HEADER_STATS_BIN_IDX = 0;
	private static final int HEADER_STATS_DATA_IDX = 1;
	private static final int HEADER_STATS_DEVICE_IDX = 2;
	private static final int HEADER_STATS_ENERGY_IDX = 3;
	private static final int HEADER_STATS_RUNTIME_IDX = 4;
	
	private GuiModel gui;
	private BenchmarkExecutionWorker bexecWorker;
	
	public ClientGui(ClientGuiConfig config) {
		gui = new GuiModel();
		
		gui.serverExecStats = parseExecutionStats(config.executionStatFile);
		
		initialiseCharts();
		initialiseGui();
		
		gui.activeMode = ExecutionMode.MANUAL;
	}
	
	private Hashtable<RodiniaBin, Hashtable<DataSetSize, Hashtable<ExecutionDevice, GraphUpdate>>> parseExecutionStats(
			Path executionStatFile) {
		
		Hashtable<RodiniaBin, Hashtable<DataSetSize, Hashtable<ExecutionDevice, GraphUpdate>>> res = new Hashtable<>();
		checkHeader(executionStatFile);
		
		List<List<String>> recs = null;
		try {
			recs = CSVResourceTools.readRecords(executionStatFile);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		for (List<String> record : recs) {
			RodiniaBin rbin = RodiniaBin.valueOf(record.get(HEADER_STATS_BIN_IDX));
			DataSetSize data = DataSetSize.valueOf(record.get(HEADER_STATS_DATA_IDX));
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(HEADER_STATS_DEVICE_IDX));
			double avg_energyJ = Double.valueOf(record.get(HEADER_STATS_ENERGY_IDX));
			double avg_runtimeMS = Double.valueOf(record.get(HEADER_STATS_RUNTIME_IDX));
			
			if(!res.containsKey(rbin))
				res.put(rbin, new Hashtable<>());
			if(!res.get(rbin).containsKey(data))
				res.get(rbin).put(data, new Hashtable<>());
			
			res.get(rbin).get(data).put(dev, new GraphUpdate(avg_energyJ, avg_runtimeMS));
		}
		
		System.out.println("Successfully parsed " + recs.size() + " execution statistics from " + executionStatFile);
		return res;
	}
	
	private void checkHeader(Path executionStatFile) {
		List<String> header = null;
		try {
			header = CSVResourceTools.readHeader(executionStatFile);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		for(int i = 0; i < header.size(); i++) {
			if(!header.get(i).equals(ClientNiConsoleUi.STATS_HEADER[i]))
				throw new RuntimeException("Unexpected header format in precomputation file: " + header);
		}
	}

	private void initialiseCharts() {
		gui.energyChart = new XYChartBuilder().width(600).height(400).title("Energy Consumption")
				.xAxisTitle("Iteration").yAxisTitle("Energy in Joules").build();
		gui.energyChart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		gui.energyChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
		
		gui.performanceChart = new XYChartBuilder().width(600).height(400).title("Performance")
				.xAxisTitle("Iteration").yAxisTitle("Execution Time in ms").build();
		gui.performanceChart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		gui.performanceChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
	}

	private void initialiseGui() {
		gui.frame = new JFrame();
		gui.frame.setBounds(100, 100, 1200, 800);
		gui.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	assert gui.cmdHandler != null : "Command handler must not be null at this point.";
	        	gui.cmdHandler.closeConnections();
	            System.exit(0);
	        }
	    });
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.columnWidths = new int[]{0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gui.frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Workload Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_controlpanel = new GridBagConstraints();
		gbc_controlpanel.weighty = 2;
		gbc_controlpanel.fill = GridBagConstraints.BOTH;
		gbc_controlpanel.gridx = 0;
		gbc_controlpanel.gridy = 0;
		gui.frame.getContentPane().add(controlPanel, gbc_controlpanel);
		
		gui.benchcbox = new JComboBox<>();
		gui.benchcbox.setModel(new DefaultComboBoxModel<>(RodiniaBin.values()));
		controlPanel.add(gui.benchcbox);
		
		gui.datacbox = new JComboBox<>();
		gui.datacbox.setModel(new DefaultComboBoxModel<>(DataSetSize.values()));
		controlPanel.add(gui.datacbox);
		
		gui.btnRun = new JButton(GuiModel.RUN_BTN_RUN_TEXT);
		gui.btnRun.addActionListener(new RunActionListener());
		controlPanel.add(gui.btnRun);
		
		gui.btnAutomatic = new JButton(GuiModel.AUTOMATIC_BTN_START_TEXT);
		gui.btnAutomatic.addActionListener(new AutomaticActionListener());
		controlPanel.add(gui.btnAutomatic);

		JPanel wlPanel = new JPanel();
		wlPanel.setBorder(new TitledBorder(null, "Execution Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_wlpanel = new GridBagConstraints();
		gbc_wlpanel.weighty = 8;
		gbc_wlpanel.fill = GridBagConstraints.BOTH;
		gbc_wlpanel.gridx = 0;
		gbc_wlpanel.gridy = 1;
		gui.frame.getContentPane().add(wlPanel, gbc_wlpanel);
		GridBagLayout gbl_wlPanel = new GridBagLayout();
		gbl_wlPanel.columnWidths = new int[]{0, 0, 0};
		gbl_wlPanel.rowHeights = new int[]{0, 0};
		gbl_wlPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_wlPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		wlPanel.setLayout(gbl_wlPanel);	    
		
		gui.eChartPanel = new XChartPanel<XYChart>(gui.energyChart);
		GridBagConstraints gbc_echart = new GridBagConstraints();
		gbc_echart.fill = GridBagConstraints.BOTH;
		gbc_echart.gridx = 0;
		gbc_echart.gridy = 0;
		wlPanel.add(gui.eChartPanel, gbc_echart);
		
		gui.pChartPanel = new XChartPanel<XYChart>(gui.performanceChart);
		GridBagConstraints gbc_pchart = new GridBagConstraints();
		gbc_pchart.fill = GridBagConstraints.BOTH;
		gbc_pchart.gridx = 1;
		gbc_pchart.gridy = 0;
		wlPanel.add(gui.pChartPanel, gbc_pchart);
	}
	
	private class RunActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
    		assert gui.activeMode == ExecutionMode.MANUAL : "Expected to be in manual execution mode when run button is used.";
    		if(bexecWorker == null || bexecWorker.isDone()) {
    			gui.btnRun.setEnabled(false);
    			gui.btnAutomatic.setEnabled(false);
				gui.btnRun.setText(GuiModel.RUN_BTN_RUNNING_TEXT);
	    		bexecWorker = new BenchmarkExecutionWorker(gui);
	    		bexecWorker.execute();	
    		}
		}
	}
	
	private class AutomaticActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			switch (gui.activeMode) {
				case MANUAL:
					gui.activeMode = ExecutionMode.AUTOMATIC;
					gui.btnRun.setEnabled(false);
					gui.datacbox.setEnabled(false);
					gui.btnAutomatic.setText(GuiModel.AUTOMATIC_BTN_STOP_TEXT);
					
		    		if(bexecWorker == null || bexecWorker.isDone()) {
			    		bexecWorker = new BenchmarkExecutionWorker(gui);
			    		bexecWorker.execute();	
		    		}
					break;
				case AUTOMATIC:
					assert bexecWorker != null && !bexecWorker.isDone() : "Execution worker in unexpected state.";
					bexecWorker.stopAutomatic();
					break;
				default:
					throw new RuntimeException("Unknown execution mode: " + gui.activeMode);
			}
		}
	}
	
	@Override
	public void run(ClientConnectionHandler cmdHandler) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		
		gui.cmdHandler = cmdHandler;
		initialiseChartSeries();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void initialiseChartSeries() {
		gui.iterationData = new ArrayList<>();
		gui.iterationData.add(gui.iteration++);

		gui.series = new Hashtable<>();
		addSeries(gui.series, GuiModel.GRAPH_SERIES_NAME_MAPPER, false);
		
		gui.serverAdresses = gui.cmdHandler.getServerAdresses();
		permutateAlterSeries(gui.serverAdresses.length, "");
	}
	
	private void addSeries(Hashtable<String, GraphSeriesData> seriesList, String name, boolean addFixedMappings) {
		GraphSeriesData series = new GraphSeriesData(name);
		seriesList.put(name, series);
		series.addData(0.0, 0.0);
		gui.energyChart.addSeries(name, gui.iterationData, series.energyData);
		gui.performanceChart.addSeries(name, gui.iterationData, series.performanceData);
		
		if(addFixedMappings) {
			String[] mappings = name.split("_");
			assert mappings.length == gui.serverAdresses.length : "Fixed mapping length and server addresses don't match.";
			
			for (int i = 0; i < mappings.length; i++) {
				series.addFixedMapping(gui.serverAdresses[i], 
						ExecutionDevice.valueOf(mappings[i].substring(0, mappings[i].length() - 1)));
			}
		}
	}

	private void permutateAlterSeries(int draws, String suffix) {
		if(draws == 0) {
			addSeries(gui.series, suffix, true);
		}
		else {
			for(ExecutionDevice dev : ExecutionDevice.values()) {
				final String SEP = suffix.isEmpty() ? "" : "_";
				String serverPermutation = dev.name() + draws + SEP + suffix;
				permutateAlterSeries(draws - 1, serverPermutation);
			}
		}
	}
}
