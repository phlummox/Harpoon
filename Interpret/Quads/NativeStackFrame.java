// NativeStackFrame.java, created Mon Dec 28 17:22:54 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HMethod;

/**
 * <code>NativeStackFrame</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NativeStackFrame.java,v 1.1.2.3 1999-08-04 05:52:31 cananian Exp $
 */
final class NativeStackFrame extends StackFrame {
    final HMethod method;
    NativeStackFrame(HMethod method) { this.method = method; }
    HMethod getMethod() { return method; }
    String  getSourceFile() { return "--native--"; }
    int     getLineNumber() { return 0; }
}