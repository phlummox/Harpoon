// InstrDIRECTIVE.java, created Mon May 17 16:08:00 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Label;

/**
 * <code>InstrDIRECTIVE</code> is used to represents assembler
 * directives.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: InstrDIRECTIVE.java,v 1.1.2.3 1999-09-10 22:26:05 pnkfelix Exp $
 */
public class InstrDIRECTIVE extends Instr {

    public InstrDIRECTIVE(InstrFactory inf, HCodeElement src, String a) {
        super(inf, src, a, null, null);
    } 

    public void accept(InstrVisitor v) { v.visit(this); }
}
