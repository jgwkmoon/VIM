package pnu.stemlab.vimnavi;

import java.util.ArrayList;
import java.util.List;

class Row extends ArrayList<String> {
	public Row() {
		super();
	}
}

public class Table extends ArrayList<Row> {
	private List<Integer> maxLength;
	private List<Boolean> leftAdjust;
	
	public Table() {
		super();
		maxLength = new ArrayList<Integer>();
		leftAdjust = new ArrayList<Boolean>();
	}
	public Table(List<String> titles) {
		super();
		maxLength = new ArrayList<Integer>();
		leftAdjust = new ArrayList<Boolean>();
		for(String t: titles)
			addNewField(t);
	}
	public void clear() {
		super.clear();
		maxLength.clear();
		leftAdjust.clear();
	}
	public void appendNewRow() {
		super.add(new Row());
	}
	public void appendRow(List<String> fields) {
		appendNewRow();
		for(String field: fields)
			addNewField(field);
	}
	public void addNewField(String field) {
		if(field==null || field.equals(""))
			field = "<none>";
		int lastRowIndex = super.size()-1;
		if(lastRowIndex<0) {
			appendNewRow();
			++lastRowIndex;
		}
		
		Row lastRow = super.get(lastRowIndex);
		lastRow.add(field);
		int lastRowLength = lastRow.size();
		while(lastRowLength > maxLength.size()) {
			maxLength.add( 0 );
			leftAdjust.add( true );
		}
		
		int fieldLength = field.length();
		int columnIndex = lastRowLength-1;
		int preMaxLenth = maxLength.get(columnIndex);
		maxLength.set(columnIndex, Math.max(preMaxLenth, fieldLength));
	}
	public void setRightAdjust(int index) {
		leftAdjust.set(index, false);
	}
	public void setMaxLength(int index, int len) {
		maxLength.set(index, len);
	}
	private String toString(Row column) {
		StringBuffer col = new StringBuffer();
		String sep = "  ";
		for(int i=0; i<column.size(); ++i) {
			int maxLen = maxLength.get(i);  // you can set a appropriate maxLen.
			String field = column.get(i);
			String format = leftAdjust.get(i) ? 
				String.format("%s%%-%ds", (i==0 ? "" : sep), maxLen):
				String.format("%s%%%ds", (i==0 ? "" : sep), maxLen);
			int fieldLen = Math.min(field.length(), maxLen);
			String subField = field.substring(0,fieldLen);
			String FormatedField = String.format(format, subField);
			col.append(FormatedField);
		}
		col.append("\n");
		return new String(col);
	}
	private String getBar() {
		StringBuffer bar = new StringBuffer();
		String sep = "  ";
		int barLength = 0;
		int sepLength = sep.length();
		for(int i=0; i<maxLength.size(); ++i)
			barLength += maxLength.get(i) + (i==0 ? 0 : sepLength);
		for(int i=0; i<barLength; ++i)
			bar.append("=");
		bar.append("\n");
		return new String(bar);
	}
	public String toString() {
		StringBuffer table = new StringBuffer();
		Row title = super.get(0);
		table.append( toString(title) );
		table.append( getBar() );
		for(int i=1; i<super.size(); ++i)
			table.append( toString(super.get(i)) );
		return new String(table);
	}
}
