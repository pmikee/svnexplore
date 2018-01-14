package com.pmikee.svnexplorer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Dependency;

public class POMTableModel extends AbstractTableModel {

	private String[] columnNames = new String[] { "groupId", "artifact", "version" };
	private List<POMDependency> data = new ArrayList<>();
	private List<POMDependency> oldData = new ArrayList<>();
	private Set<POMDependency> changedDeps = new HashSet<>();

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	public void setRowValue(POMDependency d) {
		data.add(d);
		oldData.add(d);
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		if(data.isEmpty()) {
			return "";
		}
		switch (col) {
		case 0:
			return data.get(row).getGroupId();
		case 1:
			return data.get(row).getArtifact();
		case 2:
			return data.get(row).getVersion();
		default:
			return "";
		}
	}

	@Override
	public Class getColumnClass(int c) {
		return getValueAt(0, c) != null ? getValueAt(0, c).getClass() : String.class;
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 2;

	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		String val = value.toString();
		switch (col) {
		case 0:
			data.get(row).setGroupId(val);
			changedDeps.add(data.get(row));
			break;
		case 1:
			data.get(row).setArtifact(val);
			changedDeps.add(data.get(row));
			break;
		case 2:
			data.get(row).setOriginalVersion(data.get(row).getVersion());
			data.get(row).setVersion(val);
			changedDeps.add(data.get(row));
			break;
		}
		fireTableCellUpdated(row, col);
	}
	
	public void emptyTable() {
		data = new ArrayList<>();
	}
	
	public void updateRow(Dependency dep) {
		for(POMDependency d : data) {
			if(d.getArtifact().equals(dep.getArtifactId()) && StringUtils.isBlank(d.getVersion())) {
				d.setVersion(dep.getVersion());
			}
		}
		for(POMDependency d : oldData) {
			if(d.getArtifact().equals(dep.getArtifactId()) && StringUtils.isBlank(d.getVersion())) {
				d.setVersion(dep.getVersion());
			}
		}
		fireTableDataChanged();
	}
	
	public Set<POMDependency> getChangedValues(){
		return changedDeps;
	}

}
