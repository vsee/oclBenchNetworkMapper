package lancs.dividend.oclBenchMapper.benchmark;

import java.util.Hashtable;
import java.util.TreeMap;

public class BenchmarkData {

	private static Hashtable<Benchmark, TreeMap<String, String>> dataArgConfig;
	
	public static void initialise(Hashtable<Benchmark, TreeMap<String, String>> dataConfig) {
		if(dataConfig == null) throw new IllegalArgumentException("Given data config must not be null.");
		dataArgConfig = dataConfig;
	}
	
	public static String getDataPath(Benchmark rbin, String dsetSize) {
		if(rbin == null) throw new IllegalArgumentException("Given Benchmark must not be null");
		if(dsetSize == null) throw new IllegalArgumentException("Given dataset size must not be null");
		
		if(!isValidDataSetSize(rbin, dsetSize)) return null;
		
		return dataArgConfig.get(rbin).get(dsetSize);
	}

	public static boolean isValidDataSetSize(Benchmark rbin, String dsetSize) {
		if(rbin == null) throw new IllegalArgumentException("Given Benchmark must not be null");
		if(dsetSize == null) throw new IllegalArgumentException("Given dataset size must not be null");
		
		if(!dataArgConfig.containsKey(rbin)) return false;
		if(!dataArgConfig.get(rbin).containsKey(dsetSize)) return false;
		
		return true;
	}

	public static String[] getAvailableDSetSizes(Benchmark rbin) {
		if(rbin == null) throw new IllegalArgumentException("Given Benchmark must not be null");
		if(!dataArgConfig.containsKey(rbin)) return null;
		
		return dataArgConfig.get(rbin).keySet().toArray(new String[dataArgConfig.get(rbin).size()]);
	}
}
