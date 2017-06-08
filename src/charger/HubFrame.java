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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import kb.ObjectHistoryEvent;
import mm.MProject;

// used for making the project web page link clickable

/* 
 $Header$ 
 */
/*
 CharGer - Conceptual Graph Editor
 Copyright reserved 1998-2014 by Harry S. Delugach

 This package is free software; you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as
 published by the Free Software Foundation; either version 2.1 of the
 License, or (at your option) any later version. This package is 
 distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 PARTICULAR PURPOSE. See the GNU Lesser General Public License for more 
 details. You should have received a copy of the GNU Lesser General Public
 License along with this package; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
/**
 * Central frame for coordinating CharGer graphs. Maintains a current list of
 * graphs in the graph directory, and some other miscellaneous information about
 * CharGer.
 *
 * @author Harry S. Delugach ( delugach@uah.edu ) Copyright (c) 1998-2014 by
 * Harry S. Delugach
 */
public class HubFrame extends JFrame implements ManagedWindow {

    public JMenuItem NewWindowItem = new JMenuItem( Global.strs( "NewWindowLabel" ) );
    public static DatabaseFrame DataBaseLinkToolWindow = null;	// means there can only be one Database frame per hub
    /**
     * time value at which the graphs folder was modified; used in keeping the
     * graph list up to date.
     */
    public long GraphFolderLastModified = 0;
//    public static ImageIcon logoIcon = Util.getIconFromClassPath(Hub.gifpath + "CharGerLogoWhite.gif");
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
    JMenu menuFile = new JMenu();
    JMenuItem menuFileNewGraph = new JMenuItem();
    JMenuItem menuFileOpen = new JMenuItem();
    JMenuItem menuFileOpenCGIF = new JMenuItem();
    JMenuItem menuFilePreferences = new JMenuItem();
    JMenuItem menuToolsQuit = new JMenuItem();
    JMenuItem menuConvertCGXML = new JMenuItem(); // added by Esha Deshpande
    JMenu menuTools = new JMenu();
    JMenuItem menuToolsMMTeamAnalysis = null;
    JMenuItem menuToolsDatabaseLinkingTool = new JMenuItem();
    JMenuItem menuToolsCraftTool = new JMenuItem();
    public Action MMTeamAnalysisAction = null;
    JMenu windowMenu = new JMenu();
    CGButton NewWindowButton = new CGButton();
    CGButton QuitButton = new CGButton();
    JLabel CopyrightNotice = new JLabel();
    JLabel VersionInfo = new JLabel();
    JLabel GraphFolderLabel = new JLabel();
    JTextField GraphFolderField = new JTextField();
    JLabel EmailAddressLabel = new JLabel();
    JButton WebAddressButton = new JButton();
    JLabel CharGerLogoLabel = new JLabel();
    JLabel HubLabel = new JLabel();
    JLabel CGLabel = new JLabel();
    JLabel totalMemory = new JLabel();
    JLabel usedMemory = new JLabel();
    JLabel freeMemory = new JLabel();

    public HubFrame() {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        WindowManager.manageWindow( this, KeyStroke.getKeyStroke( KeyEvent.VK_0, Global.AcceleratorKey ) );
        initComponents();
        setVisible( true );
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void initComponents() //throws Exception
    {
        //  SET UP MENU ITEMS
        mainMenuBar.add( menuFile );
        mainMenuBar.add( menuTools );
        mainMenuBar.add( windowMenu );

        menuFile.setText( Global.strs( "FileMenuLabel" ) );
        menuFile.add( menuFileNewGraph );
        menuFile.add( menuFileOpen );
//        if ( !Global.OfficialRelease ) {
            menuFile.add( menuFileOpenCGIF );
//        }
//        menuFile.add( menuConvertCGXML ); // added by Esha Deshpande

        menuFile.add( menuFilePreferences );

        menuFile.add( menuToolsQuit );

        menuFileNewGraph.setText( Global.strs( "NewWindowLabel" ) );
        menuFileNewGraph.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, Global.AcceleratorKey ) );
        menuFileNewGraph.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                menuFileNewGraphActionPerformed( e );
            }
        } );

        menuFileOpen.setText( "Open..." );
        menuFileOpen.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, Global.AcceleratorKey ) );
        menuFileOpen.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                menuFileOpenActionPerformed( e );
            }
        } );


        menuFileOpenCGIF.setText( "Open CGIF..." );
        menuFileOpenCGIF.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                menuFileOpenCGIFActionPerformed( e );
            }
        } );

        menuFilePreferences.setText( Global.strs( "PreferencesLabel" ) );
        menuFilePreferences.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_COMMA, Global.AcceleratorKey ) );
        menuFilePreferences.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                menuFilePreferencesActionPerformed( e );
            }
        } );

        menuToolsQuit.setText( Global.strs( "QuitLabel" ) );
        menuToolsQuit.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Q, Global.AcceleratorKey ) );
        menuToolsQuit.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                menuToolsQuitActionPerformed( e );
            }
        } );

        menuTools.setText( "Tools" );
        menuTools.setToolTipText( "Hold down Shift and CNTL to enable dimmed items." );



        menuTools.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                if ( e.isShiftDown() && e.isControlDown() ) {
                    menuToolsCraftTool.setEnabled( true );
                    Global.craftEnabled = true;
                    Global.consoleMsg( "CRAFT Subsystem enabled: " + Global.craftEnabled );
                    if ( Global.pf != null ) Global.pf.enableCraftPanel( true );
                    menuToolsMMTeamAnalysis.setEnabled( true );
                    Global.mmatEnabled = true;
                    Global.consoleMsg( "MMAT Subsystem enabled: " + Global.mmatEnabled );
                }
            }
        } );

        // This needs to be put in the "Tools" of the HubFrame, if possible
        MMTeamAnalysisAction = new AbstractAction() {
            public Object getValue( String s ) {
                if ( s.equals( Action.NAME ) ) {
                    return Global.strs( "MMATAnalysisLabel" );
                }
                return super.getValue( s );
            }

            public void actionPerformed( ActionEvent e ) {
                // TODO: Eliminate this as a full-fledged analysis when it's just being initialized
                performActionMMTeamAnalysis();
            }
        };
//        if ( Global.mmatEnabled ) {
        menuToolsMMTeamAnalysis = new JMenuItem( MMTeamAnalysisAction );
        menuToolsMMTeamAnalysis.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_T, Global.AcceleratorKey ) );
        menuTools.add( menuToolsMMTeamAnalysis );
//        }
        if ( Global.mmatEnabled ) {
            menuToolsMMTeamAnalysis.setEnabled( true );
        } else {
            menuToolsMMTeamAnalysis.setEnabled( false );
        }
        menuToolsMMTeamAnalysis.setToolTipText( "Hold down Shift and CNTL to enable dimmed items." );



        menuTools.add( menuToolsDatabaseLinkingTool );


        menuToolsDatabaseLinkingTool.setText( "Database Linking Tool" );
        menuToolsDatabaseLinkingTool.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                menuToolsDatabaseLinkingToolActionPerformed( e );
            }
        } );

//        if ( Global.craftEnabled ) {
        menuTools.add( menuToolsCraftTool );
        menuToolsCraftTool.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_R, Global.AcceleratorKey ) );
        menuToolsCraftTool.setText( "Requirements Acquisition" );
        menuToolsCraftTool.setToolTipText( "Hold down Shift and CNTL to enable dimmed items." );


        menuToolsCraftTool.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                menuToolsCraftToolActionPerformed();
            }
        } );
//        }
        if ( Global.craftEnabled ) {
            menuToolsCraftTool.setEnabled( true );
        } else {
            menuToolsCraftTool.setEnabled( false );
        }


        windowMenu.setText( "Window" );
        //windowMenu.addActionListener( (ActionListener) this );	

        //  SET UP GRAPHICS
        // icon image to be set later
        CharGerLogoLabel.setForeground( new java.awt.Color( 255, 255, 255 ) );
        CharGerLogoLabel.setLocation( new Point( 2, 45 ) );
        CharGerLogoLabel.setVisible( true );
        CharGerLogoLabel.setSize( new java.awt.Dimension( 250, 50 ) );
        //Image i = logoIcon.getImage();
        //CharGerLogoLabel.setIcon( new ImageIcon ( i.getScaledInstance( 
        //	CharGerLogoLabel.getSize().width -4, -1, Image.SCALE_AREA_AVERAGING ) ) );
        CharGerLogoLabel.setText( "CharGer" );
        CharGerLogoLabel.setFont( new Font( "Arial", Font.BOLD + Font.ITALIC, 48 ) );

        //  SET UP LABELS

        CGLabel.setText( "Conceptual Graph Tools" );
        CGLabel.setForeground( new java.awt.Color( 255, 255, 255 ) );
        CGLabel.setLocation( new Point( 8, 85 ) );
        CGLabel.setVisible( true );
        CGLabel.setFont( new Font( "Arial", Font.BOLD + Font.ITALIC, 14 ) );
        CGLabel.setSize( new java.awt.Dimension( 200, 20 ) );


        CopyrightNotice.setText( Global.copyrightNotice );
        CopyrightNotice.setForeground( new java.awt.Color( 255, 255, 255 ) );
        CopyrightNotice.setLocation( new Point( 10, 280 ) );
        CopyrightNotice.setVisible( true );
        CopyrightNotice.setFont( new Font( "Arial", Font.PLAIN, 10 ) );
        CopyrightNotice.setSize( new java.awt.Dimension( 255, 20 ) );

        VersionInfo.setText( Global.EditorNameString + "   v" + Global.CharGerVersion + " " + Global.CharGerDate );
        VersionInfo.setForeground( new java.awt.Color( 255, 255, 255 ) );
        VersionInfo.setLocation( new Point( 10, 220 ) );
        VersionInfo.setVisible( true );
        VersionInfo.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
        VersionInfo.setSize( new java.awt.Dimension( 255, 20 ) );

        GraphFolderLabel.setText( "Graph folder:" );
        GraphFolderLabel.setForeground( new java.awt.Color( 255, 255, 255 ) );
        GraphFolderLabel.setLocation( new Point( 10, 6 ) );
        GraphFolderLabel.setHorizontalAlignment( JLabel.TRAILING );
        GraphFolderLabel.setVisible( true );
        GraphFolderLabel.setFont( new Font( "Arial", Font.BOLD, 11 ) );
        GraphFolderLabel.setSize( new java.awt.Dimension( 85, 20 ) );

        EmailAddressLabel.setText( "delugach@uah.edu" );
        EmailAddressLabel.setForeground( new java.awt.Color( 255, 255, 255 ) );
        EmailAddressLabel.setLocation( new Point( 10, 260 ) );
        EmailAddressLabel.setVisible( true );
        EmailAddressLabel.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
        EmailAddressLabel.setSize( new java.awt.Dimension( 255, 20 ) );

        WebAddressButton.setText( "http://sourceforge.net/projects/charger/" );
        WebAddressButton.setHorizontalAlignment( JLabel.LEFT );
        WebAddressButton.setBackground( new java.awt.Color( 255, 255, 255 ) );
        WebAddressButton.setForeground( Global.chargerBlueColor );
        WebAddressButton.setLocation( new Point( 10, 300 ) );
        WebAddressButton.setVisible( true );
        WebAddressButton.setFont( new Font( "Arial", Font.PLAIN, 12 ) );
        WebAddressButton.setSize( new java.awt.Dimension( 250, 20 ) );
        WebAddressButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                try {
                    Desktop.getDesktop().browse( new java.net.URI( "http://sourceforge.net/projects/charger/" ) );
                } catch ( Exception ee ) {
                }
                ;
            }
        } );


        totalMemory.setText( "label6" );
        totalMemory.setForeground( new java.awt.Color( 255, 255, 255 ) );
        totalMemory.setLocation( new Point( 10, 140 ) );
        totalMemory.setVisible( true );
        totalMemory.setFont( new Font( "Arial", Font.BOLD, 12 ) );
        totalMemory.setSize( new java.awt.Dimension( 150, 25 ) );

        usedMemory.setText( "label6" );
        usedMemory.setForeground( new java.awt.Color( 255, 255, 255 ) );
        usedMemory.setLocation( new Point( 10, 160 ) );
        usedMemory.setVisible( true );
        usedMemory.setFont( new Font( "Arial", Font.BOLD, 12 ) );
        usedMemory.setSize( new java.awt.Dimension( 150, 25 ) );

        freeMemory.setText( "label6" );
        freeMemory.setForeground( new java.awt.Color( 255, 255, 255 ) );
        freeMemory.setLocation( new Point( 10, 180 ) );
        freeMemory.setVisible( true );
        freeMemory.setFont( new Font( "Arial", Font.BOLD, 12 ) );
        freeMemory.setSize( new java.awt.Dimension( 150, 25 ) );

        // to be modified during execution
        AvailLabel.setText( "Graphs available" );
        AvailLabel.setForeground( new java.awt.Color( 255, 255, 255 ) );
        AvailLabel.setLocation( new Point( 380, 290 ) );
        AvailLabel.setHorizontalAlignment( JLabel.CENTER );
        AvailLabel.setVisible( true );
        AvailLabel.setFont( new Font( "Arial", Font.BOLD, 12 ) );
        AvailLabel.setSize( new java.awt.Dimension( 230, 30 ) );

        //  Initialize the buttons
        BrowseButton.setLocation( new Point( 670, 6 ) );
        BrowseButton.setText( Global.strs( "BrowseLabel" ) );
        BrowseButton.setToolTipText( "Choose and set the default graph directory" );
        BrowseButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                BrowseButtonActionPerformed( e );
            }
        } );

        NewWindowButton.setLocation( new Point( 670, 55 ) );
        NewWindowButton.setText( Global.strs( "NewWindowLabel" ) );
        NewWindowButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                NewWindowButtonActionPerformed( e );
            }
        } );

        OpenButton.setLocation( new Point( 670, 90 ) );
        OpenButton.setText( "Open Selected" );
        OpenButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OpenButtonActionPerformed( e );
            }
        } );

        OpenAllButton.setLocation( new Point( 670, 125 ) );
        OpenAllButton.setText( Global.strs( "OpenAllLabel" ) );
        OpenAllButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                OpenAllButtonActionPerformed( e );
            }
        } );

        SaveAllAsCGIFButton.setLocation( new Point( 670, 200 ) );
        SaveAllAsCGIFButton.setText( "Save All CGIF" );
        SaveAllAsCGIFButton.setToolTipText( "Disabled until CGIF generation is tested.");
        SaveAllAsCGIFButton.setVisible( false );
        SaveAllAsCGIFButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                SaveAllAsCGIFButtonActionPerformed();
            }
        } );

        CloseAllButton.setLocation( new Point( 670, 235 ) );
        CloseAllButton.setText( "Close All" );
        CloseAllButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                CloseAllButtonActionPerformed( e );
            }
        } );

        //QuitButton.setForeground(new java.awt.Color(255, 255, 255));
        QuitButton.setLocation( new Point( 670, 270 ) );
        QuitButton.setText( Global.strs( "QuitLabel" ) );
        QuitButton.addActionListener( new java.awt.event.ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                QuitButtonActionPerformed( e );
            }
        } );

        //  SET UP ACTUAL USEFUL CONTENT DISPLAYS
        //GraphDisplayList.setMultipleMode( false );		// too many problems with multiple selections
        GraphFolderField.setText( "Graphs" );		// will be over-ridden by preferences

        GraphFolderField.setLocation( new Point( 100, 6 ) );
        GraphFolderField.setForeground( new java.awt.Color( 0, 0, 0 ) );
        GraphFolderField.setVisible( true );
        GraphFolderField.setBackground( new java.awt.Color( 255, 255, 255 ) );
        GraphFolderField.setFont( new Font( "SansSerif", Font.PLAIN, 12 ) );
        GraphFolderField.setSize( new java.awt.Dimension( 565, 23 ) );
        GraphFolderField.setEditable( false );
        GraphFolderField.setText( Global.GraphFolderFile.getAbsolutePath() + File.separator );

        GraphDisplayList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION ); // default, but say it anyway

        GraphDisplayList.setForeground( new java.awt.Color( 0, 0, 0 ) );
        GraphDisplayList.setLocation( new Point( 480, 30 ) );
        GraphDisplayList.setVisible( true );
        GraphDisplayList.setBackground( new java.awt.Color( 255, 255, 255 ) );
        GraphDisplayList.setFont( new Font( "SansSerif", Font.PLAIN, 12 ) );
        //GraphDisplayList.setSize(new java.awt.Dimension(180, 190));
        GraphDisplayList.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                GraphDisplayListMouseClicked( e );
            }
        } );
        GraphDisplayList.addListSelectionListener( new ListSelectionListener() {
            public void valueChanged( ListSelectionEvent e ) {
                GraphDisplayListItemStateChanged( e );
            }
        } );


        GraphDisplayPane = new JScrollPane( GraphDisplayList );
        GraphDisplayPane.setLocation( new Point( 280, 35 ) );
        GraphDisplayPane.setSize( new java.awt.Dimension( 380, 260 ) );
        GraphDisplayPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        GraphDisplayPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );

        GraphDisplayPane.setBorder( Global.BeveledBorder );
        GraphDisplayPane.setOpaque( true );


        //  SET UP THE FRAME ITSELF
        setLocation( new Point( 0, 0 ) );
        setTitle( "CharGer - Conceptual Graph Tools" );
        getContentPane().setBackground( Global.chargerBlueColor );
        setFont( new Font( "Dialog", 1, 10 ) );
        getContentPane().setLayout( null );
        setJMenuBar( mainMenuBar );
        setSize( new java.awt.Dimension( 820, 400 ) );
        //setPreferredSize(new java.awt.Dimension(642, 400));
        getContentPane().add( AvailLabel );
        getContentPane().add( GraphDisplayPane );
        getContentPane().add( OpenAllButton );
        getContentPane().add( CloseAllButton );
        getContentPane().add( SaveAllAsCGIFButton );
        getContentPane().add( NewWindowButton );
        getContentPane().add( QuitButton );
        getContentPane().add( CGLabel );
        getContentPane().add( CopyrightNotice );
        getContentPane().add( VersionInfo );
        getContentPane().add( GraphFolderLabel );
        getContentPane().add( GraphFolderField );
        getContentPane().add( EmailAddressLabel );
        getContentPane().add( WebAddressButton );
        getContentPane().add( CharGerLogoLabel );
        getContentPane().add( totalMemory );
        getContentPane().add( usedMemory );
        getContentPane().add( freeMemory );
        getContentPane().add( OpenButton );
        getContentPane().add( BrowseButton );

        addFocusListener( new java.awt.event.FocusAdapter() {
            public void focusGained( java.awt.event.FocusEvent e ) {
                // Global.info( "hub frame focus gained" );
                thisFocusGained( e );
            }
        } );
        addWindowListener( new java.awt.event.WindowAdapter() {
            public void windowClosing( java.awt.event.WindowEvent e ) {
                thisWindowClosing( e );
            }

            public void windowActivated( java.awt.event.WindowEvent e ) {
                //Global.info( "hub frame activated" );
                thisWindowActivated( e );
            }
        } );


        refresh();
        //menuToolsQuit.setMnemonic( KeyEvent.VK_Q );
        setFont( new Font( "SansSerif", Font.BOLD, 10 ) );
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );

    }
    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if ( mShown ) {
            return;
            // move components to account for insets
        }
        Insets insets = getInsets();
        Component[] components = getComponents();
        for ( int i = 0; i < components.length; i++ ) {
            Point location = components[i].getLocation();
            location.move( location.x, location.y + insets.top );
            components[i].setLocation( location );
        }

        mShown = true;
    }

    // Close the window when the close box is clicked
    public void thisWindowClosing( java.awt.event.WindowEvent e ) {
        if ( Global.closeOutAll() ) {
            WindowManager.forgetWindow( this );
            setVisible( false );
            dispose();
            System.exit( 0 );
        }
    }

    /**
     * re-reads the directory to re-display the list of graph files in the user
     * directory
     */
    // needs to be optimized; put the text in a string and then set the list all at once.
    public void refreshFileList() {

        if ( !Global.GraphFolder.equals( GraphFolderField.getText() ) ) // note upper/lower case only matters for LINUX!
        {
            GraphFolderField.setText( Global.GraphFolder );
            GraphFolderLastModified = 0;	// force a refresh if user changed the graph directory

        }
        // get folder name from hub frame
        File ff = new File( GraphFolderField.getText() );
        // look for the graph directory here --- 
        if ( ff != null ) {
            if ( GraphFolderLastModified != ff.lastModified() ) {
                GraphDisplayList.removeAll();

                String gg[] = ff.list( new FilenameFilter() {
                    public boolean accept( File f, String name ) {
                        if ( !name.startsWith( "." ) ) {
                            return Global.acceptCGXFileName( name );
                        } else {
                            return false;
                        }
                    }
                } );
                //Global.info( "found " + gg.length + " files." );	
                // Sort the list, not case sensitive
                Arrays.sort( gg, Global.ignoreCase );
                if ( gg != null ) {
                    GraphDisplayList.setEnabled( false );
                    GraphDisplayList.setVisible( false );

                    GraphDisplayList.setListData( gg );

                    GraphDisplayList.setEnabled( true );
                    GraphDisplayList.setVisible( true );
                }
                GraphFolderLastModified = ff.lastModified();
            }
        }
        AvailLabel.setText( GraphDisplayList.getModel().getSize() + " graphs available" );
    }

    public void thisFocusGained( java.awt.event.FocusEvent e ) {
        //refresh();
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    /**
     * Doesn't do anything while user is making selections
     *
     * @see #OpenButtonActionPerformed
     * @see #OpenAllButtonActionPerformed
     */
    public void GraphDisplayListItemStateChanged( ListSelectionEvent e ) {
    }

    /**
     * Get all the selected file names, and convert them from objects to
     * strings, then open them
     *
     * @see HubFrame#openNamedFiles
     */
    public void OpenButtonActionPerformed( ActionEvent e ) {
        Object[] fileNameObjects = GraphDisplayList.getSelectedValuesList().toArray();
        String[] fileNames = new String[ fileNameObjects.length ];
        for ( int entrynum = 0; entrynum < fileNames.length; entrynum++ ) {
            fileNames[entrynum] = (String)fileNameObjects[entrynum];
        }
        Global.info( "Preparing to open " + fileNames.length + " files." );
        openNamedFiles( fileNames );
    }

    /**
     * Opens a list of files, each in its own editor window.
     *
     * @param files list of (relative) file paths
     */
    public void openNamedFiles( String[] files ) {
        if ( files.length == 0 ) {
            return;
        }

        if ( files.length > 50 ) {
            int result = JOptionPane.showConfirmDialog( this,
                    "There are " + files.length + " graphs to open.\n" + ""
                    + Global.EditorNameString + " may not have enough resources.\nDo you still want to try?" );
            if ( result != JOptionPane.YES_OPTION ) {
                return;
            }
        }

        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        int numFilesOpened = 0;
        for ( int n = 0; n < files.length; n++ ) {
            int minMB = 4;
            //pbar.setValue( n );
            //jd.toFront();
            if ( Runtime.getRuntime().freeMemory() / ( 1024d * 1024d ) > minMB ) {
//                Global.info("[openNamedFiles] file " + (int) (n + 1) + " of " + files.length + " is " + files[n]);
                String newname = Global.openGraphInNewFrame( files[n] );
                numFilesOpened++;
            } else {
                Global.warning( "Memory less than " + minMB + " MB. Can't open graph." );
                break;
            }
        }
        // need to choose an edit frame to be first
        EditFrame pickone = Global.getNewestEditFrame();
        if ( pickone != null ) {
            Global.setCurrentEditFrame( pickone );
            pickone.requestFocus();
            pickone.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
        }

        Global.info( "finished loading " + numFilesOpened + " files." );
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void NewWindowButtonActionPerformed( ActionEvent e ) {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        EditFrame ef = new EditFrame();
        if ( Global.enableEditFrameThreads ) {
            new Thread( Global.EditFrameThreadGroup, ef ).start();
            //Hub.setCurrentEditFrame( ef );
        }
        ef.toFront();	// Global.info( "to front in HubFrame new window button" );

        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void QuitButtonActionPerformed( ActionEvent e ) {
        // Global.info( "ready for hub frame's quit." );
        if ( Global.closeOutAll() ) {
            System.exit( 0 );
        }
    }

    /**
     * Updates all the menus and displays in the master frame, but not
     * responsible for repaint.
     */
    public void refresh() {
        Global.refreshWindowMenuList( windowMenu, this );
        refreshFileList();
        // prepare a memory display, but do garbage collection first
        System.gc();
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
        NumberFormat nformat = NumberFormat.getNumberInstance();
        nformat.setMaximumFractionDigits( 2 );
        nformat.setMinimumFractionDigits( 2 );
        totalMemory.setText( nformat.format( (double)Runtime.getRuntime().totalMemory() / ( 1024d * 1024d ) ) + " M total" );
        usedMemory.setText( nformat.format( (double)( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) / ( 1024d * 1024d ) ) + " M used" );
        freeMemory.setText( nformat.format( Runtime.getRuntime().freeMemory() / ( 1024d * 1024d ) ) + " M free" );
        GraphFolderField.setText( Global.GraphFolderFile.getAbsolutePath() + File.separator );
        /* Code using java.lang.ref objects is for determining why some garbage is not being made finalizable */

        repaint();
    }

    /**
     * Instantiates an MProject, which then spawns all the other windows and
     * classes needed for the MMAT.
     *
     *
     */
    public void performActionMMTeamAnalysis() {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );

        MProject mmatProject = null;
        if ( mmatProject == null ) {
            mmatProject = new MProject( Global.GraphFolderFile );
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
        Global.setCurrentEditFrame( null );
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
        return "~ " + Global.strs( "BackToHubCmdLabel" );
    }

    public String getFilename() {
        return null;
    }

    public void thisWindowActivated( java.awt.event.WindowEvent e ) {
        refresh();
        //if ( Hub.CurrentEditFrame != null )
        //Hub.removeCurrentEditFrame( Hub.CurrentEditFrame );
        //Hub.setCurrentEditFrame( null );	// commented 12-11-02
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void GraphDisplayListMouseClicked( MouseEvent e ) {
        if ( e.getClickCount() == 2 ) {
            OpenButtonActionPerformed(
                    new ActionEvent( e.getSource(), ActionEvent.ACTION_PERFORMED, null ) );
        }
    }

    public void menuToolsDatabaseLinkingToolActionPerformed( ActionEvent e ) {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        //Global.info( "at menuToolsDatabaseLinkingToolActionPerformed.. ");
        if ( DataBaseLinkToolWindow == null ) {
            DataBaseLinkToolWindow =
                    new DatabaseFrame( Global.DatabaseFolder + File.separator + "DBElements.txt" );
        }
        DataBaseLinkToolWindow.toFront();
        DataBaseLinkToolWindow.setVisible( true );
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void menuToolsCraftToolActionPerformed() {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        //Global.info( "at menuToolsCraftToolActionPerformed.. ");
        if ( Global.craftModule == null ) {
            Global.craftModule = new craft.Craft();
        }
        Craft.craftWindow.refresh();
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void menuToolsQuitActionPerformed( ActionEvent e ) {
        QuitButtonActionPerformed( e );
    }

    public void menuFilePreferencesActionPerformed( ActionEvent e ) {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        Global.managePreferencesFrame();
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    /**
     * Loads a graph into a new edit frame. If the user cancels the load, no
     * window is created.
     *
     * @see EditManager#actionPerformed
     * @see IOManager
     */
    public void menuFileOpenActionPerformed( ActionEvent e ) {
        String filename = null; // = Hub.makeUpFileName(  FileNameField.getText(), stat ); 
        //setCursor( new Cursor( Cursor.WAIT_CURSOR ) );

        filename = Global.openGraphInNewFrame( null );
        //setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void openCGIFActionPerformed( ActionEvent e ) {
        File absFile = null;
        CGIFParser parser = new CGIFParser( (Reader)null );


            IOManager iomgr = new IOManager( this );
            //iomgr.FileToGraph( filename, outerFrame.TheGraph, outerFrame);
                    // TODO: A long awaited CGIF parser to be invoked here
            File cgifFile = Util.queryForInputFile( "Import CGIF", Global.GraphFolderFile, Global.CGIFFileFilter);
            if ( cgifFile == null ) {
                return;
            }
            StringBuffer stringBuilder = new StringBuffer( "" );
            try {
                BufferedReader reader = new BufferedReader( new FileReader( cgifFile ) );
                String line = null;
                String ls = System.getProperty( "line.separator" );
                while ( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                    stringBuilder.append( ls );
                }
            } catch ( FileNotFoundException ex ) {
                JOptionPane.showMessageDialog( this, ex);
                return;
            } catch ( IOException ex ) {
                JOptionPane.showMessageDialog( this, ex);
                return;
            }

//            cgifFile = iomgr.CGIFFileToGraph( null, attempt,/* this ,*/ new Point2D.Double( 0, 0 ) );
            String contents = stringBuilder.toString();
            Graph attempt = null;
            try {
                 attempt = parser.parseCGIFString( contents );
            } catch ( ParseException ex ) {
                JOptionPane.showMessageDialog( this, "Error in file " + cgifFile.getName() + "\n" + ex.getMessage(),
                        "CGIF Formation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            attempt.addHistory( new ObjectHistoryEvent( cgifFile));
            EditFrame ef = new EditFrame( cgifFile, attempt, true );

            if ( Global.enableEditFrameThreads ) {
                new Thread( Global.EditFrameThreadGroup, ef ).start();
            }
    }

    public void menuFileNewGraphActionPerformed( ActionEvent e ) {
        NewWindowButtonActionPerformed( e );
    }

    public void menuFileOpenCGIFActionPerformed( ActionEvent e ) {
        setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
        openCGIFActionPerformed( e );
        setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void OpenAllButtonActionPerformed( ActionEvent e ) {
        // JList or ListModel don't seem to have method to get everything in the list
        String[] allfiles = new String[ GraphDisplayList.getModel().getSize() ];
//        String[] allfiles = (String[])((DefaultListModel)GraphDisplayList.getModel()).toArray();
        for ( int fnum = 0; fnum < allfiles.length; fnum++ ) {
//        for ( String filename : GraphDisplayList.getModel() ) {
            allfiles[ fnum] = (String)GraphDisplayList.getModel().getElementAt( fnum );
        }

        if ( allfiles.length > 0 ) {
            setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
        }
        openNamedFiles( allfiles );
    }

    /**
     * Convenience method for calling FileToGraph repeatedly for all files in
     * the displaylist.
     */
    public void SaveAllAsCGIFButtonActionPerformed() {
        int successful = 0;
        int failure = 0;
        //String[] allfiles = new String[ GraphDisplayList.getModel().getSize() ];

        for ( int entrynum = 0; entrynum < GraphDisplayList.getModel().getSize(); entrynum++ ) {
            File fileToConvert = new File( Global.GraphFolderFile,
                    (String)GraphDisplayList.getModel().getElementAt( entrynum ) );
            Graph g = new Graph( null );
            try {
                File f = IOManager.FileToGraph( fileToConvert, g, null );
                Global.info( g.getBriefSummary() );
                String CGIFpath = Util.stripFileExtension( fileToConvert.getAbsolutePath() );
                f = IOManager.GraphToFile( g, FileFormat.CGIF2007, CGIFpath, null );
                successful++;
            } catch ( CGFileException cgfe ) {
                Global.warning( "CG File Exception: can't convert " + fileToConvert + ": " + cgfe.getMessage() );
                failure++;
            } catch ( CGStorageError cgse ) {
                Global.warning( "CG Storage Error: can't convert " + fileToConvert + ": " + cgse.getMessage() );
                failure++;
            }

        }
        String msg = null;
        if ( successful > 0 ) {
            msg = "Successfully saved " + successful + " file(s).";
        } else {
            msg = "No files saved.";
        }
        if ( failure > 0 ) {
            msg = msg + "\nFailed on " + failure + " file(s); see console for details.";
        }
        JOptionPane.showMessageDialog( this, msg, "Saving Files as CGIF", JOptionPane.INFORMATION_MESSAGE );
    }

    public void CloseAllButtonActionPerformed( ActionEvent e ) {
        Global.closeOutAll();
        refresh();
    }

    
    public void BrowseButtonActionPerformed( ActionEvent e ) {
        Global.queryForGraphFolder( this );
        GraphFolderField.setText( Global.GraphFolder );
        refreshFileList();
        refresh();
    }
}
