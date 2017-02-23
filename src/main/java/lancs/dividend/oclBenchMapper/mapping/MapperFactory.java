package lancs.dividend.oclBenchMapper.mapping;

public final class MapperFactory {

	public enum MapperType { FCFS, DUPLICATE }
	
	public static WorkloadMapper createWorkloadMapper(MapperType type) {
		if(type == null) throw new IllegalArgumentException("Given mapper type must not be null.");
		switch(type) {
			case FCFS:
				return new FcfsMapper();
			case DUPLICATE:
				return new DuplicateMapper();
			default:
				throw new IllegalArgumentException("Unhanlded workload mapper type: " + type);
		}
	}
	
}
