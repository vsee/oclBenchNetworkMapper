package lancs.dividend.oclBenchMapper.ui.gui.update;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.ui.gui.GraphUpdate;


public class GuiUpdateGraph extends GuiUpdate {

	public final Hashtable<String, GraphUpdate> seriesUpdates;
	
	public GuiUpdateGraph(Hashtable<String, GraphUpdate> seriesUpdates) {
		super(GuiUpdateType.GRAPH);
		this.seriesUpdates = seriesUpdates;
	}

}
