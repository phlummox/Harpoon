/*
 * @(#)FileInputStream.java	1.34 98/07/01
 *
 * Copyright 1995-1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 */

package java.io;
import harpoon.Analysis.ContBuilder.*;

/**
 * A file input stream is an input stream for reading data from a 
 * <code>File</code> or from a <code>FileDescriptor</code>. 
 *
 * @author  Arthur van Hoff
 * @version 1.34, 07/01/98
 * @see     java.io.File
 * @see     java.io.FileDescriptor
 * @see	    java.io.FileOutputStream
 * @since   JDK1.0
 */
public
class FileInputStream extends InputStream 
{
    /* File Descriptor - handle to the open file */
    private FileDescriptor fd;
    
    /**
     * Creates an input file stream to read from a file with the 
     * specified name. 
     *
     * @param      name   the system-dependent file name.
     * @exception  FileNotFoundException  if the file is not found.
     * @exception  SecurityException      if a security manager exists, its
     *               <code>checkRead</code> method is called with the name
     *               argument to see if the application is allowed read access
     *               to the file.
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     * @since      JDK1.0
     */
    public FileInputStream(String name) throws FileNotFoundException {
	SecurityManager security = System.getSecurityManager();
	if (security != null) {
	    security.checkRead(name);
	}
	try {
	    fd = new FileDescriptor();
	    open(name);
	} catch (IOException e) {
	    throw new FileNotFoundException(name);
	}
    }
    
    public void makeAsync()
    {
	//Offset by 1 for JDK1.1
    	NativeIO.makeNonBlockJNI(fd.fd-1);
    }

    /**
     * Creates an input file stream to read from the specified 
     * <code>File</code> object. 
     *
     * @param      file   the file to be opened for reading.
     * @exception  FileNotFoundException  if the file is not found.
     * @exception  SecurityException      if a security manager exists, its
     *               <code>checkRead</code> method is called with the pathname
     *               of this <code>File</code> argument to see if the
     *               application is allowed read access to the file.
     * @see        java.io.File#getPath()
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     * @since      JDK1.0
     */
    public FileInputStream(File file) throws FileNotFoundException {
	this(file.getPath());
    }

    /**
     * Creates an input file stream to read from the specified file descriptor.
     *
     * @param      fdObj   the file descriptor to be opened for reading.
     * @exception  SecurityException  if a security manager exists, its
     *               <code>checkRead</code> method is called with the file
     *               descriptor to see if the application is allowed to read
     *               from the specified file descriptor.
     * @see        java.lang.SecurityManager#checkRead(java.io.FileDescriptor)
     * @since      JDK1.0
     */
    public FileInputStream(FileDescriptor fdObj) {
	SecurityManager security = System.getSecurityManager();
	if (fdObj == null) {
	    throw new NullPointerException();
	}
	if (security != null) {
	    //security.checkRead(fdObj);
	}
	fd = fdObj;
    }

    /**
     * Opens the specified file for reading.
     * @param name the name of the file
     */
    private native void open(String name) throws IOException;

    /**
     * Reads a byte of data from this input stream. This method blocks 
     * if no input is yet available. 
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             file is reached.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public native int read() throws IOException;

    
    // pesimistic version of this inherited from InputStream
    public IntContinuation _readAsync() throws IOException
    {
	//Offset by 1 for jdk1.1
	int b= NativeIO.getCharJNI(fd.fd-1);
	switch(b)
	    {
	    case NativeIO.ERROR: throw new IOException();
	    case NativeIO.EOF: return new IntContinuationOpt(-1);
	    case NativeIO.TRYAGAIN: return new readAsyncC();
	    default: return new IntContinuationOpt(b);
    }
    }

    class readAsyncC extends IntContinuation implements IOContinuation
    {
	public void exception(Throwable t) {}
	
	public readAsyncC() { done=false; Scheduler.addRead(this); }
	public void resume()
	{
	    //offset by 1 for jdk 1.1
	    int b= NativeIO.getCharJNI(fd.fd-1);
	    switch(b)
		{
		case NativeIO.ERROR: next.exception( new IOException() );return;
		case NativeIO.EOF: next.resume(-1);return;
		case NativeIO.TRYAGAIN:  Scheduler.addRead(this); return;
		default: next.resume(b);
		} 
  	}
	
	public FileDescriptor getFD() { return fd; }
    }

    /** 
     * Reads a subarray as a sequence of bytes. 
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @exception IOException If an I/O error has occurred. 
     */ 
    private native int readBytes(byte b[], int off, int len) throws IOException;

    /**
     * Reads up to <code>b.length</code> bytes of data from this input 
     * stream into an array of bytes. This method blocks until some input 
     * is available. 
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public int read(byte b[]) throws IOException {
	return readBytes(b, 0, b.length);
    }


    public IntContinuation readAsync(byte b[]) throws IOException {
    	return readAsync(b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream 
     * into an array of bytes. This method blocks until some input is 
     * available. 
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public int read(byte b[], int off, int len) throws IOException {
	return readBytes(b, off, len);
    }


    // again, inherited pesimistic version
    public IntContinuation _readAsync(byte b[], int ofs, int len) throws IOException
    {
	if (b == null) {
	    throw new NullPointerException();
	} else if ((ofs < 0) || (ofs > b.length) || (len < 0) ||
		   ((ofs + len) > b.length) || ((ofs + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return new IntContinuationOpt(0);
	}
	//offset by one for jdk1.1
	int n= NativeIO.readJNI(fd.fd-1, b, ofs, len);
	switch(n)
	    {
	    case NativeIO.ERROR: throw new IOException();
	    case NativeIO.EOF: return new IntContinuationOpt(-1);
	    case NativeIO.TRYAGAIN: return new readAsync2C(b, ofs, len);
	    default: return new IntContinuationOpt(n);
	    }
    }
    // Reuses the continuation passed to it if it's not null
    public IntContinuation readAsyncFunky(byte b[], int ofs, int len, IntContinuation prev) throws IOException
    {
	if (b == null) {
	    throw new NullPointerException();
	} else if ((ofs < 0) || (ofs > b.length) || (len < 0) ||
		   ((ofs + len) > b.length) || ((ofs + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return new IntContinuationOpt(0);
	}
	//offset by 1 for jdk1.1
	int n= NativeIO.readJNI(fd.fd-1, b, ofs, len);
	switch(n)
	    {
	    case NativeIO.ERROR: throw new IOException();
	    case NativeIO.EOF: return new IntContinuationOpt(-1);
	    case NativeIO.TRYAGAIN:
		if (prev!=null) {
		    ((readAsync2C) prev) .init (b, ofs, len);
		    return prev;
		} else return new readAsync2C(b, ofs, len);
	    default: return new IntContinuationOpt(n);
	    }
    }
    class readAsync2C extends IntContinuation implements IOContinuation
    {
	public void exception(Throwable t) {}
	
	byte b[];
	int ofs, len;
	public readAsync2C(byte b[], int ofs, int len) {
	    done=false;
	    init(b, ofs, len);
	}

	public void init(byte b[], int ofs, int len ) { 
	    this.b= b;
	    this.ofs= ofs;
	    this.len= len;
	    Scheduler.addRead(this); 
	}
	public void resume()
	{
	    //offset by 1 for jdk1.1
	    int n= NativeIO.readJNI(fd.fd-1,b, ofs, len);
	    switch(n)
		{
		case NativeIO.ERROR: next.exception( new IOException() );return;
		case NativeIO.EOF: next.resume(-1);return;
		case NativeIO.TRYAGAIN: Scheduler.addRead(this); return;
		default: next.resume(n);
		} 
	    
	}
	    
	public FileDescriptor getFD() { return fd; }
    }
    
    
    /**
     * Skips over and discards <code>n</code> bytes of data from the 
     * input stream. The <code>skip</code> method may, for a variety of 
     * reasons, end up skipping over some smaller number of bytes, 
     * possibly <code>0</code>. The actual number of bytes skipped is returned.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public native long skip(long n) throws IOException;

    /**
     * Returns the number of bytes that can be read from this file input
     * stream without blocking.
     *
     * @return     the number of bytes that can be read from this file input
     *             stream without blocking.
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public native int available() throws IOException;

    /**
     * Closes this file input stream and releases any system resources 
     * associated with the stream. 
     *
     * @exception  IOException  if an I/O error occurs.
     * @since      JDK1.0
     */
    public native void close() throws IOException;

    /**
     * Returns the opaque file descriptor object associated with this stream.
     *
     * @return     the file descriptor object associated with this stream.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FileDescriptor
     * @since      JDK1.0
     */
    public final FileDescriptor getFD() throws IOException {
	if (fd != null) return fd;
	throw new IOException();
    }

    /**
     * Ensures that the <code>close</code> method of this file input stream is
     * called when there are no more references to it. 
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FileInputStream#close()
     * @since      JDK1.0
     */
    protected void finalize() throws IOException {
	if (fd != null) {
	    if (fd != fd.in) {
		close();
	    }
	}
    }
}
