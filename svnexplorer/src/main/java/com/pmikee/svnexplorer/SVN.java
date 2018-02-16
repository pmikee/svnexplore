package com.pmikee.svnexplorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.TransferHandler;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class SVN {

	private JFrame frame;
	private JTextField filePathField;
	private JButton btnProcess;
	private JScrollPane tableScollPane;
	private JTable dependencyTable;
	private JScrollPane logScrollPane;
	private JTextArea logArea;
	private JButton svnButton;
	private JPanel buttonPanel;
	private JFileChooser chooser;
	private JCheckBox simpleMode;
	private JButton fileButton;
	private JButton btnNewButton;
	private Preferences prefs;
	private char[] password;
	private StringBuilder csv = new StringBuilder();
	private SVNRepository repository = null;
	private JButton btnExport;
	private JComboBox<String> foldersCombo;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					SVN window = new SVN();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SVN() {
		loadProperties();
		initialize();
		new Thread(new Runnable() {

			public void run() {
				initSVNFolders();
				foldersCombo.revalidate();
				foldersCombo.repaint();
			}
		}).start();

	}

	private void loadProperties() {
		prefs = Preferences.userNodeForPackage(SVN.class);

		// Preference key name
		final String PREF_NAME = "FR_SVN_ROOT";

		try {
			if (!Arrays.asList(prefs.keys()).contains(PREF_NAME)) {
				prefs.put(PREF_NAME, "");
			}
		} catch (BackingStoreException e) {
			logArea.setText(System.getProperty("line.separator") + " " + ReflectionToStringBuilder.toString(e));
		}

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 843, 883);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 800 };
		gridBagLayout.rowHeights = new int[] { 800 };
		gridBagLayout.columnWeights = new double[] { 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		frame.getContentPane().setLayout(gridBagLayout);

		JPanel panel = new JPanel();

		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		frame.getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 150, 200, 0, 200, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 329, 260, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);
		panel.setTransferHandler(handler);

		foldersCombo = new JComboBox();
		GridBagConstraints gbc_foldersCombo = new GridBagConstraints();
		gbc_foldersCombo.gridwidth = 2;
		gbc_foldersCombo.insets = new Insets(0, 0, 5, 5);
		gbc_foldersCombo.fill = GridBagConstraints.HORIZONTAL;
		gbc_foldersCombo.gridx = 0;
		gbc_foldersCombo.gridy = 0;
		panel.add(foldersCombo, gbc_foldersCombo);
		DefaultComboBoxModel model = new DefaultComboBoxModel();

		foldersCombo.setModel(model);

		foldersCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				process();

			}
		});

		buttonPanel = new JPanel();
		GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_buttonPanel.insets = new Insets(0, 0, 5, 5);
		gbc_buttonPanel.gridx = 3;
		gbc_buttonPanel.gridy = 0;
		panel.add(buttonPanel, gbc_buttonPanel);
		buttonPanel.setLayout(new GridLayout(1, 2, 0, 0));

		svnButton = new JButton("Változások");

		buttonPanel.add(svnButton);

		btnExport = new JButton("Export");
		buttonPanel.add(btnExport);
		btnExport.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				logArea.setText(logArea.getText() + System.getProperty("line.separator") + "verziók csv elkészült: "
						+ ((POMTableModel) dependencyTable.getModel()).createCSV().getAbsolutePath());
			}
		});

		svnButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				POMTableModel model = (POMTableModel) dependencyTable.getModel();
				for (POMDependency d : model.getChangedValues()) {
					generateChanges(d.getArtifact(), d.getOriginalVersion(), d.getVersion());
				}
				if (csv.toString().length() > 0) {
					PrintWriter pw = null;
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH-mm-ss");
						Date date = new Date();
						File f = new File("issues" + dateFormat.format(date) + ".csv");

						pw = new PrintWriter(f, "ISO-8859-2");
						pw.write(csv.toString());
						logArea.setText(logArea.getText() + System.getProperty("line.separator") + "a fájl elkészült: "
								+ f.getAbsolutePath());
					} catch (FileNotFoundException | UnsupportedEncodingException e1) {
						logArea.setText(logArea.getText() + System.getProperty("line.separator")
								+ ToStringBuilder.reflectionToString(e1));
					} finally {
						pw.close();
					}
				}
			}
		});

		btnNewButton = new JButton("Beállítások");
		btnNewButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JLabel mvnHomeLabel = new JLabel("maven home (bin): ");
				JTextField mavenHome = new JTextField();
				if (!StringUtils.isBlank(prefs.get("MVN_HOME", ""))) {
					mavenHome.setText(prefs.get("MVN_HOME", ""));
				}

				JLabel svnTagLabel = new JLabel("SVN Tag: ");
				JTextField svnTagHome = new JTextField();
				if (!StringUtils.isBlank(prefs.get("SVN_TAG_ROOT", ""))) {
					svnTagHome.setText(prefs.get("SVN_TAG_ROOT", ""));
				}
				int n = JOptionPane.showConfirmDialog(null,
						new Object[] { mvnHomeLabel, mavenHome, svnTagLabel, svnTagHome }, "Beállítások:",
						JOptionPane.OK_CANCEL_OPTION);

				if (n != JOptionPane.OK_OPTION) {
					return;
				}
				prefs.put("MVN_HOME", mavenHome.getText());
				prefs.put("SVN_TAG_ROOT", svnTagHome.getText());
			}

		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 0;
		panel.add(btnNewButton, gbc_btnNewButton);

		filePathField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 2;
		gbc_textField.fill = GridBagConstraints.BOTH;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		filePathField.setColumns(50);
		panel.add(filePathField, gbc_textField);

		fileButton = new JButton("...");
		GridBagConstraints gbc_fileButton = new GridBagConstraints();
		gbc_fileButton.anchor = GridBagConstraints.WEST;
		gbc_fileButton.insets = new Insets(0, 0, 5, 5);
		gbc_fileButton.gridx = 2;
		gbc_fileButton.gridy = 1;
		panel.add(fileButton, gbc_fileButton);

		fileButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("XML", "xml");
				chooser.setFileFilter(filter);
				// chooser.setCurrentDirectory(new File("C:\\Users\\Miki\\Downloads\\"));

				int returnVal = chooser.showOpenDialog(panel);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					filePathField.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}

		});

		btnProcess = new JButton("Feldolgoz");
		GridBagConstraints gbc_btnProcess = new GridBagConstraints();
		gbc_btnProcess.anchor = GridBagConstraints.WEST;
		gbc_btnProcess.insets = new Insets(0, 0, 5, 5);
		gbc_btnProcess.gridx = 3;
		gbc_btnProcess.gridy = 1;
		panel.add(btnProcess, gbc_btnProcess);

		btnProcess.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				process();
			}
		});

		simpleMode = new JCheckBox("Egyszerű mód");
		simpleMode.setSelected(true);
		GridBagConstraints gbc_simpleMode = new GridBagConstraints();
		gbc_simpleMode.anchor = GridBagConstraints.WEST;
		gbc_simpleMode.insets = new Insets(0, 0, 5, 5);
		gbc_simpleMode.gridx = 0;
		gbc_simpleMode.gridy = 2;
		panel.add(simpleMode, gbc_simpleMode);

		tableScollPane = new JScrollPane();
		GridBagConstraints gbc_tableScollPane = new GridBagConstraints();
		gbc_tableScollPane.gridwidth = 4;
		gbc_tableScollPane.fill = GridBagConstraints.BOTH;
		gbc_tableScollPane.insets = new Insets(0, 0, 5, 5);
		gbc_tableScollPane.gridx = 0;
		gbc_tableScollPane.gridy = 3;
		panel.add(tableScollPane, gbc_tableScollPane);

		dependencyTable = new JTable(new POMTableModel()) {

			private Border outside = new MatteBorder(1, 0, 1, 0, Color.RED);
			private Border inside = new EmptyBorder(0, 1, 0, 1);
			private Border highlight = new CompoundBorder(outside, inside);

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component c = super.prepareRenderer(renderer, row, column);
				JComponent jc = (JComponent) c;
				if (isRowSelected(row)) {
					jc.setBorder(highlight);
				}

				if (!isRowSelected(row)) {
					c.setBackground(getBackground());
					POMDependency d = ((POMTableModel) getModel()).getRowValue(row);
					if (d != null && !StringUtils.isBlank(d.getOriginalVersion())) {
						c.setBackground(Color.RED);
					}
				}
				return c;

			}
		};

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dependencyTable.getModel());
		dependencyTable.setRowSorter(sorter);
		dependencyTable.changeSelection(0, 0, false, false);

		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
		sorter.setSortKeys(sortKeys);

		tableScollPane.setViewportView(dependencyTable);

		logScrollPane = new JScrollPane();
		GridBagConstraints gbc_logScrollPane = new GridBagConstraints();
		gbc_logScrollPane.gridwidth = 5;
		gbc_logScrollPane.fill = GridBagConstraints.BOTH;
		gbc_logScrollPane.gridx = 0;
		gbc_logScrollPane.gridy = 4;
		panel.add(logScrollPane, gbc_logScrollPane);

		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setRows(25);
		logArea.setColumns(50);
		DefaultCaret caret = (DefaultCaret) logArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		logArea.setCaret(caret);

		logScrollPane.setColumnHeaderView(logArea);
		logScrollPane.setViewportView(logArea);

	}

	private TransferHandler handler = new TransferHandler() {

		@Override
		public boolean canImport(TransferHandler.TransferSupport support) {
			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				return false;
			}
			return true;
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			if (!canImport(support)) {
				return false;
			}

			Transferable t = support.getTransferable();

			try {
				List<File> fileList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
				for (File f : fileList) {
					filePathField.setText(f.getAbsolutePath());
					process();
				}
			} catch (UnsupportedFlavorException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	};

	private void process() {
		POMTableModel tableModel = (POMTableModel) dependencyTable.getModel();
		tableModel.emptyTable();
		tableModel.fireTableDataChanged();
		if (simpleMode.isSelected()) {
			MavenXpp3Reader reader = new MavenXpp3Reader();
			try {
				Model model = null;
				if (!StringUtils.isBlank(filePathField.getText())) {
					model = reader.read(new FileReader(new File(filePathField.getText())));
				} else if (!StringUtils.isBlank((String) foldersCombo.getSelectedItem())) {
					try {
						SVNProperties fileProperties = new SVNProperties();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						String filename = foldersCombo.getSelectedItem() + "/trunk/pom.xml";
						repository.getFile(filename, -1, fileProperties, baos);

						model = reader.read(new ByteArrayInputStream(baos.toByteArray()));
					} catch (Exception e) {
						logArea.append(
								System.getProperty("line.separator") + " " + ToStringBuilder.reflectionToString(e));
					}
				}
				if (model != null) {
					for (Dependency dep : model.getDependencies()) {
						if (dep.getGroupId().equals("com.fusionr.erps")
								|| dep.getGroupId().equals("com.fusionr.erps.ws")) {
							POMDependency d = new POMDependency(dep.getArtifactId(), dep.getGroupId(),
									dep.getVersion());
							if (d != null) {
								tableModel.setRowValue(d);
							}
						}
					}
					if (model.getDependencyManagement() != null) {
						for (Dependency d : model.getDependencyManagement().getDependencies()) {
							tableModel.updateRow(d);
						}
					}
				}
			} catch (IOException | XmlPullParserException e) {
				logArea.setText(ReflectionToStringBuilder.toString(e));
			}
		} else {
			InvocationRequest request = new DefaultInvocationRequest();
			request.setPomFile(new File(filePathField.getText()));
			request.setGoals(Arrays.asList("dependency:list".split(", ")));

			Invoker invoker = new DefaultInvoker();
			invoker.setMavenHome(new File(prefs.get("MVN_HOME", "")));
			invoker.setOutputHandler(new JTextAreaInvocationOutputHandler(logArea, dependencyTable));
			try {
				invoker.execute(request);
			} catch (MavenInvocationException ex) {
				logArea.setText(ReflectionToStringBuilder.toString(ex));
			}
		}
	}

	private void initSVNFolders() {

		JLabel userNameLabe = new JLabel("Username: ");
		JTextField userName = new JTextField();
		if (!StringUtils.isBlank(prefs.get("USERNAME", ""))) {
			userName.setText(prefs.get("USERNAME", ""));
		}
		JLabel label = new JLabel("Password:");
		JPasswordField jpf = new JPasswordField();
		if (password == null || password.length == 0 && StringUtils.isBlank(userName.getText())) {
			int n = JOptionPane.showConfirmDialog(null, new Object[] { userNameLabe, userName, label, jpf },
					"Password:", JOptionPane.OK_CANCEL_OPTION);

			if (n != JOptionPane.OK_OPTION) {
				return;
			}
			prefs.put("USERNAME", userName.getText());
			password = jpf.getPassword();
		}

		DAVRepositoryFactory.setup();
		String url = "https://svn/fr/mvn/com.fusionr/";

		try {

			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil
					.createDefaultAuthenticationManager(userName.getText().trim(), password);
			repository.setAuthenticationManager(authManager);

			Collection<SVNDirEntry> entries = repository.getDir("", -1, null, (Collection<SVNDirEntry>) null);

			foldersCombo.removeAllItems();
			foldersCombo.addItem(null);
			List<String> folders = new ArrayList<String>();
			for (SVNDirEntry entry : entries) {
				folders.add(entry.getRelativePath());
			}
			Collections.sort(folders);
			for (String folder : folders) {
				if (folder.contains("webapp")) {
					foldersCombo.addItem(folder);
				}
			}
		} catch (Exception e) {
			logArea.append(ToStringBuilder.reflectionToString(e));
		}

	}

	private void generateChanges(String artifactId, String startVersion, String version) {

		try {

			String url = prefs.get("SVN_TAG_ROOT", "") + artifactId + "/tags";
			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
			ISVNAuthenticationManager authManager = SVNWCUtil
					.createDefaultAuthenticationManager(prefs.get("USERNAME", "").trim(), password);
			repository.setAuthenticationManager(authManager);

			List<SVNDirEntry> entries = new ArrayList(repository.getDir("", -1, null, (Collection<SVNDirEntry>) null));
			Collections.sort(entries, new SVNDirEntryComparator());
			boolean innentol = false;
			boolean idaig = false;
			for (SVNDirEntry entry : entries) {
				String[] artifactArray = entry.getRelativePath().split("-");
				String versionNumber = artifactArray[artifactArray.length - 1];
				
				if (innentol && !idaig) {
					System.out.println(entry.getName());
					if (StringUtils.countMatches(versionNumber, ".") != StringUtils.countMatches(startVersion, ".")
							&& StringUtils.countMatches(versionNumber, ".") >= 2
							&& StringUtils.countMatches(version, ".") >= 2
							&& !versionNumber.split(Pattern.quote("."))[2]
									.equals(version.split(Pattern.quote("."))[2])) {
						continue;
					}

					SVNProperties fileProperties = new SVNProperties();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					String filename = entry.getRelativePath() + "/src/package/changes.xml";
					repository.getFile(filename, -1, fileProperties, baos);

					XMLReader myReader = XMLReaderFactory.createXMLReader();
					DataHandler handler = new DataHandler();
					handler.setHeaderArray(new String[] { "issue", "dev", "action" });
					handler.setTextArea(logArea);

					List<String> versionString = Arrays.asList(entry.getName().split("-"));
					handler.setVersion(versionString.get(versionString.size() - 1));
					handler.setArtifact(artifactId);
					myReader.setContentHandler(handler);
					myReader.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())));
					csv.append(handler.getCSV());
				}
				if (!innentol && entry.getName().equals(artifactId + "-" + startVersion)) {
					innentol = true;
				}
				if (!idaig && entry.getName().equals(artifactId + "-" + version)) {
					idaig = true;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			logArea.append(System.getProperty("line.separator") + " " + ToStringBuilder.reflectionToString(e));
		}

	}

}
