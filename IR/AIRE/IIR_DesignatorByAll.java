// IIR_DesignatorByAll.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_DesignatorByAll</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DesignatorByAll.java,v 1.4 1998-10-11 02:37:15 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DesignatorByAll extends IIR_Designator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_DESIGNATOR_BY_ALL).
     * @return <code>IR_Kind.IR_DESIGNATOR_BY_ALL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_DESIGNATOR_BY_ALL; }
    //CONSTRUCTOR:
    public IIR_DesignatorByAll() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

