package lancs.dividend.oclBenchMapper.userCmd;

import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkRunner.DataSetSize;

public class RunBenchCmd extends UserCommand {

	private static final long serialVersionUID = -8705036194082222085L;
	
	/** Specifies the execution device for a given benchmark */
	public enum ExecutionDevice { CPU, GPU	}

	private static final ExecutionDevice DEFAULT_EXEC_DEVICE = ExecutionDevice.CPU;
	
	private final Benchmark bin;
	private final DataSetSize dsetSize;
	private ExecutionDevice device;
	
	public RunBenchCmd(Benchmark benchBinary, DataSetSize datasetSize) {
		this(benchBinary, datasetSize, DEFAULT_EXEC_DEVICE);
	}
	
	public RunBenchCmd(Benchmark benchBinary, DataSetSize datasetSize, ExecutionDevice execDev) {
		super(CmdType.RUNBENCH);
		bin = benchBinary;
		dsetSize = datasetSize;
		device = execDev;
	}

	public Benchmark getBinaryName() { return bin; }
	public DataSetSize getDataSetSize() { return dsetSize; }
	public ExecutionDevice getExecutionDevice() { return device; }
	
	public void setExecutionDevice(ExecutionDevice d) {
		if(d == null) throw new IllegalArgumentException("Given execution device must not be null.");
		device = d;
	}
	
	@Override
	public String toString() {
		return new StringJoiner(",")
			.add(type.name())
			.add(bin.name())
			.add(dsetSize.name())
			.add(device.name())
			.toString();
	}

}
