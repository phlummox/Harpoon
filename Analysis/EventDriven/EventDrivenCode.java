// EventDrivenCode.java, created Fri Nov 12 14:40:55 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.RETURN;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

/**
 * <code>EventDrivenCode</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: EventDrivenCode.java,v 1.1.2.5 1999-11-22 20:59:27 bdemsky Exp $
 */
public class EventDrivenCode extends harpoon.IR.Quads.QuadNoSSA {
    public static final String codename = "quad-no-ssa";

    /** Creates a <code>EventDrivenCode</code>. */
    public EventDrivenCode(HMethod parent, HMethod newmain, Temp[] params) {
        super(parent, null);
	this.quads = buildCode(newmain, params);
    }

    private EventDrivenCode(HMethod parent) {
	super(parent, null);
    }

    /** Clone this code representation.  The clone has its own copy of
     *  the quad graph. 
     */
    public HCode clone(HMethod newMethod) {
	EventDrivenCode edc = new EventDrivenCode(newMethod);
	edc.quads = Quad.clone(edc.qf, this.quads);
	return edc;
    }

    /**
     * Return the name of this code view.
     * @return the name of the <code>parent</code>'s code view.
     */
    public String getName() {
	return harpoon.IR.Quads.QuadNoSSA.codename;
    }

    private Quad buildCode(HMethod newmain, Temp[] params) {
	System.out.println("Entering EventDrivenCode.buildCode()");
	HEADER h = new HEADER(this.qf, null);
	FOOTER f = new FOOTER(this.qf, null, 4);
	Quad.addEdge(h, 0, f, 0);

	System.out.println("Debug 1");

	METHOD m = new METHOD(this.qf, null, params, 1);
	Quad.addEdge(h, 1, m, 0);

	System.out.println("Debug 2");

	if (newmain == null) System.out.println("newmain is null");

	// call to new main method (mainAsync)
	System.out.println(newmain);
	TempFactory tf=this.qf.tempFactory();
	Temp t=new Temp(tf);
	Temp exc=new Temp(tf);

	CALL c1 = new CALL(this.qf, null, newmain, params, t, exc, true,
			   false, new Temp[0]);
	THROW throwq=new THROW(this.qf, null, exc);
	Quad.addEdge(c1, 1, throwq,0);
	System.out.println("Debug 3");

	Quad.addEdge(m, 0, c1, 0);

	// call to scheduler
	HMethod schloop = null;
	try {
	    final HClass sch = 
		HClass.forName("harpoon.Analysis.ContBuilder.Scheduler");
		//		HClass.forName("java.continuation.Scheduler");
	    schloop = sch.getDeclaredMethod("loop", new HClass[0]);
	} catch (Exception e) {
	    System.err.println
		("Cannot find harpoon.Analysis.EventDriven.Scheduler");
	}

	System.out.println("Debug 4");

	Temp exc2=new Temp(tf);
	CALL c2 = new CALL(this.qf, null, schloop, new Temp[0], null, exc2, 
			   true, false, new Temp[0]);
	THROW throwq2=new THROW(this.qf, null, exc2);
	Quad.addEdge(c1, 0, c2, 0);
	Quad.addEdge(c2, 1, throwq2,0);


	System.out.println("Debug 5");

	RETURN r = new RETURN(this.qf, null, null);
	Quad.addEdge(c2,0,r,0);
	Quad.addEdge(r, 0, f, 1);
	Quad.addEdge(throwq,0,f,2);
	Quad.addEdge(throwq2,0, f,3);
	System.out.println("Leaving EventDrivenCode.buildCode()");
	return h;
    }    
}


