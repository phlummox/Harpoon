/* java.lang.reflect.AccessibleObject
   Copyright (C) 2001 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package java.lang.reflect;

/**
 * This class is the superclass of various reflection classes, and
 * allows sufficiently trusted code to bypass normal restrictions to
 * do necessary things like invoke private methods outside of the
 * class during Serialization.  If you don't have a good reason
 * to mess with this, don't try. Fortunately, there are adequate
 * security checks before you can set a reflection object as accessible.
 * <p>
 * HACKED by C. Scott to basically make all classes accessible.
 * this saves a field per reflection object and also preserves the
 * read-only nature of these static objects.  Also our implementations
 * of java.lang.reflect don't do accessibility checks anyhow.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author Tom Tromey <tromey@cygnus.com>
 * @author Eric Blake <ebb9@email.byu.edu>
 * @see Field
 * @see Constructor
 * @see Method
 * @see ReflectPermission
 * @since 1.2
 * @status updated to 1.4
 */
public class AccessibleObject
{

    // default visibility for use by inherited classes
    // [AS] added to make sure we support GNU Classpath 0.08 
  /**
   * True if this object is marked accessible, which means the reflected
   * object bypasses normal security checks.
   */
  boolean flag = false;

  /**
   * Only the three reflection classes that extend this can create an
   * accessible object.  This is not serializable for security reasons.
   */
  protected AccessibleObject()
  {
  }

  /**
   * Return the accessibility status of this object.
   *
   * @return true if this object bypasses security checks
   */
  public boolean isAccessible()
  {
      // [AS] Harpoon/Runtime use to return always "true", but
      // classpath-0.08 returns flag.
    return flag;
  }


  /**
   * Convenience method to set the flag on a number of objects with a single
   * security check. If a security manager exists, it is checked for
   * <code>ReflectPermission("suppressAccessChecks")</code>.<p>
   *
   * If <code>flag</code> is true, and the initial security check succeeds,
   * this can still fail if a forbidden object is encountered, leaving the
   * array half-modified. At the moment, the forbidden members are:<br>
   * <ul>
   *  <li>Any Constructor for java.lang.Class</li>
   *  <li>Any AccessibleObject for java.lang.reflect.AccessibleObject
   *      (this is not specified by Sun, but it closes a big security hole
   *      where you can use reflection to bypass the security checks that
   *      reflection is supposed to provide)</li>
   * </ul>
   * (Sun has not specified others, but good candidates might include
   * ClassLoader, String, and such. However, the more checks we do, the
   * slower this method gets).
   *
   * @param array the array of accessible objects
   * @param flag the desired state of accessibility, true to bypass security
   * @throws NullPointerException if array is null
   * @throws SecurityException if the request is denied
   * @see SecurityManager#checkPermission(java.security.Permission)
   * @see RuntimePermission
   */
  public static void setAccessible(AccessibleObject[] array, boolean flag)
  {
    checkPermission();
    for (int i = 0; i < array.length; i++)
      array[i].secureSetAccessible(flag);
  }

  /**
   * Sets the accessibility flag for this reflection object. If a security
   * manager exists, it is checked for
   * <code>ReflectPermission("suppressAccessChecks")</code>.<p>
   *
   * If <code>flag</code> is true, and the initial security check succeeds,
   * this will still fail for a forbidden object. At the moment, the
   * forbidden members are:<br>
   * <ul>
   *  <li>Any Constructor for java.lang.Class</li>
   *  <li>Any AccessibleObject for java.lang.reflect.AccessibleObject
   *      (this is not specified by Sun, but it closes a big security hole
   *      where you can use reflection to bypass the security checks that
   *      reflection is supposed to provide)</li>
   * </ul>
   * (Sun has not specified others, but good candidates might include
   * ClassLoader, String, and such. However, the more checks we do, the
   * slower this method gets).
   *
   * @param flag the desired state of accessibility, true to bypass security
   * @throws NullPointerException if array is null
   * @throws SecurityException if the request is denied
   * @see SecurityManager#checkPermission(java.security.Permission)
   * @see RuntimePermission
   */
  public void setAccessible(boolean flag)
  {
    checkPermission();
    secureSetAccessible(flag);
  }

  /**
   * Performs the specified security check, for
   * <code>ReflectPermission("suppressAccessChecks")</code>.
   *
   * @throws SecurityException if permission is denied
   */
  private static final void checkPermission()
  {
    SecurityManager sm = System.getSecurityManager();
    if (sm != null)
      sm.checkPermission(new ReflectPermission("suppressAccessChecks"));
  }

  /**
   * Performs the actual accessibility change, this must always be invoked
   * after calling checkPermission.
   *
   * @param flag the desired status
   * @throws SecurityException if flag is true and this is one of the
   *         forbidden members mentioned in {@link setAccessible(boolean)}.
   */
  final void secureSetAccessible(boolean flag)
  {
    if (flag &&
        ((this instanceof Constructor
          && ((Constructor) this).getDeclaringClass() == Class.class)
         || ((Member) this).getDeclaringClass() == AccessibleObject.class))
      throw new SecurityException("Cannot make object accessible: " + this);
    if (!flag)
	throw new SecurityException("Can't make object non-accessible: "+this);
    //this.flag = flag;
  }
}