package lancs.dividend.oclBenchMapper.energy;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.utils.CSVResourceTools;

public class EnergyLog implements Serializable {

	private static final long serialVersionUID = -2373947949125610091L;

	private static final char SEPARATOR = ';';
	private static final String EXPECTED_HEADER = "Status;Event;Command;Issue;Start;Stop;Workload;Energy;Name";
	private static final int HEADER_ISSUE_IDX = 3;
	private static final int HEADER_STOP_IDX = 5;
	private static final int HEADER_ENERGY_IDX = 7;

	private static final int HEADER_SKIP = 2;
	
	private final List<List<String>> logRecords;
	
	private double energyJ;
	private double runtimeMS;
	
	public EnergyLog(Path logFile) {
		if (logFile == null)
			throw new IllegalArgumentException("Given log file path must not be null.");
		
		logRecords = parseLogFile(logFile);
		
		calculateStatistics(logRecords);
	}

	public List<List<String>> getLogRecords() { return logRecords; }
	
	public double getEnergyJ() { return energyJ; }
	public double getRuntimeMS() { return runtimeMS; }

	private List<List<String>> parseLogFile(Path logFile) {
		if (!logFile.toFile().exists() || 
			!logFile.toFile().isFile() || 
			!logFile.toFile().canRead())
			throw new IllegalArgumentException("Error reading given log file: " + logFile);
		
		checkHeader(logFile);
		
		List<List<String>> recs = null;
		try {
			recs = CSVResourceTools.readRecords(logFile, HEADER_SKIP, SEPARATOR);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		return recs;
	}

	private void checkHeader(Path logFile) {
		List<String> header = null;
		try {
			header = CSVResourceTools.readHeader(logFile, HEADER_SKIP, SEPARATOR);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		StringJoiner join = new StringJoiner(Character.toString(SEPARATOR));
		for (String column : header) join.add(column);
		if(!join.toString().equals(EXPECTED_HEADER))
			throw new RuntimeException("Unexpected header format in log file: " + join);
	}
	
	
	private void calculateStatistics(List<List<String>> records) {

		runtimeMS = 0;
		energyJ = 0;
		
		for (List<String> rec : records) {
			energyJ += Double.parseDouble(rec.get(HEADER_ENERGY_IDX));
			runtimeMS += (Double.parseDouble(rec.get(HEADER_STOP_IDX)) - 
					Double.parseDouble(rec.get(HEADER_ISSUE_IDX))) / 1000000;
		}
	}
}
