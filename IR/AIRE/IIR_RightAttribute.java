// IIR_RightAttribute.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_RightAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RightAttribute.java,v 1.4 1998-10-11 02:37:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RightAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_RIGHT_ATTRIBUTE).
     * @return <code>IR_Kind.IR_RIGHT_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_RIGHT_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_RightAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

