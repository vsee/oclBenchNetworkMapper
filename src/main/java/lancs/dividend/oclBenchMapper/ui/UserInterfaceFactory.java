package lancs.dividend.oclBenchMapper.ui;

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
				return new ClientGui();
			case NICONSOLE:
				if(!(conf instanceof NiConsoleConfig))
					throw new IllegalArgumentException("Building a non interactive console us requires NiConsoleConfig.");
				return new ClientNiConsoleUi((NiConsoleConfig)conf);
			default:
				throw new IllegalArgumentException("Unhanlded workload mapper type: " + type);
		}
	}

}
