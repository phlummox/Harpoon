// LiveWebs.java, created Mon Nov  8 23:56:44 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.IR.Properties.UseDef;
import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;


import harpoon.Analysis.DataFlow.LiveVars;
import harpoon.Analysis.BasicBlock;
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.DefaultMultiMap;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;


/**
 * <code>LiveWebs</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LiveWebs.java,v 1.1.2.4 1999-11-09 08:27:21 pnkfelix Exp $
 */
public class LiveWebs extends LiveVars {
    
    // universe of values for this analysis
    Set webs;

    // maps a (Temp x reference) to the Web containing that reference
    // to that Temp
    Map tXrefToWeb;

    /** Creates a <code>LiveWebs</code>, using <code>webs</code> as
	its universe of values. 
    */
    public LiveWebs(Set webs, Iterator basicBlocks) {
	super();
	this.webs = webs;
	this.tXrefToWeb = new HashMap();
	initializeBBtoLVI(basicBlocks, new BitSetFactory(webs));
	Iterator webIter = webs.iterator();
	while(webIter.hasNext()) {
	    Web w = (Web) webIter.next();
	    Iterator instrs = w.refs.iterator();
	    while(instrs.hasNext()) {
		tXrefToWeb.put(new TempInstrPair(w.var, (Instr)instrs.next()), w);
	    }
	}
    }

    protected Set findUniverse(Iterator bbs) {
	return webs;
    }
    
    protected LiveVars.LiveVarInfo makeUseDef(BasicBlock bb, SetFactory sf) {
	LiveVars.LiveVarInfo info = new LiveVars.LiveVarInfo(sf);
	Iterator instrs = bb.listIterator();
	
	while (instrs.hasNext()) {
	    Instr ref = (Instr) instrs.next();
	    
	    // USE: set of vars used in block before being defined
	    for(int i=0; i<ref.use().length; i++) {
		Temp t = ref.use()[i];
		Web web  = (Web)
		    tXrefToWeb.get(new TempInstrPair(t, ref));

		if ( !info.def.contains(web) ) {
		    info.use.add(web);
		}
	    }	    
	    // DEF: set of vars defined in block before being used
	    for(int i=0; i<ref.def().length; i++) {
		Temp t = ref.def()[i];
		Web web = (Web)
		    tXrefToWeb.get(new TempInstrPair(t, ref));

		if ( !info.use.contains(web) ) {
		    info.def.add(web);
		}
	    }


	}
	
	return info;
    }
}
