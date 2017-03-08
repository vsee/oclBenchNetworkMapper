package lancs.dividend.oclBenchMapper.mapping;

import java.nio.file.Path;

import lancs.dividend.oclBenchMapper.mapping.MapperFactory.MapperConfig;

public class PredictiveMapperConfig implements MapperConfig {
	public final Path mappingPrecompute;
	public PredictiveMapperConfig(Path mappingPrecompute) {
		this.mappingPrecompute = mappingPrecompute;
	}
}
