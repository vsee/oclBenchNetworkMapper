package lancs.dividend.oclBenchMapper.ui;

import java.nio.file.Path;

import lancs.dividend.oclBenchMapper.ui.UserInterfaceFactory.UiConfig;

public class NiConsoleConfig implements UiConfig {

	public final Path cmdInputFile;
	public final Path statOutputDir;
	
	public NiConsoleConfig(Path input, Path output) {
		if(input == null) throw new IllegalArgumentException("Given input file must not be null.");
		if(output == null) throw new IllegalArgumentException("Given output folder must not be null.");
		cmdInputFile = input;
		statOutputDir = output;
	}

}
