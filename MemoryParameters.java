// MemoryParameters.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.util.LinkedList;
import java.util.Iterator;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** Memory parameters can be given on the constructor of
 *  <code>RealtimeThread</code> and <code>AsyncEventHandler</code>. These
 *  can be used both for the purposes of admission control by the
 *  scheduler and for the purposes of pacing the garbage collector to
 *  satisfy all of the thread allocation rates.
 *  <p>
 *  When a reference to a <code>MemoryParameters</code> object is given
 *  as a parameter to a constructor, the <code>MemoryParameters</code>
 *  object becomes bound to the object being created. Changes to the
 *  values in the <code>MemoryParameters</code> object affect the
 *  constructed object. If given to more than one constructor, then
 *  changes to the values in the <code>MemoryParameters</code> object
 *  affect <i>all</i> of the associated objects. Note that this is a
 *  one-to-many relationship and <i>not</i> a many-to-many.
 *  <p>
 *  <b>Caution:</b> This class is explicitly unsafe in multithreaded
 *  situations when it is being changed. No synchronization is done. It
 *  is assumed that users of this class who are mutating instances will
 *  be doing their own synchronization at a higher level.
 */
public class MemoryParameters {

    /** Specifies no maximum limit */
    public static final long NO_MAX = -1;
    private long allocationRate;
    private long maxImmortal;
    private long maxMemoryArea;
    private MemoryArea memoryArea;

    LinkedList schList = new LinkedList();

    public MemoryParameters(long maxMemoryArea, long maxImmortal) 
	throws IllegalArgumentException {
	this.maxMemoryArea = Math.max(maxMemoryArea, NO_MAX);
	this.maxImmortal = Math.max(maxImmortal, NO_MAX);
	this.allocationRate = NO_MAX;
    }

    public MemoryParameters(long maxMemoryArea, long maxImmortal, 
			    long allocationRate) 
	throws IllegalArgumentException {
	this.maxMemoryArea = Math.max(maxMemoryArea, NO_MAX);
	this.maxImmortal = Math.max(maxImmortal, NO_MAX);
	this.allocationRate = Math.max(allocationRate, NO_MAX);
    }

    public MemoryParameters(MemoryArea memoryArea) {
	this.memoryArea = memoryArea;
    }

    /** Get the allocation rate. Units are in bytes per second. */
    public long getAllocationRate() {
	return allocationRate;
    }

    /** Get the limit on the amount of memory the thread may allocate
     *  in the immortal area. Units are in bytes.
     */
    public long getMaxImmortal() {
	return maxImmortal;
    }

    /** Get the limit on the amount of memory the thread may allocate
     *  in the memory area. Units are in bytes.
     */
    public long getMaxMemoryArea() {
	return maxMemoryArea;
    }

    /** A limit on the rate of allocation in the heap. */
    public void setAllocationRate(long rate) {
	allocationRate = Math.max(rate, NO_MAX);
    }

    public /* private ? */ void setMaxImmortal(long maximum) {
	maxImmortal = maximum;
    }

    public /* private ? */ void setMaxMemoryArea(long maximum) {
	maxMemoryArea = maximum;
    }

    /** Change the limit on the rate of allocation in the heap. If this
     *  <code>MemoryParameters</code> object is currently associated with
     *  one or more realtime threads that have been passed admission
     *  control, this change in allocation rate will be submitted to
     *  admission control. The scheduler (in conjuction with the garbage
     *  collector) willeither admit all the effected threads with the new
     *  allocation rate, or leave the allocation rate unchanged and cause
     *  <code>setAllocationRateIfFeasible</code> to return <code>false</code>.
     */
    public boolean setAllocationRateIfFeasible(int allocationRate) {
	boolean b = true;
	Iterator it = schList.iterator();
	long old_rate = this.allocationRate;
	setAllocationRate(allocationRate);
	while (b && it.hasNext())
	    b = ((Schedulable)it.next()).setMemoryParametersIfFeasible(this);
	if (!b) {   // something is not feasible
	    setMaxMemoryArea(old_rate);   // returning the value back
	    for (it = schList.iterator(); it.hasNext(); )   // undoing all changes
		((Schedulable)it.next()).setMemoryParameters(this);
	}
	return b;
    }

    /** A limit on the amount of memory the thread may allocate in the immortal area. */
    public boolean setMaxImmortalIfFeasible(long maximum) {
	boolean b = true;
	Iterator it = schList.iterator();
	long old_maximum = this.maxImmortal;
	setMaxImmortal(maximum);
	while (b && it.hasNext())
	    b = ((Schedulable)it.next()).setMemoryParametersIfFeasible(this);
	if (!b) {   // something is not feasible
	    setMaxImmortal(old_maximum);   // returning the value back
	    for (it = schList.iterator(); it.hasNext(); )   // undoing all changes
		((Schedulable)it.next()).setMemoryParameters(this);
	}
	return b;
    }

    /** A limit on the amount of memory the thread may allocate in the memory area. */
    public boolean setMaxMemoryAreaIfFeasible(long maximum) {
	boolean b = true;
	Iterator it = schList.iterator();
	long old_maximum = this.maxMemoryArea;
	setMaxMemoryArea(maximum);
	while (b && it.hasNext())
	    b = ((Schedulable)it.next()).setMemoryParametersIfFeasible(this);
	if (!b) {   // something is not feasible
	    setMaxMemoryArea(old_maximum);   // returning the value back
	    for (it = schList.iterator(); it.hasNext(); )   // undoing all changes
		((Schedulable)it.next()).setMemoryParameters(this);
	}
	return b;
    }

    public MemoryArea getMemoryArea() {
	return memoryArea;
    }

    public boolean bindSchedulable(Schedulable sch) {
	return schList.add(sch);
    }

    public boolean unbindSchedulable(Schedulable sch) {
	return schList.remove(sch);
    }    
}
