// TempInfo.java, created Mon Aug  2 16:31:30 1999 by root
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Jasmin;

/**
 * <code>TempInfo</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: TempInfo.java,v 1.1.2.1 1999-08-06 17:28:04 bdemsky Exp $
 */
public class TempInfo {
    TempInfo(boolean stack) {
	this.stack=stack;
	this.localvar=-1;
    }
    TempInfo(int localvar) {
	this.stack=false;
	this.localvar=localvar;
    }
    boolean stack;
    int localvar;
}