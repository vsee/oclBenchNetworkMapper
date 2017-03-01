package lancs.dividend.oclBenchMapper.energy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * The energy monitor is used to activate and deactivate OpenCL energy 
 * monitoring on machines configured with the AMD power profiling API.
 * 
 * Use {@link #startMonitoring} to activate the profiling and {@link #endMonitoring}
 * to deactivate it. A log with energy statistics can then be retrieved using
 * {@link #getEnergyLog}.
 * 
 * @author vseeker
 *
 */
public class OCLEnergyMonitor {

	private static final String LOG_OUTPUT_DIR = "/tmp";
	private static final String LOG_OUTPUT_FILE = "ocleniMONITOR.csv";
	private static final String LOG_DIR_CONFIG = "OCLENI_LOG_PATH=" + LOG_OUTPUT_DIR;
	private static final String LOG_MONITOR_CONFIG = "OCLENI_MONITOR=110";

	private static OCLEnergyMonitor _instance;
	
	public static OCLEnergyMonitor getInstance() {
		if (_instance == null)
			_instance = new OCLEnergyMonitor();

		return _instance;
	}
	
	private final Path logFilePath = Paths.get(LOG_OUTPUT_DIR, LOG_OUTPUT_FILE);

	private boolean isMonitoring;
	private Optional<EnergyLog> elog = Optional.empty();
	
	public boolean isMonitoring() {
		return isMonitoring;
	}
	
	public Optional<EnergyLog> getEnergyLog() {
		return elog;
	}
	
	public String getExecutionPrefix() {
		return LOG_DIR_CONFIG + " " + LOG_MONITOR_CONFIG + " ";
	}
	
	
	public void startMonitoring() throws IOException {
		if(isMonitoring) throw new RuntimeException("Monitoring already running.");
		
		File logFile = logFilePath.toFile();
		if(logFile.exists() && logFile.isFile()) {
			if(!logFile.delete()) 
				throw new IOException("Unable to remove existing energy log file: " + logFilePath);
		}
		
		elog = Optional.empty();
		isMonitoring = true;
	}
	
	public void endMonitoring() throws IOException {
		if(!isMonitoring) throw new RuntimeException("Monitoring not running.");
		
		File logFile = logFilePath.toFile();
		if(logFile.exists() && logFile.isFile()) {
			if(!logFile.canRead())
				throw new IOException("Unable to read energy log file: " + logFilePath);
			
			elog = Optional.of(new EnergyLog(logFilePath));
		}
		
		isMonitoring = false;
	}

}
