// BitFieldNumbering.java, created Sun Mar  4 20:21:45 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Linker;
import harpoon.Util.Util;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <code>BitFieldNumbering</code> finds a bit-position and a field to
 * embed boolean flags describing object fields.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BitFieldNumbering.java,v 1.6 2004-07-02 00:08:52 cananian Exp $
 */
public class BitFieldNumbering {
    // field is 'pointer size': i.e. Int of 32-bit arch, Long on 64-bit arch.
    public final HClass FIELD_TYPE;
    public final int BITS_IN_FIELD;

    // unique suffix for the fields created by this BitFieldNumbering.
    private final String suffix;
    // cache HClass for java.lang.Object
    private final HClass HCobject;
    // set of all referenced 'bitfield' fields: mutable version.
    private final Set<HField> _bitfields = new HashSet<HField>();
    /** Set of all fields returned as part of a <code>BitFieldTuple</code>
     *  by <code>bfLoc</code> or <code>arrayBitField</code>. */
    public final Set<HField> bitfields =
	Collections.unmodifiableSet(_bitfields);
    /** Mutable set of fields you want to ignore. */
    public final Set<HField> ignoredFields = new HashSet<HField>();

    /** Creates a <code>BitFieldNumbering</code>. */
    public BitFieldNumbering(Linker l, boolean pointersAreLong) {
	this(l, pointersAreLong, "");
    }
    public BitFieldNumbering(Linker l, boolean pointersAreLong, String suffix){
	this.FIELD_TYPE = pointersAreLong ? HClass.Long : HClass.Int;
	this.BITS_IN_FIELD = pointersAreLong ? 64 : 32;
	this.suffix=suffix;
	this.HCobject = l.forName("java.lang.Object");
    }

    public static class BitFieldTuple {
	public final HField field;
	public final int bit;
	BitFieldTuple(HField field, int bit) {this.field=field; this.bit=bit;}
	public String toString() { return "Bit "+bit+" of "+field; }
    }
    public BitFieldTuple bfLoc(HField hf) {
	int n = fieldNumber(hf);
	// which class would the check field belong to?
	// answer: same class as contains the definition of field #(marker)
	int marker = BITS_IN_FIELD * (n/BITS_IN_FIELD);
	HClass hc = hf.getDeclaringClass();
	while (classNumber(hc.getSuperclass()) > marker)
	    hc = hc.getSuperclass();
	// okay, fetch this field, creating if necessary.
	HField bff = getOrMake(hc, n/BITS_IN_FIELD);
	// done.
	return new BitFieldTuple(bff, n % BITS_IN_FIELD);
    }

    // fetch a bitfield, creating if necessary.
    private HField getOrMake(HClass where, int which) {
	/* for safety: always call classNumber(where) to cache the field
	 * numbering for 'where' *before* we screw it up by adding fields. */
	classNumber(where);
	/* okay, now fetch/make the bitfield field. */
	String fieldname="$$bitfield"+which+suffix;
	try {
	    return where.getDeclaredField(fieldname);
	} catch (NoSuchFieldError nsfe) {
	    HField hf =
		where.getMutator().addDeclaredField(fieldname, FIELD_TYPE);
	    _bitfields.add(hf);
	    return hf;
	}
    }
    // field numbering.
    final Map<HField,Integer> fieldNumbers = new HashMap<HField,Integer>();
    final Map<HClass,Integer> classNumbers = new HashMap<HClass,Integer>();
    private int fieldNumber(HField hf) {
	assert !hf.isStatic();
	assert !hf.getDeclaringClass().isInterface();
	assert !ignoredFields.contains(hf);
	if (!fieldNumbers.containsKey(hf))
	    classNumber(hf.getDeclaringClass());
	assert fieldNumbers.containsKey(hf) : hf + " / "+fieldNumbers;
	return fieldNumbers.get(hf).intValue();
    }
    /* all fields in 'hc' are numbered *strictly less than* classNumber(hc) */
    private int classNumber(HClass hc) {
	assert !hc.isArray();
	assert !hc.isInterface();
	if (!classNumbers.containsKey(hc)) {
	    HClass sc = hc.getSuperclass();
	    int start = (sc==null) ? 0 : classNumber(sc);
	    HField[] hfa = hc.getDeclaredFields();
	    for (int i=0; i<hfa.length; i++)
		if (! (hfa[i].isStatic() || ignoredFields.contains(hfa[i])))
		    fieldNumbers.put(hfa[i], new Integer(start++));
	    classNumbers.put(hc, new Integer(start));
	}
	return classNumbers.get(hc).intValue();
    }
}
