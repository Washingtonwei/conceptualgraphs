package charger;

import cgif.parser.javacc.CGIFParser;
import cgif.parser.javacc.ParseException;
import charger.db.DatabaseFrame;
import charger.exception.*;
import charger.obj.Graph;
import charger.util.CGButton;
import charger.util.ManagedWindow;
import charger.util.Util;
import charger.util.WindowManager;
import craft.Craft;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import kb.ObjectHistoryEvent;
import mm.MProject;

/**
 * Frame for choosing CG Rules.
 *
 * @author Bingyang Wei work based on the class HubFrame by Harry S. Delugach
 *         Copyright (c) 1998-2014
 * 
 */
public class RulesSelectionFrame extends JFrame implements ManagedWindow {

	public JMenuItem NewWindowItem = new JMenuItem(Global.strs("NewWindowLabel"));
	public static DatabaseFrame DataBaseLinkToolWindow = null; // means there
																// can only be
																// one Database
																// frame per hub
	public EditFrame currentEF;// to which CG frame we apply the list of rules
	public ArrayList<Graph> rules = new ArrayList<Graph>();// list of rules
															// selected and
															// opened
	/**
	 * time value at which the graphs folder was modified; used in keeping the
	 * graph list up to date.
	 */
	public long GraphFolderLastModified = 0;
	// public static ImageIcon logoIcon = Util.getIconFromClassPath(Hub.gifpath
	// + "CharGerLogoWhite.gif");
	// member declarations
	JLabel AvailLabel = new JLabel();
	JList GraphDisplayList = new JList();
	JScrollPane GraphDisplayPane = null; // to contain the graph display list
	CGButton OpenAllButton = new CGButton();
	CGButton CloseAllButton = new CGButton();
	CGButton SaveAllAsCGIFButton = new CGButton();
	CGButton BrowseButton = new CGButton();
	CGButton OpenButton = new CGButton();
	JMenuBar mainMenuBar = new JMenuBar();
	public Action MMTeamAnalysisAction = null;
//	CGButton NewWindowButton = new CGButton();
//	CGButton QuitButton = new CGButton();
	JLabel GraphFolderLabel = new JLabel();
	JTextField GraphFolderField = new JTextField();
	JTextArea Instructions = new JTextArea(5, 20);
	JLabel HubLabel = new JLabel();
	JLabel CGLabel = new JLabel();

	public RulesSelectionFrame(EditFrame ef) {
		currentEF = ef;
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		WindowManager.manageWindow(this, KeyStroke.getKeyStroke(KeyEvent.VK_0, Global.AcceleratorKey));
		initComponents();
		setVisible(true);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void initComponents() // throws Exception
	{

		// This needs to be put in the "Tools" of the HubFrame, if possible
		MMTeamAnalysisAction = new AbstractAction() {
			public Object getValue(String s) {
				if (s.equals(Action.NAME)) {
					return Global.strs("MMATAnalysisLabel");
				}
				return super.getValue(s);
			}

			public void actionPerformed(ActionEvent e) {
				// TODO: Eliminate this as a full-fledged analysis when it's
				// just being initialized
				performActionMMTeamAnalysis();
			}
		};

		// SET UP GRAPHICS
		// icon image to be set later
		Instructions.setBackground(Color.gray);
		Instructions.setForeground(Color.white);
		Instructions.setLocation(new Point(10, 45));
		Instructions.setVisible(true);
		Instructions.setSize(new java.awt.Dimension(200, 200));
		Instructions.setText(
				"Please select rules you want to apply to the current CG, and then press buttion Apply Selected.");
		Instructions.setEditable(false);
		Instructions.setFont(new Font("Arial", Font.BOLD + Font.ITALIC, 20));
		Instructions.setLineWrap(true);
		Instructions.setWrapStyleWord(true);

		GraphFolderLabel.setText("Rule folder:");
		GraphFolderLabel.setForeground(new Color(255, 255, 255));
		GraphFolderLabel.setLocation(new Point(10, 6));
		GraphFolderLabel.setHorizontalAlignment(JLabel.TRAILING);
		GraphFolderLabel.setVisible(true);
		GraphFolderLabel.setFont(new Font("Arial", Font.BOLD, 11));
		GraphFolderLabel.setSize(new java.awt.Dimension(85, 20));

		// to be modified during execution
		AvailLabel.setText("Rules available");
		AvailLabel.setForeground(new Color(255, 255, 255));
		AvailLabel.setLocation(new Point(380, 290));
		AvailLabel.setHorizontalAlignment(JLabel.CENTER);
		AvailLabel.setVisible(true);
		AvailLabel.setFont(new Font("Arial", Font.BOLD, 12));
		AvailLabel.setSize(new java.awt.Dimension(230, 30));

		// Initialize the buttons
		BrowseButton.setLocation(new Point(670, 6));
		BrowseButton.setText(Global.strs("BrowseLabel"));
		BrowseButton.setToolTipText("Choose and set the default graph directory");
		BrowseButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowseButtonActionPerformed(e);
			}
		});

//		NewWindowButton.setLocation(new Point(670, 55));
//		NewWindowButton.setText(Global.strs("NewWindowLabel"));
//		NewWindowButton.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				NewWindowButtonActionPerformed(e);
//			}
//		});

		OpenButton.setLocation(new Point(670, 90));
		OpenButton.setText("Apply Selected");
		OpenButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ApplySelectedRulesActionPerformed(e);
			}
		});

		OpenAllButton.setLocation(new Point(670, 125));
		OpenAllButton.setText("Apply All Rules");
		OpenAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ApplyAllRulesActionPerformed(e);
			}
		});

		SaveAllAsCGIFButton.setLocation(new Point(670, 200));
		SaveAllAsCGIFButton.setText("Save All CGIF");
		SaveAllAsCGIFButton.setToolTipText("Disabled until CGIF generation is tested.");
		SaveAllAsCGIFButton.setVisible(false);
		SaveAllAsCGIFButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SaveAllAsCGIFButtonActionPerformed();
			}
		});

		CloseAllButton.setLocation(new Point(670, 235));
		CloseAllButton.setText("Close All");
		CloseAllButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CloseAllButtonActionPerformed(e);
			}
		});

		// QuitButton.setForeground(new java.awt.Color(255, 255, 255));
//		QuitButton.setLocation(new Point(670, 270));
//		QuitButton.setText(Global.strs("QuitLabel"));
//		QuitButton.addActionListener(new java.awt.event.ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				QuitButtonActionPerformed(e);
//			}
//		});

		// SET UP ACTUAL USEFUL CONTENT DISPLAYS
		// GraphDisplayList.setMultipleMode( false ); // too many problems with
		// multiple selections
		GraphFolderField.setText("Graphs"); // will be over-ridden by
											// preferences

		GraphFolderField.setLocation(new Point(100, 6));
		GraphFolderField.setForeground(new Color(0, 0, 0));
		GraphFolderField.setVisible(true);
		GraphFolderField.setBackground(new Color(255, 255, 255));
		GraphFolderField.setFont(new Font("SansSerif", Font.PLAIN, 12));
		GraphFolderField.setSize(new java.awt.Dimension(565, 23));
		GraphFolderField.setEditable(false);
		GraphFolderField.setText(Global.GraphFolderFile.getAbsolutePath() + File.separator);

		GraphDisplayList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // default,
																							// but
																							// say
																							// it
																							// anyway

		GraphDisplayList.setForeground(new Color(0, 0, 0));
		GraphDisplayList.setLocation(new Point(480, 30));
		GraphDisplayList.setVisible(true);
		GraphDisplayList.setBackground(new Color(255, 255, 255));
		GraphDisplayList.setFont(new Font("SansSerif", Font.PLAIN, 12));
		// GraphDisplayList.setSize(new java.awt.Dimension(180, 190));
		GraphDisplayList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				GraphDisplayListMouseClicked(e);
			}
		});
		GraphDisplayList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				GraphDisplayListItemStateChanged(e);
			}
		});

		GraphDisplayPane = new JScrollPane(GraphDisplayList);
		GraphDisplayPane.setLocation(new Point(280, 35));
		GraphDisplayPane.setSize(new java.awt.Dimension(380, 260));
		GraphDisplayPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		GraphDisplayPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		GraphDisplayPane.setBorder(Global.BeveledBorder);
		GraphDisplayPane.setOpaque(true);

		// SET UP THE FRAME ITSELF
		setLocationRelativeTo(null);
		setTitle("Applying Rules to CGs");
		getContentPane().setBackground(Color.GRAY);
		setFont(new Font("Dialog", 1, 10));
		getContentPane().setLayout(null);
		setJMenuBar(mainMenuBar);
		setSize(new java.awt.Dimension(820, 400));
		// setPreferredSize(new java.awt.Dimension(642, 400));
		getContentPane().add(AvailLabel);
		getContentPane().add(GraphDisplayPane);
		getContentPane().add(OpenAllButton);
		getContentPane().add(CloseAllButton);
		getContentPane().add(SaveAllAsCGIFButton);
//		getContentPane().add(NewWindowButton);
//		getContentPane().add(QuitButton);
		getContentPane().add(GraphFolderLabel);
		getContentPane().add(GraphFolderField);
		getContentPane().add(Instructions);
		getContentPane().add(OpenButton);
		getContentPane().add(BrowseButton);

		addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(java.awt.event.FocusEvent e) {
				// Global.info( "hub frame focus gained" );
				thisFocusGained(e);
			}
		});
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent e) {
				thisWindowClosing(e);
			}

			public void windowActivated(java.awt.event.WindowEvent e) {
				// Global.info( "hub frame activated" );
				thisWindowActivated(e);
			}
		});

		refresh();
		// menuToolsQuit.setMnemonic( KeyEvent.VK_Q );
		setFont(new Font("SansSerif", Font.BOLD, 10));
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

	}

	private boolean mShown = false;

	public void addNotify() {
		super.addNotify();

		if (mShown) {
			return;
			// move components to account for insets
		}
		Insets insets = getInsets();
		Component[] components = getComponents();
		for (int i = 0; i < components.length; i++) {
			Point location = components[i].getLocation();
			location.move(location.x, location.y + insets.top);
			components[i].setLocation(location);
		}

		mShown = true;
	}

	// Close the window when the close box is clicked
	public void thisWindowClosing(java.awt.event.WindowEvent e) {
		Global.RuleSelectionFrame = null;
		WindowManager.forgetWindow(this);
		setVisible(false);
		dispose();
	}

	/**
	 * re-reads the directory to re-display the list of graph files in the user
	 * directory
	 */
	// needs to be optimized; put the text in a string and then set the list all
	// at once.
	public void refreshFileList() {

		if (!Global.GraphFolder.equals(GraphFolderField.getText())) // note
																	// upper/lower
																	// case only
																	// matters
																	// for
																	// LINUX!
		{
			GraphFolderField.setText(Global.GraphFolder);
			GraphFolderLastModified = 0; // force a refresh if user changed the
											// graph directory

		}
		// get folder name from hub frame
		File ff = new File(GraphFolderField.getText());
		// look for the graph directory here ---
		if (ff != null) {
			if (GraphFolderLastModified != ff.lastModified()) {
				GraphDisplayList.removeAll();

				String gg[] = ff.list(new FilenameFilter() {
					public boolean accept(File f, String name) {
						if (!name.startsWith(".")) {
							return Global.acceptCGXFileName(name);
						} else {
							return false;
						}
					}
				});
				// Global.info( "found " + gg.length + " files." );
				// Sort the list, not case sensitive
				Arrays.sort(gg, Global.ignoreCase);
				if (gg != null) {
					GraphDisplayList.setEnabled(false);
					GraphDisplayList.setVisible(false);

					GraphDisplayList.setListData(gg);

					GraphDisplayList.setEnabled(true);
					GraphDisplayList.setVisible(true);
				}
				GraphFolderLastModified = ff.lastModified();
			}
		}
		AvailLabel.setText(GraphDisplayList.getModel().getSize() + " graphs available");
	}

	public void thisFocusGained(java.awt.event.FocusEvent e) {
		// refresh();
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Doesn't do anything while user is making selections
	 *
	 * see #OpenButtonActionPerformed
	 * see #OpenAllButtonActionPerformed
	 */
	public void GraphDisplayListItemStateChanged(ListSelectionEvent e) {
	}

	/**
	 * Get all the selected rule file names, and convert them from objects to
	 * strings, then open them, then apply them to the main CG
	 *
	 * @see RulesSelectionFrame#openNamedFiles
	 */
	public void ApplySelectedRulesActionPerformed(ActionEvent e) {
		Object[] fileNameObjects = GraphDisplayList.getSelectedValuesList().toArray();
		String[] fileNames = new String[fileNameObjects.length];
		for (int entrynum = 0; entrynum < fileNames.length; entrynum++) {
			fileNames[entrynum] = (String) fileNameObjects[entrynum];
		}
		Global.info("Preparing to open " + fileNames.length + " files.");
		openNamedFiles(fileNames);
		// apply rules to the current graph
		currentEF.omgr.performActionApplyRules(currentEF.TheGraph, rules);
	}

	/**
	 * Opens a list of files, each in its own editor window.
	 *
	 * @param files
	 *            list of (relative) file paths
	 */
	public void openNamedFiles(String[] files) {
		if (files.length == 0) {
			return;
		}

		if (files.length > 50) {
			int result = JOptionPane.showConfirmDialog(this, "There are " + files.length + " graphs to open.\n" + ""
					+ Global.EditorNameString + " may not have enough resources.\nDo you still want to try?");
			if (result != JOptionPane.YES_OPTION) {
				return;
			}
		}
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		int numFilesOpened = 0;
		for (int n = 0; n < files.length; n++) {
			int minMB = 4;
			// pbar.setValue( n );
			// jd.toFront();
			if (Runtime.getRuntime().freeMemory() / (1024d * 1024d) > minMB) {
				// Global.info("[openNamedFiles] file " + (int) (n + 1) + " of "
				// + files.length + " is " + files[n]);
				EditFrame newEF = Global.openRuleInNewFrame(files[n]);
				numFilesOpened++;
				rules.add(newEF.TheGraph);
			} else {
				Global.warning("Memory less than " + minMB + " MB. Can't open graph.");
				break;
			}
		}
		// no need to choose an edit frame to be first
		// EditFrame pickone = Global.getNewestEditFrame();
		// if (pickone != null) {
		// Global.setCurrentEditFrame(pickone);
		// pickone.requestFocus();
		// pickone.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		// }

		Global.info("finished loading " + numFilesOpened + " files.");
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void NewWindowButtonActionPerformed(ActionEvent e) {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		EditFrame ef = new EditFrame();
		if (Global.enableEditFrameThreads) {
			new Thread(Global.EditFrameThreadGroup, ef).start();
			// Hub.setCurrentEditFrame( ef );
		}
		ef.toFront(); // Global.info( "to front in HubFrame new window button"
						// );

		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Need to modify this
	 * 
	 * @param e
	 */
	public void QuitButtonActionPerformed(ActionEvent e) {
		// Global.info( "ready for hub frame's quit." );
		if (Global.closeOutAll()) {
			System.exit(0);
		}
	}

	/**
	 * Updates all the menus and displays in the master frame, but not
	 * responsible for repaint.
	 */
	public void refresh() {

		refreshFileList();
		// prepare a memory display, but do garbage collection first
		System.gc();
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		NumberFormat nformat = NumberFormat.getNumberInstance();
		nformat.setMaximumFractionDigits(2);
		nformat.setMinimumFractionDigits(2);
		GraphFolderField.setText(Global.GraphFolderFile.getAbsolutePath() + File.separator);
		/*
		 * Code using java.lang.ref objects is for determining why some garbage
		 * is not being made finalizable
		 */

		repaint();
	}

	/**
	 * Instantiates an MProject, which then spawns all the other windows and
	 * classes needed for the MMAT.
	 *
	 *
	 */
	public void performActionMMTeamAnalysis() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		MProject mmatProject = null;
		if (mmatProject == null) {
			mmatProject = new MProject(Global.GraphFolderFile);
		} else {
			mmatProject.frame.toFront();
		}

	}

	/**
	 * Go back to the master frame main window, forcing it to be updated.
	 */
	public void performActionBackToHub() {
		Global.CharGerMasterFrame.toFront();
		Global.CharGerMasterFrame.requestFocus();
		Global.setCurrentEditFrame(null);
	}

	/**
	 * Part of the ManagedWindow interface
	 *
	 * @see ManagedWindow
	 */
	public void bringToFront() {
		performActionBackToHub();
		refresh();
	}

	/**
	 * Part of the ManagedWindow interface
	 *
	 * @see ManagedWindow
	 */
	public String getMenuItemLabel() {
		return "~ " + Global.strs("BackToHubCmdLabel");
	}

	public String getFilename() {
		return null;
	}

	public void thisWindowActivated(java.awt.event.WindowEvent e) {
		refresh();
		// if ( Hub.CurrentEditFrame != null )
		// Hub.removeCurrentEditFrame( Hub.CurrentEditFrame );
		// Hub.setCurrentEditFrame( null ); // commented 12-11-02
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void GraphDisplayListMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			ApplySelectedRulesActionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, null));
		}
	}

	public void menuToolsDatabaseLinkingToolActionPerformed(ActionEvent e) {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		// Global.info( "at menuToolsDatabaseLinkingToolActionPerformed.. ");
		if (DataBaseLinkToolWindow == null) {
			DataBaseLinkToolWindow = new DatabaseFrame(Global.DatabaseFolder + File.separator + "DBElements.txt");
		}
		DataBaseLinkToolWindow.toFront();
		DataBaseLinkToolWindow.setVisible(true);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void menuToolsCraftToolActionPerformed() {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		// Global.info( "at menuToolsCraftToolActionPerformed.. ");
		if (Global.craftModule == null) {
			Global.craftModule = new Craft();
		}
		Craft.craftWindow.refresh();
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void menuToolsQuitActionPerformed(ActionEvent e) {
		QuitButtonActionPerformed(e);
	}

	public void menuFilePreferencesActionPerformed(ActionEvent e) {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		Global.managePreferencesFrame();
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Loads a graph into a new edit frame. If the user cancels the load, no
	 * window is created.
	 *
	 * @see EditManager#actionPerformed
	 * @see IOManager
	 */
	public void menuFileOpenActionPerformed(ActionEvent e) {
		String filename = null; // = Hub.makeUpFileName(
								// FileNameField.getText(), stat );
		// setCursor( new Cursor( Cursor.WAIT_CURSOR ) );

		filename = Global.openGraphInNewFrame(null);
		// setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
	}

	public void openCGIFActionPerformed(ActionEvent e) {
		File absFile = null;
		CGIFParser parser = new CGIFParser((Reader) null);

		IOManager iomgr = new IOManager(this);
		// iomgr.FileToGraph( filename, outerFrame.TheGraph, outerFrame);
		// TODO: A long awaited CGIF parser to be invoked here
		File cgifFile = Util.queryForInputFile("Import CGIF", Global.GraphFolderFile, Global.CGIFFileFilter);
		if (cgifFile == null) {
			return;
		}
		StringBuffer stringBuilder = new StringBuffer("");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(cgifFile));
			String line = null;
			String ls = System.getProperty("line.separator");
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append(ls);
			}
		} catch (FileNotFoundException ex) {
			JOptionPane.showMessageDialog(this, ex);
			return;
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, ex);
			return;
		}

		// cgifFile = iomgr.CGIFFileToGraph( null, attempt,/* this ,*/ new
		// Point2D.Double( 0, 0 ) );
		String contents = stringBuilder.toString();
		Graph attempt = null;
		try {
			attempt = parser.parseCGIFString(contents);
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, "Error in file " + cgifFile.getName() + "\n" + ex.getMessage(),
					"CGIF Formation Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		attempt.addHistory(new ObjectHistoryEvent(cgifFile));
		EditFrame ef = new EditFrame(cgifFile, attempt, true);

		if (Global.enableEditFrameThreads) {
			new Thread(Global.EditFrameThreadGroup, ef).start();
		}
	}

	public void menuFileNewGraphActionPerformed(ActionEvent e) {
		NewWindowButtonActionPerformed(e);
	}

	public void menuFileOpenCGIFActionPerformed(ActionEvent e) {
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		openCGIFActionPerformed(e);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void ApplyAllRulesActionPerformed(ActionEvent e) {
		// JList or ListModel don't seem to have method to get everything in the
		// list
		String[] allfiles = new String[GraphDisplayList.getModel().getSize()];
		// String[] allfiles =
		// (String[])((DefaultListModel)GraphDisplayList.getModel()).toArray();
		for (int fnum = 0; fnum < allfiles.length; fnum++) {
			// for ( String filename : GraphDisplayList.getModel() ) {
			allfiles[fnum] = (String) GraphDisplayList.getModel().getElementAt(fnum);
		}

		if (allfiles.length > 0) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
		openNamedFiles(allfiles);
	}

	/**
	 * Convenience method for calling FileToGraph repeatedly for all files in
	 * the displaylist.
	 */
	public void SaveAllAsCGIFButtonActionPerformed() {
		int successful = 0;
		int failure = 0;
		// String[] allfiles = new String[ GraphDisplayList.getModel().getSize()
		// ];

		for (int entrynum = 0; entrynum < GraphDisplayList.getModel().getSize(); entrynum++) {
			File fileToConvert = new File(Global.GraphFolderFile,
					(String) GraphDisplayList.getModel().getElementAt(entrynum));
			Graph g = new Graph(null);
			try {
				File f = IOManager.FileToGraph(fileToConvert, g, null);
				Global.info(g.getBriefSummary());
				String CGIFpath = Util.stripFileExtension(fileToConvert.getAbsolutePath());
				f = IOManager.GraphToFile(g, FileFormat.CGIF2007, CGIFpath, null);
				successful++;
			} catch (CGFileException cgfe) {
				Global.warning("CG File Exception: can't convert " + fileToConvert + ": " + cgfe.getMessage());
				failure++;
			} catch (CGStorageError cgse) {
				Global.warning("CG Storage Error: can't convert " + fileToConvert + ": " + cgse.getMessage());
				failure++;
			}

		}
		String msg = null;
		if (successful > 0) {
			msg = "Successfully saved " + successful + " file(s).";
		} else {
			msg = "No files saved.";
		}
		if (failure > 0) {
			msg = msg + "\nFailed on " + failure + " file(s); see console for details.";
		}
		JOptionPane.showMessageDialog(this, msg, "Saving Files as CGIF", JOptionPane.INFORMATION_MESSAGE);
	}

	public void CloseAllButtonActionPerformed(ActionEvent e) {
		Global.closeOutAll();
		refresh();
	}

	public void BrowseButtonActionPerformed(ActionEvent e) {
		Global.queryForGraphFolder(this);
		GraphFolderField.setText(Global.GraphFolder);
		refreshFileList();
		refresh();
	}
}
