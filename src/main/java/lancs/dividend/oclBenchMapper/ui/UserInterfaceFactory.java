package lancs.dividend.oclBenchMapper.ui;

public final class UserInterfaceFactory {
	
	public enum UserInterfaceType { CONSOLE, GUI }
	
	public static UserInterface createUserInterface(UserInterfaceType type) {
		if(type == null) throw new IllegalArgumentException("Given ui type must not be null.");
		
		switch(type) {
			case CONSOLE:
				return new ClientConsoleInterface();
			case GUI:
				return new ClientGui();
			default:
				throw new IllegalArgumentException("Unhanlded workload mapper type: " + type);
		}
	}

}
