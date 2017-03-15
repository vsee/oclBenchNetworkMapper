package lancs.dividend.oclBenchMapper.ui.gui;

import java.util.TreeMap;

import javax.swing.table.AbstractTableModel;

public class ExecutionResultsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -4361664238516575663L;
	
	private final String[] columnNames = new String[] { 
			"Mapping", "Energy J", "Runtime ms" };
	private final String[] rowNames;
    private final TreeMap<String, GraphSeriesData> data;
    
    public ExecutionResultsTableModel(TreeMap<String, GraphSeriesData> series) {
    	if(series == null)
    		throw new IllegalArgumentException("Given mapper names must not be null.");
    	data = series;
    	rowNames = series.keySet().toArray(new String[series.size()]);
    }
    
	@Override
	public int getRowCount() { return data.size(); }

	@Override
	public int getColumnCount() { return columnNames.length; }
	
	@Override
	public String getColumnName(int column) { return columnNames[column]; }
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case 0: return data.get(rowNames[rowIndex]).name;
			case 1: return data.get(rowNames[rowIndex]).getTotalEnergyJ(); 
			case 2: return data.get(rowNames[rowIndex]).getTotalRuntimeMS(); 
			default: throw new IllegalArgumentException("Invalid column index: " + columnIndex);
		}
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return getValueAt(0, columnIndex).getClass();
	}

//	@Override
//	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//		data[rowIndex][columnIndex] = aValue;
//		fireTableCellUpdated(rowIndex, columnIndex);
//	}
}
