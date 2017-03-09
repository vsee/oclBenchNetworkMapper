package lancs.dividend.oclBenchMapper.ui.gui;

import java.nio.file.Path;

import lancs.dividend.oclBenchMapper.ui.UserInterfaceFactory.UiConfig;

public class ClientGuiConfig implements UiConfig {

	public final Path executionStatFile;
	
	public ClientGuiConfig(Path statFile) {
		if(statFile == null) throw new IllegalArgumentException("Execution statistics file must not be null.");
		executionStatFile = statFile;
	}

}
