// UncolorableGraphException.java, created Wed Jan 13 14:22:42 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>UncolorableGraphException</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: UncolorableGraphException.java,v 1.1.2.3 2000-07-25 23:25:03 pnkfelix Exp $
 */

public class UncolorableGraphException extends Throwable {
    
    /** Creates a <code>UncolorableGraphException</code>. */
    public UncolorableGraphException() {
        super();
    }

    /** Creates a <code>UncolorableGraphException</code>. */
    public UncolorableGraphException(String s) {
        super(s);
    }
    
}
