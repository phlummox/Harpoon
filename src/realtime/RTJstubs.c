/* RTJstubs.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

/* Stubs in case you don't want to use RTJ, but your Java input 
   to the Flex compiler still has references to CTMemory's, etc. */

#include <jni.h>
#include "config.h"

#ifdef WITH_FAKE_SCOPES
#include "jni-private.h"
#include "../java.lang.reflect/java_lang_reflect_Constructor.h"
#include "../java.lang.reflect/java_lang_reflect_Array.h"

void RTJ_preinit() {
  JNIEnv* env = FNI_GetJNIEnv();
  jclass clazz = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  (*env)->SetStaticBooleanField(env, clazz,
				(*env)->GetStaticFieldID(env, clazz,
							 "RTJ_init_in_progress", 
							 "Z"), JNI_TRUE);
}

void RTJ_init() {
  JNIEnv* env = FNI_GetJNIEnv();
  jclass clazz = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  (*env)->SetStaticBooleanField(env, clazz,
				(*env)->GetStaticFieldID(env, clazz,
							 "RTJ_init_in_progress", 
							 "Z"), JNI_FALSE);
}
#endif

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    enterMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;Ljavax/realtime/MemAreaStack;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_enterMemBlock
(JNIEnv* env, jobject memoryArea, 
 jobject realtimeThread, jobject memAreaStack) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    exitMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_exitMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2ILjava_lang_Object_2
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jint length, jobject memBlockObj) {
#ifdef WITH_FAKE_SCOPES
  return Java_java_lang_reflect_Array_newArray(env, NULL, componentClass, length);
#else
  printf("Did you forget to compile --with-realtime-java?\n");
  return NULL;
#endif
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;[ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3ILjava_lang_Object_2
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jintArray dims, jobject memBlockObj) {
#ifdef WITH_FAKE_SCOPES
  return Java_java_lang_reflect_Array_multiNewArray(env, NULL, componentClass, dims);
#else
  printf("Did you forget to compile --with-realtime-java?\n");
  return NULL;
#endif
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newInstance
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/reflect/Constructor;[Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newInstance
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, 
 jobject constructor, jobjectArray args, jobject memBlock) {
#ifdef WITH_FAKE_SCOPES
  return Java_java_lang_reflect_Constructor_newInstance(env, constructor, args);
#else
  printf("Did you forget to compile --with-realtime-java?\n");
  return NULL;
#endif
}

/*
 * Class:     HeapMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_initNative
(JNIEnv* env, jobject heapMemory, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     HeapMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_newMemBlock
(JNIEnv* env, jobject heapMemory, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     CTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_initNative
(JNIEnv* env, jobject CTMemory, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     CTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_newMemBlock
(JNIEnv* env, jobject CTMemory, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     javax_realtime_CTMemory
 * Method:    doneNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_doneNative
(JNIEnv *env, jobject CTMemory) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     VTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_VTMemory_initNative
(JNIEnv* env, jobject VTMemory, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     VTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_VTMemory_newMemBlock
(JNIEnv* env, jobject VTMemory, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     ImmortalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_initNative
(JNIEnv* env, jobject ImmortalMemory, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     ImmortalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_newMemBlock
(JNIEnv* env, jobject ImmortalMemory, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_initNative
(JNIEnv* env, jobject ImmortalPhysicalMemory, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_newMemBlock
(JNIEnv* env, jobject ImmortalPhysicalMemory, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     LTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_initNative
(JNIEnv* env, jobject LTMemory, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     LTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_newMemBlock
(JNIEnv* env, jobject LTMemory, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     NullMemoryArea
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_initNative
(JNIEnv* env, jobject NullMemoryArea, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     NullMemoryArea
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_newMemBlock
(JNIEnv* env, jobject NullMemoryArea, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     ScopedPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_initNative
(JNIEnv* env, jobject ScopedPhysicalMemory, jlong size) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

/*
 * Class:     ScopedPhysicalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_newMemBlock
(JNIEnv* env, jobject ScopedPhysicalMemory, jobject realtimeThread) {
#ifndef WITH_FAKE_SCOPES
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}

void* RTJ_malloc(size_t size) {
  printf("Did you forget to compile --with-realtime-java?\n");
  return NULL;
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    throwIllegalAssignmentError
 * Signature: (Ljava/lang/Object;Ljavax/realtime/MemoryArea;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_throwIllegalAssignmentError
(JNIEnv* env, jobject fromMA, jobject toObj, jobject toMA) {
  jclass excls;
#ifdef WITH_FAKE_SCOPES
  excls = (*env)->FindClass(env, "javax/realtime/IllegalAssignmentError");
  (*env)->ThrowNew(env, excls, "illegal assignment detected");
#else
  printf("Did you forget to compile --with-realtime-java?\n");
#endif
}