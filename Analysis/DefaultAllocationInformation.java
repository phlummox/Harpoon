// DefaultAllocationInformation.java, created Mon Apr  3 18:46:15 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>DefaultAllocationInformation</code> returns a simple
 * no-analysis <code>AllocationInformation</code> structure which
 * works for allocation sites in quad form.  The
 * <code>DefaultAllocationInformation</code> will conservatively say
 * that nothing can be stack or thread-locally allocated.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DefaultAllocationInformation.java,v 1.1.2.1 2000-04-03 23:09:17 cananian Exp $
 */
public class DefaultAllocationInformation implements AllocationInformation {
    
    /** Creates a <code>DefaultAllocationInformation</code>. */
    public DefaultAllocationInformation() { }

    /** Return an <code>AllocationProperties</code> object for the given
     *  allocation site.  The allocation site must be either a
     *  <code>harpoon.IR.Quads.NEW</code> or a
     *  <code>harpoon.IR.Quads.ANEW</code>. */
    public AllocationProperties query(HCodeElement allocationSite) {
	if (allocationSite instanceof harpoon.IR.Quads.NEW)
	    return hasInteriorPointers
		(((harpoon.IR.Quads.NEW)allocationSite).hclass());
	if (allocationSite instanceof harpoon.IR.Quads.ANEW)
	    return hasInteriorPointers
		(((harpoon.IR.Quads.ANEW)allocationSite).hclass());
	Util.assert(false, "not a NEW or ANEW quad.");
	return null;
    }
    /** Return an AllocationProperties object matching the allocated object
     *  type specified by the parameter. */
    private AllocationProperties hasInteriorPointers(HClass cls) {
	return _hasInteriorPointers(cls) ?
	    HAS_INTERIOR_POINTERS : NO_INTERIOR_POINTERS;
    }
    /** Return true iff the specified object type has no interior pointers;
     *  that is, iff all its fields are primitive. */
    private boolean _hasInteriorPointers(HClass cls) {
	Util.assert(!cls.isInterface() && !cls.isPrimitive());
	if (cls.isArray()) return !cls.getComponentType().isPrimitive();
	// okay, it's an object.  see if it has any non-primitive fields.
	for (HClass sc=cls; sc!=null; sc=sc.getSuperclass()) {
	    HField[] hf = sc.getDeclaredFields();
	    for (int i=0; i<hf.length; i++)
		if (!hf[i].getType().isPrimitive()) return true;
	}
	return false; // no interior pointers.
    }
	
    /* Statically allocate the two possible allocation properties. */
    private static final AllocationProperties
	HAS_INTERIOR_POINTERS = new MyAP() {
	    public boolean hasInteriorPointers() { return true; }
	},
	NO_INTERIOR_POINTERS = new MyAP() {
	    public boolean hasInteriorPointers() { return false; }
	};
    /* convenience class for code reuse. */
    private static abstract class MyAP implements AllocationProperties {
	public abstract boolean hasInteriorPointers();
	public boolean canBeStackAllocated() { return false; }
	public boolean canBeThreadAllocated() { return false; }
	public Temp allocationHeap() { return null; }
    }
}
