// PreallocOpt.java, created Tue Nov 26 16:19:50 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.SerializableCodeFactory;

import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadWithTry;
import harpoon.IR.Quads.NEW;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.MetaMethods.SmartCallGraph;
import harpoon.Analysis.MetaMethods.MetaCallGraphImpl;


/**
 * <code>PreallocOpt</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: PreallocOpt.java,v 1.3 2002-11-27 19:08:45 salcianu Exp $
 */
public class PreallocOpt {

    // name of the wrapper for the static fields pointing to
    // pre-allocated memory chunks.
    public static final String PREALLOC_MEM_CLASS_NAME =
	"harpoon.Runtime.PreallocOpt.PreallocatedMemory";
    public static final String FIELD_ROOT_NAME = "preallocmem_";
    public static final String INIT_FIELDS_METHOD_NAME = "initFields";

    public Map new2field  = new HashMap();
    private Map field2coll = new HashMap();

    public PreallocOpt(Linker linker, HCodeFactory hcf,
		       ClassHierarchy ch, HMethod mainM, Set roots) {

	CachingCodeFactory hcf_nossa = getCachingQuadNoSSA(hcf);
	boolean OLD_FLAG = QuadSSI.KEEP_QUAD_MAP_HACK;
        QuadSSI.KEEP_QUAD_MAP_HACK = true;
	CachingCodeFactory hcf_ssi = new CachingCodeFactory
	    (QuadSSI.codeFactory(new SafeCachingCodeFactory(hcf_nossa)), true);

	// 1. execute Ovy's analysis
	IncompatibilityAnalysis ia = new IncompatibilityAnalysis
	    (mainM, hcf_ssi, buildCallGraph(linker, hcf_nossa, ch, roots));

	// restore this flag (the backend crashes without this ...)
	QuadSSI.KEEP_QUAD_MAP_HACK = OLD_FLAG;

	addFields(linker, ia);
    }

    // CachingCodeFactory that ignores all calls to clear()
    private static class SafeCachingCodeFactory
	implements SerializableCodeFactory {
	public SafeCachingCodeFactory(CachingCodeFactory ccf) {
	    this.ccf = ccf;
	}
	private final CachingCodeFactory ccf;	
	public HCode  convert(HMethod m) { return ccf.convert(m); }
	public String getCodeName() { return ccf.getCodeName(); }
	public void   clear(HMethod m) {} // ignore!
    }


    private CachingCodeFactory getCachingQuadNoSSA(HCodeFactory hcf) {
	HCodeFactory hcf_nossa = 
	    hcf.getCodeName().equals(QuadNoSSA.codename) ?
	    hcf : QuadNoSSA.codeFactory(hcf);

	return (hcf_nossa instanceof CachingCodeFactory) ?
	    (CachingCodeFactory) hcf_nossa :
	    new CachingCodeFactory(hcf_nossa, true);
    }


    // build a (smart) call graph
    private CallGraph buildCallGraph
	(Linker linker, CachingCodeFactory hcf_nossa,
	 ClassHierarchy ch, Set roots) {
	Set mroots = new HashSet();
	// filter out things that are not hmethods
        for (Iterator it = roots.iterator(); it.hasNext(); ) {
            Object root = it.next();
            if(root instanceof HMethod) mroots.add(root);
        }

	// now add static initializers;
        for(Iterator it = ch.classes().iterator(); it.hasNext(); ) {
            HClass hclass = (HClass) it.next();
            HMethod hm = hclass.getClassInitializer();
            if (hm != null)
                mroots.add(hm);
        }

	MetaCallGraphImpl.COLL_HACK = true;
        return new SmartCallGraph(hcf_nossa, linker, ch, mroots);
    }


    private void addFields(Linker linker, IncompatibilityAnalysis ia) {
	HClass pam = linker.forName(PREALLOC_MEM_CLASS_NAME);
	HClassMutator mutator = pam.getMutator();
	// this can be improved by directly creating an HField
	HField pattern = pam.getDeclaredField("field_pattern");
	int k = 0;

	for(Iterator it = ia.getCompatibleClasses().iterator();
	    it.hasNext(); k++) {
	    Collection coll = (Collection) it.next();
	    HField f = mutator.addDeclaredField(FIELD_ROOT_NAME + k, pattern);
	    field2coll.put(f, allocatedClasses(coll));
	    for(Iterator it_new = coll.iterator(); it_new.hasNext(); )
		new2field.put(it_new.next(), f);
	}

	System.out.println("PreallocOpt: " + k + " static field(s) generated");
    }


    // Compute the collection of classes that are allocated by the NEW
    // instructions from coll.
    private Collection allocatedClasses(Collection coll) {
	List classes = new LinkedList();
	for(Iterator it_new = coll.iterator(); it_new.hasNext(); )
	    classes.add(((NEW) it_new.next()).hclass());
	return classes;
    }


    /**
       Returns the <code>Collection</code> of classes (=types)
       allocated at the allocation sites that reuse the memory chunk
       that will be pointed to by <code>hfield</code>.  Special code
       in the backend will use this method to compute the size of that
       memory chunk when generating code for allocating the memory and
       initializing the appropriate field.

       @param hfield static field that will point (at runtime) to a
       preallocated memory chunk.

       @return a <code>Collection</code> of classes allocated the
       comaptibe unitary allocation sites that use the preallocated
       memory chunk. */
    public Collection getCollForMemoryChunk(HField hfield) {
	return (Collection) field2coll.get(hfield);
    }
}