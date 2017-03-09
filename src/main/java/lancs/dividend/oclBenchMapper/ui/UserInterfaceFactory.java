package lancs.dividend.oclBenchMapper.ui;

import lancs.dividend.oclBenchMapper.ui.console.ClientConsoleUi;
import lancs.dividend.oclBenchMapper.ui.console.ClientNiConsoleUi;
import lancs.dividend.oclBenchMapper.ui.console.NiConsoleConfig;
import lancs.dividend.oclBenchMapper.ui.gui.ClientGui;
import lancs.dividend.oclBenchMapper.ui.gui.ClientGuiConfig;

public final class UserInterfaceFactory {
	
	public enum UserInterfaceType { CONSOLE, GUI, NICONSOLE }
	public interface UiConfig { }
	
	public static UserInterface createUserInterface(UserInterfaceType type) {
		return createUserInterface(type, null);
	}
	
	public static UserInterface createUserInterface(UserInterfaceType type, UiConfig conf) {
		if(type == null) throw new IllegalArgumentException("Given ui type must not be null.");
		
		switch(type) {
			case CONSOLE:
				return new ClientConsoleUi();
			case GUI:
				if(!(conf instanceof ClientGuiConfig))
					throw new IllegalArgumentException("Building a client gui us requires ClientGuiConfig.");
				return new ClientGui((ClientGuiConfig) conf);
			case NICONSOLE:
				if(!(conf instanceof NiConsoleConfig))
					throw new IllegalArgumentException("Building a non interactive console us requires NiConsoleConfig.");
				return new ClientNiConsoleUi((NiConsoleConfig)conf);
			default:
				throw new IllegalArgumentException("Unhanlded workload mapper type: " + type);
		}
	}

}
