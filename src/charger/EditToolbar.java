/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package charger;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

/**
 * Encapsulates both the basic toolbar editing user controls, as well as maintains
 * the editing mode for its EditFrame.
 * @author hsd
 */
public class EditToolbar extends javax.swing.JPanel {

    private EditFrame ef = null;
    protected Mode mode = Mode.None;

    public static enum Mode {

        Select, Concept, Relation, Actor, Arrow, Coref, TypeLabel, RelationLabel, GenSpecLink, Note, CustomEdge, Delete, None
    }
    private EnumMap<Mode, JRadioButton> modeMap = new EnumMap<>( Mode.class );
    private EnumMap<Mode, JCheckBoxMenuItem> modeMenuMap = new EnumMap<>( Mode.class );

    public static enum Command {

        MakeContext, MakeCut, UnmakeContext, /*AlignHorizontally, AlignVertically,*/ None
    }
//    private EnumMap<Command, JButton> commandMap = new EnumMap<>( Command.class );
    private EnumMap<Command, JCheckBoxMenuItem> commandMenuMap = new EnumMap<>( Command.class );

    /**
     * Creates new form EditToolbar
     */
    public EditToolbar( EditFrame ef ) {
        this.ef = ef;
        initComponents();
        initModeMap();
//        initCommandMap();
        initMenuMaps();
    }

    /**
     * Return the owner edit frame.
     *
     * @return the enclosing edit frame
     */
    public EditFrame getEditFrame() {
        return ef;
    }

    /**
     * Handles the check box group for the various tool modes, as well as the command buttons.
     *
     * @param ie the item event (containing the source button) that changed
     */
    public void toolStateChanged( ItemEvent ie ) {
        Object source = ie.getSource();
        ef.clearStatus();
        if ( source instanceof JRadioButton ) {
            // Global.info( "at item state changed of EditToolbar.");
            // all checkboxes go here
            // if user has checked another mode box, then cancel selection
            ef.resetSelection();
            ef.cp.reset();
            setMode( getMode( (JRadioButton)source ) );
//             Global.info( "tool mode is now " + mode );
        } else if ( source instanceof JButton ) {       //it's a command
            Command command = Command.None;
            for ( Command com : commandMenuMap.keySet() ) {
                if ( commandMenuMap.get( com ) == source ) {
                    command = com;
                    ( (JCheckBoxMenuItem)source ).setSelected( false );
                    break;
                }
            }
            performCommand( command );
        }
    }

    /**
     *
     */
    public void initModeMap() {
        modeMap.clear();
        modeMap.put( Mode.Select, selectionTool );
        modeMap.put( Mode.Concept, conceptTool );
        modeMap.put( Mode.Relation, relationTool );
        modeMap.put( Mode.Actor, actorTool );
        modeMap.put( Mode.Arrow, arrowTool );
        modeMap.put( Mode.Coref, corefTool );
        modeMap.put( Mode.TypeLabel, typeTool );
        modeMap.put( Mode.RelationLabel, relTypeTool );
        modeMap.put( Mode.GenSpecLink, genSpecLinkTool );
        modeMap.put( Mode.Note, noteTool );
        modeMap.put( Mode.Delete, deleteTool );
        modeMap.put(  Mode.CustomEdge, null);
        modeMap.put( Mode.None, null );
    }

//    public void initCommandMap() {
//        commandMap.clear();
//        commandMap.put( Command.MakeContext, makeContextButton );
//        commandMap.put( Command.MakeCut, makeCutButton );
//        commandMap.put( Command.UnmakeContext, unMakeContextButton );
//        commandMap.put( Command.AlignHorizontally, alignHorizontallyButton );
//        commandMap.put( Command.AlignVertically, alignVerticallyButton );
//    }

    public void initMenuMaps() {
        modeMenuMap.clear();
        modeMenuMap.put( Mode.Select, menuItemSelect );
        modeMenuMap.put( Mode.Concept, menuItemConcept );
        modeMenuMap.put( Mode.Relation, menuItemRelation );
        modeMenuMap.put( Mode.Actor, menuItemActor );
        modeMenuMap.put( Mode.Arrow, menuItemArrow );
        modeMenuMap.put( Mode.Coref, menuItemCoref );
        modeMenuMap.put( Mode.TypeLabel, menuItemType );
        modeMenuMap.put( Mode.RelationLabel, menuItemRelType );
        modeMenuMap.put( Mode.GenSpecLink, menuItemGenSpecLink );
        modeMenuMap.put( Mode.Note, menuItemNote );
        modeMenuMap.put( Mode.Delete, menuItemDelete );

        commandMenuMap.clear();
        commandMenuMap.put( Command.MakeContext, menuItemMakeContext  );
        commandMenuMap.put( Command.MakeCut, menuItemMakeCut );
        commandMenuMap.put( Command.UnmakeContext, menuItemUnmakeContext  );
//        commandMenuMap.put( Command.AlignHorizontally, menuItemAlignHorizontally  );
//        commandMenuMap.put( Command.AlignVertically, menuItemAlignVertically  );
    }

    /**
     * What is the current tool mode?
     *
     * @return the current mode set in this toolbar
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the current mode of the toolbar. Overrides any user events.
     */
    public void setMode( Mode m ) {
        if ( m == mode ) return;
        mode = m;
        JRadioButton button = modeMap.get( m );
        button.setSelected( true );
        JCheckBoxMenuItem menuitem = modeMenuMap.get( m );
        clearMenuCheckboxes();
        menuitem.setSelected( true );
    }

    /**
     * Get the mode associated with this button.
     *
     * @param button
     * @return the mode associated with this button; Mode.None if not found.
     */
    public Mode getMode( JRadioButton button ) {
        if ( button == null ) {
            return Mode.None;
        }
        for ( Mode m : modeMap.keySet() ) {
            if ( modeMap.get( m ) == button ) {
                return m;
            }
        }
        return Mode.None;
    }

    /**
     * Get the mode associated with this menu item.
     *
     * @param item the item being checked.
     * @return the mode associated with this menu item; Mode.None if not found.
     */
    public Mode getMode( JCheckBoxMenuItem item ) {
        if ( item == null ) {
            return Mode.None;
        }
        for ( Mode m : modeMenuMap.keySet() ) {
            if ( modeMenuMap.get( m ).equals( item ) ) {
                return m;
            }
        }
        return Mode.None;
    }

    /**
     * Determine whether the tool mode is one for inserting nodes
     *
     * @return whether the current tool mode is for inserting a node
     */
    public boolean isNodeInsertMode() {
        switch ( mode ) {
            case Concept:
            case Relation:
            case Actor:
            case TypeLabel:
            case RelationLabel:
            case Note:
                return true;
            default:
                return false;
        }
    }

    /**
     * Determine whether the current tool mode is one for inserting edges
     *
     * @return whether the tool mode is one that can insert edges
     */
    public boolean isEdgeInsertMode() {
        switch ( mode ) {
            case Arrow:
            case Coref:
            case GenSpecLink:
                return true;
            default:
                return false;
        }
    }

    /**
     * Set the mode based on a KeyEvent value
     *
     * @param key the KeyEvent value returned by mousePressed
     * @see KeyEvent
     */
    public void shortcutKeys( int key ) {
        switch ( key ) {
            case KeyEvent.VK_S:
            case KeyEvent.VK_SPACE:
                setMode( Mode.Select );
                break;
            case KeyEvent.VK_C:
                setMode( Mode.Concept );
                break;
            case KeyEvent.VK_R:
                setMode( Mode.Relation );
                break;
            case KeyEvent.VK_A:
                setMode( Mode.Actor );
                break;
            case KeyEvent.VK_PERIOD:
                setMode( Mode.Arrow );
                break;
            case KeyEvent.VK_I:
                setMode( Mode.Coref );
                break;
            case KeyEvent.VK_T:
                setMode( Mode.TypeLabel );
                break;
            case KeyEvent.VK_L:
                setMode( Mode.RelationLabel );
                break;
            case KeyEvent.VK_COMMA:
                setMode( Mode.GenSpecLink );
                break;
            case KeyEvent.VK_N:
                setMode( Mode.Note );
                break;

        }
    }

    public JMenu getMenu() {
        return drawingMenu;
    }

    public void clearMenuCheckboxes() {
        for ( Component item : getMenu().getMenuComponents() ) {
            if ( item instanceof JCheckBoxMenuItem ) {
                ( (JCheckBoxMenuItem)item ).setSelected( false );
            }
        }
    }

    public void menuItemSelected( ActionEvent evt ) {
        Object source = evt.getSource();
        ef.clearStatus();
        if ( source instanceof JCheckBoxMenuItem ) {
            // Global.info( "at menuItemSelectedMode of EditToolbar.");
            // all menu items go here
            Mode m = getMode( (JCheckBoxMenuItem)source );
            if ( m != Mode.None ) {
                ef.resetSelection();
                ef.cp.reset();

                setMode( m );
                Global.info( "tool mode is now " + mode );
                return;
            }
            // Here if it's not a mode
            Command command = Command.None;
            for ( Command com : commandMenuMap.keySet() ) {
                if ( commandMenuMap.get( com ) == source ) {
                    command = com;
                    ( (JCheckBoxMenuItem)source ).setSelected( false );
                    break;
                }
            }
            performCommand( command );
        }
    }

    /**
     * If something is selected, then enable the commands, otherwise disable
     * them.
     *
     * @param somethingIsSelected
     */
    public void setAvailableCommands( boolean somethingIsSelected ) {
        for ( Command com : commandMenuMap.keySet() ) {
            JMenuItem item = commandMenuMap.get( com );
            item.setEnabled( somethingIsSelected );
        }
    }

    public void performCommand( Command command ) {
        switch ( command ) {
            case MakeContext:
                ef.emgr.performActionMakeContext( "context" );
                break;
            case MakeCut:
                ef.emgr.performActionMakeContext( "cut" );
                break;
            case UnmakeContext:
                ef.emgr.performActionUnMakeContext();
                break;
//            case AlignHorizontally:
//                ef.emgr.performActionAlign( "horizontal" );
//                break;
//            case AlignVertically:
//                ef.emgr.performActionAlign( "vertical" );
//                break;
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

        toolButtonGroup = new javax.swing.ButtonGroup();
        drawingMenu = new JMenu();
        menuItemSelect = new JCheckBoxMenuItem();
        menuItemConcept = new JCheckBoxMenuItem();
        menuItemRelation = new JCheckBoxMenuItem();
        menuItemActor = new JCheckBoxMenuItem();
        menuItemArrow = new JCheckBoxMenuItem();
        menuItemCoref = new JCheckBoxMenuItem();
        menuItemType = new JCheckBoxMenuItem();
        menuItemRelType = new JCheckBoxMenuItem();
        menuItemGenSpecLink = new JCheckBoxMenuItem();
        menuItemNote = new JCheckBoxMenuItem();
        menuItemDelete = new JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        menuItemMakeContext = new JCheckBoxMenuItem();
        menuItemMakeCut = new JCheckBoxMenuItem();
        menuItemUnmakeContext = new JCheckBoxMenuItem();
        conceptTool = new JRadioButton();
        selectionTool = new JRadioButton();
        relationTool = new JRadioButton();
        actorTool = new JRadioButton();
        arrowTool = new JRadioButton();
        corefTool = new JRadioButton();
        typeTool = new JRadioButton();
        relTypeTool = new JRadioButton();
        genSpecLinkTool = new JRadioButton();
        noteTool = new JRadioButton();
        deleteTool = new JRadioButton();
        jSeparator1 = new javax.swing.JSeparator();
        makeContextButton = new JButton();
        makeCutButton = new JButton();
        unMakeContextButton = new JButton();

        drawingMenu.setText("Draw");

        menuItemSelect.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        menuItemSelect.setSelected(true);
        menuItemSelect.setText("Select");
        menuItemSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemSelectActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemSelect);

        menuItemConcept.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
        menuItemConcept.setSelected(true);
        menuItemConcept.setText("Concept");
        menuItemConcept.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemConceptActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemConcept);

        menuItemRelation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
        menuItemRelation.setSelected(true);
        menuItemRelation.setText("Relation");
        menuItemRelation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemRelationActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemRelation);

        menuItemActor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
        menuItemActor.setSelected(true);
        menuItemActor.setText("Actor");
        menuItemActor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemActorActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemActor);

        menuItemArrow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0));
        menuItemArrow.setSelected(true);
        menuItemArrow.setText("Relation Arrow");
        menuItemArrow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemArrowActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemArrow);

        menuItemCoref.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
        menuItemCoref.setSelected(true);
        menuItemCoref.setText("Co-referent Link");
        menuItemCoref.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemCorefActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemCoref);

        menuItemType.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0));
        menuItemType.setSelected(true);
        menuItemType.setText("Type Label (hierarchy)");
        menuItemType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemTypeActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemType);

        menuItemRelType.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0));
        menuItemRelType.setSelected(true);
        menuItemRelType.setText("Relation Label (hierarchy)");
        menuItemRelType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemRelTypeActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemRelType);

        menuItemGenSpecLink.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0));
        menuItemGenSpecLink.setSelected(true);
        menuItemGenSpecLink.setText("Super/Sub-Type Link");
        menuItemGenSpecLink.setActionCommand("");
        menuItemGenSpecLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemGenSpecLinkActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemGenSpecLink);

        menuItemNote.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        menuItemNote.setSelected(true);
        menuItemNote.setText("Note");
        menuItemNote.setActionCommand("");
        menuItemNote.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemNoteActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemNote);

        menuItemDelete.setSelected(true);
        menuItemDelete.setText("Delete...");
        menuItemDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemDeleteActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemDelete);
        drawingMenu.add(jSeparator2);

        menuItemMakeContext.setSelected(true);
        menuItemMakeContext.setText("Make Context");
        menuItemMakeContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemMakeContextActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemMakeContext);

        menuItemMakeCut.setSelected(true);
        menuItemMakeCut.setText("Make Cut");
        drawingMenu.add(menuItemMakeCut);

        menuItemUnmakeContext.setSelected(true);
        menuItemUnmakeContext.setText("Unmake Context");
        menuItemUnmakeContext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                menuItemUnmakeContextActionPerformed(evt);
            }
        });
        drawingMenu.add(menuItemUnmakeContext);

        setBackground(Global.chargerBlueColor);
        setMaximumSize(new java.awt.Dimension(41, 2000));
        setMinimumSize(new java.awt.Dimension(41, 650));

        conceptTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(conceptTool);
        conceptTool.setToolTipText("C   Insert Concept");
        conceptTool.setAlignmentX(0.5F);
        conceptTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        conceptTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        conceptTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/ConceptIcon.gif"))); // NOI18N
        conceptTool.setMaximumSize(new java.awt.Dimension(37, 32));
        conceptTool.setMinimumSize(new java.awt.Dimension(37, 32));
        conceptTool.setPreferredSize(new java.awt.Dimension(37, 32));
        conceptTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/ConceptIconInverted.gif"))); // NOI18N
        conceptTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                conceptToolItemStateChanged(evt);
            }
        });

        selectionTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(selectionTool);
        selectionTool.setToolTipText("S or spacebar   Selection Mode");
        selectionTool.setAlignmentX(0.5F);
        selectionTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        selectionTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        selectionTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/SelectIcon.gif"))); // NOI18N
        selectionTool.setMaximumSize(new java.awt.Dimension(37, 32));
        selectionTool.setMinimumSize(new java.awt.Dimension(37, 32));
        selectionTool.setPreferredSize(new java.awt.Dimension(37, 32));
        selectionTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/SelectIconInverted.gif"))); // NOI18N
        selectionTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                selectionToolItemStateChanged(evt);
            }
        });

        relationTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(relationTool);
        relationTool.setToolTipText("R   Insert Relation");
        relationTool.setAlignmentX(0.5F);
        relationTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        relationTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        relationTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/RelationIcon.gif"))); // NOI18N
        relationTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/RelationIconInverted.gif"))); // NOI18N
        relationTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                relationToolItemStateChanged(evt);
            }
        });

        actorTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(actorTool);
        actorTool.setToolTipText("A   Insert Actor");
        actorTool.setAlignmentX(0.5F);
        actorTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        actorTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        actorTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/ActorIcon.gif"))); // NOI18N
        actorTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/ActorIconInverted.gif"))); // NOI18N
        actorTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                actorToolItemStateChanged(evt);
            }
        });

        arrowTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(arrowTool);
        arrowTool.setToolTipText(". (period)   Insert Relation Arrow");
        arrowTool.setAlignmentX(0.5F);
        arrowTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        arrowTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        arrowTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/ArrowIcon.gif"))); // NOI18N
        arrowTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/ArrowIconInverted.gif"))); // NOI18N
        arrowTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                arrowToolItemStateChanged(evt);
            }
        });

        corefTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(corefTool);
        corefTool.setToolTipText("i   Insert Co-referent Link");
        corefTool.setAlignmentX(0.5F);
        corefTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        corefTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        corefTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/CorefIcon.gif"))); // NOI18N
        corefTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/CorefIconInverted.gif"))); // NOI18N
        corefTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                corefToolItemStateChanged(evt);
            }
        });

        typeTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(typeTool);
        typeTool.setToolTipText("T   Insert Type Label for hierarchy");
        typeTool.setAlignmentX(0.5F);
        typeTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        typeTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        typeTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/TypeIcon.gif"))); // NOI18N
        typeTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/TypeIconInverted.gif"))); // NOI18N
        typeTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                typeToolItemStateChanged(evt);
            }
        });

        relTypeTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(relTypeTool);
        relTypeTool.setToolTipText("L   Insert Relation Type for hierarchy");
        relTypeTool.setAlignmentX(0.5F);
        relTypeTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        relTypeTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        relTypeTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/RelTypeIcon.gif"))); // NOI18N
        relTypeTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/RelTypeIconInverted.gif"))); // NOI18N
        relTypeTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                relTypeToolItemStateChanged(evt);
            }
        });

        genSpecLinkTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(genSpecLinkTool);
        genSpecLinkTool.setToolTipText(", (comma)   Insert super/subtype link");
        genSpecLinkTool.setAlignmentX(0.5F);
        genSpecLinkTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        genSpecLinkTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        genSpecLinkTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/GenSpecLinkIcon.gif"))); // NOI18N
        genSpecLinkTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/GenSpecLinkIconInverted.gif"))); // NOI18N
        genSpecLinkTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                genSpecLinkToolItemStateChanged(evt);
            }
        });

        noteTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(noteTool);
        noteTool.setToolTipText("N   Insert a note");
        noteTool.setAlignmentX(0.5F);
        noteTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        noteTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        noteTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/NoteIcon.gif"))); // NOI18N
        noteTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/NoteIconInverted.gif"))); // NOI18N
        noteTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                noteToolItemStateChanged(evt);
            }
        });

        deleteTool.setBackground(new java.awt.Color(255, 255, 255));
        toolButtonGroup.add(deleteTool);
        deleteTool.setToolTipText("Delete (click on item)");
        deleteTool.setAlignmentX(0.5F);
        deleteTool.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        deleteTool.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        deleteTool.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/DeleteIcon.gif"))); // NOI18N
        deleteTool.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/DeleteIconInverted.gif"))); // NOI18N
        deleteTool.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                deleteToolItemStateChanged(evt);
            }
        });

        jSeparator1.setBackground(new java.awt.Color(255, 255, 255));
        jSeparator1.setBorder(javax.swing.BorderFactory.createBevelBorder(0));

        makeContextButton.setBackground(new java.awt.Color(255, 255, 255));
        makeContextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/MakeContextIcon.gif"))); // NOI18N
        makeContextButton.setToolTipText("Make context using selection");
        makeContextButton.setAlignmentX(0.5F);
        makeContextButton.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        makeContextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        makeContextButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        makeContextButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/MakeContextIconInverted.gif"))); // NOI18N
        makeContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                makeContextButtonActionPerformed(evt);
            }
        });

        makeCutButton.setBackground(new java.awt.Color(255, 255, 255));
        makeCutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/MakeCutIcon.gif"))); // NOI18N
        makeCutButton.setToolTipText("Make \"cut\" (negated context) using selection");
        makeCutButton.setAlignmentX(0.5F);
        makeCutButton.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        makeCutButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        makeCutButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        makeCutButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/MakeCutIconInverted.gif"))); // NOI18N
        makeCutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                makeCutButtonActionPerformed(evt);
            }
        });

        unMakeContextButton.setBackground(new java.awt.Color(255, 255, 255));
        unMakeContextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/UnMakeContext.gif"))); // NOI18N
        unMakeContextButton.setToolTipText("Remove selected context (but not its contents)");
        unMakeContextButton.setAlignmentX(0.5F);
        unMakeContextButton.setBorder(javax.swing.BorderFactory.createBevelBorder(0));
        unMakeContextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        unMakeContextButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/GIF/UnMakeContextInverted.gif"))); // NOI18N
        unMakeContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                unMakeContextButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(conceptTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(selectionTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(relationTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(actorTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(arrowTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(corefTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(typeTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(relTypeTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(genSpecLinkTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(noteTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(deleteTool, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(makeContextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(makeCutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(unMakeContextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 4, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectionTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conceptTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relationTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(actorTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(arrowTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(corefTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(typeTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relTypeTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genSpecLinkTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noteTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteTool, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(makeContextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(makeCutButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unMakeContextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(193, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectionToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_selectionToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_selectionToolItemStateChanged

    private void conceptToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_conceptToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_conceptToolItemStateChanged

    private void relationToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_relationToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_relationToolItemStateChanged

    private void actorToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_actorToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_actorToolItemStateChanged

    private void arrowToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_arrowToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_arrowToolItemStateChanged

    private void corefToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_corefToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_corefToolItemStateChanged

    private void typeToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_typeToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_typeToolItemStateChanged

    private void relTypeToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_relTypeToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_relTypeToolItemStateChanged

    private void genSpecLinkToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_genSpecLinkToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_genSpecLinkToolItemStateChanged

    private void noteToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_noteToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_noteToolItemStateChanged

    private void deleteToolItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_deleteToolItemStateChanged
        toolStateChanged( evt );
    }//GEN-LAST:event_deleteToolItemStateChanged

    private void makeContextButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_makeContextButtonActionPerformed
        performCommand( Command.MakeContext );
    }//GEN-LAST:event_makeContextButtonActionPerformed

    private void makeCutButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_makeCutButtonActionPerformed
        performCommand( Command.MakeCut );
    }//GEN-LAST:event_makeCutButtonActionPerformed

    private void unMakeContextButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_unMakeContextButtonActionPerformed
        performCommand( Command.UnmakeContext );
	}//GEN-LAST:event_unMakeContextButtonActionPerformed

    private void menuItemSelectActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemSelectActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemSelectActionPerformed

    private void menuItemConceptActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemConceptActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemConceptActionPerformed

    private void menuItemRelationActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemRelationActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemRelationActionPerformed

    private void menuItemActorActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemActorActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemActorActionPerformed

    private void menuItemArrowActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemArrowActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemArrowActionPerformed

    private void menuItemCorefActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemCorefActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemCorefActionPerformed

    private void menuItemTypeActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemTypeActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemTypeActionPerformed

    private void menuItemRelTypeActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemRelTypeActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemRelTypeActionPerformed

    private void menuItemGenSpecLinkActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemGenSpecLinkActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemGenSpecLinkActionPerformed

    private void menuItemNoteActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemNoteActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemNoteActionPerformed

    private void menuItemDeleteActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemDeleteActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemDeleteActionPerformed

    private void menuItemMakeContextActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemMakeContextActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemMakeContextActionPerformed

    private void menuItemUnmakeContextActionPerformed(ActionEvent evt) {//GEN-FIRST:event_menuItemUnmakeContextActionPerformed
        menuItemSelected( evt );
    }//GEN-LAST:event_menuItemUnmakeContextActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton actorTool;
    private JRadioButton arrowTool;
    private JRadioButton conceptTool;
    private JRadioButton corefTool;
    private JRadioButton deleteTool;
    private JMenu drawingMenu;
    private JRadioButton genSpecLinkTool;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private JButton makeContextButton;
    private JButton makeCutButton;
    private JCheckBoxMenuItem menuItemActor;
    private JCheckBoxMenuItem menuItemArrow;
    private JCheckBoxMenuItem menuItemConcept;
    private JCheckBoxMenuItem menuItemCoref;
    private JCheckBoxMenuItem menuItemDelete;
    private JCheckBoxMenuItem menuItemGenSpecLink;
    private JCheckBoxMenuItem menuItemMakeContext;
    private JCheckBoxMenuItem menuItemMakeCut;
    private JCheckBoxMenuItem menuItemNote;
    private JCheckBoxMenuItem menuItemRelType;
    private JCheckBoxMenuItem menuItemRelation;
    private JCheckBoxMenuItem menuItemSelect;
    private JCheckBoxMenuItem menuItemType;
    private JCheckBoxMenuItem menuItemUnmakeContext;
    private JRadioButton noteTool;
    private JRadioButton relTypeTool;
    private JRadioButton relationTool;
    private JRadioButton selectionTool;
    private javax.swing.ButtonGroup toolButtonGroup;
    private JRadioButton typeTool;
    private JButton unMakeContextButton;
    // End of variables declaration//GEN-END:variables
}
