// CJUMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Util.Util;

/**
 * <code>CJUMP</code> objects are statements which stand for conditional
 * branches.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CJUMP.java,v 1.1.2.11 1999-08-03 21:12:57 duncan Exp $
 */
public class CJUMP extends Stm {
    /** An expression that evaluates into a boolean result. */
    public Exp test;
    /** The label to jump to if <code>test</code> is <code>true</code>. */
    public Label iftrue;
    /** The label to jump to if <code>test</code> is <code>false</code>. */
    public Label iffalse;
    /** Constructor. */
    public CJUMP(TreeFactory tf, HCodeElement source,
		 Exp test, Label iftrue, Label iffalse) {
	super(tf, source, 2);
	this.test = test; this.iftrue = iftrue; this.iffalse = iffalse;
	Util.assert(test!=null && iftrue!=null && iffalse!=null);
	Util.assert(tf == test.tf, "This and Test must have same tree factory");
    }
    
    public ExpList kids() {return new ExpList(test, null); }

    public int kind() { return TreeKind.CJUMP; }

    public Stm build(ExpList kids) { return build(tf, kids); }

    public Stm build(TreeFactory tf, ExpList kids) {
	Util.assert(tf == kids.head.tf);
	return new CJUMP(tf, this, kids.head, iftrue, iffalse);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new CJUMP(tf, this, (Exp)test.rename(tf, ctm), iftrue, iffalse);
    }

    public String toString() {
        return "CJUMP(#"+test.getID()+", "+iftrue+", "+iffalse+")";
    }
}

