#include <jni.h>
#include <jni-private.h> 

#include <assert.h>
#include "config.h"
#include "flexthread.h"



FLEX_MUTEX_DECLARE_STATIC(uid_mutex);
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

static int instr=1;

static int inited=0;
static jlong lastuid=1000000;

static jfieldID UIDfd=0; /* Field ID of Object.UID*/
static jfieldID strvalue; /* Field ID of String.value*/

static jsize utf8length(const jchar *buf, jsize len); 
static jsize toUTF8(const jchar *src, int srclen, char *dst);


static void initialize(JNIEnv *env) {
  jclass objcls,strcls;
  FLEX_MUTEX_LOCK(&init_mutex);
  if (!inited) {
    objcls=(*env)->FindClass(env, "java/lang/Object");
    strcls=(*env)->FindClass(env, "java/lang/String");
    UIDfd=(*env)->GetFieldID(env, objcls, "UID","J");
    strvalue=(*env)->GetFieldID(env, strcls, "value","[C");
    inited=1;
  }
  FLEX_MUTEX_UNLOCK(&init_mutex);
}

JNIEXPORT void JNICALL Java_java_lang_Object_assignUID(JNIEnv *env, jobject obj,jclass cls) {
  jlong id;
  void *classstr;
  if (!inited)
    initialize(env);
  FLEX_MUTEX_LOCK(&uid_mutex);
  id=lastuid;
  lastuid++;
  FLEX_MUTEX_UNLOCK(&uid_mutex);
  classstr=FNI_GetClassInfo(cls)->name;
  printf("UI: %s %lld\n",classstr,id);

  (*env)->SetLongField(env, obj, UIDfd, id);
}

void NativeassignUID(JNIEnv *env, jobject obj,jclass cls) {
  jlong id;
  void *classstr;
  if (!inited)
    initialize(env);
  FLEX_MUTEX_LOCK(&uid_mutex);
  id=lastuid;
  lastuid++;
  FLEX_MUTEX_UNLOCK(&uid_mutex);
  classstr=FNI_GetClassInfo(cls)->name;
  printf("NI: %s %lld\n",classstr,id);

  (*env)->SetLongField(env, obj, UIDfd, id);
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_arrayassign(JNIEnv *env, jclass cls, jobject array, jint index, jobject component) {
  jlong arrayuid, componentuid;
  arrayuid=(*env)->GetLongField(env, array, UIDfd);
  if (component!=NULL)
    componentuid=(*env)->GetLongField(env, component, UIDfd);
  else
    componentuid=-1;
  printf("AA: %lld %ld %lld\n",arrayuid,index,componentuid);
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_fieldassign(JNIEnv *env, jclass cls, jobject source, jobject field, jobject component) {
  jlong componentuid, sourceuid;
  jclass methodclass;
  char * fieldstr;
  char * classstr;

  if (source!=NULL)
    sourceuid=(*env)->GetLongField(env, source, UIDfd);
  else
    sourceuid=-1;
  //FIXME--THIS IS GLOBAL VARIABLE CASE!!!!!
  if (component!=NULL)
    componentuid=(*env)->GetLongField(env, component, UIDfd);
  else
    componentuid=-1;
  methodclass=FNI_WRAP(FNI_GetFieldInfo(field)->declaring_class_object);
  classstr=FNI_GetClassInfo(methodclass)->name;
  fieldstr=FNI_GetFieldInfo(field)->fieldID->name;
  printf("FA: %lld %s %s %lld\n",sourceuid,classstr,fieldstr,componentuid);
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_marklocal(JNIEnv *env, jclass cls, jstring localvar, jobject obj) {
  jlong objuid;
  jobject carray=(*env)->GetObjectField(env, localvar, strvalue);
  jchar *strchr=(*env)->GetCharArrayElements(env, carray, NULL);
  jsize length=(*env)->GetArrayLength(env, carray);
  jsize  newlen = utf8length(strchr, length);
  char * result = malloc(sizeof(char)*(newlen+1)); 
  if (obj!=NULL)
    objuid=(*env)->GetLongField(env, obj, UIDfd);
  else
    objuid=-1;
  toUTF8(strchr, length, result);
  result[newlen]='\0';
  printf("ML: %s %lld\n",result, objuid);
  free(result);
  (*env)->ReleaseCharArrayElements(env, carray, strchr, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_killlocal(JNIEnv *env, jclass cls, jstring localvar) {
  jobject carray=(*env)->GetObjectField(env, localvar, strvalue);
  jchar *strchr=(*env)->GetCharArrayElements(env, carray, NULL);
  jsize length=(*env)->GetArrayLength(env, carray);
  jsize  newlen = utf8length(strchr, length);
  char * result = malloc(sizeof(char)*(newlen+1)); 
  toUTF8(strchr, length, result);
  result[newlen]='\0';
  printf("KL: %s\n",result);
  free(result);
  (*env)->ReleaseCharArrayElements(env, carray, strchr, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_returnmethod(JNIEnv *env, jclass cls, jobject obj) {
  jlong objuid;
  if (obj!=NULL)
    objuid=(*env)->GetLongField(env, obj, UIDfd);
  else
    objuid=-1;
  printf("RM: %lld\n", objuid);
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_invokemethod(JNIEnv *env, jclass cls, jobject method, jint isstatic) {
  char * methodstr, * classstr, * methoddesc;
  methodstr=FNI_GetMethodInfo(method)->methodID->name;
  methoddesc=FNI_GetMethodInfo(method)->methodID->desc;

  classstr=FNI_GetClassInfo(FNI_WRAP(FNI_GetMethodInfo(method)->declaring_class_object))->name;


  printf("IM: %s %s %s %ld\n",classstr, methodstr,methoddesc,isstatic);
}

static jsize utf8length(const jchar *buf, jsize len) {
  jsize i=0, result=0;
  for (i=0; i<len; i++) {
    jchar c = buf[i];
    if (c == 0x0000) result+=2;
    else if (c >= 0x0001 && c <= 0x007F) result+=1;
    else if (c >= 0x0080 && c <= 0x07FF) result+=2;
    else if (c >= 0x0800 && c <= 0xFFFF) result+=3;
    else assert(0);
  }
  return result;
}

static jsize toUTF8(const jchar *src, int srclen, char *dst) {
  jsize i=0, r=0;
  for (i=0; i<srclen; i++) {
    jchar c = src[i];
    if (c >= 0x0001 && c <= 0x007F) {
      dst[r++]=(char)c;
    } else if (c==0x0000 ||
               (c>= 0x0080 && c <= 0x07FF)) {
      dst[r++]=0xC0 | (c>>6);
      dst[r++]=0x80 | (c&0x3F);
    } else if (c >= 0x0800 && c <= 0xFFFF) {
      dst[r++]=0xE0 | (c>>12);
      dst[r++]=0x80 | ((c>>6)&0x3F);
      dst[r++]=0x80 | (c&0x3F);
    } else assert(0);
  }
  return r;
}