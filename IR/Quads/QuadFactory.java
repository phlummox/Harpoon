// QuadFactory.java, created Sun Dec 13 02:13:47 1998 by cananian
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

/**
 * A <code>QuadFactory</code> is responsible for assigning unique numbers
 * to the <code>Quad</code>s in a method, and for maintaining some
 * method-wide information (such as a pointer to the parent 
 * <code>HCode</code>).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadFactory.java,v 1.1.2.5 1999-01-03 03:01:42 cananian Exp $
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
    /** Returns a unique number for this <code>Quad</code> within
     *  this <code>QuadFactory</code>. */
    abstract int getUniqueID();
    /** Returns a human-readable representation for this 
     *  <code>QuadFactory</code>. */
    public String toString() {
	return "QuadFactory["+getParent().toString()+"]";
    }

    //---------------------------------------------------------------------
    // CONSTRUCTORS FOR QUADS:
    /*
    public AGET newAGET(HCodeElement source,
			Temp dst, Temp objectref, Temp index) {
	return new AGET(this, source, dst, objectref, index);
    }
    public ALENGTH newALENGTH(HCodeElement source,
			      Temp dst, Temp objectref) {
	return new ALENGTH(this, source, dst, objectref);
    }
    public ANEW newANEW(HCodeElement source,
			Temp dst, HClass hclass, Temp dims[]) {
	return new ANEW(this, source, dst, hclass, dims);
    }
    public ARRAYINIT newARRAYINIT(HCodeElement source,
				  Temp objectref, int offset,
				  HClass type, Object[] value){
	return new ARRAYINIT(this, source, objectref, offset, type, value);
    }
    public ASET newASET(HCodeElement source,
			Temp objectref, Temp index, Temp src) {
	return new ASET(this, source, objectref, index, src);
    }
    public CALL newCALL(HCodeElement source,
			HMethod method, Temp[] params, Temp retval, Temp retex,
			boolean isVirtual) {
	return new CALL(this, source, method, params, retval,retex, isVirtual);
    }
    public CJMP newCJMP(HCodeElement source,
			Temp test, Temp dst[][], Temp src[]) {
	return new CJMP(this, source, test, dst, src); 
    }
    public CJMP newCJMP(HCodeElement source, Temp test, Temp src[]) {
	return new CJMP(this, source, test, src);
    }
    public COMPONENTOF newCOMPONENTOF(HCodeElement source,
				      Temp dst, Temp arrayref, Temp objectref){
	return new COMPONENTOF(this, source, dst, arrayref, objectref);
    }
    public CONST newCONST(HCodeElement source,
			  Temp dst, Object value, HClass type) {
	return new CONST(this, source, dst, value, type);
    }
    public DEBUG newDEBUG(HCodeElement source, String str) {
	return new DEBUG(this, source, str);
    }
    public FOOTER newFOOTER(HCodeElement source, int arity) {
	return new FOOTER(this, source, arity);
    }
    public GET newGET(HCodeElement source,
		      Temp dst, HField field, Temp objectref) {
	return new GET(this, source, dst, field, objectref);
    }
    public HEADER newHEADER(HCodeElement source) { 
	return new HEADER(this, source);
    }
    public INSTANCEOF newINSTANCEOF(HCodeElement source,
				    Temp dst, Temp src, HClass hclass) {
	return new INSTANCEOF(this, source, dst, src, hclass);
    }
    public LABEL newLABEL(HCodeElement source, String label,
			  Temp dst[], Temp src[][], int arity) {
	return new LABEL(this, source, label, dst, src, arity);
    }
    public LABEL newLABEL(HCodeElement source, String label,
			  Temp dst[], int arity) {
	return new LABEL(this, source, label, dst, arity);
    }
    public LABEL newLABEL(PHI phi, String label) {
	return new LABEL(this, phi, label);
    }
    public HANDLER newHANDLER(HCodeElement source, 
			      Temp exceptionTemp, HClass caughtException,
			      HANDLER.ProtectedSet protectedSet) {
	return new HANDLER(this, source,
			   exceptionTemp, caughtException, protectedSet);
    }
    public METHOD newMETHOD(HCodeElement source, Temp[] params, int arity) {
	return new METHOD(this, source, params, arity);
    }
    public MONITORENTER newMONITORENTER(HCodeElement source, Temp lock) {
	return new MONITORENTER(this, source, lock);
    }
    public MONITOREXIT newMONITOREXIT(HCodeElement source, Temp lock) {
	return new MONITOREXIT(this, source, lock);
    }
    public MOVE newMOVE(HCodeElement source, Temp dst, Temp src) {
	return new MOVE(this, source, dst, src);
    }
    public NEW newNEW(HCodeElement source, Temp dst, HClass hclass) {
	return new NEW(this, source, dst, hclass);
    }
    public NOP newNOP(HCodeElement source) {
	return new NOP(this, source);
    }
    public OPER newOPER(HCodeElement source, int opcode,
			Temp dst, Temp[] operands) {
	return new OPER(this, source, opcode, dst, operands);
    }
    public PHI newPHI(HCodeElement source, Temp dst[],Temp src[][],int arity) {
	return new PHI(this, source, dst, src, arity);
    }
    public PHI newPHI(HCodeElement source, Temp dst[], int arity) {
	return new PHI(this, source, dst, arity);
    }
    public RETURN newRETURN(HCodeElement source, Temp retval) {
	return new RETURN(this, source, retval);
    }
    public SET newSET(HCodeElement source,
		      HField field, Temp objectref, Temp src) {
	return new SET(this, source, field, objectref, src);
    }
    public SWITCH newSWITCH(HCodeElement source,
			    Temp index, int keys[], Temp dst[][], Temp src[]){
	return new SWITCH(this, source, index, keys, dst, src);
    }
    public SWITCH newSWITCH(HCodeElement source,
			    Temp index, int keys[], Temp src[]) {
	return new SWITCH(this, source, index, keys, src);
    }
    public THROW newTHROW(HCodeElement source, Temp throwable) {
	return new THROW(this, source, throwable);
    }
    public TYPECAST newTYPECAST(HCodeElement source,
				Temp objectref, HClass hclass) {
	return new TYPECAST(this, source, objectref, hclass);
    }
    */
}
