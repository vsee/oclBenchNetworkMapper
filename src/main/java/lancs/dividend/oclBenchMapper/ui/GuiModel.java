package lancs.dividend.oclBenchMapper.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import lancs.dividend.oclBenchMapper.client.ClientConnectionHandler;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class GuiModel {

	public enum ExecutionMode { MANUAL, AUTOMATIC }
	
	protected static final String AUTOMATIC_BTN_STOP_TEXT = "Stop Automatic";
	protected static final String AUTOMATIC_BTN_START_TEXT = "Start Automatic";
	protected static final String AUTOMATIC_BTN_STOPPING_TEXT = "Stopping Automatic";
	
	protected static final String RUN_BTN_RUN_TEXT = "Run";
	protected static final String RUN_BTN_RUNNING_TEXT = "Running";
	
	protected ExecutionMode activeMode;
	
	protected JFrame frame;
	protected JComboBox<RodiniaBin> bechcbox;
	protected JComboBox<DataSetSize> datacbox;
	protected XChartPanel<XYChart> eChartPanel;
	protected XChartPanel<XYChart> pChartPanel;
	
	protected XYChart energyChart;
	protected XYChart performanceChart;
	
	protected JButton btnRun;
	protected JButton btnAutomatic;
	
	protected ClientConnectionHandler cmdHandler;
	
	protected double iteration = 0;
	protected final List<Double> energyData = new ArrayList<>();
	protected final List<Double> performanceData = new ArrayList<>();
	protected final List<Double> iterationData = new ArrayList<>();
	
}
