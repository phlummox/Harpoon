// PointerAnalysis.java, created Sat Jan  8 23:22:24 2000 by salcianu
// Copyright (C) 2000 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Collections;
import java.util.Arrays;
import java.util.Comparator;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HCode;


import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Util.LightBasicBlocks.LightBasicBlock;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;

import harpoon.Temp.Temp;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.TYPECAST;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaAllCallers;

import harpoon.Util.TypeInference.CachingArrayInfo;

import harpoon.Util.LightBasicBlocks.LBBConverter;
import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;
import harpoon.Util.UComp;
import harpoon.Util.FilterIterator;

import harpoon.Util.Util;

/**
 * <code>SyncElimination</code> implements synchronization elimination
 * based on the results of pointer analysis.
 * 
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: SyncElimination.java,v 1.1.2.1 2000-07-03 02:27:09 jwhaley Exp $
 */
public class SyncElimination {

    private PointerAnalysis pa;
    private Set necessarySyncs;	    // PASyncs that are necessary.
    private Set necessaryQuads;	    // Monitorenter/exit quads that are necessary.
    private Relation callpath2syncops;	    // Relation from call paths to the sync ops we should specialize on.
    private Relation method2speccall;	    // Relation from specialized methods to the specialized call.
    
    public SyncElimination(PointerAnalysis pa) {
	this.pa = pa;
	this.necessarySyncs = new HashSet();
    }
    
    public void addRoot(MetaMethod mm) {

	System.out.println("Adding root method "+mm);

	final ParIntGraph pig = pa.threadInteraction(mm);
	final ActionRepository ar = pig.ar;
	// add to the set of necessary sync ops.
	// a sync op is necessary if it is executed in parallel with another
	// thread that has a sync op on the same node.
	ParActionVisitor par_act_visitor = new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    PANode n = sync.n;
		    PANode nt = sync.nt;
		    // Sync on node n is performed by nt in || with nt2.
		    // If nt2 has syncs on node n, this sync is necessary.
		    Iterator i = ar.syncsOn(n, nt2);
		    if (!i.hasNext()) return;
		    necessarySyncs.add(sync);
		    // Also add all syncs by nt2 on node n as necessary.
		    do {
			necessarySyncs.add(i.next());
		    } while (i.hasNext());
		}
	    };
	ar.forAllParActions(par_act_visitor);

    }
    
    public HMethod[] calculate() {

	System.out.println("Necessary sync ops:");
	System.out.println(necessarySyncs);
	
	necessaryQuads = new HashSet();
	
	if (false) { // not yet working.
	    // in the case of specialization:
	    // generate specialized sync versions of methods
	    // change calls to refer to specialized methods
	    // eliminate all other syncs
	    callpath2syncops = new LightRelation();
	    Iterator i = necessarySyncs.iterator();
	    while (i.hasNext()) {
		PASync s = (PASync)i.next();
		System.out.println("Looking at necessary sync : "+s);
		ListCell lc = s.call_path;
		System.out.println("Specialization depth : "+s.depth);
		if (s.depth == 0) {
		    System.out.println("Adding as a necessary quad : "+s.hce);
		    necessaryQuads.add(s.hce);
		} else {
		    // add this call path to a multimap from call paths
		    // to necessary sync ops.
		    CallPath cp = new CallPath(lc);
		    callpath2syncops.add(cp, s);
		}
	    }

	    int specialization_path_number = 1;
	    // generate specialized versions of all of the call paths in
	    // the multimap.
	    Iterator e = callpath2syncops.keys().iterator();
	    while (e.hasNext()) {
		CallPath cp = (CallPath)e.next();
		Iterator calliter = cp.iterator();
		int count = 1;
		while (calliter.hasNext()) {
		    CALL cq = (CALL)calliter.next();
		    // need a way to go from a CALL to the HMethod that contains that CALL.
		    HMethod caller; // = cq.getContainingMethod();
		    HClass hc = caller.getDeclaringClass();
		    HClassMutator mut = hc.getMutator();
		    String newMethodName = caller.getName()+"__spec"+specialization_path_number+"_"+i;
		    HMethod specmethod = mut.addDeclaredMethod(newMethodName,
							       caller.getParameterTypes(),
							       caller.getReturnType());
		    // ...
		}
	    }
	    
	    return new HMethod[0];
	    
	} else {
	    // no specialization
	    FilterIterator.Filter f =
	        new FilterIterator.Filter() {
	    	public Object map(Object o) {
	    	    return ((PASync)o).hce;
	    	}
	        };
	    Iterator i = new FilterIterator(necessarySyncs.iterator(), f);
	    while (i.hasNext()) necessaryQuads.add(i.next());
	    return new HMethod[0];
	}
    }
    
    static class CallPath {
	ListCell lc;
	
	CallPath(ListCell lc) {
	    this.lc = lc;
	}
	public Iterator iterator() {
	    final ListCell lc_top = lc;
	    return new Iterator() {
		ListCell p = lc_top;
		public boolean hasNext() {
		    return p == null;
		}
		public Object next() {
		    Object o = p; //lc.info;
		    p = p.next;
		    return o;
		}
		public void remove() { }
	    };
	}
	public int hashCode() {
	    ListCell p = lc; int hash = 0;
	    while (p != null) {
		hash ^= p.info.hashCode();
		p = p.next;
	    }
	    return hash;
	}
	public boolean equals(Object o) {
	    if (o instanceof CallPath) return equals((CallPath)o);
	    return false;
	}
	public boolean equals(CallPath that) {
	    return ListCell.identical(this.lc, that.lc);
	}
	public String toString() {
	    StringBuffer buffer = new StringBuffer();
	    for(ListCell p = lc; p != null; p = p.next)
	        buffer.append(" <- " + (CALL) p.info + " - ");
	    return buffer.toString();
	}
    };
    
    private HCode transform(HCode hcode) {
    	// Now we can remove all sync ops that are not in the necessary set.
	Iterator it2 = hcode.getElementsI();
	// I wish this iterator were modifiable.
	while (it2.hasNext()) {
	    Quad q = (Quad)it2.next();
	    if (q instanceof MONITORENTER ||
	        q instanceof MONITOREXIT) {
	        if (!necessaryQuads.contains(q)) {
	    	    System.out.println("Eliminating unnecessary sync: "+q);
	    	    NOP nq = new NOP(q.getFactory(), q);
	    	    Quad.replace(q, nq);
	        } else {
	    	    System.out.println("Leaving necessary sync: "+q);
	        }
	    }
	}
	return hcode;
    }
    
    /** Returns a <code>HCodeFactory</code> that uses <code>SyncElimination</code>. */
    public static HCodeFactory codeFactory(final HCodeFactory parent, final SyncElimination se) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		if (hc!=null) {
		    System.out.println("Transforming method "+m);
		    return se.transform(hc);
		} else
		    return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

}
