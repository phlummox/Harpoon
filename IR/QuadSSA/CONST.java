// CONST.java, created Mon Aug 24 16:46:52 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>CONST</code> objects represent an assignment of a constant value
 * to a compiler temporary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CONST.java,v 1.4 1998-09-03 06:42:25 cananian Exp $
 */

public class CONST extends Quad {
    public Temp dst;
    public Object value;
    public HClass type;
    /** Creates a <code>CONST</code> from a destination temporary, and object
     *  value and its class type. */
    public CONST(String sourcefile, int linenumber,
		 Temp dst, Object value, HClass type) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.value = value;
	this.type = type;
    }
    CONST(HCodeElement hce, Temp dst, Object value, HClass type) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, value, type);
    }
    /** Returns the Temp defined by this Quad.
     * @return The <code>dst</code> field.
     */
    public Temp[] def() { return new Temp[] { dst }; }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString());
	sb.append(" = CONST ");
	if (type == HClass.forName("java.lang.String"))
	    sb.append("(String)\""+Util.escape(value.toString())+"\"");
	else if (type == HClass.Void && value==null)
	    sb.append("null");
	else
	    sb.append("("+type.getName()+")"+value.toString());
	return sb.toString();
    }
}
