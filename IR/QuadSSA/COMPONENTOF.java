// COMPONENTOF.java, created Wed Sep  9 13:05:33 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
/**
 * <code>COMPONENTOF</code> objects implement the test needed to determine
 * if an <code>ASET</code> needs to throw an exception.  Specifically,
 * <code>COMPONENTOF</code> evaluates to boolean <code>true</code> if
 * a certain temporary is a instance of the component type of a certain
 * array, or boolean <code>false</code> otherwise.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: COMPONENTOF.java,v 1.7 1998-10-11 02:37:56 cananian Exp $
 * @see ASET
 * @see "The Java Virtual Machine Specification"
 */

public class COMPONENTOF extends Quad {
    /** The temp in which to store the result of the evaluation. */
    public Temp dst;
    /** The array object to test. */
    public Temp arrayref;
    /** The compoment object to test. */
    public Temp objectref;

    /** Creates a <code>COMPONENTOF</code>. */
    public COMPONENTOF(HCodeElement source, 
		       Temp dst, Temp arrayref, Temp objectref) {
	super(source);
	this.dst = dst;
	this.arrayref = arrayref;
	this.objectref = objectref;
    }
    
    /** Returns the <code>Temp</code>s used by this quad. */
    public Temp[] use() { return new Temp[] { arrayref, objectref }; }
    /** Returns the <code>Temp</code>s defined by this quad. */
    public Temp[] def() { return new Temp[] { dst }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	arrayref = tm.tempMap(arrayref);
	objectref = tm.tempMap(objectref);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return dst.toString() + " = " +
	    objectref.toString() + " COMPONENTOF " + arrayref.toString();
    }
}
