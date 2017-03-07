package lancs.dividend.oclBenchMapper.ui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.TextResponseMessage;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand.CmdType;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;

public class ClientGui implements UserInterface {

	// TODO clean up initialise
	// TODO add graceful shutdown upon window close
	
	private enum ExecutionMode { MANUAL, AUTOMATIC }
	
	private class GraphUpdateStats {
		public double energyJ;
		public double runtimeMS;
		
		public GraphUpdateStats(double e, double r) {
			energyJ = e;
			runtimeMS = r;
		}
	}
	
	private class BenchmarkExecutionWorker extends SwingWorker<Integer,GraphUpdateStats> {
		
		@Override
		protected Integer doInBackground() throws Exception {
			// check run mode (automatic, manual)
			// manual: get current config and execute once, then update status and return
			// automatic: start a loop and generate random commands, as long as not cancelled, if cancelled wait for last to quit then end
			
			switch (activeMode) {
				case MANUAL:
		    		handleUserCmdGuiEffects(receiveCommand(false)); 
					break;
				case AUTOMATIC:
					while(!isCancelled()) {
			    		handleUserCmdGuiEffects(receiveCommand(true)); 
					}
					break;
				default:
					throw new RuntimeException("Unknown execution mode: " + activeMode);
			}
			
			
			return null;
		}
		
		private UserCommand receiveCommand(boolean randomDataSize) {
			Random rnd = new Random();
			
			RodiniaBin rbin = (RodiniaBin) bechcbox.getSelectedItem();
			DataSetSize dsetSize;
			if(!randomDataSize) dsetSize = (DataSetSize) datacbox.getSelectedItem();
			else dsetSize = DataSetSize.values()[rnd.nextInt(DataSetSize.values().length)];
				
			return new RunBenchCmd(rbin, dsetSize);
		}
		
		private void handleUserCmdGuiEffects(UserCommand cmd) {
			assert cmdHandler != null : "Command handler must not be null at this point.";
			Hashtable<ServerConnection, ExecutionItem> executionMap = new Hashtable<>();

			if(!cmdHandler.handleUserCommand(cmd, executionMap)) {
				if(cmd.getType() == CmdType.EXIT) {
					JOptionPane.showMessageDialog(null, "Shutting down client.", 
							"Exit", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Server communication failed. Shutting Down client.", 
							"Communication Error", JOptionPane.ERROR_MESSAGE);
				}
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
			
			// TODO do something graphical about null returns
			GraphUpdateStats gupdate = processServerResponse(executionMap, cmd);
			if(gupdate != null) publish(gupdate);
		}
		
		public GraphUpdateStats processServerResponse(Hashtable<ServerConnection, ExecutionItem> executionMap, UserCommand cmd) {
			if(executionMap == null || executionMap.size() == 0)
				throw new RuntimeException("Given execution map must not be null or empty.");
			if(cmd == null)
				throw new RuntimeException("Given user command must not be null.");
			
			boolean validResponse = true;
			double totalEnergyJ = 0;
			double totalRuntimeMS = 0;
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
					case TEXT:
						System.out.println("\t" + ((TextResponseMessage) response).getText());
						validResponse = false;
						break;
					case BENCHSTATS:
						BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
						System.out.println("### Execution standard output:\n" + br.getStdOut());
						System.out.println("### Has Energy Log: " + br.hasEnergyLog());
						
						if(br.hasEnergyLog()) {
							System.out.println("### Energy Log:");
							System.out.println(br.getEnergyLog().getLogRecords().size() + " log entries found.");
							System.out.println("### Energy: " + br.getEnergyLog().getEnergyJ() + " J");
							System.out.println("### Runtime: " + br.getEnergyLog().getRuntimeMS() + " ms");
						}
						
						totalEnergyJ += br.getEnergyLog().getEnergyJ();
						totalRuntimeMS += br.getEnergyLog().getRuntimeMS();
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

			if(validResponse) return new GraphUpdateStats(totalEnergyJ, totalRuntimeMS);
			else return null;
		}
		
		@Override
		protected void process(List<GraphUpdateStats> chunks) {
			for (GraphUpdateStats update : chunks) {
				iterationData.add(iteration++);
				energyData.add(update.energyJ);
				performanceData.add(update.runtimeMS);

				energyChart.updateXYSeries("mapper", iterationData, energyData, null);
				performanceChart.updateXYSeries("mapper", iterationData, performanceData, null);
				eChartPanel.revalidate();
				eChartPanel.repaint();
				pChartPanel.revalidate();
				pChartPanel.repaint();
			}
		}
	}
	
	protected static final String AUTOMATIC_BTN_STOP_TEXT = "Stop Automatic";
	protected static final String AUTOMATIC_BTN_START_TEXT = "Start Automatic";
	
	private ExecutionMode activeMode;
	
	private JFrame frame;
	private JComboBox<RodiniaBin> bechcbox;
	private JComboBox<DataSetSize> datacbox;
	private XChartPanel<XYChart> eChartPanel;
	private XChartPanel<XYChart> pChartPanel;
	
	private XYChart energyChart;
	private XYChart performanceChart;
	
	private ClientConnectionHandler cmdHandler;
	
	
	
	private double iteration = 0;
	private final List<Double> energyData = new ArrayList<>();
	private final List<Double> performanceData = new ArrayList<>();
	private final List<Double> iterationData = new ArrayList<>();
	
	public ClientGui() {
		initialiseGraphs();
		initialiseGui();
		
		activeMode = ExecutionMode.MANUAL;
	}
	
	private void initialiseGraphs() {
		energyChart = new XYChartBuilder().width(600).height(400).title("Energy Consumption")
				.xAxisTitle("Iteration").yAxisTitle("Energy in Joules").build();
		energyChart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		energyChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
		energyChart.getStyler().setXAxisMin(0.0);
		
		performanceChart = new XYChartBuilder().width(600).height(400).title("Performance")
				.xAxisTitle("Iteration").yAxisTitle("Execution Time in ms").build();
		performanceChart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		performanceChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
		performanceChart.getStyler().setXAxisMin(0.0);
		
		energyData.add(0.0);
		performanceData.add(0.0);
		iterationData.add(iteration++);
		
		energyChart.addSeries("mapper", iterationData, energyData);
		performanceChart.addSeries("mapper", iterationData, performanceData);
	}
	
	private BenchmarkExecutionWorker bexecWorker;

	private void initialiseGui() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1200, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent winEvt) {
	        	assert cmdHandler != null : "Command handler must not be null at this point.";
	        	cmdHandler.closeConnections();
	            System.exit(0);
	        }
	    });
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]{1.0};
		gridBagLayout.columnWidths = new int[]{0};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Workload Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_controlpanel = new GridBagConstraints();
		gbc_controlpanel.weighty = 2;
		gbc_controlpanel.fill = GridBagConstraints.BOTH;
		gbc_controlpanel.gridx = 0;
		gbc_controlpanel.gridy = 0;
		frame.getContentPane().add(controlPanel, gbc_controlpanel);
		
		bechcbox = new JComboBox<>();
		bechcbox.setModel(new DefaultComboBoxModel<>(RodiniaBin.values()));
		controlPanel.add(bechcbox);
		
		datacbox = new JComboBox<>();
		datacbox.setModel(new DefaultComboBoxModel<>(DataSetSize.values()));
		controlPanel.add(datacbox);
		
		JButton btnRun = new JButton("run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
	    		assert activeMode == ExecutionMode.MANUAL : "Expected to be in manual execution mode when run button is used.";
	    		if(bexecWorker == null || bexecWorker.isDone()) {
		    		bexecWorker = new BenchmarkExecutionWorker();
		    		bexecWorker.execute();	
	    		}
			}
		});
		controlPanel.add(btnRun);
		
		JButton btnAutomatic = new JButton(AUTOMATIC_BTN_START_TEXT);
		btnAutomatic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				switch (activeMode) {
					case MANUAL:
						activeMode = ExecutionMode.AUTOMATIC;
						btnRun.setEnabled(false);
						datacbox.setEnabled(false);
						btnAutomatic.setText(AUTOMATIC_BTN_STOP_TEXT);
						break;
					case AUTOMATIC:
						activeMode = ExecutionMode.MANUAL;
						btnRun.setEnabled(true);
						datacbox.setEnabled(true);
						btnAutomatic.setText(AUTOMATIC_BTN_START_TEXT);
						break;
					default:
						throw new RuntimeException("Unknown execution mode: " + activeMode);
				}
				
				
			}
		});
		controlPanel.add(btnAutomatic);
		

		JPanel wlPanel = new JPanel();
		wlPanel.setBorder(new TitledBorder(null, "Execution Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_wlpanel = new GridBagConstraints();
		gbc_wlpanel.weighty = 8;
		gbc_wlpanel.fill = GridBagConstraints.BOTH;
		gbc_wlpanel.gridx = 0;
		gbc_wlpanel.gridy = 1;
		frame.getContentPane().add(wlPanel, gbc_wlpanel);
		GridBagLayout gbl_wlPanel = new GridBagLayout();
		gbl_wlPanel.columnWidths = new int[]{0, 0, 0};
		gbl_wlPanel.rowHeights = new int[]{0, 0};
		gbl_wlPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_wlPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		wlPanel.setLayout(gbl_wlPanel);	    
		
		eChartPanel = new XChartPanel<XYChart>(energyChart);
		GridBagConstraints gbc_echart = new GridBagConstraints();
		gbc_echart.fill = GridBagConstraints.BOTH;
		gbc_echart.gridx = 0;
		gbc_echart.gridy = 0;
		wlPanel.add(eChartPanel, gbc_echart);
		
		pChartPanel = new XChartPanel<XYChart>(performanceChart);
		GridBagConstraints gbc_pchart = new GridBagConstraints();
		gbc_pchart.fill = GridBagConstraints.BOTH;
		gbc_pchart.gridx = 1;
		gbc_pchart.gridy = 0;
		wlPanel.add(pChartPanel, gbc_pchart);
	}
	
	@Override
	public void run(ClientConnectionHandler cmdHandler) {
		if(cmdHandler == null)
			throw new IllegalArgumentException("Given command handler must not be null.");
		
		this.cmdHandler = cmdHandler;
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
