// CONST.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>CONST</code> objects are expressions which stand for a constant
 * value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONST.java,v 1.1.2.1 1999-01-14 05:54:58 cananian Exp $
 */
public abstract class CONST extends Exp {
    /** Return the constant value of this <code>CONST</code> expression. */
    public abstract Number value();

    public ExpList kids() {return null;}
    public Exp build(ExpList kids) {return this;}
}

