// Operand.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.*;
import harpoon.Util.ArrayFactory;

/**
 * <code>Operand</code> represents the operands of a java bytecode
 * instruction.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Operand.java,v 1.2.2.1 1998-11-30 21:21:02 cananian Exp $
 * @see Instr
 * @see InGen
 * @see OpConstant
 * @see OpLocalVariable
 * @see OpClass
 * @see OpMethod
 * @see OpField
 */
public abstract class Operand {
  /** Return a human-readable representation of this Operand. */
  public abstract String toString(); // force a reimplementation.

  /** Array factory: returns new <code>Operand[]</code>. */
  public static final ArrayFactory arrayFactory =
    new ArrayFactory() {
      public Object[] newArray(int len) { return new Operand[len]; }
    };
}
