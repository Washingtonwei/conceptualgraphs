//
//  Harry S. Delugach.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun May 18 2003.
//
package charger.util;


import charger.EditFrame;
import charger.Global;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

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
 * Manages multiple windows, where windows may be added and removed dynamically.
 * Its basic use is in creating and handling events for a "Windows" menu. Since
 * all methods are static, there is effectively only one WindowManager per
 * session.
 *
 * @see ManagedWindow
 */
public class WindowManager {

    /**
     * Keeps the window entries in the form of (MenuItemLabel, ManagedWindow)
     * pairs
     */
    private static Hashtable menuWindowLookup = new Hashtable();
    /**
     * Keeps the window file entries in the form of ( file, ManagedWindow) pairs
     */
    private static Hashtable fileWindowLookup = new Hashtable();
    /**
     * The global list of all managed windows, independent of any menus
     */
    private static ArrayList<ManagedWindow> windowList = new ArrayList<ManagedWindow>();
    /**
     * for each window, keeps whatever accelerator key has been stored for it
     */
    private static Hashtable acceleratorList = new Hashtable();
    private static boolean listToBeSorted = false;

    /**
     * Notify this window manager that a window wants to be managed.
     *
     * @param mw the window to be managed; must implement the ManagedWindow
     * interface.
     */
    public static void manageWindow( ManagedWindow mw ) {
        manageWindow( mw, null );
    }

    /**
     * Notify this window manager that a window wants to be managed.
     *
     * @param mw the window to be managed; must implement the ManagedWindow
     * interface.
     * @param key the accelerator to be associated with this window's menu item.
     * If <code>null</code> then do not include an accelerator.
     */
    public static void manageWindow( ManagedWindow mw, KeyStroke key ) {
        // charger.Global.info( "manage window: window is " + mw.getClass().getName() );
        if ( listToBeSorted ) {
            insertNewWindowIntoList( mw );
        } else {
            windowList.add( mw );
        }
        if ( key != null ) {
            acceleratorList.put( mw, key );
        }
        changeFilename( mw, mw.getFilename() );
    }

    /**
     * Notify this window manager that a window no longer needs to be managed.
     *
     * @param mw the window to be removed; if not found, then do nothing.
     */
    public static void forgetWindow( ManagedWindow mw ) {
        windowList.remove( mw );
        acceleratorList.remove( mw );
        fileWindowLookup.remove( mw );
    }

    /**
     * Brings to the front whatever window goes with the menu item specified.
     *
     * @param mi A menu item previously associated with a particular window by
     * makeMenu.
     * @see #makeMenu
     */
    public static void chooseWindowFromMenu( JMenuItem mi ) {
        ManagedWindow mw = (ManagedWindow)menuWindowLookup.get( mi );
        if ( mw != null ) {
            mw.bringToFront();
        }
    }

    private static void insertNewWindowIntoList( ManagedWindow mw ) {
        String label = mw.getMenuItemLabel();
        //Global.info( "insert new window \"" + label + "\" into list" );
        if ( windowList.size() == 0 ) {
            windowList.add( mw );
        } else {
            int num = 0;
            while ( num < windowList.size()
                    && ( windowList.get( num ) ).getMenuItemLabel().compareToIgnoreCase( label ) < 0 ) {
                //Global.info( ((ManagedWindow)windowList.get( num )).getMenuItemLabel() +
                //		" is less than " + label );
                num++;
            }
            windowList.add( num, mw );
        }
        //Global.info( "after inserting " + mw.getMenuItemLabel() + " into list, the window list has " +
        //	windowList.toString() );
    }

    /**
     * Create menu entries for every managed window. Uses only the last
     *
     * @param menu The menu (usually a "Windows" menu) to which items are to be
     * added.
     * @param currentWindow put a check box next to this window's entry.
     * @see ManagedWindow
     */
    public static synchronized void makeMenu( JMenu menu, ManagedWindow currentWindow ) {
        int tailLength = 50;		// only this length string will be used in menu
        //menu.removeAll();       // should dispose of each item, not just remove from menu
        Util.tearDownMenu( menu );
        menuWindowLookup.clear();
        Iterator wins = windowList.iterator();
        while ( wins.hasNext() ) {
            ManagedWindow mw = (ManagedWindow)wins.next();
            String label = mw.getMenuItemLabel();
            if ( label.length() > tailLength ) {
                label = "..." + label.substring( label.length() - tailLength );
            }
            // TODO: Figure out where this should be deleted from the menu
            JCheckBoxMenuItem mi = new JCheckBoxMenuItem( label );
            //Global.info( "in refreshmenu, about to add menu item for " + mw.getMenuItemLabel() );
            menu.add( mi );
            if ( acceleratorList.get( mw ) != null ) {
                mi.setAccelerator( (KeyStroke)acceleratorList.get( mw ) );
            }

            if ( mw == currentWindow ) {
                mi.setState( true );
            } else {
                mi.setState( false );
            }
            //Global.info( "in refreshmenu, about to addactionlistener." );
            menuWindowLookup.put( mi, mw );
            mi.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    windowActionPerformed( e );
                }
            } );
        }
    }

    /**
     * Handles the window menu selections for the EditFrame, HubFrame, Craft and
     * CraftWindow classes.
     */
    public static synchronized void windowActionPerformed( ActionEvent e ) {
        // handle all menu events here
        Object source = e.getSource();
        String menuText = e.getActionCommand();
        chooseWindowFromMenu( (JMenuItem)source );
    }

    /**
     * A default method to be used by ManagedWindow implementations. It's
     * probably best for implementations to provide their real refresh method in
     * response to a windowActivated event; this method merely brings them to
     * the front and requests focus. Any window that is NOT an editframe should
     * set currentEditFrame to null if necessary.
     *
     */
    public static void bringToFront( JFrame win ) {
        win.toFront();
        win.requestFocus();
    }

    public static void setSorted( boolean sortme ) {
        listToBeSorted = sortme;
    }

    public static void changeFilename( ManagedWindow mw, String filename ) {
        String dummy = (String)fileWindowLookup.remove( mw );
        if ( filename != null ) {
            fileWindowLookup.put( mw, filename );
        }
    }

    /**
     * Handles the window menu selections for the EditFrame, HubFrame, Craft and
     * CraftWindow classes.
     */
    private static void actionPerformedOLD( ActionEvent e ) {
        // handle all menu events here
        Object source = e.getSource();
        String menuText = e.getActionCommand();
        if ( menuText.equals( Global.strs( "BackToHubCmdLabel" ) ) ) {
            //performActionBackToHub();	// the return to hub action was in the tool panel originally...
        } else if ( menuText.equals( Global.strs( "BackToCraftCmdLabel" ) ) ) {
            craft.Craft.craftWindow.refresh();
            craft.Craft.craftWindow.toFront();
        } else // must be one of the frames in the window menu
        {
            JFrame selectedFrame = null; //(JFrame)Hub.allWindowsList.get( source );

            if ( selectedFrame == null ) {
                Global.info( "no window goes with " + source.toString() );
            } else {
                EditFrame ef = null;
                repgrid.RGDisplayWindow rwin = null;
                if ( selectedFrame instanceof EditFrame ) {
                    ef = (EditFrame)selectedFrame;
                } else if ( selectedFrame instanceof repgrid.RGDisplayWindow ) {
                    rwin = (repgrid.RGDisplayWindow)selectedFrame;
                }
                String fname = menuText;
                Global.info( "frame chosen is " + fname );
                Global.setCurrentEditFrame( ef );
                if ( ef != null ) {
                    //HubFrame.refreshWindowMenuList( ef.windowMenu, ef );
                    ef.toFront();
                    Global.info( ef.getTitle() + " to front in HubFrame action performed" ); //commented 12-16-02
                    ef.requestFocus();
                } else // is a rep grid window
                {
                    //HubFrame.refreshWindowMenuList( rwin.windowMenu, rwin );
                    rwin.toFront();
                    rwin.requestFocus();
                }
            }

        }
    }

    /**
     * Find the window that corresponds to a particular file. This is the
     * reverse lookup from what the usual menu item does.
     *
     * @param f A file that is associated with some window.
     * @return The window that's associated with this file. If there is no such
     * window, return null.
     */
    public static ManagedWindow getWindowFromFile( File f ) {
        Iterator keys = fileWindowLookup.keySet().iterator();
        while ( keys.hasNext() ) {
            ManagedWindow mw = (ManagedWindow)keys.next();
            String filename = (String)fileWindowLookup.get( mw );
            if ( filename.equals( f.getAbsolutePath() ) ) {
                return mw;
            }
        }
        return null;
    }
}
