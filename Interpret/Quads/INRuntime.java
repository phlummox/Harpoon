// INRuntime.java, created Fri Jan  1 12:17:30 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INRuntime</code> provides implementations of the native methods in
 * <code>java.lang.Runtime</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INRuntime.java,v 1.1.2.5 2000-01-28 05:27:26 cananian Exp $
 */
public class INRuntime {
    static final void register(StaticState ss) {
	ss.registerOverride(privateConstructor(ss));
	ss.register(gc(ss));
	try { // JDK 1.2 only
	    ss.register(runFinalization0(ss));
	} catch (NoSuchMethodError e) { // JDK 1.1 fallback.
	    ss.register(runFinalization(ss));
	}
	ss.register(freeMemory(ss));
	ss.register(totalMemory(ss));
    }
    // the runtime for the interpreter is identical to the current runtime.
    private static final NativeMethod privateConstructor(StaticState ss0) {
	final HMethod hm = ss0.HCruntime.getConstructor(new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
	        ObjectRef obj = (ObjectRef) params[0];
		obj.putClosure(Runtime.getRuntime());
		return null;
	    }
	};
    }
    private static final NativeMethod gc(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("gc","()V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		r.gc();
		return null;
	    }
	};
    }
    private static final NativeMethod runFinalization(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("runFinalization","()V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		r.runFinalization();
		return null;
	    }
	};
    }
    // JDK 1.2 stub
    private static final NativeMethod runFinalization0(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("runFinalization0","()V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		return runFinalization(ss).invoke(ss, params);
	    }
	};
    }
    private static final NativeMethod freeMemory(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("freeMemory","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		return new Long(r.freeMemory());
	    }
	};
    }
    private static final NativeMethod totalMemory(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("totalMemory","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		return new Long(r.totalMemory());
	    }
	};
    }
}