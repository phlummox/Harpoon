package harpoon.ClassFile.Raw;

/** 
 * Specialized IOException class to handle malformed class files.
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassDataException.java,v 1.6 1998-07-31 07:05:54 cananian Exp $
 */
public class ClassDataException extends java.io.IOException {
  public ClassDataException() { super(); }
  public ClassDataException(String s) { super(s); }
}
