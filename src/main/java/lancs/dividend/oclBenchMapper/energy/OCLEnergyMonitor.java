package lancs.dividend.oclBenchMapper.energy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

	private static final String LOG_OUTPUT_FILE = "ocleniMONITOR.csv";
	private static final String LOG_DIR_PREFIX = "oclMonitor_";

	private static final String LOG_DIR_CONFIG = "OCLENI_LOG_PATH=";
	private static final String LOG_MONITOR_CONFIG = "OCLENI_MONITOR=110";

	private static OCLEnergyMonitor _instance;
	
	public static OCLEnergyMonitor getInstance() {
		if (_instance == null)
			_instance = new OCLEnergyMonitor();

		return _instance;
	}
	
	private boolean isMonitoring;
	private Optional<EnergyLog> elog = Optional.empty();
	private Optional<Path> elogDir = Optional.empty();
	
	public boolean isMonitoring() {
		return isMonitoring;
	}
	
	public Optional<EnergyLog> getEnergyLog() {
		return elog;
	}
	
	public void resetMonitor() {
		elogDir = Optional.empty();
		elog = Optional.empty();
		isMonitoring = false;
	}
	
	public String startMonitoring() throws IOException {
		if(isMonitoring) throw new RuntimeException("Monitoring already running.");
		
		elogDir = Optional.of(Files.createTempDirectory(LOG_DIR_PREFIX));
		elog = Optional.empty();
		isMonitoring = true;
		
		return LOG_DIR_CONFIG + elogDir.get() + " " + LOG_MONITOR_CONFIG + " ";
	}
	
	public void endMonitoring() throws IOException {
		if(!isMonitoring) throw new RuntimeException("Monitoring not running.");
		
		if(elogDir.isPresent()) {
			File logFile = elogDir.get().resolve(LOG_OUTPUT_FILE).toFile();
			if(logFile.exists() && logFile.isFile()) {
				if(!logFile.canRead())
					throw new IOException("Unable to read energy log file: " + logFile.getAbsolutePath());
				
				elog = Optional.of(new EnergyLog(logFile.toPath()));
			}
			
		}
		
		isMonitoring = false;
	}

}
