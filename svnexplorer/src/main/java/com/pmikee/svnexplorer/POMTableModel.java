package com.pmikee.svnexplorer;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.maven.model.Dependency;

public class POMTableModel extends AbstractTableModel {

	private String[] columnNames = new String[]
		{ "groupId", "artifact", "version" };
	private List<POMDependency> data = new ArrayList<>();
	private List<POMDependency> oldData = new ArrayList<>();
	private Set<POMDependency> changedDeps = new HashSet<>();
	private List<String> artifacts = new ArrayList<>();

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
		artifacts.add(d.getArtifact());
		oldData.add(d);
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (data.isEmpty()) {
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
				POMDependency d = data.get(row);
				d.setOriginalVersion(data.get(row).getVersion());
				d.setVersion(val);
				data.set(row, d);
				changedDeps.add(data.get(row));
				break;
		}
		fireTableRowsUpdated(0, data.size() - 1);
	}

	public void emptyTable() {
		data = new ArrayList<>();
	}

	public File createCSV() {
		PrintWriter pw = null;
		File f = null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH-mm-ss");
			Date date = new Date();
			f = new File("versions" + dateFormat.format(date) + ".csv");

			pw = new PrintWriter(f);
			for (POMDependency d : data) {
				pw.write(d.toCsv());
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			pw.close();
		}
		return f;
	}

	public void updateRow(Dependency dep) {
		POMDependency innerDep = new POMDependency(dep.getArtifactId(), dep.getGroupId(), dep.getVersion());
		if (!artifacts.contains(innerDep.getArtifact()) && innerDep.getGroupId().equals("com.fusionr.erps")) {
			data.add(innerDep);
			oldData.add(innerDep);
		}
		for (POMDependency d : data) {
			if (d.getArtifact().equals(dep.getArtifactId()) && StringUtils.isBlank(d.getVersion())) {
				data.set(data.indexOf(d), innerDep);
			}
		}
		for (POMDependency d : oldData) {
			if (d.getArtifact().equals(dep.getArtifactId()) && StringUtils.isBlank(d.getVersion())) {
				d.setVersion(dep.getVersion());
			}
		}
		Collections.sort(data);
		fireTableDataChanged();
	}

	public Set<POMDependency> getChangedValues() {
		return changedDeps;
	}

	public void setRowColour(int row, Color c) {
		fireTableRowsUpdated(row, row);
	}

	public POMDependency getRowValue(int row) {
		if (row < data.size()) {
			return data.get(row);
		}
		return null;
	}

	public Color getRowColour(int row) {
		if (data.get(row).getOriginalVersion() != null) {
			System.out.println(ToStringBuilder.reflectionToString(data.get(row)));
		}
		return data.get(row).getOriginalVersion() != null ? Color.RED : Color.WHITE;
	}

}
