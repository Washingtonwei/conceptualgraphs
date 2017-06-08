//
//  SenseQueryDialog.java
//  CharGer 2003
//
//  Created by Harry Delugach on Fri Jul 04 2003.
//
package craft;


import charger.gloss.GenericTypeDescriptor;
import charger.gloss.AbstractTypeDescriptor;
import charger.gloss.wn.WordnetManager;
import charger.gloss.wn.WordnetTypeDescriptor;
import static com.sun.glass.ui.Cursor.setVisible;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import net.didion.jwnl.data.Synset;

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
 * A general dialog box to handle querying of word senses from an interactive user.
 * Its overall operation is to present a list of Wordnet senses, and if the user
 * rejects them, get some free-form generic information from the user. There are
 * two main uses of the dialog: for the user to choose from a list of Wordnet
 * senses, or for the user to enter a generic free-form definition, with a part
 * of speech. It is the caller's responsibility to decide which of the uses is
 * appropriate, with the following exception: if a Wordnet sense is being
 * queried, the user can provide their own definition if they want. In that
 * case, even though a Wordnet type descriptor was passed in, a generic type
 * descriptor is readied for return.
 * <br>
 * If the Wordnet dictionary can't be found or opened, then the dialog silently
 * ignores Wordnet and just displays the generic pos and definition panel.
 * 
 
 */
public class SenseQueryDialog extends JDialog {

    private Frame owner = null;
    private String word = null;
    private String phrase = null;

    charger.util.Transcript transcript = null;
    private String genericPOS = null;
    private String genericDef = null;

    private Synset defaultSynset = null;

    final private JComboBox poschoices = new JComboBox( AbstractTypeDescriptor.legalPartsOfSpeech );
    final private JComboBox sensechoices = new JComboBox();
    final private JTextField def = new JTextField();

    private AbstractTypeDescriptor initial = null;
    private AbstractTypeDescriptor result = null;

    private boolean userEditable = true;		// used in place of tryGeneric

    private boolean cancelled = false;

    private Color bg = Craft.craftPink;

    private WordnetManager wnmgr = null;

    Synset[] synsetList = null;
    String[] glossList = null;
    String[] posList = null;

    private JPanel labelPanel = new JPanel();
    private JPanel wordnetPanel = new JPanel();
    private JPanel genericPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();

    class MyDocumentListener implements DocumentListener {

        public void insertUpdate( DocumentEvent e ) {
            commitChanges( e );
        }

        public void removeUpdate( DocumentEvent e ) {
            commitChanges( e );
        }

        public void changedUpdate( DocumentEvent e ) {
            commitChanges( e );
        }

        private void commitChanges( DocumentEvent e ) {
            /*if ( e.getDocument().getProperty( "name" ).equals( "elemField" ) )
             grid.elementLabel = elemField.getText();
             */
            if ( def.getText().equals( "" ) ) {
                arrangeAppearanceToDescriptorType( WordnetTypeDescriptor.class );
            } else {
                arrangeAppearanceToDescriptorType( GenericTypeDescriptor.class );
            }
        }
    }
    MyDocumentListener docListener = new MyDocumentListener();

    /**
     * Sets up and runs the querying for a sense.
     *
     * @param frame the controlling frame for this dialog
     * @param trans the transcript on which to write the results of the dialog.
     * @param w The word being queried
     * @param p The phrase in which the word appears
     * @param defaultDescriptor an initial or default value for the type
     * descriptor. If it's a Wordnet type descriptor, then the dialog will
     * gather its own collection of Wordnet senses, then query the user for
     * either one of them, or a free-form definition entered by a user. If it's
     * a generic type descriptor, then there's no choice, just a definition and
     * part of speech that the user can provide.
     * @param tryGeneric Whether to query for a generic definition in case the
     * wordnet query fails.
     *
     * @see charger.obj.TypeDescriptor#legalPartsOfSpeech
     */
    public SenseQueryDialog(
            Frame frame, charger.util.Transcript trans, String w, String p, AbstractTypeDescriptor defaultDescriptor, boolean tryGeneric ) {
        super( frame, "Meaning of \"" + w + "\"", true );
        owner = frame;
        transcript = trans;
        word = w;
        phrase = p;
        initial = defaultDescriptor;
        userEditable = tryGeneric;
        wnmgr = WordnetManager.getInstance( trans );
        setup();
    }

    private void setup() {
        int margin = 7;
        int width = 700;

        this.setSize( new Dimension( width, 350 ) );
        //dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
        this.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
        setBackground( Color.pink );
        this.setLocation( new Point( 100, 100 ) );

        getContentPane().setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );
        //getContentPane().setLayout( null );
        labelPanel.setPreferredSize( new Dimension( width, 30 ) );
        labelPanel.setBackground( bg );
        getContentPane().add( labelPanel );

        genericPanel.setPreferredSize( new Dimension( width, 100 ) );
        genericPanel.setBackground( bg );
        getContentPane().add( genericPanel );

        JPanel spacer = new JPanel();
        spacer.setPreferredSize( new Dimension( width, 25 ) );
        spacer.setBackground( bg );
        getContentPane().add( spacer );

        wordnetPanel.setPreferredSize( new Dimension( width, 100 ) );
        wordnetPanel.setBackground( bg );
        getContentPane().add( wordnetPanel );

        buttonPanel.setPreferredSize( new Dimension( width, 60 ) );
        buttonPanel.setBackground( bg );
        getContentPane().add( buttonPanel );

        String prompt = makePrompt();
        JLabel label = new JLabel( "Tell what \"" + word + "\"" + prompt );
        //label.setLocation( new Point( margin, margin ) );
        label.setFont( label.getFont().deriveFont( Font.BOLD ) );
        label.setPreferredSize( label.getPreferredSize() );
        label.setBackground( Craft.craftPink );

        def.setLocation( new Point( 2 * margin, 4 * margin + 25 ) );
        def.setSize( new Dimension( genericPanel.getPreferredSize().width - 4 * margin, 28 ) );
        def.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( !def.getText().equals( "" ) ) {
                    arrangeAppearanceToDescriptorType( GenericTypeDescriptor.class );
                }
            }
        } );
        def.getDocument().addDocumentListener( docListener );

        poschoices.setEditable( false );
        poschoices.setBackground( Craft.craftPink );
        poschoices.setLocation( new Point( def.getLocation().x, def.getLocation().y - 25 ) );
        poschoices.setSize( new Dimension( 100, def.getSize().height ) );
        poschoices.getEditor().getEditorComponent().setBackground( Color.white );

        sensechoices.setEditable( userEditable );
        sensechoices.setEditable( false );
        sensechoices.setLocation( new Point( 2 * margin, 3 * margin ) );
        //sensechoices.setSize( def.getSize() );
        sensechoices.setBackground( Color.white );
        //sensechoices.getEditor().getEditorComponent().setBackground( Color.white );
        ComboBoxRenderer renderer = new ComboBoxRenderer();
		//renderer.setSize( new Dimension( sensechoices.getSize().width, 130) );
        //renderer.setPreferredSize( new Dimension( sensechoices.getSize().width, 130) );
        sensechoices.setMaximumRowCount( 5 );
        sensechoices.setRenderer( renderer );
        sensechoices.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                def.setText( "" );
                arrangeAppearanceToDescriptorType( WordnetTypeDescriptor.class );
            }
        } );
        sensechoices.addMouseListener( new MouseInputAdapter() {
            public void mouseClicked( MouseEvent me ) {
                arrangeAppearanceToDescriptorType( WordnetTypeDescriptor.class );
            }
        } );
        int choiceWidth = def.getSize().width - 150;
        int choiceHeight = def.getSize().height * 2;
        sensechoices.setSize(new Dimension( choiceWidth, choiceHeight ) );
        sensechoices.setPreferredSize(new Dimension( choiceWidth, choiceHeight ) );
        sensechoices.setMaximumSize(new Dimension( choiceWidth, choiceHeight ) );


        Point buttonLoc = new Point( buttonPanel.getSize().width - margin, buttonPanel.getSize().height - margin );
        charger.util.CGButton okButton = new charger.util.CGButton();
        buttonLoc.translate( -1 * okButton.getSize().width - margin, -1 * okButton.getSize().height - margin );
        okButton.setText( "OK" );
        okButton.setFont( okButton.getFont().deriveFont( Font.BOLD ) );
        //okButton.setLocation( buttonLoc );
        okButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                performOK();
            }
        } );

        charger.util.CGButton skipButton = new charger.util.CGButton();
        skipButton.setText( charger.Global.strs( "SkipIgnoreLabel" ) );
        buttonLoc.translate( -1 * skipButton.getSize().width - 2 * margin, 0 );
        skipButton.setFont( skipButton.getFont().deriveFont( Font.BOLD ) );
        //skipButton.setLocation( buttonLoc );
        skipButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                performSkip();
            }
        } );

        charger.util.CGButton cancelButton = new charger.util.CGButton();
        cancelButton.setText( charger.Global.strs( "CancelLabel" ) );
        buttonLoc.translate( -1 * cancelButton.getSize().width - 2 * margin, 0 );
        cancelButton.setFont( cancelButton.getFont().deriveFont( Font.BOLD ) );
        //cancelButton.setLocation( buttonLoc );
        cancelButton.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                performCancel();
            }
        } );

				// decide whether to provide a combo box list of choices
        //if ( ( initial == null || initial instanceof WordnetTypeDescriptor ) && WordnetManager.isWordnetAvailable() )
        {
            setupWordnetChoices();
        }
        //else if ( initial instanceof GenericTypeDescriptor ) 
        if ( initial != null && initial instanceof GenericTypeDescriptor ) {
            def.setText( initial.getDefinition() );
            poschoices.setSelectedItem( initial.getPOS() );
        }

        // set up borders
        genericPanel.setBorder( BorderFactory.createTitledBorder(
                //new BevelBorder( BevelBorder.RAISED, Color.white, Color.gray ),
                new LineBorder( Color.black, 2 ),
                " Write your own definition ", TitledBorder.LEFT,
                TitledBorder.TOP, new Font( "SansSerif", Font.BOLD, 11 ), Color.black ) );
        wordnetPanel.setBorder( BorderFactory.createTitledBorder(
                //new BevelBorder( BevelBorder.RAISED, Color.white, Color.gray ),
                new LineBorder( Color.black, 2 ),
                " Pick a dictionary definition ", TitledBorder.LEFT,
                TitledBorder.TOP, new Font( "SansSerif", Font.BOLD, 11 ), Color.black ) );

        genericPanel.setLayout( null );
        wordnetPanel.setLayout( null );
        buttonPanel.setLayout( new FlowLayout() );

        labelPanel.add( label );
        genericPanel.add( def );
        genericPanel.add( poschoices );
        //buttonPanel.add( cancelButton );
        buttonPanel.add( skipButton );	// NEED to figure out how to abort to an upper level
        buttonPanel.add( okButton );
        wordnetPanel.add( sensechoices );

        setLocation( new Point( 100, 100 ) ); 		// just to offset from the upper corner
        if ( initial != null ) {
            arrangeAppearanceToDescriptorType( initial.getClass() );
        } else {
            arrangeAppearanceToDescriptorType( Object.class );
        }
        setVisible( true );
        //show();
    }

    private void setupWordnetChoices() {
        synsetList = WordnetManager.collectAllSenses( word );

        if ( synsetList.length != 0 ) {
            int defaultOne = 0;		// which of the synsets is the default
            glossList = new String[ synsetList.length ];
            posList = new String[ synsetList.length ];
            for ( int snum = 0; snum < synsetList.length; snum++ ) {
				//glossList[ snum ] = synsetList[ snum ].getGloss();
                //glossList[ snum ] = wrap( synsetList[ snum ].getGloss(), 40 );
                glossList[snum] = wnmgr.getTrimmedGloss( synsetList[snum], 0, true, true );
                //craft.Craft.say( "gloss " + snum + " has length " + glossList[ snum ].length() );
                posList[snum] = synsetList[snum].getPOS().getLabel();
                if ( initial != null
                        && initial instanceof WordnetTypeDescriptor
                        && ( (WordnetTypeDescriptor)initial ).getSynset().equals( synsetList[snum] ) ) {
                    defaultOne = snum;		// tells us what should be the combo box's initial value
                }
            }
            sensechoices.setModel( new DefaultComboBoxModel( glossList ) );
            wordnetPanel.setVisible( true );
            sensechoices.setSelectedIndex( defaultOne );
            poschoices.setSelectedItem( posList[defaultOne] );
        } else // no sense choices from wordnet
        {
            wordnetPanel.setVisible( false );
            JLabel label = new JLabel( "To enable Wordnet, choose its dictionary folder in the Compatibility panel in Preferences." );
            label.setFont( new Font( "Arial", 1, 12 ) );
//            label.setLocation( new Point( 0, 200 ) );
            getContentPane().add( label );
        }
    }

    private void setGenericValues( String pos, String definition ) {
        genericPOS = pos;
        genericDef = definition;
    }

    /**
     * Get the result of the dialog's querying.
     *
     * @return <code>null</code> if the user cancelled, or something went wrong,
     * otherwise whatever type descriptor is appropriate. It is up to the caller
     * to determine the return type and handle it appropriately.
     */
    public AbstractTypeDescriptor getTypeDescriptor() {
        return result;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void arrangeAppearanceToDescriptorType( Class descriptorClass ) {
        if ( descriptorClass == GenericTypeDescriptor.class ) {
            wordnetPanel.setBackground( bg );
            genericPanel.setBackground( Color.pink );
        } else if ( descriptorClass == WordnetTypeDescriptor.class ) {
            wordnetPanel.setBackground( Color.pink );
            genericPanel.setBackground( bg );
        } else {
            wordnetPanel.setBackground( bg );
            genericPanel.setBackground( bg );
        }
    }

    private void performOK() {
        cancelled = false;
        this.setVisible( false );
        result = null;
        Craft.say( "def is \"" + def.getText() + "\"; has " + def.getText().length() + " chars" );
        if ( def.getText().equals( "" ) ) {
            setGenericValues( "", "" );
            // prepare a wordnet descriptor and assign to result
            if ( glossList != null ) {
                for ( int k = 0; k < glossList.length; k++ ) {
                    if ( glossList[k].equals( sensechoices.getSelectedItem() ) ) {
                        result = new WordnetTypeDescriptor( synsetList[k] );
                        result.setLabel( word );
                        break;
                    }
                }
            } else {
                // no wordnet descriptor found and no user-supplied definition
            }
        } else {
            String userword = def.getText();
            if ( wnmgr.wordExists( userword ) ) {
                SenseQueryDialog dialog = new SenseQueryDialog(
                        owner, transcript, userword, word + "\" of \"" + phrase, null, false );
                result = dialog.getTypeDescriptor();
            }
            if ( result == null ) {
                // prepare a generic descriptor and assign to result
                setGenericValues( poschoices.getSelectedItem().toString(), def.getText() );
                result = new GenericTypeDescriptor( word, genericPOS, genericDef );
            }
        }
        if ( result != null ) {
            arrangeAppearanceToDescriptorType( result.getClass() );
        } else {
            arrangeAppearanceToDescriptorType( null );
        }
    }

    private void performSkip() {
        this.setVisible( false );
        result = null;
        setGenericValues( null, null );
        cancelled = false;
    }

    private void performCancel() {
        this.setVisible( false );
        result = null;
        setGenericValues( null, null );
        cancelled = true;
    }

    private String makePrompt() {
        String prompt = "";
        if ( phrase != null && !phrase.equals( "" ) ) {
            prompt = " means in the phrase \"" + phrase + "\":";
        } else {
            prompt = " means:";
            phrase = word;
        }
        return prompt;
    }

    private String wrap( String string, int width ) {
        StringBuilder s = new StringBuilder( "" );
        java.util.StringTokenizer toks = new java.util.StringTokenizer( string, " \t" );
        int currentLen = 0;
        while ( toks.hasMoreTokens() ) {
            String next = toks.nextToken();
            if ( currentLen + next.length() > width ) {
                currentLen = 0;
                s.append( "\n" );
            }
            s.append( next + " " );
            currentLen = currentLen + next.length() + 1;
        }
        return s.toString();
    }

    // extends JPanel because Java's documentation recommends only putting a border on a JPanel or JLabel
    class ComboBoxRenderer extends JPanel
            implements ListCellRenderer {

        JTextArea textarea = new JTextArea();

        public ComboBoxRenderer() {
            add( textarea );
            textarea.setOpaque( true );
            textarea.setWrapStyleWord( true );
            textarea.setColumns( 40 );
            textarea.setRows( 3 );
            textarea.setLineWrap( true );
            setBorder( BorderFactory.createLineBorder( Color.black ) );
            textarea.setMargin( new Insets( 1, 3, 3, 3 ) );
            ( (FlowLayout)getLayout() ).setHgap( 1 );
            ( (FlowLayout)getLayout() ).setVgap( 1 );
       // setHorizontalAlignment( SwingConstants.LEFT );
            //textarea.setVerticalAlignment( SwingConstants.CENTER );
        }

        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus ) {

            if ( isSelected ) {
                textarea.setBackground( list.getSelectionBackground() );
                textarea.setForeground( list.getSelectionForeground() );
            } else {
                textarea.setBackground( Color.white );
                textarea.setForeground( Color.black );
            }
            setBackground( Color.white );
            //craft.Craft.say( "in get list cell renderer .. " );
            textarea.setText( (String)value );
            textarea.setPreferredSize( this.getSize() );
            textarea.setSize( this.getSize() );
            return this;
        }
    }

}
