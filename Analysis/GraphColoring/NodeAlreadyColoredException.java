// NodeAlreadyColoredException.java, created Thu Jan 14 17:15:00 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

/**
 * <code>NodeAlreadyColoredException</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: NodeAlreadyColoredException.java,v 1.1.2.5 2001-06-17 22:29:39 cananian Exp $
 */

public class NodeAlreadyColoredException extends RuntimeException {
    
    /** Creates a <code>NodeAlreadyColoredException</code>. */
    public NodeAlreadyColoredException() {
        super();
    }

    /** Creates a <code>NodeAlreadyColoredException</code>. */
    public NodeAlreadyColoredException(String s) {
        super(s);
    }
    
}