// MemoryArea.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/** <code>MemoryArea</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public abstract class MemoryArea {

    /** The size of this MemoryArea.
     */

    protected long size;

    /** */

    protected long memoryConsumed;  // Size is somewhat inaccurate

    /** Indicates whether this is a ScopedMemory. 
     */

    boolean scoped;

    /** Indicates whether this is a HeapMemory.
     */

    boolean heap;

    /** Indicates whether this is a NullMemoryArea. 
     */
    
    protected boolean nullMem;

    /** Every MemoryArea has a unique ID. 
     */

    protected int id;

    /** This is used to create the unique ID.
     */

    private static int num = 0;

    /** Indicates whether this memoryArea refers to a constant or not. 
     *  This is set by the compiler.
     */

    boolean constant;

    protected native void enterMemBlock(RealtimeThread rt, MemAreaStack mas);
    protected native void exitMemBlock(RealtimeThread rt);
    protected native Object newArray(RealtimeThread rt, 
				     Class type, 
				     int number, Object memBlock);
    protected native Object newArray(RealtimeThread rt, 
				     Class type, int[] dimensions, 
				     Object memBlock);
    protected native Object newInstance(RealtimeThread rt, 
			      Constructor constructor, 
			      Object[] parameters, 
			      Object memBlock)
	throws InvocationTargetException;
    protected native void throwIllegalAssignmentError(Object obj, 
						      MemoryArea ma)
	throws IllegalAssignmentError;

    protected MemoryArea(long sizeInBytes) {
	size = sizeInBytes;
	scoped = false; // To avoid the dreaded instanceof
	heap = false;
	id = num++;
	constant = false;
	nullMem = false;
	initNative(sizeInBytes);
    }
    
    /** */

    protected abstract void initNative(long sizeInBytes);

    /** */

    protected abstract void newMemBlock(RealtimeThread rt);
    
    /** */

    public void enter(Runnable logic) {
	RealtimeThread current = RealtimeThread.currentRealtimeThread();
	current.enterFromMemArea(this);
	logic.run();
	current.exit();
    }

    /** */
    
    public static MemoryArea getMemoryArea(Object object) {
	if (object == null) {
	    return ImmortalMemory.instance();
	}
	MemoryArea mem = object.memoryArea;
	// I'm completely punting this for now...
  	if (mem == null) { // Native methods return objects 
  	    // allocated out of the current scope.
	    return RealtimeThread.currentRealtimeThread().getMemoryArea();
//    	    return NullMemoryArea.instance();
	} 
	if (mem.constant) { // Constants are allocated out of ImmortalMemory
	    return ImmortalMemory.instance();
	}
	return mem;
    }
    
    /** Return the amount of memory consumed in this MemoryArea.
     */

    public long memoryConsumed() {
	return memoryConsumed;
    }
    
    /** Return the amount of memory remaining in this MemoryArea.
     */

    public long memoryRemaining() {
	return size()-memoryConsumed();
    }
    
    /** Return the size of this MemoryArea.
     */

    public long size() {
	return size;
    }
    
    /** */

    public void bless(java.lang.Object obj) { 
	obj.memoryArea = this;
    }
    
    /** Create a new array, allocated out of this MemoryArea. 
     */

    public Object newArray(final Class type, final int number) 
	throws IllegalAccessException, InstantiationException, OutOfMemoryError
    {
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	MemoryArea rtMem = rt.getMemoryArea();
	Object o; 
	if (rtMem == this) {
	    o = Array.newInstance(type, number);
	} else {
	    o = new Object();
	    o.memoryArea = this;
	    rtMem.checkAccess(o);
	    MemAreaStack mas = rt.memAreaStack.first(this);
	    if (mas == null) {
		newMemBlock(rt);
		o = newArray(rt, type, number, rt);
	    } else {
		o = newArray(rt, type, number, mas);
	    }
	}
	o.memoryArea = this;
	return o;
    }

    /** Create a new array, allocated out of this MemoryArea. 
     */
    
    public Object newArray(final Class type,
			   final int[] dimensions) 
	throws IllegalAccessException, OutOfMemoryError
    {
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	MemoryArea rtMem = rt.getMemoryArea();
	Object o;
	if (rtMem == this) {
	    return Array.newInstance(type, dimensions);
	} else {
	    o = new Object();
	    o.memoryArea = this;
	    rtMem.checkAccess(o);
	    MemAreaStack mas = rt.memAreaStack.first(this);
	    if (mas == null) {
		newMemBlock(rt);
		o = newArray(rt, type, dimensions, rt);
	    } else {
		o = newArray(rt, type, dimensions, mas);
	    }
	}
	o.memoryArea = this;
	return o;
    }

    /** Create a new object, allocated out of this MemoryArea.
     */
    
    public Object newInstance(Class type)
	throws IllegalAccessException, InstantiationException,
	OutOfMemoryError {
	return newInstance(type, new Class[0], new Object[0]);
    }
    
    /** Create a new object, allocated out of this MemoryArea.
     */
    
    public Object newInstance(final Class type,
			      final Class[] parameterTypes,
			      final Object[] parameters) 
	throws IllegalAccessException, InstantiationException,
	OutOfMemoryError {
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	Object o = new Object();
	MemoryArea rtMem = rt.getMemoryArea();
	o.memoryArea = this;
	rtMem.checkAccess(o);
	try {
	    Constructor c = type.getConstructor(parameterTypes);
	    if (rtMem == this) {
		o = c.newInstance(parameters);
	    } else {
		MemAreaStack mas = rt.memAreaStack.first(this);
		if (mas == null) {
		    newMemBlock(rt);
		    o = newInstance(rt, c, parameters, rt);
		} else {
		    o = newInstance(rt, c, parameters, mas);
		}
	    }
	} catch (NoSuchMethodException e) {
	    throw new InstantiationException(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new InstantiationException(e.getMessage());
	}
	o.memoryArea = this;
	return o;
    }
    
    /** Check to see if this object can be accessed from this MemoryArea
     */
    
    public void checkAccess(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && 
	    obj.memoryArea.scoped) {
	    throwIllegalAssignmentError(obj, obj.memoryArea);
	}
    }
    
    /** Get the outerScope of this MemoryArea, for non-ScopedMemory's,
     *  this defaults to null.
     */

    public MemoryArea getOuterScope() {
	return null;
    }
    
    /** Output a helpful string describing this MemoryArea.
     */

    public String toString() {
//  	MemoryArea parent = null;
//  	try {
//  	    parent = getOuterScope();
//  	} catch (MemoryScopeError e) {
//  	    return String.valueOf(id + " not in execution path");
//  	}
//  	if (parent == null) {
//  	    return String.valueOf(id);      
//  	} else {
//  	    return parent.toString() + "." + String.valueOf(id);
//  	}    
	return String.valueOf(id);
    }
}