package lancs.dividend.oclBenchMapper.userCmd;

import java.util.StringJoiner;

import lancs.dividend.oclBenchMapper.server.RodiniaRunner.DataSetSize;
import lancs.dividend.oclBenchMapper.server.RodiniaRunner.RodiniaBin;

public class RunBenchCmd extends UserCommand {

	private static final long serialVersionUID = -8705036194082222085L;
	
	/** Specifies the execution device for a given benchmark */
	public enum ExecutionDevice { CPU, GPU	}

	private static final ExecutionDevice DEFAULT_EXEC_DEVICE = ExecutionDevice.CPU;
	
	private final RodiniaBin bin;
	private final DataSetSize dsetSize;
	private ExecutionDevice device;
	
	public RunBenchCmd(RodiniaBin benchBinary, DataSetSize datasetSize) {
		super(CmdType.RUNBENCH);
		bin = benchBinary;
		dsetSize = datasetSize;
		device = DEFAULT_EXEC_DEVICE;
	}
	
	public RodiniaBin getBinaryName() { return bin; }
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
