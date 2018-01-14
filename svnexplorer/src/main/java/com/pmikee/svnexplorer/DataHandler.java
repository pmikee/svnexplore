package com.pmikee.svnexplorer;

import javax.swing.JTextArea;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.LinkedHashMultimap;

public class DataHandler extends DefaultHandler {

	private String content;
	private String currentElement;
	private boolean insideElement = false;
	private Attributes attribs;
	private LinkedHashMultimap<Object, Object> dataMap;
	private String[] headerArray;
	private JTextArea textArea;
	private String version;
	private String releaseVersion;
	private StringBuilder csv = new StringBuilder();

	public DataHandler() {
		super();
	}

	@Override
	public void startElement(String uri, String name, String qName, Attributes atts) {
		if ("release".equalsIgnoreCase(qName)) {
			dataMap = LinkedHashMultimap.create();
			releaseVersion = atts.getValue("version");
		}
		currentElement = qName;
		content = null;
		insideElement = true;
		attribs = atts;
	}

	@Override
	public void endElement(String uri, String name, String qName) {
		if (!"document".equalsIgnoreCase(qName) && !"properties".equalsIgnoreCase(qName)
				&& !"body".equalsIgnoreCase(qName) && !"title".equalsIgnoreCase(qName)
				&& !"author".equalsIgnoreCase(qName)) {
			if (content != null && qName.equals(currentElement) && content.trim().length() > 0) {
				dataMap.put(qName, content.trim());
			}
			if (attribs != null) {
				int attsLength = attribs.getLength();
				if (attsLength > 0) {
					for (int i = 0; i < attsLength; i++) {
						String attName = attribs.getLocalName(i);
						dataMap.put(attName, attribs.getValue(i));
					}
				}
			}
		}
		if (releaseVersion != null && releaseVersion.compareTo(version) != 0) {
			return;
		}
		if ("action".equalsIgnoreCase(qName)) {
			String data[] = new String[headerArray.length];
			int i = 0;
			for (String h : headerArray) {
				if (dataMap.containsKey(h)) {
					Object[] values = dataMap.get(h).toArray();
					data[i] = (String) values[0];
					if (values.length > 1) {
						dataMap.removeAll(h);
						for (int j = 1; j < values.length; j++) {
							dataMap.put(h, values[j]);
						}
					} else {
						dataMap.removeAll(h);
					}
				} else {
					data[i] = "";
				}
				i++;
			}
			StringBuilder sb = new StringBuilder();
			for (String d : data) {
				sb.append(d);
				sb.append(';');
				csv.append(d);
				csv.append(';');
			}
			sb.append(version);
			csv.append(version);
			csv.append(System.getProperty("line.separator"));
			textArea.setText(textArea.getText() + System.getProperty("line.separator") + (sb.toString().trim()));
			textArea.setCaretPosition(textArea.getText().length());
			textArea.update(textArea.getGraphics());
		}
		insideElement = false;
		currentElement = null;
		attribs = null;
	}

	@Override
	public void characters(char ch[], int start, int length) {
		if (insideElement) {
			content = new String(ch, start, length);
		}
	}

	public void setHeaderArray(String[] headerArray) {
		this.headerArray = headerArray;
	}

	public void setTextArea(JTextArea textArea) {
		this.textArea = textArea;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public StringBuilder getCSV() {
		return csv;
	}
}