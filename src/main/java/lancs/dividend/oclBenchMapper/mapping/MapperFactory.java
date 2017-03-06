package lancs.dividend.oclBenchMapper.mapping;

import java.nio.file.Paths;

public final class MapperFactory {

	public enum MapperType { FCFS, DUPLICATE, PREDICTIVE }
	
	public static WorkloadMapper createWorkloadMapper(MapperType type) {
		if(type == null) throw new IllegalArgumentException("Given mapper type must not be null.");
		switch(type) {
			case FCFS:
				return new FcfsMapper();
			case DUPLICATE:
				return new DuplicateMapper();
			case PREDICTIVE:
				// TODO avoid hard coding here
				return new PredictiveMapper(Paths.get("src/main/resources/dividend_device_predictions.csv"));
			default:
				throw new IllegalArgumentException("Unhanlded workload mapper type: " + type);
		}
	}
	
}
