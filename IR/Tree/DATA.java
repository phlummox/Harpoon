package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>DATA</code> objects are statements which write a value to memory
 * at the time when a program is loaded.  The location written is 
 * calculated using this formula:
 *
 * <PRE>
 *         location = base + offset
 * 
 * where 
 *         base   = location of nearest LABEL, l,  which precedes this DATA
 *         offset = the total size of all instructions between l and this DATA
 *                      
 * </PRE>
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: DATA.java,v 1.1.2.3 1999-08-03 21:12:57 duncan Exp $
 */
public class DATA extends Stm { 
    /** The expression to write to memory.  If null, this location in 
     *  memory is reserved, with an unspecified value. */
    public final Exp data;
    
    /** Class constructor. 
     *  Reserves this location in memory without assigning it a value. 
     */
    public DATA(TreeFactory tf, HCodeElement source) { 
	super(tf, source);
	this.data = null;
    }

    /** Class constructor. 
     *  The parameter <br><code>data</code> must be an instance of either
     *  <code>harpoon.IR.Tree.CONST</code> or 
     *  <code>harpoon.IR.Tree.NAME</code>.
     */
    public DATA(TreeFactory tf, HCodeElement source, Exp data) {
	super(tf, source);
	this.data = data; 
	Util.assert(data.kind()==TreeKind.CONST || data.kind()==TreeKind.NAME);
	Util.assert(tf == data.tf, "Dest and Src must have same tree factory");
    }

    public ExpList kids() { return new ExpList(data, null); } 
    
    public int kind() { return TreeKind.DATA; } 

    public Stm build(ExpList kids) { return build(tf, kids); }

    public Stm build(TreeFactory tf, ExpList kids) { 
	Util.assert(kids.head == null || tf == kids.head.tf);
	return new DATA(tf, this, kids.head);
    }

    /** Implements the typed interface.  This throws an error if 
     *  this <code>DATA</code> object reserves memory with an 
     *  unspecifid value. */
    public int type() { 
	if (data==null) 
	    throw new Error("Type of this DATA object is not specified");
	else return data.type(); 
    } 

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); } 

    public Tree rename(TreeFactory tf, CloningTempMap ctm) { 
	return new DATA(tf, this, (Exp)data.rename(tf, ctm));
    }    

    public String toString() { 
	StringBuffer sb = new StringBuffer("DATA<");
	sb.append(data==null?"Unspecified type":(Type.toString(type())));
	sb.append(">(#"); sb.append(getID()); sb.append(")");
	return sb.toString();
    }
}
