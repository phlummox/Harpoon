// ObjectBuilder.java, created Mon Oct 11 18:56:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Backend.Generic.Runtime.ObjectBuilder.Info;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ArrayInfo;
import harpoon.Backend.Generic.Runtime.ObjectBuilder.ObjectInfo;
import harpoon.Backend.Maps.FieldMap;
import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.Temp.Label;
import harpoon.Util.ArrayIterator;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
/**
 * <code>ObjectBuilder</code> is an implementation of
 * <code>harpoon.Backend.Generic.Runtime.ObjectBuilder</code> for the
 * <code>Runtime1</code> runtime.
 * <p>
 * To accomodate transformations which add fields to
 * <code>java.lang.Object</code>, this
 * <code>Runtime.ObjectBuilder</code> initializes all fields of
 * <code>Object</code> with <code>null</code>, rather than attempting
 * to consult the given <code>Info</code> for them.  If other behavior
 * is eventually needed, it is a custom <code>RootOracle</code> be
 * defined and provided to the constructor which will be consulted on
 * the value of every field *before* any <code>Info</code>.
 * This provides for extensibility without direct code modifications
 * to the various parts of <code>Runtime1</code> which use this
 * <code>ObjectBuilder</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectBuilder.java,v 1.1.4.15 2001-07-10 22:49:50 cananian Exp $
 */
public class ObjectBuilder
    extends harpoon.Backend.Generic.Runtime.ObjectBuilder {
    final Runtime runtime;
    final boolean pointersAreLong;
    final RootOracle ro;
    private final Random rnd;
    private final HClass HCobject; // cache HClass for java.lang.Object

    /** Creates a <code>ObjectBuilder</code> with a <code>RootOracle</code>
     *  which supplies <code>null</code> values to any field of
     *  <code>java.lang.Object</code> (which there usually aren't any of).
     */
    public ObjectBuilder(Runtime runtime) {
	this(runtime, new RootOracle() {
	    public Object get(HField hf, Info addlinfo) {
	      if (hf.getDeclaringClass().getName().equals("java.lang.Object"))
		  return defaultValue(hf);// fields of Object initialized to 0
	      else return NOT_A_VALUE;
	    }
	});
    }
    /** Creates a <code>ObjectBuilder</code>. */
    public ObjectBuilder(Runtime runtime, RootOracle ro) {
	this.runtime = runtime;
	this.pointersAreLong = runtime.frame.pointersAreLong();
	this.ro = ro;
	this.rnd = new Random(runtime.frame.hashCode());//quasi-repeatable?
	this.HCobject = runtime.frame.getLinker().forName("java.lang.Object");
    }

    public Stm buildObject(TreeFactory tf, ObjectInfo info,
			   boolean exported) {
	FieldMap cfm = ((TreeBuilder) runtime.getTreeBuilder()).cfm;
	Util.assert(!info.type().isArray());
	Util.assert(!info.type().isPrimitive());
	List stmlist = new ArrayList();
	// header
	stmlist.add(makeHeader(tf, info, exported));
	// fields, in field order
	List l = cfm.fieldList(info.type());
	for (Iterator it = l.iterator(); it.hasNext(); )
	    stmlist.add(makeDatum(tf, lookup(info, (HField)it.next())));
	// done -- ta da!
	return Stm.toStm(stmlist);
    }
    private Object lookup(ObjectInfo info, HField hf) {
	Object o = ro.get(hf, info);
	return (o==ro.NOT_A_VALUE) ? info.get(hf) : o;
    }
    public Stm buildArray(TreeFactory tf, ArrayInfo info,
			  boolean exported) {
	FieldMap cfm = ((TreeBuilder) runtime.getTreeBuilder()).cfm;
	Util.assert(info.type().isArray());
	HClass cType = info.type().getComponentType();
	List stmlist = new ArrayList(info.length()+2);
	// header
	stmlist.add(makeHeader(tf, info, exported));
	// fields of java.lang.Object, in proper field order
	List l = cfm.fieldList(HCobject);
	for (Iterator it = l.iterator(); it.hasNext(); )
	    stmlist.add(makeDatum(tf, lookup(info, (HField)it.next())));
	// length
	stmlist.add(_DATUM(tf, new CONST(tf, null, info.length())));
	// data
	for (int i=0; i<info.length(); i++)
	    stmlist.add(makeDatum(tf, info.get(i)));
	// done -- ta da!
	return Stm.toStm(stmlist);
    }
    private Object lookup(ArrayInfo info, HField hf) {
	Object o = ro.get(hf, info);
	Util.assert(o != ro.NOT_A_VALUE, "field of array not given a value");
	return o;
    }
    Stm makeHeader(TreeFactory tf, Info info, boolean exported)
    {
	List stmlist = new ArrayList(4);
	// align to word boundary.
	stmlist.add(new ALIGN(tf, null, 4));
	// label:
	stmlist.add(new LABEL(tf, null, info.label(), exported));
	// claz pointer
	stmlist.add(_DATUM(tf, runtime.getNameMap().label(info.type())));
	// hash code.
	// this is of pointer size, and must have the low bit set.  we *could*
	// emit a symbolic reference to info.label()+1 or some such, but
	// this would complicate the pattern-matching instruction selector.
	// so instead we'll just select a random number of the right length
	// and set the low bit.
	stmlist.add(makeDatum(tf, pointersAreLong ?
			      (Number) new Long(1 | rnd.nextLong()) :
			      (Number) new Integer(1 | rnd.nextInt())));
	// okay, done with header.
	return Stm.toStm(stmlist);
    }
    Stm makeDatum(TreeFactory tf, Object datum) {
	if (datum==null)
	    return _DATUM(tf, new CONST(tf, null)); // null constant.
	else if (datum instanceof Integer)
	    return _DATUM(tf, new CONST(tf, null,
				   ((Integer)datum).intValue()));
	else if (datum instanceof Long)
	    return _DATUM(tf, new CONST(tf, null,
				   ((Long)datum).longValue()));
	else if (datum instanceof Float)
	    return _DATUM(tf, new CONST(tf, null,
				   ((Float)datum).floatValue()));
	else if (datum instanceof Double)
	    return _DATUM(tf, new CONST(tf, null,
				   ((Double)datum).doubleValue()));
	else if (datum instanceof Boolean)
	    return _DATUM(tf, new CONST(tf, null, 8, false,
				   ((Boolean)datum).booleanValue()?1:0));
	else if (datum instanceof Byte)
	    return _DATUM(tf, new CONST(tf, null, 8, true,
				   ((Byte)datum).intValue()));
	else if (datum instanceof Short)
	    return _DATUM(tf, new CONST(tf, null,16, true,
				   ((Short)datum).intValue()));
	else if (datum instanceof Character)
	    return _DATUM(tf, new CONST(tf, null,16, false,
				   ((Character)datum).charValue()));
	else if (datum instanceof Info)
	    return _DATUM(tf, ((Info)datum).label());
	else throw new Error("ILLEGAL DATUM TYPE");
    }
    DATUM _DATUM(TreeFactory tf, Exp e) { 
	return new DATUM(tf, null, e); 
    }
    DATUM _DATUM(TreeFactory tf, Label l) {
	return new DATUM(tf,null,new NAME(tf,null,l));
    }

    /** utility function copied from Interpret/Quads/Ref.java */
    static final Object defaultValue(HField f) {
	if (f.isConstant()) return f.getConstant();
	return defaultValue(f.getType());
    }
    static final Object defaultValue(HClass ty) {
	if (!ty.isPrimitive()) return null;
	if (ty == HClass.Boolean) return new Boolean(false);
	if (ty == HClass.Byte) return new Byte((byte)0);
	if (ty == HClass.Char) return new Character((char)0);
	if (ty == HClass.Double) return new Double(0);
	if (ty == HClass.Float) return new Float(0);
	if (ty == HClass.Int) return new Integer(0);
	if (ty == HClass.Long) return new Long(0);
	if (ty == HClass.Short) return new Short((short)0);
	throw new Error("Ack!  What kinda default value is this?!");
    }


    /** A <code>RootOracle</code> allows a transformation to add
     *  fields to <code>java.lang.Object</code> (or any other
     *  class, really) and provide the initial values for that
     *  field in the various runtime constant objects without
     *  having to directly extend every <code>ObjectInfo</code>
     *  used in this <code>Generic.Runtime</code> implementation.
     *  The <code>RootOracle</code> will be consulted for the
     *  value of every field *before* the <code>ObjectInfo</code>,
     *  allowing an override -- if no override is desired, the
     *  <code>RootOracle.get()</code> method should return
     *  <code>NOT_A_VALUE</code>.
     */
    public static abstract class RootOracle {
	/** This is the constant value returned which this
	 *  oracle doesn't wish to override the given field. */
	public static final Object NOT_A_VALUE = new Object();
	/** Returns the override value of the given field <code>hf</code>
	 *  (more information about the object in question is
	 *   provided by <code>addlinfo</code>); if this
	 *   <code>RootOracle</code> doesn't wish to override this
	 *   field, it should return the constant <code>NOT_A_VALUE</code>.
	 */
	public abstract Object get(HField hf, Info addlinfo);
    }
}