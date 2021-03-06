// Hysteresis.java, created by wbeebee
// Copyright (C) 2003 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package imagerec.graph;

/** A {@link Hysteresis} node implements recursive hysteresis to
 *  sharpen edges by deciding whether "maybe" edges (determined
 *  through {@link Thresholding}) should be considered real edges.
 *
 *  @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class Hysteresis extends Node {

    private static final int T1 = 127;
    private static final int T2 = 64;

    /** Construct a new {@link Hysteresis} node that passes its output to <code>out</code>. */
    public Hysteresis(Node out) {
	super(out);
    }

    /** Process an image, sending the sharpened image to <code>out</code>. */ 
    public void process(ImageData id) {
	//System.out.println("Hysteresis.process()");
	byte[] in = id.gvals;
	boolean[] seen = new boolean[id.gvals.length];
	int w = id.width;
	int h = id.height;
	for (int i=w+1;i<(w*(h-2)-1);i++) {
	    if (in[i]==T1) {
		recurse(i, in, w, h, seen);
	    }
	}
	for (int i=0; i<in.length; i++) {
	    if (in[i]==T2) { /* Get rid of "maybe edges which are left". */
		in[i]=0;
	    }
	} 
	super.process(id);
    }
    
    /** Recursively determine whether a possible edge is a real edge. */
    public static void recurse(int i, byte[] in, int w, int h, boolean[] seen) {
	if (i<w || i>w*(h-1) || (i%w)==0 || ((i+1)%w)==0)
	    return;
	in[i] = (byte)T1;
	//seen[i] = true;
	int[] pos = new int[] {i-w-1,i-w,i-w+1,i-1,i+1,i+w-1,i+w,i+w+1};
	for (int j=0; j<pos.length; j++) {
	    if (in[pos[j]]==T2) {
		recurse(pos[j], in, w, h, seen);
	    }
	}
    }
}
