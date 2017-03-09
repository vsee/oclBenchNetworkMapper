package lancs.dividend.oclBenchMapper.energy;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;

import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;

public class EnergyLog implements Serializable {

	private static final long serialVersionUID = -2373947949125610091L;

	private final List<List<String>> logRecords;
	
	private double energyJ;
	private double runtimeMS;
	
	public EnergyLog(Path logFile) {
		if (logFile == null)
			throw new IllegalArgumentException("Given log file path must not be null.");
		
		logRecords = OclBenchMapperCsvHandler.parseEnergyLogFile(logFile);
		
		calculateStatistics(logRecords);
	}

	public List<List<String>> getLogRecords() { return logRecords; }
	
	public double getEnergyJ() { return energyJ; }
	public double getRuntimeMS() { return runtimeMS; }

	private void calculateStatistics(List<List<String>> records) {

		runtimeMS = 0;
		energyJ = 0;
		
		for (List<String> rec : records) {
			energyJ += Double.parseDouble(rec.get(OclBenchMapperCsvHandler.HEADER_ELOG_ISSUE_IDX));
			runtimeMS += (Double.parseDouble(rec.get(OclBenchMapperCsvHandler.HEADER_ELOG_STOP_IDX)) - 
					Double.parseDouble(rec.get(OclBenchMapperCsvHandler.HEADER_ELOG_ISSUE_IDX))) / 1000000;
		}
	}
}
