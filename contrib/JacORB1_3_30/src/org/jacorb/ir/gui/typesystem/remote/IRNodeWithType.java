package org.jacorb.ir.gui.typesystem.remote;


import org.omg.CORBA.*;
import org.jacorb.ir.gui.typesystem.*;

/**
 * This class was generated by a SmartGuide.
 * 
 */
public class IRNodeWithType extends IRNode implements TypeAssociator{
	private String associatedType;
	private TypeSystemNode associatedTypeNode = null;



/**
 * IRNodeWithType constructor comment.
 */
protected IRNodeWithType() {
	super();
}
/**
 * IRNodeWithType constructor comment.
 * @param irObject org.omg.CORBA.IRObject
 */
protected IRNodeWithType(org.omg.CORBA.IRObject irObject) {
	super(irObject);
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String description() {
	String result = super.description();
	if (getAssociatedType()!=null) {
		result = result + "\nType:\t" + getAssociatedType();
	}	
	return result;	
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String getAssociatedType ( ) {
	if (getAssociatedTypeSystemNode()!=null) {
		return getAssociatedTypeSystemNode().getAbsoluteName();
	}
	else return "--";
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public TypeSystemNode getAssociatedTypeSystemNode() {
	return associatedTypeNode;
}
/**
 * This method was created by a SmartGuide.
 */
protected void setAssociatedTypeSystemNode(TypeSystemNode typeNode) {
	this.associatedTypeNode = typeNode;
}
}










