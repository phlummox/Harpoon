// IIR_CaseStatementAlternative.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternative</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternative.java,v 1.2 1998-10-10 09:58:34 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_CaseStatementAlternative extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = 
    
    
    //METHODS:  
    //MEMBERS:  
    IIR_SequentialStatementList sequence_of_statements;

// PROTECTED:
} // END class

