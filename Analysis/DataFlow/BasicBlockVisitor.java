// BasicBlockVisitor.java, created Sat Apr 10 10:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

/**
 * BasicBlockVisitor
 *
 * Implemented similarly to QuadVisitor, with the idea that we may
 * eventually have different kinds of basic blocks.
 * @author John Whaley
 * @version $Id: BasicBlockVisitor.java,v 1.1.2.3 1999-08-04 05:52:20 cananian Exp $
 */

public abstract class BasicBlockVisitor  {
  protected BasicBlockVisitor() { }

  /** Visit a basic block b. */
  public abstract void visit(BasicBlock b);
}
