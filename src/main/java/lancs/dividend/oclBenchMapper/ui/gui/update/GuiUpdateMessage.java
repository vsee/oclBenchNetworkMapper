package lancs.dividend.oclBenchMapper.ui.gui.update;


public class GuiUpdateMessage extends GuiUpdate {

	public final String message;
	
	public GuiUpdateMessage(String msg) {
		super(GuiUpdateType.MESSAGE);
		message = msg;
	}

}
