// IIR_Tuple.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_Tuple</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Tuple.java,v 1.2 1998-10-10 09:58:35 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_Tuple extends IIR
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

