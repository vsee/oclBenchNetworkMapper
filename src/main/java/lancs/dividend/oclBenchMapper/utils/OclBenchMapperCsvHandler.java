package lancs.dividend.oclBenchMapper.utils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;
import lancs.dividend.oclBenchMapper.ui.gui.GraphUpdate;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

public final class OclBenchMapperCsvHandler {

	private static final String PRECOMP_MAPPING_HEADER = "RodiniaBin,DataSetSize,ExecutionDevice";
	private static final String USER_COMMAND_HEADER = PRECOMP_MAPPING_HEADER + ",Iterations";
	private static final String EXEC_STATS_HEADER = PRECOMP_MAPPING_HEADER + ",AvgEnergyJ,AvgRuntimeMS";
	
	private static final String ENERGY_LOG_HEADER = "Status;Event;Command;Issue;Start;Stop;Workload;Energy;Name";
	private static final int ENERGY_LOG_HEADER_SKIP = 2;
	private static final char ENERGY_LOG_SEPARATOR = ';';
	
	private static final int HEADER_BIN_IDX = 0;
	private static final int HEADER_DATA_IDX = 1;
	private static final int HEADER_DEVICE_IDX = 2;
	
	private static final int HEADER_ITER_IDX = 3;
	
	private static final int HEADER_ENERGY_IDX = 3;
	private static final int HEADER_RUNTIME_IDX = 4;
	
	
	public static final int HEADER_ELOG_ISSUE_IDX = 3;
	public static final int HEADER_ELOG_STOP_IDX = 5;
	public static final int HEADER_ELOG_ENERGY_IDX = 7;

	
	private static List<List<String>> parseRecords(Path csvFile, String expectedHeader) {
		return parseRecords(csvFile, expectedHeader, 0, CSVResourceTools.DEFAULT_SEPARATOR);
	}
	
	private static List<List<String>> parseRecords(Path csvFile, String expectedHeader, int skip, char separator) {
		if (csvFile == null)
			throw new IllegalArgumentException("Given csv file must not be null.");
	
		checkHeader(csvFile, expectedHeader, skip, separator);
		
		List<List<String>> recs = null;
		try {
			recs = CSVResourceTools.readRecords(csvFile, skip, separator);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		return recs;
	}
	
	private static void checkHeader(Path csvFile, String expectedHeader, int skip, char separator) {
		List<String> header = null;
		try {
			header = CSVResourceTools.readHeader(csvFile, skip, separator);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		
		StringJoiner join = new StringJoiner(Character.toString(separator));
		for (String column : header) join.add(column);
		if(!join.toString().equals(expectedHeader))
			throw new RuntimeException("Unexpected header format in csv file: " + join);
	}
	
	
	
	
	public static Hashtable<RodiniaBin, Hashtable<DataSetSize, ExecutionDevice>> parsePrecomputedMapping(Path csvFile) {
		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.PRECOMP_MAPPING_HEADER);
		
		Hashtable<RodiniaBin, Hashtable<DataSetSize, ExecutionDevice>> map =  new Hashtable<>();
		
		for (List<String> record : recs) {
			RodiniaBin rbin = RodiniaBin.valueOf(record.get(HEADER_BIN_IDX));
			DataSetSize data = DataSetSize.valueOf(record.get(HEADER_DATA_IDX));
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(HEADER_DEVICE_IDX));
			
			if(!map.containsKey(rbin)) map.put(rbin, new Hashtable<>());
			map.get(rbin).put(data, dev);
		}
		
		return map;
	}
	
	public static void writePrecomputedMapping(Path csvFile, List<String[]> records) {
		CSVResourceTools.writeCSVFile(csvFile, PRECOMP_MAPPING_HEADER.split(","), records);
		System.out.println("Optimal mapping written to: " + csvFile);
	}
	
	
	
	public static List<UserCommand> parseUserCommands(Path csvFile) {
		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.USER_COMMAND_HEADER);
		
		List<UserCommand> res = new ArrayList<>();
		for (List<String> record : recs) {
			RodiniaBin rbin = RodiniaBin.valueOf(record.get(HEADER_BIN_IDX));
			DataSetSize data = DataSetSize.valueOf(record.get(HEADER_DATA_IDX));
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(HEADER_DEVICE_IDX));
			
			int iterations = Integer.valueOf(record.get(HEADER_ITER_IDX));
			
			for(int i = 0; i < iterations; i++) {
				res.add(new RunBenchCmd(rbin, data, dev));
			}
		}
		
		return res;
	}
	
	
	public static Hashtable<RodiniaBin, Hashtable<DataSetSize, Hashtable<ExecutionDevice, GraphUpdate>>> parseExecutionStats(
			Path csvFile) {

		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.EXEC_STATS_HEADER);

		Hashtable<RodiniaBin, Hashtable<DataSetSize, Hashtable<ExecutionDevice, GraphUpdate>>> res = new Hashtable<>();
		
		for (List<String> record : recs) {
			RodiniaBin rbin = RodiniaBin.valueOf(record.get(HEADER_BIN_IDX));
			DataSetSize data = DataSetSize.valueOf(record.get(HEADER_DATA_IDX));
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(HEADER_DEVICE_IDX));
			double avg_energyJ = Double.valueOf(record.get(HEADER_ENERGY_IDX));
			double avg_runtimeMS = Double.valueOf(record.get(HEADER_RUNTIME_IDX));
			
			if(!res.containsKey(rbin))
				res.put(rbin, new Hashtable<>());
			if(!res.get(rbin).containsKey(data))
				res.get(rbin).put(data, new Hashtable<>());
			
			res.get(rbin).get(data).put(dev, new GraphUpdate(avg_energyJ, avg_runtimeMS));
		}
		
		return res;
	}
	
	public static void writeExecutionStats(Path csvFile, List<String[]> records) {
		CSVResourceTools.writeCSVFile(csvFile, EXEC_STATS_HEADER.split(","), records);
		System.out.println("Execution statistics written to: " + csvFile);
	}
	
	
	public static List<List<String>> parseEnergyLogFile(Path csvFile) {
		if (!csvFile.toFile().exists() || 
			!csvFile.toFile().isFile() || 
			!csvFile.toFile().canRead())
			throw new IllegalArgumentException("Error reading given log file: " + csvFile);
		
		return parseRecords(csvFile, OclBenchMapperCsvHandler.ENERGY_LOG_HEADER, ENERGY_LOG_HEADER_SKIP, ENERGY_LOG_SEPARATOR);
	}
}
