java_io_File.h                                                                                      100755     766     766         4652  6751400004  12603  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 /* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_io_File */

#ifndef _Included_java_io_File
#define _Included_java_io_File
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_io_File
 * Method:    exists0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_exists0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    canWrite0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_canWrite0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    canRead0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_canRead0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    isFile0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isFile0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    isDirectory0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isDirectory0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    lastModified0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_io_File_lastModified0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    length0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_io_File_length0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    mkdir0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_mkdir0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    renameTo0
 * Signature: (Ljava/io/File;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_renameTo0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_io_File
 * Method:    delete0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_delete0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    rmdir0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_rmdir0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    list0
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_java_io_File_list0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    canonPath
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_io_File_canonPath
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_io_File
 * Method:    isAbsolute
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isAbsolute
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
                                                                                      java_io_FileDescriptor.c                                                                            100755     766     766         3103  6751400003  14622  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 static int fdID = 0;

/*
 * Class:     java_io_FileDescriptor
 * Method:    valid
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_FileDescriptor_valid
(JNIEnv * env, jobject obj) { 
    if (fdID==0) 
	if (!initialize_FD_data(env)) IO_ERROR("Couldn't init native I/O");
    
    return (*env)->GetIntField(env, obj, fdID) >= 0;
}

/*
 * Class:     java_io_FileDescriptor
 * Method:    sync
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileDescriptor_sync
(JNIEnv * env, jobject obj) { 
    int    fd;
    jclass SFExcCls;  /* SyncFailedException class */
    
    if (fdID==0) 
	if (!initialize_FD_data(env)) IO_ERROR("Couldn't init native I/O");
    
    fd = (*env)->GetIntField(env, obj, fdID);
    if (fsync(fd) < 0) { /* An error has occured */
	SFExcCls = (*env)->FindClass(env, "java/io/SyncFailedException");
	if (SFExcCls == NULL) { return; /* Give up */ }
	(*env)->ThrowNew(env, SFExcCls, "Couldn't write to file");
    }

  /* Success! */
}

/*
 * Class:     java_io_FileDescriptor
 * Method:    initSystemFD
 * Signature: (Ljava/io/FileDescriptor;I)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_java_io_FileDescriptor_initSystemFD
  (JNIEnv * env, jclass lcs, jobject obj, jint fd) { 
    if (fdID==0) 
	if (!initialize_FD_data(env)) IO_ERROR("Couldn't init native I/O");
    
    (*env)->SetIntField(env, obj, fdID, fd);
    return obj;
}

int initialize_FD_data(JNIEnv * env) { 
    jclass FDCls = (*env)->FindClass(env, "java/io/FileDescriptor");
    if (FDCls == NULL) return 0;
    fdID = (*env)->GetFieldID(env,FDCls,"fd","I");
    return 1;
}

                                                                                                                                                                                                                                                                                                                                                                                                                                                             java_io_FileDescriptor.h                                                                            100755     766     766         1742  6751400004  14637  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 /* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_io_FileDescriptor */

#ifndef _Included_java_io_FileDescriptor
#define _Included_java_io_FileDescriptor
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     java_io_FileDescriptor
 * Method:    valid
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_FileDescriptor_valid
  (JNIEnv *, jobject);

/*
 * Class:     java_io_FileDescriptor
 * Method:    sync
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileDescriptor_sync
  (JNIEnv *, jobject);

/*
 * Class:     java_io_FileDescriptor
 * Method:    initSystemFD
 * Signature: (Ljava/io/FileDescriptor;I)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_java_io_FileDescriptor_initSystemFD
  (JNIEnv *, jclass, jobject, jint);

/* 
 * Initialize the FileDescriptor native library.  Returns 1 if successful,
 * returns 0 on failure.  
 */
int initialize_FD_data(JNIEnv * env);

#ifdef __cplusplus
}
#endif
#endif
                              java_io_FileInputStream.c                                                                           100755     766     766        12640  6751400003  15005  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 include "java_io_FileOutputStream.h"

static int    fdObjID = 0;  /* The field ID of fd in class FileOutputStream */
static int    fdID    = 0;  /* The field ID of fd in class FileDescriptor */
static jclass IOExcCls;

#define IO_ERROR(env, str) \
    (JNIEnv *)env;  (const char *)str;  /* Check types */             \
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");         \   
    if (IOExcCls == NULL) return; /* give up */                       \
    else (*env)->ThrowNew(env, IOExcCls, "Couldn't write to file")

/*
 * Class:     java_io_FileInputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_open
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */

    /* If static data has not been loaded, load it now */
    if ((fdObjID & fdID) == 0) 
	if (!initialize_FIS_data(env)) IO_ERROR("Couldn't init native I/O");

    cstr  = (*env)->getStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_RDONLY|O_BINARY|O_NONBLOCK);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1) { IO_ERROR("Couldn't open file"); }
}


/*
 * Class:     java_io_FileInputStream
 * Method:    read
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_read
(JNIEnv * env, jobject obj) { 
    int            fd, result; 
    jobject        fdObj;
    unsigned char  buf[1];

    /* NOTE: Assumes static data has been loaded */
    fdObj    = (*env)->GetObjectField(env, obj, fdObjID);
    fd       = (*env)->GetIntField(env, fdObj, fdID);

    if ((result = read(fd, (void*)buf, 1)) == -1) { 
        IO_ERROR("Couldn't read from file"); 
    }

    /* Java language spec requires -1 at EOF, not 0 */ 
    return (jint)(result ? buf[0] : -1);
}

/*
 * Class:     java_io_FileInputStream
 * Method:    readBytes
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_readBytes
(JNIEnv * env, jobject, obj, jbyteArray buf, jint start, jint len) { 
    int              fd; 
    jobject          fdObj;
    unsigned char    buf[len];

    /* NOTE: Assumes static data has been loaded (should be true) */
    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    
    if ((len = read(fd,(void*)buf,len)) == -1) { 
        IO_ERROR("Couldn't read from file"); 
    }

    (*env)->SetByteArrayRegion(env, buf, start, len, buf); 

    /* Java language spec requires -1 at EOF, not 0 */ 
    return (jint)(len ? len : -1);
}

/*
 * Class:     java_io_FileInputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_close
(JNIEnv * env, jobject obj) { 
    int fd;
    jclass fdObj;

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    close(fd);
    (*env)->SetIntField(env, fdObj, fdID, -1);
}


/*
 * Class:     java_io_FileInputStream
 * Method:    skip
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_java_io_FileInputStream_skip
(JNIEnv * env, jobject obj, jlong n) { 
    int    fd;
    off_t  result, orig;
    jclass fdObj;

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);

    /* Get original offset */
    if ((orig = lseek(fd,0,SEEK_SET)) == -1)   { IO_ERROR("Could not seek"); }
    if ((result = lseek(fd,n,SEEK_CUR)) == -1) { IO_ERROR("Could not seek"); }

    return (jlong)(result - orig);
}
    
/*
 * Class:     java_io_FileInputStream
 * Method:    available
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_available
(JNIEnv * env, jobject obj) { 
    fd_set          read_fds;
    int             fd, retval, result;
    off_t           orig;
    struct stat     fdStat;
    struct timeval  timeout;
    
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    fd    = (*env)->GetIntField(env, fdObj, fdID);

    if ((orig = lseek(fd, 0, SEEK_SET)) == -1) { IO_ERROR("Could not seek"); }

    result = fstat(fd, &fdStat);
    if ((!result) && (S_ISREG(fdStat.st_mode))) { 
        retval = fdStat.st_size - orig;
    }
    else { 
        /* File is not regular, attempt to use FIONREAD ioctl() */
        /* NOTE: FIONREAD ioctl() reports 0 for some fd's */        
        if ((ioctl(fd, FIONREAD, &result) >= 0) && result) { /* we're done */ }
	else { 
	    /* The best we can do now is to use select to see if the fd is
	       available.  Returns 1 if true, 0 otherwise. */
	    timeout = {0,0};
	    FD_ZERO(&read_fds);
	    FD_SET(fd, &read_fds);
	    if (select(fd+1, &read_fds, NULL, NULL, &timeout) == -1) { 
	        IO_ERROR("Can't test availability of file descriptor");
	    }
	    else { retval = (FD_ISSET(fd, &read_fds)) ? 1 : 0; }
	}
    }
    return (jint)retval;
}
	  

void initialize_FIS_data(JNIEnv * env) { 
    jclass FOSCls, FDCls;

    FOSCls  = (*env)->FindClass(env, "Ljava/io/FileOutputStream");
    if (FOSCls == NULL) { IO_ERROR("Couldn't initialize native I/O"); }
    fdObjID = (*env)->GetFieldID(env, FOSCls, "fd", "Ljava/io/FileDescriptor");
    FDCls   = (*env)->FindClass(env, "Ljava/io/FileDescriptor");
    if (FDCls == NULL)  { IO_ERROR("Couldn't initialize native I/O"); }
    fdID    = (*env)->GetFieldID(env, FDCls, "fd", "I");
}


                                                                                                java_io_FileInputStream.h                                                                           100755     766     766         2726  6751400004  14777  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 /* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_io_FileInputStream */

#ifndef _Included_java_io_FileInputStream
#define _Included_java_io_FileInputStream
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     java_io_FileInputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_open
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_io_FileInputStream
 * Method:    read
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_read
  (JNIEnv *, jobject);

/*
 * Class:     java_io_FileInputStream
 * Method:    readBytes
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_readBytes
  (JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     java_io_FileInputStream
 * Method:    skip
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_java_io_FileInputStream_skip
  (JNIEnv *, jobject, jlong);

/*
 * Class:     java_io_FileInputStream
 * Method:    available
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_available
  (JNIEnv *, jobject);

/*
 * Class:     java_io_FileInputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_close
  (JNIEnv *, jobject);

/* 
 * Initialize the FileInputStream native library.  Returns 1 if successful,
 * returns 0 on failure.  
 */
int initialize_FIS_data(JNIEnv * env);


#ifdef __cplusplus
}
#endif
#endif
                                          java_io_FileOutputStream.c                                                                          100755     766     766        10055  6751400003  15204  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 #include "java_io_FileOutputStream.h"

static int fdObjID = 0;  /* The field ID of fd in class FileOutputStream */
static int fdID    = 0;  /* The field ID of fd in class FileDescriptor */

#define IO_ERROR(env, str)                                          \
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");       \
    if (IOExcCls == NULL) return; /* give up */                     \
    else (*env)->ThrowNew(env, IOExcCls, "Couldn't write to file")

/*
 * Class:     java_io_FileOutputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_open
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */

    /* If static data has not been loaded, load it now */
    if ((fdObjID & fdID) == 0) 
      if (!initialize_FOS_data(env)) IO_ERROR("Couldn't init native I/O");

    cstr  = (*env)->getStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_TRUNC|O_NONBLOCK);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1) { IO_ERROR("Couldn't open file"); }
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
    if ((fdObjID & fdID) == 0) 
      if (!initialize_FOS_data(env)) IO_ERROR("Couldn't init native I/O");

    cstr  = (*env)->getStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_APPEND|O_NONBLOCK);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1) { IO_ERROR("Couldn't open file"); }
}
    

/*
 * Class:     java_io_FileOutputStream
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_write
  (JNIEnv * env, jobject obj, jint i) { 
    unsigned char  buf[1];
    int            fd; 
    jobject        fdObj;

    fdObj    = (*env)->GetObjectField(env, obj, fdObjID);
    fd       = (*env)->GetIntField(env, fdObj, fdID);
    buf[0]   = (unsigned char)i;

    if (write(fd, (void*)buf, 1) != 1) { IO_ERROR("Couldn't write to file"); }
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    writeBytes
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes
(JNIEnv * env, jobject obj, jbyteArray buf, jint start, jint len) { 
    unsigned char *  buf;
    int              fd; 
    jobject          fdObj;

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    (*env)->GetByteArrayRegion(env, buf, start, len, buf); 

    if (write(fd,(void*)buf,len)!=len) { IO_ERROR("Couldn't write to file"); }
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_close
(JNIEnv * env, jobject obj) { 
    int      fd;
    jobject  fdObj;

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    close(fd);
    (*env)->SetIntField(env, fdObj, fdID, -1);
}


int initialize_FOS_data(JNIEnv * env) { 
    jclass FOSCls, FDCls;

    FOSCls  = (*env)->FindClass(env, "Ljava/io/FileOutputStream");
    if (FOSCls == NULL) return 0;
    fdObjID = (*env)->GetFieldID(env, FOSCls,"fd","Ljava/io/FileDescriptor");
    FDCls   = (*env)->FindClass(env, "Ljava/io/FileDescriptor");
    if (FDCls == NULL)  return 0;
    fdID    = (*env)->GetFieldID(env, cls, "fd", "I");

    return 1;  /* Success */
}



                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   java_io_FileOutputStream.h                                                                          100755     766     766         2541  6751400004  15173  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 /* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_io_FileOutputStream */

#ifndef _Included_java_io_FileOutputStream
#define _Included_java_io_FileOutputStream
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     java_io_FileOutputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_open
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_io_FileOutputStream
 * Method:    openAppend
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_openAppend
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_io_FileOutputStream
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_write
  (JNIEnv *, jobject, jint);

/*
 * Class:     java_io_FileOutputStream
 * Method:    writeBytes
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes
  (JNIEnv *, jobject, jbyteArray, jint, jint);

/*
 * Class:     java_io_FileOutputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_close
  (JNIEnv *, jobject);


/* 
 * Initialize the FileOutputStream native library.  Returns 1 if successful,
 * returns 0 on failure.  
 */
int initialize_FOS_data(JNIEnv * env);


#ifdef __cplusplus
}
#endif
#endif
                                                                                                                                                               native.h                                                                                            100755     766     766         3712  6764753610  11540  0                                                                                                    ustar   duncan                          duncan                                                                                                                                                                                                                 /**
 * Header file containing definitions of the run-time datatypes.
 * Much of it is based on Scott's original native.h, updated to reflect
 * the current class layout.  
 *
 * $Id: java_io_File.c,v 1.1 1999-09-08 15:42:31 cananian Exp $ 
 */

union  _field;
struct _claz;

/* Object information. */
typedef struct _oobj {
    int     hashcode;    // initialized on allocation, if neccessary.
    claz *  clazptr;     // pointer actually points here.
    field * objectdata;  // field info goes here:
    // for an array, first word of objectdata is always length.
} oobj;

/* Field info. */
typedef union _field {
    oobj *   objectref;
    int     i;          // also used for char, boolean, etc.
    float   f;
} field;

typedef field (*method_t)(); 

/* Static class data */
typedef struct _claz { 
    // Array of methods inherited from interfaces implemented by the class
    method_t         iMethods[MAX_INTERFZ_METHODS];
    // Null-terminated list of interfaces implemented by the class
    struct claz **   iListPtr;
    // If an array class, pointer to the component-type's claz structure.
    // Otherwise this field is NULL. 
    struct claz *    componentType;
    // Display information for this class
    struct claz      display[MAX_CLASS_DEPTH];
    // Array of methods inherited from superclasses, and declared by this class
    method_t         cMethods[MAX_CLASS_METHODS];
} claz;

/* Specification for memory layout of java.lang.Class objects.  */
typedef struct _jlclass { 
    int    hashcode;
    claz * clazPtr; 
    // Pointer to a string representing the name of this class
    oobj * strName;
    // Pointer to the clazz structure for the class represented by this object
    claz * jlClazzPtr;  
    // Pointer to a java.lang.reflect.Method[] object storing the methods of 
    // this class (NULL if the class is a primitive type)
    oobj * methods;
    // Pointer to a java.lang.reflect.Field[] object storing the fields of 
    // this class (NULL if the class is a primitive type)
    oobj * fields;
} jlclass;

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      