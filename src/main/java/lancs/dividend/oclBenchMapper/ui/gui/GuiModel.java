package lancs.dividend.oclBenchMapper.ui.gui;

import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import lancs.dividend.oclBenchMapper.benchmark.BenchExecutionResults;
import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class GuiModel {
	
	public enum ExecutionMode { MANUAL, AUTOMATIC }
		
	protected static final String AUTOMATIC_BTN_STOP_TEXT = "Stop Automatic";
	protected static final String AUTOMATIC_BTN_START_TEXT = "Start Automatic";
	protected static final String AUTOMATIC_BTN_STOPPING_TEXT = "Stopping Automatic";
	
	protected static final String RUN_BTN_RUN_TEXT = "Run";
	protected static final String RUN_BTN_RUNNING_TEXT = "Running";
	protected static final String ALTER_CHECKBOX_TEXT = "Execute Alternative Mappings";

	protected static final String GRAPH_SERIES_NAME_MAPPER = "mapper";
	protected static final int MAX_ITERATION_DISPLAY = 10;
	
	protected ExecutionMode activeMode;
	
	protected JFrame frame;
	protected JComboBox<Benchmark> benchcbox;
	protected JComboBox<String> datacbox;
	protected XChartPanel<XYChart> eChartPanel;
	protected XChartPanel<XYChart> pChartPanel;
	
	protected XYChart energyChart;
	protected XYChart performanceChart;
	
	protected JButton btnRun;
	protected JButton btnAutomatic;
	protected JCheckBox alterChkBox;
	
	protected JScrollPane msgOutScroll;
	protected JTextArea msgOutTextArea;
	
	protected JTable resTable;
	
	protected ClientConnectionHandler cmdHandler;
	protected WorkloadMapper wlMap;

	protected Hashtable<String, Hashtable<Benchmark, Hashtable<String, 
					Hashtable<ExecutionDevice, BenchExecutionResults>>>> serverExecStats;
	
	protected long iteration;
	protected boolean initialIteration;
	protected List<Double> iterationData;
	protected TreeMap<String, GraphSeriesData> series;
}
