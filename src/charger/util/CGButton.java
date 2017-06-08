package charger.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;


/**
 * A convenience class primarily for ensuring uniformity among CharGer buttons.
 * Deals with appearance only.
 */
public class CGButton extends JButton {

    private static final Dimension ButtonSize = new Dimension( 120, 25 );

    public CGButton() {
        super();
        init();
    }

    public CGButton( Action a ) {
        super( a );
        init();
    }

    private void init() {
//        this.setBackground( Color.white );
//        this.setOpaque( true );
        this.setVisible( true );
//        this.setFont( new Font( "Dialog", 0, 12 ) );
        this.setSize( ButtonSize );
        this.setPreferredSize( ButtonSize );
        this.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
//        this.setMargin( new Insets( 4, 4, 4, 4 ) );
//        this.setBorder( BorderFactory.createRaisedBevelBorder() );
    }
}