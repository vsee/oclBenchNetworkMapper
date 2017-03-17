package lancs.dividend.oclBenchMapper.ui.gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkData;
import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.server.ServerDescription;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.ui.gui.GuiModel.ExecutionMode;
import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;

public class ClientGui implements UserInterface {

	// TODO add graceful shutdown upon window close	
	private GuiModel gui;
	private BenchmarkExecutionWorker bexecWorker;
	
	public ClientGui(ClientGuiConfig config) {
		gui = new GuiModel();
		
		gui.serverExecStats = OclBenchMapperCsvHandler.parseExecutionStats(config.executionStatFile);
		
		initialiseCharts();
		initialiseGui();
		
		gui.activeMode = ExecutionMode.MANUAL;
	}

	private void initialiseCharts() {
		gui.energyChart = new XYChartBuilder().width(600).height(400).title("Energy Consumption")
				.xAxisTitle("Iteration").yAxisTitle("Energy Normalised To Best Mapping").build();
		gui.energyChart.getStyler().setLegendPosition(LegendPosition.InsideNW);
		gui.energyChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
		
		gui.performanceChart = new XYChartBuilder().width(600).height(400).title("Benchmark Execution Time")
				.xAxisTitle("Iteration").yAxisTitle("Execution Time Normalised To Best Mapping").build();
		gui.performanceChart.getStyler().setLegendPosition(LegendPosition.InsideNW);
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
		
		initControlPanel();
		initChartPanel();
	}
		
	private void initControlPanel() {
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Workload Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_cpanel = new GridBagConstraints();
		gbc_cpanel.weighty = 2;
		gbc_cpanel.fill = GridBagConstraints.BOTH;
		gbc_cpanel.gridx = 0;
		gbc_cpanel.gridy = 0;
		gbc_cpanel.insets = new Insets(0, 0, 10, 0);
		gui.frame.getContentPane().add(controlPanel, gbc_cpanel);
		
		controlPanel.setLayout(new GridBagLayout());    
		
		gbc_cpanel = new GridBagConstraints();
		gbc_cpanel.weightx = 1;
		gbc_cpanel.weighty = 1;
		gbc_cpanel.fill = GridBagConstraints.BOTH;
		gbc_cpanel.gridx = 0;
		gbc_cpanel.gridy = 0;
		gui.benchcbox = new JComboBox<>();
		gui.benchcbox.setModel(new DefaultComboBoxModel<>(Benchmark.values()));
		gui.benchcbox.addItemListener(new BenchmarkItemChangeListener());
		controlPanel.add(gui.benchcbox, gbc_cpanel);
		
		gbc_cpanel.gridy = 1;
		gui.datacbox = new JComboBox<>();
		controlPanel.add(gui.datacbox, gbc_cpanel);
		
		gbc_cpanel.gridy = 2;
		gui.btnRun = new JButton(GuiModel.RUN_BTN_RUN_TEXT);
		gui.btnRun.addActionListener(new RunActionListener());
		controlPanel.add(gui.btnRun, gbc_cpanel);
		
		gbc_cpanel.gridy = 3;
		gui.btnAutomatic = new JButton(GuiModel.AUTOMATIC_BTN_START_TEXT);
		gui.btnAutomatic.addActionListener(new AutomaticActionListener());
		controlPanel.add(gui.btnAutomatic, gbc_cpanel);
		
		gbc_cpanel.gridy = 4;
		gui.alterChkBox = new JCheckBox(GuiModel.ALTER_CHECKBOX_TEXT);
		gui.alterChkBox.setSelected(false);
		controlPanel.add(gui.alterChkBox, gbc_cpanel);
		
		gbc_cpanel.weightx = 5;
		gbc_cpanel.weighty = 5;
		gbc_cpanel.gridheight = 5;
		gbc_cpanel.gridx = 1;
		gbc_cpanel.gridy = 0;
		gbc_cpanel.insets = new Insets(0, 10, 0, 0);
		
		gui.msgOutTextArea = new JTextArea();
		gui.msgOutTextArea.setEditable(false);
		gui.msgOutTextArea.setColumns(1);
		gui.msgOutScroll = new JScrollPane(gui.msgOutTextArea);
		gui.msgOutScroll.setBorder(new LineBorder(new Color(0, 0, 0)));
		controlPanel.add(gui.msgOutScroll, gbc_cpanel);
		
		gui.resTable = new JTable();
		JScrollPane scrollPane = new JScrollPane(gui.resTable);
		gui.resTable.setFillsViewportHeight(true);
		gbc_cpanel.weightx = 4;
		gbc_cpanel.gridx = 2;
		gbc_cpanel.insets = new Insets(0, 10, 0, 0);
		controlPanel.add(scrollPane, gbc_cpanel);
	}
	
	private void initChartPanel() {
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
	
	private class BenchmarkItemChangeListener implements ItemListener{
	    @Override
	    public void itemStateChanged(ItemEvent event) {
	       if (event.getStateChange() == ItemEvent.SELECTED) {
	    	  assert event.getItem() instanceof Benchmark : "Unexpected item change event.";
	       
	          Benchmark rbin = (Benchmark) event.getItem();
	          setDataSetSizeModel(rbin);
	       }
	    }       
	}
	
	@Override
	public void run(ClientConnectionHandler cmdHandler, WorkloadMapper mapper) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		
		gui.cmdHandler = cmdHandler;
		gui.wlMap = mapper;
		initialiseChartSeries();
		
		Benchmark rbin = (Benchmark) gui.benchcbox.getSelectedItem();
		setDataSetSizeModel(rbin);
		
		gui.resTable.setModel(new ExecutionResultsTableModel(gui.series));
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					gui.frame.setVisible(true);
					printServerInfo();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void setDataSetSizeModel(Benchmark rbin) {
		String[] dsetSizeList = BenchmarkData.getAvailableDSetSizes(rbin);
		assert dsetSizeList != null && dsetSizeList.length > 0 : "No data set sizes assigned to " + rbin;
		gui.datacbox.setModel(new DefaultComboBoxModel<>(dsetSizeList));
	}
	
	private void printServerInfo() {
		
		for (ServerDescription descr : gui.cmdHandler.getServerDescriptions()) {
			gui.msgOutTextArea.append("Connected to server ");
			gui.msgOutTextArea.append(descr.address);
			gui.msgOutTextArea.append(" with architecture ");
			gui.msgOutTextArea.append(descr.architecture);
			gui.msgOutTextArea.append("\n");
		}
		
		gui.msgOutTextArea.update(gui.msgOutTextArea.getGraphics());

	}

	private void initialiseChartSeries() {
		gui.iterationData = new ArrayList<>();
		gui.iterationData.add((double)gui.iteration++);
		gui.initialIteration = true;

		gui.series = new TreeMap<>();
		
		permutateAlterSeries(gui.cmdHandler.getServerDescriptions().length, "");
		addSeries(gui.series, GuiModel.GRAPH_SERIES_NAME_MAPPER, false);
	}
	
	private void addSeries(TreeMap<String, GraphSeriesData> seriesList, String name, boolean addFixedMappings) {
		GraphSeriesData series = new GraphSeriesData(name);
		seriesList.put(name, series);
		gui.energyChart.addSeries(name, gui.iterationData, series.energyData);
		gui.performanceChart.addSeries(name, gui.iterationData, series.performanceData);
		
		ServerDescription[] servers = gui.cmdHandler.getServerDescriptions();
		if(addFixedMappings) {
			String[] mappings = name.split("_");
			assert mappings.length == servers.length : 
				"Fixed mapping length and server addresses don't match.";
			
			for (int i = 0; i < mappings.length; i++) {
				series.addFixedMapping(servers[i].address, 
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
