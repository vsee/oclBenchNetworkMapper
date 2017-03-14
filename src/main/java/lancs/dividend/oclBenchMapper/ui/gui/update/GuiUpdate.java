package lancs.dividend.oclBenchMapper.ui.gui.update;

public abstract class GuiUpdate {

	public enum GuiUpdateType { 
		/** Exit client and server connection */
		EXIT,
		/** Update Energy and Runtime graphs */
		GRAPH,
		/** Handle error case */
		ERROR
	}

	protected GuiUpdateType type;

	public GuiUpdate(GuiUpdateType type) {
		this.type = type;
	}
	
	public GuiUpdateType getType() { return type; }

	@Override
	public String toString() {
		return type.name();
	}
}
