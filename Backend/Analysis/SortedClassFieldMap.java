// SortedClassFieldMap.java, created Wed Jul 11 12:22:16 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * A <code>SortedClassFieldMap</code> is an extension of
 * <code>ClassFieldMap</code> which sorts object fields to
 * minimize "holes" between fields.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SortedClassFieldMap.java,v 1.2 2002-02-25 21:00:47 cananian Exp $
 */
public abstract class SortedClassFieldMap extends ClassFieldMap {
    
    /** Creates a <code>SortedClassFieldMap</code>. */
    public SortedClassFieldMap() { }

    private final Map cache = new HashMap();
    protected HField[] declaredFields(HClass hc) {
	if (!cache.containsKey(hc)) {
	    // determine the alignment of the last field of the superclass.
	    int alignment=0;
	    HClass sc = hc.getSuperclass();
	    if (sc!=null) {
		List l = fieldList(sc);
		if (l.size()>0) {
		    HField lastfield = (HField) l.get(l.size()-1);
		    alignment = fieldOffset(lastfield)+fieldSize(lastfield);
		}
	    }
	    // make list of non-static fields.
	    List l = new ArrayList(Arrays.asList(hc.getDeclaredFields()));
	    for (Iterator it=l.iterator(); it.hasNext(); )
		if (((HField)it.next()).isStatic())
		    it.remove();
	    // sort declared fields by max(alignment,size)
	    Collections.sort(l, new Comparator() {
		public int compare(Object o1, Object o2) {
		    HField hf1=(HField)o1, hf2=(HField)o2;
		    return Math.max(fieldSize(hf1),fieldAlignment(hf1))
			- Math.max(fieldSize(hf2),fieldAlignment(hf2));
		}
	    });
	    // if parent is unaligned, start at small end; else start at big
	    // end.
	    if (l.size()>0) {
		HField big = (HField) l.get(l.size()-1);
		if ((alignment % Math.max(fieldSize(big),fieldAlignment(big)))
		    ==0)
		    Collections.reverse(l);
	    }
	    // done.
	    cache.put(hc, l.toArray(new HField[l.size()]));
	}
	return (HField[]) cache.get(hc);
    }
}
