/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mm;

import charger.Global;
import charger.util.WindowManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

/**
 * The main user interface frame for MM Analysis. Created in NetBeans and should
 * be edited (as a "Design") there.
 *
 * @author Harry Delugach
 * @see MMAnalysisMgr
 * @since Charger 3.8.0
 */
public class MMAnalysisFrame extends javax.swing.JFrame implements charger.util.ManagedWindow {

    /**
     * For the JList operation, not to be confused with any MMAT model!
     */
    DefaultListModel projListModel = new DefaultListModel();
    /**
     * For the JList operation, not to be confused with any MMAT model!
     */
    DefaultListModel groupListModel = new DefaultListModel();
    /**
     * For the JList operation, not to be confused with any MMAT model!
     */
    DefaultListModel teamListModel = new DefaultListModel();
    /**
     * For the JList operation, not to be confused with any MMAT model!
     */
    DefaultListModel teamPhaseListBListModel = new DefaultListModel();
    /**
     * For the JList operation, not to be confused with any MMAT model!
     */
    DefaultListModel teamPhaseListMListModel = new DefaultListModel();
    /**
     * For the JList operation, not to be confused with any MMAT model!
     */
    DefaultListModel teamPhaseListEListModel = new DefaultListModel();
    MMAnalysisMgr analyzer = null;
    MMModelMgr modelMgr = null;
    
    private MProject project = null;
    // EnumMap used to streamline multiple phases all processed identicallyL
    /**
     * Associates each phase with its label in the display
     */
    public EnumMap<MPhase, JLabel> phaseLabel = new EnumMap<>(MPhase.class);
    /**
     * Associates each phase with the text field containing the results of its
     * analysis
     */
    public EnumMap<MPhase, JTextPane> phaseAnalysisResultsDisplay = new EnumMap<>(MPhase.class);
    public EnumMap<MPhase, JList> teamPhaseModelList = new EnumMap<>(MPhase.class);
    public EnumMap<MPhase, JEditorPane> synonymList = new EnumMap<>(MPhase.class);
    /**
     * Has an odd name because of confusion with MM models. "teamphase list" is
     * in mental models terms. ListModel is therefore referring to an instance
     * of DefaultListModel.
     */
    public EnumMap<MPhase, DefaultListModel> teamPhaseList_ListModel = new EnumMap<>(MPhase.class);

    /**
     * Creates new form MMAnalysisFrame
     */
    public MMAnalysisFrame( MProject project ) {
        this.project = project;
        setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        initComponents();
        graphFolderPathLabel.setText( getProject().MMSubmittedFolder.getAbsolutePath() );

        WindowManager.manageWindow( this, KeyStroke.getKeyStroke( KeyEvent.VK_T, Global.AcceleratorKey ) );
        setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                thisWindowClosing( e );
            }
        } );

        initializeHashMaps();
        
        analyzer = new MMAnalysisMgr( this );
        modelMgr = new MMModelMgr( this );


        this.getContentPane().setBackground( Global.chargerBlueColor );
        setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );

    }

    public MProject getProject() {
        return project;
    }

    public void setProject( MProject project ) {
        this.project = project;
    }

    public MMAnalysisMgr getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer( MMAnalysisMgr analyzer ) {
        this.analyzer = analyzer;
    }

    public MMModelMgr getModelMgr() {
        return modelMgr;
    }

    public void setModelMgr( MMModelMgr modelMgr ) {
        this.modelMgr = modelMgr;
    }
    
    public void tearDown() {
        getAnalyzer().tearDown();
        modelMgr.tearDown();
    }
    
    
    
    

    protected void finalize() throws Throwable {
        try {
            Global.info( "-- MMAT Analysis Frame finalizer invoked." );
            super.finalize();
        } catch ( Throwable t ) {
            throw t;
        } finally {
            super.finalize();
        }
    }

    /**
     * Method to be invoked when this window is chosen to be the current window.
     *
     * @see WindowManager#bringToFront
     */
    public void bringToFront() {
        WindowManager.bringToFront( this );
        toFront();
        requestFocus();
    }

    /**
     * Tells a window manager what label to put on the menu to select this
     * window
     *
     * @return Its item label so the window manager knows what to display
     *
     */
    public String getMenuItemLabel() {
        return Global.strs( "MMATAnalysisLabel" );
    }

    /**
     * If there's a file associated with the window, return its name; null
     * otherwise.
     *
     * @return null for this frame, since it generally deals with multiple files
     * at a time.
     */
    public String getFilename() {
        return null;
    }

    /* End of methods required by the ManagedWindow interface */
    public void thisWindowClosing( WindowEvent e ) {
        WindowManager.forgetWindow( this );
    }

    public void initializeHashMaps() {
        phaseLabel.put( MPhase.Beginning, beginningListLabel );
        phaseLabel.put( MPhase.Middle, middleListLabel );
        phaseLabel.put( MPhase.End, endListLabel );

        phaseAnalysisResultsDisplay.put( MPhase.Beginning, phaseAnalysisBeginning );
        phaseAnalysisResultsDisplay.put( MPhase.Middle, phaseAnalysisMiddle );
        phaseAnalysisResultsDisplay.put( MPhase.End, phaseAnalysisEnd );

        teamPhaseList_ListModel.put( MPhase.Beginning, teamPhaseListBListModel );
        teamPhaseList_ListModel.put( MPhase.Middle, teamPhaseListMListModel );
        teamPhaseList_ListModel.put( MPhase.End, teamPhaseListEListModel );

        teamPhaseModelList.put( MPhase.Beginning, teamPhaseModelListB );
        teamPhaseModelList.put( MPhase.Middle, teamPhaseModelListM );
        teamPhaseModelList.put( MPhase.End, teamPhaseModelListE );

//        synonymList.put( MPhase.Beginning, synonymListB);
//        synonymList.put( MPhase.Middle, teamSynonymList);
//        synonymList.put( MPhase.End, synonymListE);


    }

    /**
     * Adjust the synonym display to reflect whether group and/or team synonyms
     * are enabled or not.
     *
     */
    public void adjustSynonymAppearance() {
        if ( ( getProject().currentTeam != null ) && useSynonyms.isSelected() ) {
            teamSynonymList.setFont( teamSynonymList.getFont().deriveFont( Font.PLAIN ) );
            teamSynonymList.setBackground( Color.white );
        } else {
            teamSynonymList.setFont( teamSynonymList.getFont().deriveFont( Font.ITALIC ) );
            teamSynonymList.setBackground( new Color( 180, 180, 180 ) );
        }

        if ( getProject().currentGroup != null && useGroupSynonyms.isEnabled() && useGroupSynonyms.isSelected() ) {
            groupSynonymList.setFont( groupSynonymList.getFont().deriveFont( Font.PLAIN ) );
            groupSynonymList.setBackground( Color.white );
        } else {
            groupSynonymList.setFont( groupSynonymList.getFont().deriveFont( Font.ITALIC ) );
            groupSynonymList.setBackground( new Color( 180, 180, 180 ) );
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        frameName = new JLabel();
        projNameLabel = new JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        projList = new JList( projListModel );
        jScrollPane2 = new javax.swing.JScrollPane();
        groupList = new JList( groupListModel );
        jScrollPane3 = new javax.swing.JScrollPane();
        teamList = new JList( teamListModel );
        jScrollPane5 = new javax.swing.JScrollPane();
        teamPhaseModelListB = new JList( teamPhaseListBListModel );
        beginningListLabel = new JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        teamPhaseModelListM = new JList( teamPhaseListMListModel );
        jScrollPane7 = new javax.swing.JScrollPane();
        teamPhaseModelListE = new JList( teamPhaseListEListModel );
        middleListLabel = new JLabel();
        endListLabel = new JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        phaseAnalysisBeginning = new JTextPane();
        jScrollPane9 = new javax.swing.JScrollPane();
        phaseAnalysisMiddle = new JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        phaseAnalysisEnd = new JTextPane();
        graphFolderPathLabel = new JLabel();
        optionsPanel = new javax.swing.JPanel();
        observerIsSpecial = new javax.swing.JCheckBox();
        useSynonyms = new javax.swing.JCheckBox();
        includeTrash = new javax.swing.JCheckBox();
        useGroupSynonyms = new javax.swing.JCheckBox();
        includeAdmin = new javax.swing.JCheckBox();
        graphMetricsEnabled = new javax.swing.JCheckBox();
        pairwiseEnabled = new javax.swing.JCheckBox();
        observerPairEnabled = new javax.swing.JCheckBox();
        combinedIsSpecial = new javax.swing.JCheckBox();
        combinedVsObserver = new javax.swing.JCheckBox();
        ignoreRelationSense = new javax.swing.JCheckBox();
        jScrollPane16 = new javax.swing.JScrollPane();
        teamSynonymList = new JEditorPane();
        displayOptions = new javax.swing.JPanel();
        enableRawHTML = new javax.swing.JCheckBox();
        showCorefsTotal = new javax.swing.JCheckBox();
        showCorefsPerLevel = new javax.swing.JCheckBox();
        jLabel1 = new JLabel();
        showSynonyms = new javax.swing.JCheckBox();
        openModelsE = new javax.swing.JButton();
        openModelsB = new javax.swing.JButton();
        openModelsM = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator3 = new javax.swing.JSeparator();
        groupName = new JLabel();
        teamComplete = new JLabel();
        teamName = new JLabel();
        DateB = new JLabel();
        DateM = new JLabel();
        DateE = new JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jScrollPane17 = new javax.swing.JScrollPane();
        groupSynonymList = new JEditorPane();
        combineModelsB = new javax.swing.JButton();
        combineModelsE = new javax.swing.JButton();
        combineModelsM = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JSeparator();
        jSeparator10 = new javax.swing.JSeparator();
        jSeparator11 = new javax.swing.JSeparator();
        jMenuBar2 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuCheckUnSubmitted = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        menuViewTeamReport = new javax.swing.JMenuItem();
        menuItemSaveTeamReport = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        menuItemSaveAllTeams = new javax.swing.JMenuItem();
        menuMakeGroupAsTeamReport = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        menuItemSaveSpreadsheet = new javax.swing.JMenuItem();
        menuMakeTabularSummary = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        menuClose = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        menuEditGroupSynonyms = new javax.swing.JMenuItem();
        menuEditTeamSynonyms = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JPopupMenu.Separator();
        menuItemRenameModel = new javax.swing.JMenuItem();
        menuItemRenameTeam = new javax.swing.JMenuItem();
        menuItemMoveTeam = new javax.swing.JMenuItem();
        windowMenu = new javax.swing.JMenu();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        jMenuItem3.setText("jMenuItem3");

        jMenuItem4.setText("jMenuItem4");

        jMenuItem5.setText("jMenuItem5");

        jMenuItem6.setText("jMenuItem6");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(Global.strs( "MMATAnalysisLabel") + " (" + MMConst.MMATversion + ")");
        setBackground(Global.chargerBlueColor);
        setFont(new Font("Arial", 0, 12)); // NOI18N
        setLocation(new java.awt.Point(50, 50));
        setMinimumSize(new java.awt.Dimension(1032, 800));
        setPreferredSize(new java.awt.Dimension(1010, 800));
        addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowActivated(WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        frameName.setBackground(Global.chargerBlueColor);
        frameName.setFont(new Font("Arial", 1, 18)); // NOI18N
        frameName.setForeground(new Color(255, 255, 255));
        frameName.setText(Global.strs( "MMATAnalysisLabel"));
        getContentPane().add(frameName, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, -1, -1));

        projNameLabel.setForeground(new Color(255, 255, 255));
        projNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        projNameLabel.setText("Project:");
        projNameLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        getContentPane().add(projNameLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(840, 10, 60, -1));

        projList.setBackground(Global.chargerBlueColor);
        projList.setFont(new Font("Arial", 1, 14)); // NOI18N
        projList.setForeground(new Color(255, 255, 255));
        jScrollPane1.setViewportView(projList);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 10, 99, 20));

        groupList.setBackground(new Color(230, 230, 255));
        groupList.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Group(s)", 0, 0, new Font("Lucida Grande", 2, 12))); // NOI18N
        groupList.setFont(new Font("Arial", 1, 14)); // NOI18N
        groupList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        groupList.setToolTipText("The list of groups to choose from");
        jScrollPane2.setViewportView(groupList);

        getContentPane().add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(230, 40, 110, 150));

        teamList.setBackground(new Color(230, 230, 255));
        teamList.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Team(s)", 0, 0, new Font("Lucida Grande", 2, 12))); // NOI18N
        teamList.setFont(new Font("Arial", 1, 13)); // NOI18N
        teamList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        teamList.setToolTipText("The list of teams in this group");
        jScrollPane3.setViewportView(teamList);

        getContentPane().add(jScrollPane3, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 40, 150, 150));

        teamPhaseModelListB.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Beginning Phase Models", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        teamPhaseModelListB.setFont(new Font("Arial", 1, 14)); // NOI18N
        teamPhaseModelListB.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        teamPhaseModelListB.setToolTipText("Double-click on model to view it in Charger");
        teamPhaseModelListB.setMinimumSize(new java.awt.Dimension(300, 100));
        teamPhaseModelListB.setSelectionBackground(new Color(51, 51, 51));
        jScrollPane5.setViewportView(teamPhaseModelListB);

        getContentPane().add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 340, 320, 150));

        beginningListLabel.setBackground(Global.chargerBlueColor);
        beginningListLabel.setFont(new Font("Arial", 1, 12)); // NOI18N
        beginningListLabel.setForeground(new Color(255, 255, 255));
        beginningListLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        beginningListLabel.setText("Beginning");
        beginningListLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        beginningListLabel.setMaximumSize(new java.awt.Dimension(62, 20));
        beginningListLabel.setMinimumSize(new java.awt.Dimension(62, 20));
        getContentPane().add(beginningListLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 310, 130, 24));

        teamPhaseModelListM.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Middle Phase Models", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        teamPhaseModelListM.setFont(new Font("Arial", 1, 14)); // NOI18N
        teamPhaseModelListM.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        teamPhaseModelListM.setToolTipText("Double-click on model to view it in Charger");
        teamPhaseModelListM.setSelectionBackground(new Color(102, 102, 102));
        jScrollPane6.setViewportView(teamPhaseModelListM);

        getContentPane().add(jScrollPane6, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 340, 320, 150));

        teamPhaseModelListE.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "End Phase Models", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        teamPhaseModelListE.setFont(new Font("Arial", 1, 14)); // NOI18N
        teamPhaseModelListE.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        teamPhaseModelListE.setToolTipText("Double-click on model to view it in Charger");
        teamPhaseModelListE.setSelectionBackground(new Color(102, 102, 102));
        jScrollPane7.setViewportView(teamPhaseModelListE);

        getContentPane().add(jScrollPane7, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 340, 320, 150));

        middleListLabel.setBackground(Global.chargerBlueColor);
        middleListLabel.setFont(new Font("Arial", 1, 12)); // NOI18N
        middleListLabel.setForeground(new Color(255, 255, 255));
        middleListLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        middleListLabel.setText("Middle");
        middleListLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        middleListLabel.setMaximumSize(new java.awt.Dimension(62, 20));
        middleListLabel.setMinimumSize(new java.awt.Dimension(62, 20));
        getContentPane().add(middleListLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 310, 140, 24));

        endListLabel.setBackground(Global.chargerBlueColor);
        endListLabel.setFont(new Font("Arial", 1, 12)); // NOI18N
        endListLabel.setForeground(new Color(255, 255, 255));
        endListLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        endListLabel.setText("End");
        endListLabel.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        endListLabel.setMaximumSize(new java.awt.Dimension(62, 20));
        endListLabel.setMinimumSize(new java.awt.Dimension(62, 20));
        getContentPane().add(endListLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 310, 140, 24));

        phaseAnalysisBeginning.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Results", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        phaseAnalysisBeginning.setFont(new Font("Arial", 0, 12)); // NOI18N
        jScrollPane8.setViewportView(phaseAnalysisBeginning);

        getContentPane().add(jScrollPane8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 500, 320, 210));

        phaseAnalysisMiddle.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Results", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        phaseAnalysisMiddle.setFont(new Font("Arial", 0, 12)); // NOI18N
        jScrollPane9.setViewportView(phaseAnalysisMiddle);

        getContentPane().add(jScrollPane9, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 500, 320, 210));

        phaseAnalysisEnd.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Results", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        phaseAnalysisEnd.setFont(new Font("Arial", 0, 12)); // NOI18N
        jScrollPane4.setViewportView(phaseAnalysisEnd);

        getContentPane().add(jScrollPane4, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 500, 320, 210));

        graphFolderPathLabel.setBackground(new Color(255, 255, 255));
        graphFolderPathLabel.setFont(new Font("Arial", 0, 14)); // NOI18N
        graphFolderPathLabel.setText("Model folder goes here");
        graphFolderPathLabel.setToolTipText("The folder where all the models are stored");
        graphFolderPathLabel.setOpaque(true);
        getContentPane().add(graphFolderPathLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 10, 600, 20));

        optionsPanel.setBackground(new Color(204, 204, 255));
        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Analysis Options", 0, 0, new Font("Arial", 3, 12))); // NOI18N
        optionsPanel.setToolTipText("Options that affect what is analyzed");
        optionsPanel.setAlignmentX(0.0F);

        observerIsSpecial.setBackground(new Color(204, 204, 255));
        observerIsSpecial.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        observerIsSpecial.setSelected(true);
        observerIsSpecial.setText("Observer is special");
        observerIsSpecial.setToolTipText("<html>Understand that the observer model (if present) is not from a team member.<br> Omit observer model from team congruence metrics.");
        observerIsSpecial.setOpaque(true);
        observerIsSpecial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                observerIsSpecialActionPerformed(evt);
            }
        });

        useSynonyms.setBackground(new Color(204, 204, 255));
        useSynonyms.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        useSynonyms.setSelected(true);
        useSynonyms.setText("Use synonyms");
        useSynonyms.setToolTipText("Consider the synonyms when matching");
        useSynonyms.setBorder(null);
        useSynonyms.setOpaque(true);
        useSynonyms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useSynonymsActionPerformed(evt);
            }
        });

        includeTrash.setBackground(new Color(204, 204, 255));
        includeTrash.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        includeTrash.setText("Include Trash");
        includeTrash.setToolTipText("Whether to omit the trash group in analysis");
        includeTrash.setAlignmentX(0.5F);
        includeTrash.setBorder(null);
        includeTrash.setMaximumSize(new java.awt.Dimension(140, 23));
        includeTrash.setMinimumSize(new java.awt.Dimension(140, 23));
        includeTrash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeTrashActionPerformed(evt);
            }
        });

        useGroupSynonyms.setBackground(new Color(204, 204, 255));
        useGroupSynonyms.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        useGroupSynonyms.setSelected(true);
        useGroupSynonyms.setText("Use group synonyms");
        useGroupSynonyms.setToolTipText("Use group synonym list if present");
        useGroupSynonyms.setBorder(null);
        useGroupSynonyms.setOpaque(true);
        useGroupSynonyms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useGroupSynonymsActionPerformed(evt);
            }
        });

        includeAdmin.setBackground(new Color(204, 204, 255));
        includeAdmin.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        includeAdmin.setSelected(true);
        includeAdmin.setText("Include Admin");
        includeAdmin.setToolTipText("Whether to omit the -admin- group in analysis");
        includeAdmin.setBorder(null);
        includeAdmin.setMaximumSize(new java.awt.Dimension(140, 23));
        includeAdmin.setMinimumSize(new java.awt.Dimension(140, 23));
        includeAdmin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeAdminActionPerformed(evt);
            }
        });

        graphMetricsEnabled.setBackground(new Color(204, 204, 255));
        graphMetricsEnabled.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        graphMetricsEnabled.setText("Graph metrics");
        graphMetricsEnabled.setToolTipText("Show each individual model's metrics (team only)");
        graphMetricsEnabled.setBorder(null);
        graphMetricsEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphMetricsEnabledActionPerformed(evt);
            }
        });

        pairwiseEnabled.setBackground(new Color(204, 204, 255));
        pairwiseEnabled.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        pairwiseEnabled.setText("Do pair-wise metrics");
        pairwiseEnabled.setToolTipText("Perform metrics for all pairs of models");
        pairwiseEnabled.setBorder(null);
        pairwiseEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pairwiseEnabledActionPerformed(evt);
            }
        });

        observerPairEnabled.setBackground(new Color(204, 204, 255));
        observerPairEnabled.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        observerPairEnabled.setText("Do observer pairing");
        observerPairEnabled.setToolTipText("Perform pair metrics only for observer-user model pairs");
        observerPairEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                observerPairEnabledActionPerformed(evt);
            }
        });

        combinedIsSpecial.setBackground(new Color(204, 204, 255));
        combinedIsSpecial.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        combinedIsSpecial.setSelected(true);
        combinedIsSpecial.setText("Combined is special");
        combinedIsSpecial.setToolTipText("<html>Understand that the combined model (if present) is not from a team member.<br> Omit combined model from team congruence metrics.");
        combinedIsSpecial.setOpaque(true);
        combinedIsSpecial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combinedIsSpecialActionPerformed(evt);
            }
        });

        combinedVsObserver.setBackground(new Color(204, 204, 255));
        combinedVsObserver.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        combinedVsObserver.setText("Combined vs observer");
        combinedVsObserver.setToolTipText("If there's an observer model, compare it with the team's joined graphs.");
        combinedVsObserver.setOpaque(true);
        combinedVsObserver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combinedVsObserverActionPerformed(evt);
            }
        });

        ignoreRelationSense.setBackground(new Color(204, 204, 255));
        ignoreRelationSense.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        ignoreRelationSense.setText("Ignore relation sense");
        ignoreRelationSense.setToolTipText("Ignore relations' direction when performing relation matching");
        ignoreRelationSense.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreRelationSenseActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout optionsPanelLayout = new org.jdesktop.layout.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(useSynonyms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(observerIsSpecial, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(optionsPanelLayout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(includeAdmin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(includeTrash, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(6, 6, 6))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(graphMetricsEnabled, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 157, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(16, 16, 16)
                        .add(useGroupSynonyms))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(observerPairEnabled)
                        .add(18, 18, 18)
                        .add(pairwiseEnabled, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 157, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(optionsPanelLayout.createSequentialGroup()
                        .add(combinedIsSpecial, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(ignoreRelationSense, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(combinedVsObserver, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(optionsPanelLayout.createSequentialGroup()
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(useSynonyms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(includeTrash, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(7, 7, 7)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(useGroupSynonyms)
                    .add(includeAdmin, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(ignoreRelationSense)
                .add(2, 2, 2)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(combinedIsSpecial, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, combinedVsObserver, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(observerIsSpecial, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(graphMetricsEnabled))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(optionsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pairwiseEnabled, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(observerPairEnabled, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        getContentPane().add(optionsPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 30, 350, 160));

        jScrollPane16.setMinimumSize(new java.awt.Dimension(400, 80));

        teamSynonymList.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Team Synonyms", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        teamSynonymList.setFont(new Font("Arial", 0, 14)); // NOI18N
        teamSynonymList.setToolTipText("<html>Enter a comma-separated list of synonyms per line; e.g.,<br>ROLE:  leader, boss, supervisor<br>ROLE: programmer, webmaster<br><br> Click anywhere outside the box when you're done.");
        teamSynonymList.setMinimumSize(new java.awt.Dimension(150, 42));
        teamSynonymList.setPreferredSize(new java.awt.Dimension(400, 42));
        teamSynonymList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                teamSynonymListFocusLost(evt);
            }
        });
        jScrollPane16.setViewportView(teamSynonymList);

        getContentPane().add(jScrollPane16, new org.netbeans.lib.awtextra.AbsoluteConstraints(570, 200, 440, 100));

        displayOptions.setBackground(new Color(204, 204, 255));
        displayOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Team Display Options", 0, 0, new Font("Arial", 3, 12))); // NOI18N
        displayOptions.setToolTipText("Options that affect what is displayed in team reports");

        enableRawHTML.setBackground(new Color(204, 204, 255));
        enableRawHTML.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        enableRawHTML.setText("Show raw HTML");
        enableRawHTML.setToolTipText("Show the HTML version of results; suitable for copy/paste");
        enableRawHTML.setBorder(null);
        enableRawHTML.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                enableRawHTMLItemStateChanged(evt);
            }
        });
        enableRawHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableRawHTMLActionPerformed(evt);
            }
        });

        showCorefsTotal.setBackground(new Color(204, 204, 255));
        showCorefsTotal.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        showCorefsTotal.setText("Show total");
        showCorefsTotal.setToolTipText("Show complete list of concept/relation coreferents");
        showCorefsTotal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCorefsTotalActionPerformed(evt);
            }
        });

        showCorefsPerLevel.setBackground(new Color(204, 204, 255));
        showCorefsPerLevel.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        showCorefsPerLevel.setText("Show per level");
        showCorefsPerLevel.setToolTipText("Show list of concept/relation coreferents per level");
        showCorefsPerLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCorefsPerLevelActionPerformed(evt);
            }
        });

        jLabel1.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        jLabel1.setText("Show coref lists:");
        jLabel1.setToolTipText("Controls what coreferent lists are shown");

        showSynonyms.setBackground(new Color(204, 204, 255));
        showSynonyms.setFont(new Font("Lucida Grande", 0, 12)); // NOI18N
        showSynonyms.setText("Show synonyms");
        showSynonyms.setToolTipText("Show synonym lists in overall and team reports");
        showSynonyms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSynonymsActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout displayOptionsLayout = new org.jdesktop.layout.GroupLayout(displayOptions);
        displayOptions.setLayout(displayOptionsLayout);
        displayOptionsLayout.setHorizontalGroup(
            displayOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(displayOptionsLayout.createSequentialGroup()
                .add(displayOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(displayOptionsLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(enableRawHTML, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, displayOptionsLayout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(showSynonyms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(displayOptionsLayout.createSequentialGroup()
                        .add(displayOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(showCorefsTotal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(showCorefsPerLevel))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        displayOptionsLayout.setVerticalGroup(
            displayOptionsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(displayOptionsLayout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 7, Short.MAX_VALUE)
                .add(showCorefsTotal, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(showCorefsPerLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(showSynonyms, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(enableRawHTML, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12))
        );

        getContentPane().add(displayOptions, new org.netbeans.lib.awtextra.AbsoluteConstraints(860, 30, 150, 160));

        openModelsE.setFont(new Font("Arial", 1, 10)); // NOI18N
        openModelsE.setText("View");
        openModelsE.setToolTipText("Open this phase's models in Charger");
        openModelsE.setMargin(new java.awt.Insets(0, 0, 0, 0));
        openModelsE.setMaximumSize(new java.awt.Dimension(84, 18));
        openModelsE.setPreferredSize(new java.awt.Dimension(70, 18));
        openModelsE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openModelsEActionPerformed(evt);
            }
        });
        getContentPane().add(openModelsE, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 310, 50, 20));

        openModelsB.setFont(new Font("Arial", 1, 10)); // NOI18N
        openModelsB.setText("View");
        openModelsB.setToolTipText("Open this phase's models in Charger");
        openModelsB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        openModelsB.setMaximumSize(new java.awt.Dimension(84, 18));
        openModelsB.setPreferredSize(new java.awt.Dimension(70, 18));
        openModelsB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openModelsBActionPerformed(evt);
            }
        });
        getContentPane().add(openModelsB, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 310, 50, 20));

        openModelsM.setFont(new Font("Arial", 1, 10)); // NOI18N
        openModelsM.setText("View");
        openModelsM.setToolTipText("Open this phase's models in Charger");
        openModelsM.setMargin(new java.awt.Insets(0, 0, 0, 0));
        openModelsM.setMaximumSize(new java.awt.Dimension(84, 18));
        openModelsM.setPreferredSize(new java.awt.Dimension(70, 18));
        openModelsM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openModelsMActionPerformed(evt);
            }
        });
        getContentPane().add(openModelsM, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 310, 50, 20));
        getContentPane().add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, -1, -1));
        getContentPane().add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 40, -1, -1));
        getContentPane().add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, -1, -1));

        groupName.setBackground(new Color(255, 255, 255));
        groupName.setFont(new Font("Arial", 1, 18)); // NOI18N
        groupName.setForeground(Global.chargerBlueColor);
        groupName.setText("GROUP goes here");
        groupName.setToolTipText("The current group being examined");
        groupName.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        groupName.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Group", 0, 0, new Font("Arial", 3, 10), Global.chargerBlueColor)); // NOI18N
        groupName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        groupName.setOpaque(true);
        getContentPane().add(groupName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 40, 200, -1));

        teamComplete.setBackground(new Color(255, 255, 255));
        teamComplete.setFont(new Font("Arial", 1, 14)); // NOI18N
        teamComplete.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        teamComplete.setText("complete?");
        teamComplete.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        teamComplete.setOpaque(true);
        getContentPane().add(teamComplete, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 140, 200, 20));

        teamName.setBackground(new Color(255, 255, 255));
        teamName.setFont(new Font("Arial", 1, 24)); // NOI18N
        teamName.setForeground(Global.chargerBlueColor);
        teamName.setText("TEAM goes here");
        teamName.setToolTipText("The current team being analyzed");
        teamName.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        teamName.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Team", 0, 0, new Font("Arial", 3, 10), Global.chargerBlueColor)); // NOI18N
        teamName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        teamName.setOpaque(true);
        getContentPane().add(teamName, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, 200, -1));

        DateB.setBackground(Global.chargerBlueColor);
        DateB.setFont(new Font("Arial", 1, 12)); // NOI18N
        DateB.setForeground(new Color(255, 255, 255));
        DateB.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        DateB.setText("date");
        getContentPane().add(DateB, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 320, 70, -1));

        DateM.setBackground(Global.chargerBlueColor);
        DateM.setFont(new Font("Arial", 1, 12)); // NOI18N
        DateM.setForeground(new Color(255, 255, 255));
        DateM.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        DateM.setText("date");
        getContentPane().add(DateM, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 320, 70, -1));

        DateE.setBackground(Global.chargerBlueColor);
        DateE.setFont(new Font("Arial", 1, 12)); // NOI18N
        DateE.setForeground(new Color(255, 255, 255));
        DateE.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        DateE.setText("date");
        getContentPane().add(DateE, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 320, 70, -1));
        getContentPane().add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 10, -1, -1));
        getContentPane().add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 20, -1, -1));

        jScrollPane17.setMinimumSize(new java.awt.Dimension(400, 80));

        groupSynonymList.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Group Synonyms", 0, 0, new Font("Lucida Grande", 2, 10))); // NOI18N
        groupSynonymList.setFont(new Font("Arial", 0, 14)); // NOI18N
        groupSynonymList.setToolTipText("<html>Enter a comma-separated list of synonyms per line; e.g.,<br>ROLE:  leader, boss, supervisor<br>ROLE: programmer, webmaster<br><br> Click anywhere outside the box when you're done.");
        groupSynonymList.setMinimumSize(new java.awt.Dimension(150, 42));
        groupSynonymList.setPreferredSize(new java.awt.Dimension(400, 42));
        groupSynonymList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                groupSynonymListFocusLost(evt);
            }
        });
        jScrollPane17.setViewportView(groupSynonymList);

        getContentPane().add(jScrollPane17, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 200, 540, 100));

        combineModelsB.setFont(new Font("Arial", 1, 10)); // NOI18N
        combineModelsB.setText("Merge");
        combineModelsB.setToolTipText("Combine this phase's models in Charger");
        combineModelsB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        combineModelsB.setMaximumSize(new java.awt.Dimension(84, 18));
        combineModelsB.setPreferredSize(new java.awt.Dimension(70, 18));
        combineModelsB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combineModelsBActionPerformed(evt);
            }
        });
        getContentPane().add(combineModelsB, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 310, 50, 20));

        combineModelsE.setFont(new Font("Arial", 1, 10)); // NOI18N
        combineModelsE.setText("Merge");
        combineModelsE.setToolTipText("Combine this phase's models in Charger");
        combineModelsE.setMargin(new java.awt.Insets(0, 0, 0, 0));
        combineModelsE.setMaximumSize(new java.awt.Dimension(84, 18));
        combineModelsE.setPreferredSize(new java.awt.Dimension(70, 18));
        combineModelsE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combineModelsEActionPerformed(evt);
            }
        });
        getContentPane().add(combineModelsE, new org.netbeans.lib.awtextra.AbsoluteConstraints(750, 310, 50, 20));

        combineModelsM.setFont(new Font("Arial", 1, 10)); // NOI18N
        combineModelsM.setText("Merge");
        combineModelsM.setToolTipText("Combine this phase's models in Charger");
        combineModelsM.setMargin(new java.awt.Insets(0, 0, 0, 0));
        combineModelsM.setMaximumSize(new java.awt.Dimension(84, 18));
        combineModelsM.setPreferredSize(new java.awt.Dimension(70, 18));
        combineModelsM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                combineModelsMActionPerformed(evt);
            }
        });
        getContentPane().add(combineModelsM, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 310, 50, 20));
        getContentPane().add(jSeparator9, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 60, -1, -1));
        getContentPane().add(jSeparator10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));
        getContentPane().add(jSeparator11, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 60, -1, -1));

        fileMenu.setText("File");

        menuCheckUnSubmitted.setText("List any un-submitted models");
        menuCheckUnSubmitted.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCheckUnSubmittedActionPerformed(evt);
            }
        });
        fileMenu.add(menuCheckUnSubmitted);
        fileMenu.add(jSeparator5);

        menuViewTeamReport.setText("View Current Team Report");
        menuViewTeamReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuViewTeamReportActionPerformed(evt);
            }
        });
        fileMenu.add(menuViewTeamReport);

        menuItemSaveTeamReport.setText("Save Current Team Report");
        menuItemSaveTeamReport.setToolTipText("If grayed out, there is no team selected.");
        menuItemSaveTeamReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveTeamReportActionPerformed(evt);
            }
        });
        fileMenu.add(menuItemSaveTeamReport);
        fileMenu.add(jSeparator6);

        menuItemSaveAllTeams.setText("Save All Team Reports (Current Group)");
        menuItemSaveAllTeams.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveAllTeamsActionPerformed(evt);
            }
        });
        fileMenu.add(menuItemSaveAllTeams);

        menuMakeGroupAsTeamReport.setText("Save Group-As-Team Report");
        menuMakeGroupAsTeamReport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMakeGroupAsTeamReportActionPerformed(evt);
            }
        });
        fileMenu.add(menuMakeGroupAsTeamReport);
        fileMenu.add(jSeparator12);

        menuItemSaveSpreadsheet.setText("Save Spreadsheet ");
        menuItemSaveSpreadsheet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSaveSpreadsheetActionPerformed(evt);
            }
        });
        fileMenu.add(menuItemSaveSpreadsheet);

        menuMakeTabularSummary.setText("View Tabular Summary");
        menuMakeTabularSummary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuMakeTabularSummaryActionPerformed(evt);
            }
        });
        fileMenu.add(menuMakeTabularSummary);
        fileMenu.add(jSeparator14);

        menuClose.setText("Close");
        menuClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCloseActionPerformed(evt);
            }
        });
        fileMenu.add(menuClose);

        jMenuBar2.add(fileMenu);

        editMenu.setText("Edit");

        menuEditGroupSynonyms.setText("Edit Group Synonyms...");
        menuEditGroupSynonyms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditGroupSynonymsActionPerformed(evt);
            }
        });
        editMenu.add(menuEditGroupSynonyms);

        menuEditTeamSynonyms.setText("Edit Team Synonyms...");
        menuEditTeamSynonyms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuEditTeamSynonymsActionPerformed(evt);
            }
        });
        editMenu.add(menuEditTeamSynonyms);
        editMenu.add(jSeparator8);

        menuItemRenameModel.setText("Rename Selected Model...");
        menuItemRenameModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRenameModelActionPerformed(evt);
            }
        });
        editMenu.add(menuItemRenameModel);

        menuItemRenameTeam.setText("Rename/Merge Current Team...");
        menuItemRenameTeam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemRenameTeamActionPerformed(evt);
            }
        });
        editMenu.add(menuItemRenameTeam);

        menuItemMoveTeam.setText("Move Current Team to Other Group...");
        menuItemMoveTeam.setToolTipText("Change the group of the current team");
        menuItemMoveTeam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemMoveTeamActionPerformed(evt);
            }
        });
        editMenu.add(menuItemMoveTeam);

        jMenuBar2.add(editMenu);

        windowMenu.setLabel("Window");
        jMenuBar2.add(windowMenu);

        setJMenuBar(jMenuBar2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void observerIsSpecialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_observerIsSpecialActionPerformed
        analyzer.refreshForNewTeam( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_observerIsSpecialActionPerformed

    private void enableRawHTMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableRawHTMLActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_enableRawHTMLActionPerformed

    private void enableRawHTMLItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_enableRawHTMLItemStateChanged
        if ( enableRawHTML.isSelected() ) {
            for ( JTextPane pane : phaseAnalysisResultsDisplay.values() ) {
                pane.setContentType( "text/plain" );
            }
        } else {
            for ( JTextPane pane : phaseAnalysisResultsDisplay.values() ) {
                pane.setContentType( "text/html" );
            }
        }
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_enableRawHTMLItemStateChanged

    private void useSynonymsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useSynonymsActionPerformed
        useGroupSynonyms.setEnabled( useSynonyms.isSelected() );
        if ( ! useSynonyms.isSelected() ) {
            showSynonyms.setSelected( false );
            showSynonyms.setEnabled( false );
        } else {
            showSynonyms.setEnabled( true );
        }
        adjustSynonymAppearance();
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_useSynonymsActionPerformed

    private void pairwiseEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pairwiseEnabledActionPerformed
         if ( pairwiseEnabled.isSelected() ) // we can either do observer pair or general pairwise, not both
            observerPairEnabled.setSelected( false );
        this.observerIsSpecial.setSelected( true );
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_pairwiseEnabledActionPerformed

    private void openModelsEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openModelsEActionPerformed
        // This is a place resultsfile we don't use HashMaps to decide which set of models to open
        // Just invoke the method on the current list
        modelMgr.openModelsInCharger( teamPhaseListEListModel );
    }//GEN-LAST:event_openModelsEActionPerformed

    private void openModelsBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openModelsBActionPerformed
        // This is a place resultsfile we don't use HashMaps to decide which set of models to open
        // Just invoke the method on the current list
        modelMgr.openModelsInCharger( teamPhaseListBListModel );
    }//GEN-LAST:event_openModelsBActionPerformed

    private void openModelsMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openModelsMActionPerformed
        // This is a place resultsfile we don't use HashMaps to decide which set of models to open
        // Just invoke the method on the current list
        modelMgr.openModelsInCharger( teamPhaseListMListModel );
    }//GEN-LAST:event_openModelsMActionPerformed

    private void formWindowActivated(WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        Global.refreshWindowMenuList( windowMenu, this );
        //refresh();
    }//GEN-LAST:event_formWindowActivated

    private void menuCheckUnSubmittedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCheckUnSubmittedActionPerformed
        String list = modelMgr.checkForUnSubmittedModels();
        JOptionPane.showMessageDialog( this, new JTextArea( list ), "Models saved but not submitted", JOptionPane.INFORMATION_MESSAGE );
    }//GEN-LAST:event_menuCheckUnSubmittedActionPerformed
    
    private void menuViewTeamReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuViewTeamReportActionPerformed
  // Assumes that the actual analysis and creating of the team metrics object has been performed upon selection of the team.
        File resultsfile = analyzer.reporter.writeTeamResults();
        try {
            if ( resultsfile != null ) {
                Desktop.getDesktop().open( resultsfile );
            }
        } catch ( IOException e ) {
            analyzer.message( "Problem opening team report file.\n" + e.getMessage() );
        }
    }//GEN-LAST:event_menuViewTeamReportActionPerformed

    private void menuItemSaveSpreadsheetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveSpreadsheetActionPerformed
        analyzer.makeSpreadsheetAction();
    }//GEN-LAST:event_menuItemSaveSpreadsheetActionPerformed

    private void menuMakeTabularSummaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMakeTabularSummaryActionPerformed
        analyzer.makeTabularSummaryAction();
    }//GEN-LAST:event_menuMakeTabularSummaryActionPerformed

    private void menuItemSaveTeamReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveTeamReportActionPerformed
        File resultsfile = analyzer.reporter.writeTeamResults();
        if ( resultsfile == null ) {
            return;    // sometimes there's no analysis to report (e.g., no team selected, etc.)
        }
        String[] options = { "OK", "View it" };
        int answer = JOptionPane.showOptionDialog( this,
                "Report saved to:\n\n" + resultsfile.getAbsolutePath(), "MMAT Analysis Report saved",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, "OK" );
        if ( answer == 0 ) {
            try {
                if ( resultsfile != null ) {
                    Desktop.getDesktop().open( resultsfile );
                }
            } catch ( IOException e ) {
                analyzer.message( "Problem opening team report file.\n" + e.getMessage() );
            }
        }
    }//GEN-LAST:event_menuItemSaveTeamReportActionPerformed

    private void menuItemMoveTeamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemMoveTeamActionPerformed
        modelMgr.moveCurrentTeamAction();
    }//GEN-LAST:event_menuItemMoveTeamActionPerformed

    private void menuItemRenameTeamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRenameTeamActionPerformed
        modelMgr.renameCurrentTeamAction();
    }//GEN-LAST:event_menuItemRenameTeamActionPerformed

    private void graphMetricsEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphMetricsEnabledActionPerformed
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_graphMetricsEnabledActionPerformed

    private void includeTrashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeTrashActionPerformed
        analyzer.includeTrash = includeTrash.isSelected();
    }//GEN-LAST:event_includeTrashActionPerformed

    private void formWindowClosed(WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        Global.info( "window closing on MMAnalysis frame " );
        tearDown();
//        analyzer.synonymEditor.setVisible( false );
//        setVisible( false );
    }//GEN-LAST:event_formWindowClosed

    private void menuItemRenameModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemRenameModelActionPerformed
        modelMgr.startupRenamer();
    }//GEN-LAST:event_menuItemRenameModelActionPerformed

    private void menuEditGroupSynonymsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditGroupSynonymsActionPerformed
        analyzer.editGroupSynonyms();
    }//GEN-LAST:event_menuEditGroupSynonymsActionPerformed

    private void useGroupSynonymsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useGroupSynonymsActionPerformed
        adjustSynonymAppearance();
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_useGroupSynonymsActionPerformed

    private void teamSynonymListFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_teamSynonymListFocusLost
        // Update the internal team synonym list, then re-do analysis 
        analyzer.updateTeamSynonymList( true );
    }//GEN-LAST:event_teamSynonymListFocusLost

    private void menuEditTeamSynonymsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuEditTeamSynonymsActionPerformed
        analyzer.editTeamSynonyms();
    }//GEN-LAST:event_menuEditTeamSynonymsActionPerformed

    private void groupSynonymListFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_groupSynonymListFocusLost
        analyzer.updateGroupSynonymList( true );
    }//GEN-LAST:event_groupSynonymListFocusLost

    private void combineModelsBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combineModelsBActionPerformed
        modelMgr.combineModelsInCharger( analyzer.currentTeamPhase.get( MPhase.Beginning ) );
    }//GEN-LAST:event_combineModelsBActionPerformed

    private void combineModelsEActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combineModelsEActionPerformed
        modelMgr.combineModelsInCharger( analyzer.currentTeamPhase.get( MPhase.End ) );
    }//GEN-LAST:event_combineModelsEActionPerformed

    private void combineModelsMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combineModelsMActionPerformed
        modelMgr.combineModelsInCharger( analyzer.currentTeamPhase.get( MPhase.Middle ) );
    }//GEN-LAST:event_combineModelsMActionPerformed

    private void includeAdminActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeAdminActionPerformed
        analyzer.includeAdmin = includeAdmin.isSelected();
    }//GEN-LAST:event_includeAdminActionPerformed

    private void showCorefsTotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCorefsTotalActionPerformed
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_showCorefsTotalActionPerformed

    private void showCorefsPerLevelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCorefsPerLevelActionPerformed
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_showCorefsPerLevelActionPerformed

    private void observerPairEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_observerPairEnabledActionPerformed
        if ( observerPairEnabled.isSelected() ) // we can either do observer pair or general pairwise, not both
            pairwiseEnabled.setSelected( false );
        this.observerIsSpecial.setSelected( false );
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_observerPairEnabledActionPerformed

    private void menuItemSaveAllTeamsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSaveAllTeamsActionPerformed
        analyzer.saveAllTeamsReports( getProject().currentGroup );
    }//GEN-LAST:event_menuItemSaveAllTeamsActionPerformed

    private void showSynonymsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSynonymsActionPerformed
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_showSynonymsActionPerformed

    private void menuMakeGroupAsTeamReportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuMakeGroupAsTeamReportActionPerformed
        analyzer.makeGroupAsTeamReport( getProject().currentGroup );
    }//GEN-LAST:event_menuMakeGroupAsTeamReportActionPerformed

    private void menuCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCloseActionPerformed
        setVisible( false );
        dispose();
    }//GEN-LAST:event_menuCloseActionPerformed

    private void combinedIsSpecialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combinedIsSpecialActionPerformed
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_combinedIsSpecialActionPerformed

    private void combinedVsObserverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_combinedVsObserverActionPerformed
        pairwiseEnabled.setSelected( false );
        observerPairEnabled.setSelected( false );
        observerIsSpecial.setSelected( true );
        combinedIsSpecial.setSelected( true );
        analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
    }//GEN-LAST:event_combinedVsObserverActionPerformed

    private void ignoreRelationSenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreRelationSenseActionPerformed
        if ( ignoreRelationSense.isSelected()) {
            int result = JOptionPane.showConfirmDialog( this, "Warning! Ignoring ALL relations' direction may result in spurious matches.\n" +
                    "Are you sure you want to do this?");
            if ( result != JOptionPane.YES_OPTION ) {
                ignoreRelationSense.setSelected( false );
            } else {
                analyzer.analyzeOneTeamAllPhases( getProject().currentGroup, getProject().currentTeam );
            }
        }
    }//GEN-LAST:event_ignoreRelationSenseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public JLabel DateB;
    public JLabel DateE;
    public JLabel DateM;
    public JLabel beginningListLabel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton combineModelsB;
    private javax.swing.JButton combineModelsE;
    private javax.swing.JButton combineModelsM;
    public javax.swing.JCheckBox combinedIsSpecial;
    public javax.swing.JCheckBox combinedVsObserver;
    private javax.swing.JPanel displayOptions;
    private javax.swing.JMenu editMenu;
    public javax.swing.JCheckBox enableRawHTML;
    public JLabel endListLabel;
    private javax.swing.JMenu fileMenu;
    private JLabel frameName;
    public JLabel graphFolderPathLabel;
    public javax.swing.JCheckBox graphMetricsEnabled;
    public JList groupList;
    public JLabel groupName;
    public JEditorPane groupSynonymList;
    public javax.swing.JCheckBox ignoreRelationSense;
    public javax.swing.JCheckBox includeAdmin;
    public javax.swing.JCheckBox includeTrash;
    private JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JPopupMenu.Separator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    public javax.swing.JMenuItem menuCheckUnSubmitted;
    private javax.swing.JMenuItem menuClose;
    public javax.swing.JMenuItem menuEditGroupSynonyms;
    public javax.swing.JMenuItem menuEditTeamSynonyms;
    public javax.swing.JMenuItem menuItemMoveTeam;
    public javax.swing.JMenuItem menuItemRenameModel;
    public javax.swing.JMenuItem menuItemRenameTeam;
    private javax.swing.JMenuItem menuItemSaveAllTeams;
    public javax.swing.JMenuItem menuItemSaveSpreadsheet;
    public javax.swing.JMenuItem menuItemSaveTeamReport;
    private javax.swing.JMenuItem menuMakeGroupAsTeamReport;
    public javax.swing.JMenuItem menuMakeTabularSummary;
    public javax.swing.JMenuItem menuViewTeamReport;
    public JLabel middleListLabel;
    public javax.swing.JCheckBox observerIsSpecial;
    public javax.swing.JCheckBox observerPairEnabled;
    private javax.swing.JButton openModelsB;
    private javax.swing.JButton openModelsE;
    private javax.swing.JButton openModelsM;
    private javax.swing.JPanel optionsPanel;
    public javax.swing.JCheckBox pairwiseEnabled;
    public JTextPane phaseAnalysisBeginning;
    public JTextPane phaseAnalysisEnd;
    public JTextPane phaseAnalysisMiddle;
    public JList projList;
    public JLabel projNameLabel;
    public javax.swing.JCheckBox showCorefsPerLevel;
    public javax.swing.JCheckBox showCorefsTotal;
    public javax.swing.JCheckBox showSynonyms;
    public JLabel teamComplete;
    public JList teamList;
    public JLabel teamName;
    public JList teamPhaseModelListB;
    public JList teamPhaseModelListE;
    public JList teamPhaseModelListM;
    public JEditorPane teamSynonymList;
    public javax.swing.JCheckBox useGroupSynonyms;
    public javax.swing.JCheckBox useSynonyms;
    private javax.swing.JMenu windowMenu;
    // End of variables declaration//GEN-END:variables
}
