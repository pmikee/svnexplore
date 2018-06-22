package com.pmikee.svnexplorer;

import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;
import javax.swing.JTextArea;

import org.apache.maven.shared.invoker.InvocationOutputHandler;

class JTextAreaInvocationOutputHandler implements InvocationOutputHandler {
	private static final String LINE_SEPARATOR = "line.separator";
	boolean importantData = false;
	JTextArea textArea;
	JTable table;

	public JTextAreaInvocationOutputHandler(JTextArea textArea, JTable table) {
		this.textArea = textArea;
		this.table = table;
	}

	public void consumeLine(String line) {
		try {
			List<String> dependencies = Arrays.asList(line.split(":"));
			POMDependency d = null;
			if (dependencies.size() > 3) {
				System.out.println("groupID: " + dependencies.get(0).replace("[INFO]", "").trim() + ", artifact: "
						+ dependencies.get(1).trim() + ", version: " + dependencies.get(3).trim());
				d = new POMDependency(dependencies.get(1).trim(), dependencies.get(0).replace("[INFO]", "").trim(),
						dependencies.get(3).trim());
			}
			textArea.setText(textArea.getText() + System.getProperty(LINE_SEPARATOR) + line);
			textArea.setCaretPosition(textArea.getText().length() - 1);
			textArea.update(textArea.getGraphics());
			if (d != null) {
				((POMTableModel) table.getModel()).setRowValue(d);
			}

		} catch (Exception e) {
			textArea.setText(textArea.getText() + System.getProperty(LINE_SEPARATOR) + e.toString());
			for (StackTraceElement traceElement : e.getStackTrace()) {
				textArea.setText(textArea.getText() + System.getProperty(LINE_SEPARATOR) + traceElement);
			}
		} finally {
			textArea.setCaretPosition(textArea.getText().length() - 1);
			textArea.update(textArea.getGraphics());
		}
	}

}