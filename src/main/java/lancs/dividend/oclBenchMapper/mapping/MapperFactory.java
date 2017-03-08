package lancs.dividend.oclBenchMapper.mapping;


public final class MapperFactory {
	
	public enum MapperType { FCFS, DUPLICATE, PREDICTIVE }
	public interface MapperConfig { }
	
	public static WorkloadMapper createWorkloadMapper(MapperType type) {
		return createWorkloadMapper(type, null);
	}
	
	public static WorkloadMapper createWorkloadMapper(MapperType type, MapperConfig conf) {
		if(type == null) throw new IllegalArgumentException("Given mapper type must not be null.");
		switch(type) {
			case FCFS:
				return new FcfsMapper();
			case DUPLICATE:
				return new DuplicateMapper();
			case PREDICTIVE:
				if(!(conf instanceof PredictiveMapperConfig))
					throw new IllegalArgumentException("Building a predictive mapper requires a PredictiveMapperConfig.");
				return new PredictiveMapper((PredictiveMapperConfig)conf);
			default:
				throw new IllegalArgumentException("Unhanlded workload mapper type: " + type);
		}
	}
	
}
