// FOOTER.java, created Mon Sep  7 10:36:08 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>FOOTER</code> nodes are used to anchor the bottom end of the quad
 * graph.  They do not represent bytecode and are not executable. <p>
 * <code>RETURN</code> and <code>THROW</code> nodes should have a 
 * <code>FOOTER</code> node as their only successor.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FOOTER.java,v 1.10.4.1 1998-12-11 23:44:34 mfoltz Exp $
 * @see HEADER
 * @see RETURN
 * @see THROW
 */

public class FOOTER extends Quad {
    
    /** Creates a <code>FOOTER</code>. */
    public FOOTER(HCodeElement source) {
        super(source, 0/*no predecessors initially*/, 0/*no successors ever*/);
    }

    /** Grow the arity of a FOOTER by one. */
    void grow() {
	Edge[] nprev = new Edge[prev.length + 1];
	System.arraycopy(prev, 0, nprev, 0, prev.length);
	nprev[prev.length] = null;
	prev = nprev;
    }
    /** Attach a new Quad to this FOOTER. */
    public void attach(Quad q, int which_succ) {
	grow(); 
	addEdge(q, which_succ, this, prev.length-1);
    }
    /** Remove an attachment from this FOOTER. */
    public void remove(int which_pred) {
      int j;
      for (j = which_pred+1; j < prev.length; j++) {
	prev[j].to_index--;
      }
      prev = (Edge[]) Util.shrink(prev, which_pred);
    }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) { }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) { }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this Quad. */
    public String toString() { return "FOOTER("+prev.length+")"; }
}
