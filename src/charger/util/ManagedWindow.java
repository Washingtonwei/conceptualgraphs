//
//  WindowTracking.java
//  CharGer 2003
//
//  Created by Harry Delugach on Sun May 18 2003.
//

package charger.util;

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
	Specifies services needed for a window that wants to be managed by CharGer. 
	If a window implements this interface, it is entitled to be included in
	a window menu and can be activated by selecting from that menu.
	@see WindowManager
	
 */
public interface ManagedWindow {

	/** Method to be invoked when this window is chosen to be the current window.
	@see WindowManager#bringToFront
	 */
	public void bringToFront();
	
	/** Tells a window manager what label to put on the menu to select this window */
	public String getMenuItemLabel();
	
	/**
		If there's a file associated with the window, return its name; null otherwise.
	 */
	public String getFilename();

}
