// DominatingCheckOracle.java, created Tue Jan 16 12:05:26 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.Analysis.DomTree;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.Util.ArrayIterator;
import harpoon.Util.Collections.MultiMap;

import java.util.Iterator;
import java.util.Set;
/**
 * A <code>DominatingCheckOracle</code> removes checks which
 * have already been done at a dominating node.  It improves
 * on the results of a client <code>CheckOracle</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DominatingCheckOracle.java,v 1.1.2.1 2001-01-16 19:32:48 cananian Exp $
 */
class DominatingCheckOracle extends AnalysisCheckOracle {
    /** Creates a <code>DominatingCheckOracle</code>. */
    public DominatingCheckOracle(HCode hc, CheckOracle co) {
	this(new DomTree(hc, false), co);
    }
    DominatingCheckOracle(DomTree dt, CheckOracle co) {
	/* okay, compute the proper check locations by propagating down
	 * domtree */
	for (Iterator it=new ArrayIterator(dt.roots()); it.hasNext(); )
	    propagate((HCodeElement)it.next(), dt, co, new CheckSet());
    }
    void propagate(HCodeElement hce, DomTree dt, CheckOracle co, CheckSet cs){
	/* collect checks from client CheckOracle */
	CheckSet checks = new CheckSet(co, hce);
	/* remove the dominated ones */
	checks.removeAll(cs);
	checks.readVersions.removeAll(cs.writeVersions);
	/* update our data struct with those left */
	readVMap.addAll(hce, checks.readVersions);
	writeVMap.addAll(hce, checks.writeVersions);
	checkFMap.addAll(hce, checks.fields);
	checkEMap.addAll(hce, checks.elements);
	/* create new 'dominated checks' set */
	checks.addAll(cs);
	/* nothing passes MONITORENTER or MONITOREXIT */
	if (hce instanceof MONITORENTER || hce instanceof MONITOREXIT)
	    checks.clear();
	/* recurse down dom tree */
	for (Iterator it=new ArrayIterator(dt.children(hce)); it.hasNext(); )
	    propagate((HCodeElement)it.next(), dt, co, checks);
	/* done! */
    }
}
