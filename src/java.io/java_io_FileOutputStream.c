#include "java_io_FileOutputStream.h"
#include "config.h"
#include <assert.h>	/* for assert */
#include <errno.h>	/* for errno */
#include <fcntl.h>	/* for open */
#include <string.h>	/* for strerror */
#include <unistd.h>	/* write, etc. */

static jfieldID fdObjID = 0; /* The field ID of fd in class FileOutputStream */
static jfieldID fdID    = 0; /* The field ID of fd in class FileDescriptor */
static jclass IOExcCls  = 0; /* The java/io/IOException class object. */
static int inited = 0; /* whether the above variables have been initialized */

int initializeFOS(JNIEnv *env) {
    jclass FOSCls, FDCls;

    assert(!inited);
    FOSCls  = (*env)->FindClass(env, "java/io/FileOutputStream");
    if ((*env)->ExceptionOccurred(env)) return 0;
    fdObjID = (*env)->GetFieldID(env, FOSCls, "fd","Ljava/io/FileDescriptor;");
    if ((*env)->ExceptionOccurred(env)) return 0;
    FDCls   = (*env)->FindClass(env, "java/io/FileDescriptor");
    if ((*env)->ExceptionOccurred(env)) return 0;
    fdID    = (*env)->GetFieldID(env, FDCls, "fd", "I");
    if ((*env)->ExceptionOccurred(env)) return 0;
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");
    if ((*env)->ExceptionOccurred(env)) return 0;
    /* make IOExcCls into a global reference for future use */
    IOExcCls = (*env)->NewGlobalRef(env, IOExcCls);
    inited = 1;
    return 1;
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_open
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */
    jobject      fdObj;	    /* File descriptor object */

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    cstr  = (*env)->GetStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_TRUNC);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    openAppend
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_openAppend
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */
    jobject      fdObj;    

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    cstr  = (*env)->GetStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_APPEND);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}
    

/*
 * Class:     java_io_FileOutputStream
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_write
  (JNIEnv * env, jobject obj, jint i) { 
    int            fd, result;
    jobject        fdObj;
    unsigned char  buf[1];

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    fdObj    = (*env)->GetObjectField(env, obj, fdObjID);
    fd       = (*env)->GetIntField(env, fdObj, fdID);
    buf[0]   = (unsigned char)i;

    result = write(fd, (void*)buf, 1);
    if (result==0)
	(*env)->ThrowNew(env, IOExcCls, "No bytes written");
    if (result==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    writeBytes
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes
(JNIEnv * env, jobject obj, jbyteArray buf, jint start, jint len) { 
    int              fd, result;
    jobject          fdObj;
    jbyte *          nbuf;
    int written = 0;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    (*env)->GetByteArrayRegion(env, buf, start, len, nbuf); 

    while (written < len) {
	result = write(fd, (void*)(nbuf+written), len-written);
	if (result==0) {
	    (*env)->ThrowNew(env, IOExcCls, "No bytes written");
	    return;
	}
	if (result==-1) {
	    (*env)->ThrowNew(env, IOExcCls, strerror(errno));
	    return;
	}
	written+=result;
    }
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_close
(JNIEnv * env, jobject obj) { 
    int      fd, result;
    jobject  fdObj;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    result = close(fd);
    (*env)->SetIntField(env, fdObj, fdID, -1);

    if (result==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

