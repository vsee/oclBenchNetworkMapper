package lancs.dividend.oclBenchMapper.ui.gui.update;


public class GuiUpdateError extends GuiUpdate {

	public final String errorMessage;
	
	public GuiUpdateError(String msg) {
		super(GuiUpdateType.ERROR);
		errorMessage = msg;
	}

}
