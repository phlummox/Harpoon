// QuadFactory.java, created Sun Dec 13 02:13:47 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

/**
 * A <code>QuadFactory</code> is responsible for assigning unique numbers
 * to the <code>Quad</code>s in a method, and for maintaining some
 * method-wide information (such as a pointer to the parent 
 * <code>HCode</code>).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFactory.java,v 1.1.2.9 1999-08-04 05:52:29 cananian Exp $
 */
public abstract class QuadFactory  {
    /** Returns the <code>TempFactory</code> to use for <code>Temp</code>
     *  arguments to <code>Quad</code>s generated by this factory. */
    public abstract TempFactory tempFactory();
    /** Returns the <code>HCode</code> to which all <Code>Quad</code>s
     *  generated by this <code>QuadFactory</code> belong. */
    public abstract Code getParent();
    /** Returns the <code>HMethod</code> for which all <code>Quad</code>s
     *  correspond. */
    public HMethod getMethod() { return getParent().getMethod(); }
    /** Returns a unique number for a <code>Quad</code> within
     *  this <code>QuadFactory</code>. */
    public abstract int getUniqueID();
    /** Returns a human-readable representation for this 
     *  <code>QuadFactory</code>. */
    public String toString() {
	return "QuadFactory["+getParent().toString()+"]";
    }
}
