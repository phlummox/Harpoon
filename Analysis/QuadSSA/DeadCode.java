// DeadCode2.java, created Thu Oct  8 03:11:37 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Util.Worklist;

import java.util.Enumeration;
import java.util.Hashtable;
/**
 * <code>DeadCode</code> removes dead code 
 * (unused definitions/useless jmps/one-argument phi functions/all moves) from
 * a method.  The analysis is optimistic; that is, it assumes that all code is
 * unused and seeks to prove otherwise.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DeadCode.java,v 1.11.2.1 1998-11-30 21:21:00 cananian Exp $
 */

public abstract class DeadCode  {

    public static void optimize(HCode hc) {
	// Assume everything's useless.
	Set useful = new Set(); // empty set.
	// make a renaming table
	NameMap nm = new NameMap();
	// keep track of defs.
	Hashtable defMap = new Hashtable();
	// we'll have a coupla visitors
	QuadVisitor v;
	
	// make a worklist (which everything's on, at the beginning)
	Worklist W = new Set();
	Quad[] ql = (Quad[]) hc.getElements();
	for (int i=0; i<ql.length; i++) {
	    W.push(ql[i]);
	    // build our defMap while we're at it.
	    Temp d[] = ql[i].def();
	    for (int j=0; j<d.length; j++)
		defMap.put(d[j], ql[i]);
	}
	// ...and a visitor
	v = new UsefulVisitor(W, useful, defMap, nm);
	// look for useful stuff.
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    q.visit(v);
	}

	// remove the useless stuff, including useless cjmps/phis
	for (int i=0; i<ql.length; i++)
		W.push(ql[i]);
	v = new EraserVisitor(W, useful, nm);
	while (!W.isEmpty()) {
	    Quad q = (Quad) W.pull();
	    q.visit(v);
	}

	// Finally, do all the necessary renaming
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    Quad q = (Quad) e.nextElement();
	    q.rename(nm);
	}
    } // end OPTIMIZE METHOD.

    static class EraserVisitor extends QuadVisitor {
	Worklist W;
	Set useful;
	NameMap nm;
	EraserVisitor(Worklist W, Set useful, NameMap nm) {
	    this.W = W; this.useful = useful; this.nm = nm;
	}
	void unlink(Quad q) {
	    Edge before = q.prevEdge(0);
	    Edge after  = q.nextEdge(0);
	    Quad.addEdge((Quad)before.from(), before.which_succ(),
			 (Quad)after.to(), after.which_pred() );
	}
	public void visit(Quad q) {
	    // generally, remove it if it's worthless.
	    if (useful.contains(q)) return; // safe.
	    // removing this statement could make its predecessor useless.
	    if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
	    // unlink with vigor.
	    Util.assert(q.next().length==1 && q.prev().length==1);
	    unlink(q);
	}
	public void visit(PHI q) {
	    // arity-1 phis are useless.
	    if (q.prev().length == 1) {
		// make a pseudo-MOVE for every useful function in this useless phi.
		for (int i=0; i<q.dst.length; i++)
		    if (useful.contains(q.dst[i]))
			for (int j=0; j<q.src[i].length; j++)
			    nm.map(q.dst[i], q.src[i][j]);
		// could make a CJMP useless.
		if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
		// unlink it. (fun for the whole family)
		unlink(q);
	    } else {
		// check for unused functions in the phi.
		for (int i=0; i < q.dst.length; i++) {
		    if (useful.contains(q.dst[i])) continue;
		    // shrink the phi! (secret headhunter's ritual)
		    q.dst = (Temp[])   Util.shrink(Temp.arrayFactory,
						   q.dst, i);
		    q.src = (Temp[][]) Util.shrink(Temp.doubleArrayFactory,
						   q.src, i);
		    // decrement i so we're at the right place still;
		    i--;
		}
	    }
	}
	public void visit(SIGMA q) {
	    // check for unused function in the sigma
	L1:
	    for (int i=0; i < q.dst.length; i++) {
		// if any dst is used, skip.
		for (int j=0; j < q.dst[i].length; j++)
		    if (useful.contains(q.dst[i][j])) continue L1;
		// safe to delete. ERASER MAN appears.
		// shrink the sigma function in our secret laboratory.
		q.dst = (Temp[][]) Util.shrink(Temp.doubleArrayFactory,
					       q.dst, i);
		q.src = (Temp[])   Util.shrink(Temp.arrayFactory,
					       q.src, i);
		// decrement index to keep us steady.
		i--;
	    }
	}
	public void visit(CJMP q) {
	    if (q.next(0)==q.next(1) && matchPS(q, (PHI)q.next(0))) {
		// Mu-ha-ha-ha! KILL THE CJMP!
		// make a pseudo-MOVE for every useful function in this useless sigma.
		for (int i=0; i<q.dst.length; i++)
		    for (int j=0; j<q.dst[i].length; j++)
			if (useful.contains(q.dst[i][j]))
			    nm.map(q.dst[i][j], q.src[i]);
		// removing this might make a preceding CJMP useless.
		if (q.prev(0) instanceof CJMP) W.push(q.prev(0));
		// shrink the phi (and put it on the worklist)
		((PHI)q.next(0)).remove(q.nextEdge(1).which_pred());
		W.push(q.next(0));
		// link out the CJMP
		Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(),
			     q.next(0), q.nextEdge(0).which_pred());
	    } else {
		// just shrink the functions.
		visit((SIGMA)q);
	    }
	}
	// Determine if (useful) cjmp and phi args match.
	boolean matchPS(CJMP cj, PHI ph) {
	    // a hashtable makes this easier.
	    Hashtable h = new Hashtable();
	    for (int i=0; i<cj.dst.length; i++)
		for (int j=0; j<cj.dst[i].length; j++)
		    h.put(cj.dst[i][j], cj.src[i]);

	    int which_pred0 = cj.nextEdge(0).which_pred();
	    int which_pred1 = cj.nextEdge(1).which_pred();
	    for (int i=0; i<ph.src.length; i++) {
		if (!useful.contains(ph.dst[i])) continue; // not useful, skip.
		if (h.get(ph.src[i][which_pred0]) !=
		    h.get(ph.src[i][which_pred1]) )
		    return true; // cjmp matters, either in sig or phi.
	    }
	    return false; // not useful!
	}
    }

    static class NameMap implements TempMap {
	Hashtable h = new Hashtable();
	public Temp tempMap(Temp t) {
	    while (h.containsKey(t))
		t = (Temp) h.get(t);
	    return t;
	}
	public void map(Temp Told, Temp Tnew) { h.put(Told, Tnew); }
	public String toString() { return h.toString(); }
    }

    static class UsefulVisitor extends QuadVisitor {
	Worklist W;
	Set useful;
	Hashtable defMap;
	NameMap nm;
	// maps cjmp targets past useless cjmps/phis
	Hashtable jmpMap = new Hashtable();
	// maps phi sources past useless cjmps/phis
	Hashtable phiMap = new Hashtable();

	UsefulVisitor(Worklist W, Set useful, Hashtable defMap, NameMap nm) {
	    this.W = W;
	    this.useful = useful;
	    this.defMap = defMap;
	    this.nm = nm;
	}
	void markUseful(Quad q) {
	    if (useful.contains(q)) return; // no change.
	    useful.union(q);
	    // all variables used by a useful quad are useful.
	    Temp u[] = q.use();
	    for (int i=0; i<u.length; i++)
		markUseful(u[i]);
	}
	void markUseful(Temp t) {
	    if (useful.contains(t)) return; // no change.
	    useful.union(t);
	    // the quad defining this temp is now useful, too.
	    if (defMap.containsKey(t)) // undefined vars possible.
		W.push(defMap.get(t));
	}
	public void visit(Quad q) {
	    boolean usefound = false;
	    // by default, a quad is useful iff what it defines is useful.
	    Temp d[] = q.def();
	    for (int i=0; i<d.length; i++)
		if (useful.contains(d[i]))
		    usefound = true;
	    // statements that define no variables are safe, however.
	    if (d.length==0) usefound = true;
	    // if it's useful, mark it.
	    if (usefound)
		markUseful(q);
	}
	public void visit(CALL q) {
	    // CALLs may have side-effects, thus they are always useful (conservative)
	    markUseful(q);
	}
	public void visit(CJMP q) {
	    // assume all CJMPs are useful (we'll remove useless ones later)
	    useful.union(q);
	    // if this is useful, the condition is useful
	    markUseful(q.test);
	    // process sigmas normally.
	    visit((SIGMA)q);
	}
	public void visit(SIGMA q) {
	    // Sigmas are useful iff one of the definitions is useful.
	    for (int i=0; i<q.dst.length; i++) {
		if (useful.contains(q.src[i])) continue; //skip already useful sigs.
		for (int j=0; j<q.dst[i].length; j++)
		    if (useful.contains(q.dst[i][j])) // this one's (newly) useful.
			markUseful(q.src[i]);
	    }
	}
	public void visit(METHODHEADER q) { // always useful.
	    markUseful(q);
	}
	public void visit(MONITORENTER q) { // always useful.
	    markUseful(q);
	}
	public void visit(MONITOREXIT q) { // always useful.
	    markUseful(q);
	}
	public void visit(MOVE q) { // moves are never useful (add to rename map)
	    if (useful.contains(q.dst)) {
		markUseful(q.src);
		nm.map(q.dst, q.src);
	    }
	}
	public void visit(NOP q) { // never useful.
	}
	public void visit(PHI q) {
	    // Assume all phis are useful (will remove arity-1 phis later)
	    useful.union(q);
	    // check the individual phi functions for usefulness.
	    for (int i=0; i < q.dst.length; i++)
		if (useful.contains(q.dst[i]))
		    for (int j=0; j<q.src[i].length; j++)
			markUseful(q.src[i][j]);
	}
	public void visit(RETURN q) { // always useful.
	    markUseful(q);
	}
	public void visit(SET q) { // always useful.
	    markUseful(q);
	}
	public void visit(SWITCH q) {
	    // I'm too lazy to see if the switch actually does anything, so assume
	    // it's always useful.
	    useful.union(q);
	    markUseful(q.index);
	    visit((SIGMA)q);
	}
	public void visit(THROW q) { // always useful.
	    markUseful(q);
	}
    }
}
