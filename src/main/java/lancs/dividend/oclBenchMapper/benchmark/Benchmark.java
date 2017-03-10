package lancs.dividend.oclBenchMapper.benchmark;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

public final class Benchmark implements Comparable<Benchmark>, Serializable {

	private static final long serialVersionUID = -1489598705332657801L;
	
	private static Hashtable<String, Benchmark> benchmarks = new Hashtable<>();
	private static Benchmark[] values;
	
	public static void initialise(List<String> availableBenchmarks) {
		for (String bench : availableBenchmarks) {
			if(bench == null) throw new RuntimeException("Benchmark name must not be null.");
			benchmarks.put(bench, new Benchmark(bench));
		}
		
		values = benchmarks.values().toArray(new Benchmark[benchmarks.size()]);
	}

	public static Benchmark valueOf(String benchString) {
		if(benchString == null) throw new IllegalArgumentException("Given benchmark name must not be null.");
		Benchmark res = benchmarks.get(benchString);
		if(res == null)
			throw new IllegalArgumentException("Unknown benchmark: " + benchString);
		
		return res;
	}
	
	public static Benchmark[] values() {
		return values;
	}
	
	private final String name;

	private Benchmark(String n) {
		name = n;
	}
	
	
	@Override
	public int compareTo(Benchmark o) {
		return name.compareTo(o.name);
	}

	@Override
	public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Benchmark)) {
            return false;
        }
         
        Benchmark b = (Benchmark) o;
        
        return name.equals(b.name);
	}
	
	public String name() {
		return name;
	}
	
	@Override
	public String toString() {
		return name();
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
