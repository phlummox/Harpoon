// LQMain.java, created Fri Aug  7 10:22:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;

/**
 * <code>LQMain</code> showcases LowQuad form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LQMain.java,v 1.1.2.1 1999-02-07 10:38:27 cananian Exp $
 */
public abstract class LQMain extends harpoon.IR.Registration {

    /** The compiler should be invoked with the names of classes
     *  extending <code>java.lang.Thread</code>.  These classes
     *  define the external interface of the machine. */
    public static void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	HCodeFactory hcf = new CachingCodeFactory
	    (harpoon.Analysis.QuadSSA.SCC.SCCOptimize.codeFactory
	    (harpoon.IR.Quads.QuadSSA.codeFactory()
	     ));
	hcf = harpoon.IR.LowQuad.LowQuadSSA.codeFactory(hcf);
	hcf = harpoon.IR.LowQuad.LowQuadNoSSA.codeFactory(hcf);
	
	HClass interfaceClasses[] = new HClass[args.length];
	for (int i=0; i<args.length; i++)
	    interfaceClasses[i] = HClass.forName(args[i]);
	// Do something intelligent with these classes. XXX
	for (int i=0; i<interfaceClasses.length; i++) {
	    HMethod hm[] = interfaceClasses[i].getDeclaredMethods();
	    for (int j=0; j<hm.length; j++) {
	      HCode hc = hcf.convert(hm[j]);
	      if (hc!=null) hc.print(out);
	    }
	}
    }
}
