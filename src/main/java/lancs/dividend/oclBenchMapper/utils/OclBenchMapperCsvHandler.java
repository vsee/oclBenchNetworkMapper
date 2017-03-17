package lancs.dividend.oclBenchMapper.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringJoiner;
import java.util.TreeMap;

import lancs.dividend.oclBenchMapper.benchmark.BenchExecArgs;
import lancs.dividend.oclBenchMapper.benchmark.BenchExecutionResults;
import lancs.dividend.oclBenchMapper.benchmark.BenchFullExecutionResults;
import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.mapping.CmdToDeviceMapping;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.RunBenchCmd;

public final class OclBenchMapperCsvHandler {

	private static final String BENCHMARK_CONFIG_HEADER = "Benchmark";

	private static final String USER_COMMAND_HEADER = BENCHMARK_CONFIG_HEADER + ",DataSetSize,ExecutionDevice,Iterations";
	
	private static final String BENCHMARK_DATA_CONFIG_HEADER = BENCHMARK_CONFIG_HEADER + ",DataSetSize,DataArgs";
	private static final String BENCHMARK_EXEC_CONFIG_HEADER = BENCHMARK_CONFIG_HEADER + ",ExecutionDevice,BinDir,ExecCmd";
	
	private static final String PRECOMP_MAPPING_HEADER = "Architecture,Benchmark,DataSetSize,ExecutionDevice";
	private static final String EXEC_STATS_HEADER = PRECOMP_MAPPING_HEADER + ",AvgEnergyJ,AvgRuntimeMS";

	private static final String ENERGY_LOG_HEADER = "Status;Event;Command;Issue;Start;Stop;Workload;Energy;Name";
	private static final int ENERGY_LOG_HEADER_SKIP = 2;
	private static final char ENERGY_LOG_SEPARATOR = ';';
	
	private static final int FIELD_BIN_IDX = 0;
	private static final int FIELD_DATA_IDX = 1;
	private static final int FIELD_DEVICE_IDX = 2;
	
	private static final int FIELD_STATS_ARCH_IDX = 0;
	private static final int FIELD_STATS_BIN_IDX = 1;
	private static final int FIELD_STATS_DATA_IDX = 2;
	private static final int FIELD_STATS_DEVICE_IDX = 3;
	
	private static final int FIELD_ITER_IDX = 3;
	
	private static final int FIELD_STATS_ENERGY_IDX = 4;
	private static final int FIELD_STATS_RUNTIME_IDX = 5;
	
	private static final int FIELD_ARGS_IDX = 2;
	
	private static final int FIELD_EXEC_CONF_DEVICE_IDX = 1;
	private static final int FIELD_BINDIR_IDX = 2;
	private static final int FIELD_EXECCMD_IDX = 3;

	public static final int FIELD_ELOG_ISSUE_IDX = 3;
	public static final int FIELD_ELOG_STOP_IDX = 5;
	public static final int FIELD_ELOG_ENERGY_IDX = 7;

	
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
	
	
	
	
	public static Hashtable<String, Hashtable<Benchmark, Hashtable<String, ExecutionDevice>>> parsePrecomputedMapping(Path csvFile) {
		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.PRECOMP_MAPPING_HEADER);
		
		Hashtable<String, Hashtable<Benchmark, Hashtable<String, ExecutionDevice>>> map =  new Hashtable<>();
		
		for (List<String> record : recs) {
			String arch = record.get(FIELD_STATS_ARCH_IDX);
			Benchmark rbin = Benchmark.valueOf(record.get(FIELD_STATS_BIN_IDX));
			String data = record.get(FIELD_STATS_DATA_IDX);
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(FIELD_STATS_DEVICE_IDX));
			
			if(!map.containsKey(arch)) map.put(arch, new Hashtable<>());
			if(!map.get(arch).containsKey(rbin)) map.get(arch).put(rbin, new Hashtable<>());
			map.get(arch).get(rbin).put(data, dev);
		}
		
		return map;
	}
	
	public static void writePrecomputedMapping(Path csvFile, List<String[]> records) {
		CSVResourceTools.writeCSVFile(csvFile, PRECOMP_MAPPING_HEADER.split(","), records);
		System.out.println("Optimal mapping written to: " + csvFile);
	}
	
	
	
	public static List<CmdToDeviceMapping> parseUserCommands(Path csvFile) {
		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.USER_COMMAND_HEADER);
		
		List<CmdToDeviceMapping> res = new ArrayList<>();
		for (List<String> record : recs) {
			Benchmark rbin = Benchmark.valueOf(record.get(FIELD_BIN_IDX));
			String data = record.get(FIELD_DATA_IDX);
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(FIELD_DEVICE_IDX));
			
			int iterations = Integer.valueOf(record.get(FIELD_ITER_IDX));
			
			for(int i = 0; i < iterations; i++) {
				res.add(new CmdToDeviceMapping(new RunBenchCmd(rbin, data), dev));
			}
		}
		
		return res;
	}
	
	
	public static Hashtable<String, Hashtable<Benchmark, Hashtable<String, 
							Hashtable<ExecutionDevice, BenchExecutionResults>>>> parseExecutionStats(Path csvFile) {

		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.EXEC_STATS_HEADER);

		Hashtable<String, Hashtable<Benchmark, Hashtable<String, 
				Hashtable<ExecutionDevice, BenchExecutionResults>>>> res = new Hashtable<>();
		
		for (List<String> record : recs) {
			String arch = record.get(FIELD_STATS_ARCH_IDX);
			Benchmark rbin = Benchmark.valueOf(record.get(FIELD_STATS_BIN_IDX));
			String data =record.get(FIELD_STATS_DATA_IDX);
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(FIELD_STATS_DEVICE_IDX));
			double avg_energyJ = Double.valueOf(record.get(FIELD_STATS_ENERGY_IDX));
			double avg_runtimeMS = Double.valueOf(record.get(FIELD_STATS_RUNTIME_IDX));
			
			if(!res.containsKey(arch))
				res.put(arch, new Hashtable<>());
			if(!res.get(arch).containsKey(rbin))
				res.get(arch).put(rbin, new Hashtable<>());
			if(!res.get(arch).get(rbin).containsKey(data))
				res.get(arch).get(rbin).put(data, new Hashtable<>());
			
			res.get(arch).get(rbin).get(data).put(dev, new BenchExecutionResults(avg_energyJ, avg_runtimeMS));
		}
		
		return res;
	}
	
	public static void writeExecutionStats(Path csvFile, List<String[]> records) {
		CSVResourceTools.writeCSVFile(csvFile, EXEC_STATS_HEADER.split(","), records);
		System.out.println("Execution statistics written to: " + csvFile);
	}
	
	public static Hashtable<Benchmark, Hashtable<String, 
					Hashtable<ExecutionDevice, List<BenchFullExecutionResults>>>> parseFullExecutionStats(Path datFile) {
		List<BenchFullExecutionResults> rawList = new ArrayList<>();
		
		try (ObjectInputStream ois = new ObjectInputStream(
				Files.newInputStream(datFile, StandardOpenOption.READ))) {

			int size = ois.readInt();
			for(int i = 0; i < size; i++) {
				rawList.add((BenchFullExecutionResults) ois.readObject());
			}

		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Reading full execution statistics failed: " + e);
			e.printStackTrace();
			return null;
		}
		
		Hashtable<Benchmark, Hashtable<String, 
			Hashtable<ExecutionDevice, List<BenchFullExecutionResults>>>> res = new Hashtable<>();
		
		for (BenchFullExecutionResults exec : rawList) {
			if(!res.containsKey(exec.bin))
				res.put(exec.bin, new Hashtable<>());
			if(!res.get(exec.bin).containsKey(exec.dset))
				res.get(exec.bin).put(exec.dset, new Hashtable<>());
			if(!res.get(exec.bin).get(exec.dset).containsKey(exec.dev))
				res.get(exec.bin).get(exec.dset).put(exec.dev, new ArrayList<BenchFullExecutionResults>());

			res.get(exec.bin).get(exec.dset).get(exec.dev).add(exec);
		}
		
		return res;
	}
	
	public static void writeFullExecutionStats(Path datFile, List<BenchFullExecutionResults> results) {
		try(ObjectOutputStream oos = new ObjectOutputStream(
				Files.newOutputStream(datFile, StandardOpenOption.CREATE, 
						StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
			
			oos.writeInt(results.size());
			for (BenchFullExecutionResults res : results) {
				oos.writeObject(res);
			}
			System.out.println("Full execution statistics written to: " + datFile);
		} catch (IOException e) {
			System.err.println("Writing full execution statistics failed: " + e);
			e.printStackTrace();
		}
	}
	
	public static List<List<String>> parseEnergyLogFile(Path csvFile) {
		if (!csvFile.toFile().exists() || 
			!csvFile.toFile().isFile() || 
			!csvFile.toFile().canRead())
			throw new IllegalArgumentException("Error reading given log file: " + csvFile);
		
		return parseRecords(csvFile, OclBenchMapperCsvHandler.ENERGY_LOG_HEADER, ENERGY_LOG_HEADER_SKIP, ENERGY_LOG_SEPARATOR);
	}
	
	
	public static Hashtable<Benchmark, TreeMap<String, String>> parseBenchmarkDataConfig(Path csvFile) {

		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.BENCHMARK_DATA_CONFIG_HEADER);

		Hashtable<Benchmark, TreeMap<String, String>> res = new Hashtable<>();
		
		for (List<String> record : recs) {
			Benchmark rbin = Benchmark.valueOf(record.get(FIELD_BIN_IDX));
			String data = record.get(FIELD_DATA_IDX);
			String args = record.get(FIELD_ARGS_IDX);
			
			if(!res.containsKey(rbin))
				res.put(rbin, new TreeMap<>());
			res.get(rbin).put(data, args);
		}
		
		return res;
	}
	
	public static Hashtable<Benchmark, Hashtable<ExecutionDevice, BenchExecArgs>> parseBenchmarkExecConfig(Path csvFile) {

		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.BENCHMARK_EXEC_CONFIG_HEADER);

		Hashtable<Benchmark, Hashtable<ExecutionDevice, BenchExecArgs>> res = new Hashtable<>();
		
		for (List<String> record : recs) {
			Benchmark rbin = Benchmark.valueOf(record.get(FIELD_BIN_IDX));
			ExecutionDevice dev = ExecutionDevice.valueOf(record.get(FIELD_EXEC_CONF_DEVICE_IDX));
			String binDir = record.get(FIELD_BINDIR_IDX);
			String execCmd = record.get(FIELD_EXECCMD_IDX);
			
			if(!res.containsKey(rbin))
				res.put(rbin, new Hashtable<>());
			res.get(rbin).put(dev, new BenchExecArgs(binDir, execCmd));
		}
		
		return res;
	}

	public static List<String> parseBenchmarkConfig(Path csvFile) {

		List<List<String>> recs = parseRecords(csvFile, OclBenchMapperCsvHandler.BENCHMARK_CONFIG_HEADER);

		List<String> res = new ArrayList<>();
		
		for (List<String> record : recs) {
			res.add(record.get(FIELD_BIN_IDX));
		}
		
		return res;
	}
	
	
}
