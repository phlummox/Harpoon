/* MemBlock.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "MemBlock.h"

#ifdef RTJ_DEBUG_REF
struct PTRinfo* ptr_info = NULL;
#endif

#ifdef WITH_PRECISE_GC
struct GCinfo* gc_info = NULL;
#endif

struct RefInfo* RefInfo_new(int reuse) {
  struct RefInfo* ri = (struct RefInfo*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct RefInfo));
#ifdef RTJ_DEBUG
  printf("0x%08x = RefInfo_new(%d)\n", ri, reuse);
#endif
  ri->refCount = 0;
  ri->reuse = reuse;
  return ri;
}

struct MemBlock* MemBlock_new(JNIEnv* env, 
			      jobject memoryArea, 
			      jobject realtimeThread,
			      struct MemBlock* superBlock) {
  struct MemBlock* mb = (struct MemBlock*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct MemBlock));
  struct BlockInfo* bi = (struct BlockInfo*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct BlockInfo));
  jclass memoryAreaClass;
  jclass realtimeThreadClass; 
  jmethodID methodID;
#ifdef RTJ_DEBUG_REF
  struct PTRinfo* old_ptr_info;
#endif
#ifdef RTJ_DEBUG
  checkException();
#endif
#ifdef RTJ_DEBUG_REF
  flex_mutex_lock(&ptr_info_lock);
  old_ptr_info = ptr_info;
  ptr_info = (struct PTRinfo*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct PTRinfo));
  ptr_info->next = old_ptr_info;
  ptr_info->memBlock = mb;
  flex_mutex_unlock(&ptr_info_lock);
#endif
  mb->block_info = bi;
  memoryAreaClass = (*env)->GetObjectClass(env, memoryArea);
#ifdef RTJ_DEBUG
  checkException();
#endif
  realtimeThreadClass = (*env)->GetObjectClass(env, realtimeThread);
#ifdef RTJ_DEBUG
  checkException();
#endif
  bi->memoryArea = (*env)->NewGlobalRef(env, memoryArea);
#ifdef RTJ_DEBUG
  checkException();
#endif
  bi->realtimeThread = (*env)->NewGlobalRef(env, realtimeThread);
#ifdef RTJ_DEBUG
  checkException();
#endif
  methodID = (*env)->GetMethodID(env, memoryAreaClass, 
				 "newMemBlock",
				 "(Ljavax/realtime/RealtimeThread;)V");
#ifdef RTJ_DEBUG
  checkException();
  printf("MemBlock_new(%s, %s, 0x%08x)\n", 
	 FNI_GetClassInfo(memoryAreaClass)->name, 
	 FNI_GetClassInfo(realtimeThreadClass)->name,
	 superBlock);
  printf("  methodID: 0x%08x\n", methodID);
  checkException();
#endif
  bi->superBlock = superBlock;
  while (superBlock != NULL) {
#ifdef RTJ_DEBUG
    checkException();
#endif
    MemBlock_INCREF(superBlock);
#ifdef RTJ_DEBUG
    assert(superBlock != superBlock->block_info->superBlock);
#endif
    superBlock = superBlock->block_info->superBlock;
  }
#ifdef RTJ_DEBUG
  checkException();
#endif
  getInflatedObject(env, realtimeThread)->temp = mb;
#ifdef RTJ_DEBUG
  checkException();
#endif
  (*env)->CallVoidMethod(env, memoryArea, methodID, realtimeThread);
#ifdef RTJ_DEBUG
  checkException();
#endif
#ifdef RTJ_DEBUG_REF
  mb->ptr_info = NULL;
  flex_mutex_init(&(mb->ptr_info_lock));
#endif
  MemBlock_INCREF(mb);
  return mb;
}

inline struct inflated_oobj* getInflatedObject(JNIEnv* env, 
					       jobject obj) {
#ifdef RTJ_DEBUG
  printf("getInflatedObject(0x%08x, 0x%08x)\n", env, obj);
  checkException();
#endif
  if (!FNI_IS_INFLATED(obj)) {
#ifdef RTJ_DEBUG
    printf("  inflating...\n");
#endif
    FNI_InflateObject(env, obj);
#ifdef RTJ_DEBUG
    checkException();
#endif
  }
  return FNI_UNWRAP_MASKED(obj)->hashunion.inflated;
}

void checkException() {
#ifdef RTJ_DEBUG
  JNIEnv* env = FNI_GetJNIEnv();
  if ((*env)->ExceptionOccurred(env)) {
    (*env)->ExceptionDescribe(env);
    printf("\nException occurred, quitting!\n");
    exit(0);
  }
#endif
}

const char* className(jobject obj) {
  return FNI_GetClassInfo((jclass)(FNI_UNWRAP_MASKED(obj)->claz))->name;
}

inline long MemBlock_INCREF(struct MemBlock* memBlock) {
  long* refcount = &(memBlock->ref_info->refCount);
#ifdef RTJ_DEBUG
  printf("MemBlock_INCREF(0x%08x): %d -> ", memBlock, *refcount); 
  checkException();
#endif
  atomic_add((uint32_t*)refcount, 1);
#ifdef RTJ_DEBUG
  printf("%d \n", *refcount);
#endif
  return *refcount;
}

inline long MemBlock_DECREF(struct MemBlock* memBlock) {
  long* refcount = &(memBlock->ref_info->refCount);
#ifdef RTJ_DEBUG
  printf("MemBlock_DECREF(0x%08x): %d -> ", memBlock, *refcount);
  checkException();
#endif
  atomic_add((uint32_t*)refcount, -1);
#ifdef RTJ_DEBUG
  printf("%d \n", *refcount);
#endif
  return *refcount;
}

inline void* MemBlock_alloc(struct MemBlock* memBlock, 
			    size_t size) {
#ifdef RTJ_DEBUG
  printf("MemBlock_alloc(0x%08x, %d)\n", (int)memBlock, size);
  checkException();
#endif
  if (!memBlock) { 
#ifdef RTJ_DEBUG
    printf("No memBlock yet...\n");
#endif
    return HeapMemory_alloc(size);
  }
  return memBlock->block_info->alloc(memBlock, size);
}

inline void MemBlock_free(struct MemBlock* memBlock) {
  struct MemBlock* superBlock;
#ifdef RTJ_DEBUG_REF
  struct PTRinfo* local_ptr_info;
#endif
#ifdef RTJ_DEBUG
  printf("MemBlock_free(0x%08x)\n", (int)memBlock);
  checkException();
#endif
  if (superBlock = memBlock->block_info->superBlock) {
      if (MemBlock_DECREF(memBlock)) {
#ifdef RTJ_DEBUG
	  printf("  decrementing reference count: %d\n", 
		 memBlock->ref_info->refCount);
#endif
      } else {
#ifdef RTJ_DEBUG
	  printf("  freeing memory!\n");
#endif
#ifdef RTJ_DEBUG_REF
	  /* Free each of the items in the MemBlock-local ptr_info list */
	  local_ptr_info = memBlock->ptr_info;
	  while (local_ptr_info != NULL) {
	    printf("    0x%08x allocated at %s:%d, %d bytes freed!\n", 
		   local_ptr_info->ptr, local_ptr_info->file, local_ptr_info->line, 
		   local_ptr_info->size);
	    RTJ_FREE(local_ptr_info);
	    local_ptr_info = local_ptr_info->next;
	  }
	  /* Remove the MemBlock from the global ptr_info list */
	  flex_mutex_lock(&ptr_info_lock);
	  if (ptr_info != NULL) {
	    if ((local_ptr_info = ptr_info)->memBlock == memBlock) {
	      ptr_info = ptr_info->next;
	    } else {
	      while ((local_ptr_info->next != NULL) &&
		     (local_ptr_info->next->memBlock != memBlock)) {
		local_ptr_info = local_ptr_info->next;
	      }
	      if (local_ptr_info->next != NULL) {
		local_ptr_info->next = local_ptr_info->next->next;
	      }
	    }
	  }
	  flex_mutex_unlock(&ptr_info_lock);
#endif
	  memBlock->block_info->free(memBlock);
#ifdef RTJ_DEBUG
	  checkException();
#endif
	  MemBlock_free(superBlock);
#ifdef RTJ_DEBUG
	  checkException();
#endif
      }
  }
}

inline struct MemBlock* MemBlock_prevMemBlock(struct MemBlock* memBlock) {
#ifdef RTJ_DEBUG
  printf("MemBlock_prevMemBlock(0x%08x)\n", memBlock);
  checkException();
#endif
  return memBlock->block_info->superBlock;
}

#ifdef WITH_PRECISE_GC
void find_RTJ_roots() {
  struct GCinfo* current = gc_info;
#ifdef RTJ_DEBUG
  printf("find_RTJ_roots()\n");
  checkException();
#endif
  while (current != NULL) {
#ifdef RTJ_DEBUG
    printf("  MemBlock: 0x%08x\n", current->memBlock);
#endif
    current->memBlock->block_info->gc(current->memBlock);
#ifdef RTJ_DEBUG
    checkException();
#endif
    current = current->next;
  }
}
#endif

#ifdef WITH_PRECISE_GC
inline void add_MemBlock_to_roots(struct MemBlock* mem) {
  struct GCinfo* new_gc_info = 
    (struct GCinfo*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct GCinfo));
#ifdef RTJ_DEBUG
  printf("add_MemBlock_to_roots(0x%08x)\n", mem);
  checkException();
#endif
  new_gc_info->memBlock = mem;

  flex_mutex_lock(&gc_info_lock);
#ifdef RTJ_DEBUG
  checkException();
#endif
  new_gc_info->next = gc_info;
  gc_info = new_gc_info;
#ifdef RTJ_DEBUG
  printf("  roots: ");
  while (new_gc_info != NULL) {
    printf("0x%08x, ", new_gc_info->memBlock);
    new_gc_info = new_gc_info->next;
  }
  printf("\n");
#endif
  flex_mutex_unlock(&gc_info_lock);
#ifdef RTJ_DEBUG
  checkException();
#endif
}
#endif

#ifdef WITH_PRECISE_GC
inline void remove_MemBlock_from_roots(struct MemBlock* mem) {
  struct GCinfo* current;
  flex_mutex_lock(&gc_info_lock);
#ifdef RTJ_DEBUG  
  printf("remove_MemBlock_from_roots(0x%08x)\n", mem);
  checkException();
  printf("  BEFORE roots: ");
  current = gc_info;
  while (current != NULL) {
    printf("0x%08x, ", current->memBlock); 
    current = current->next;
  }
  printf("\n");
#endif
  if (gc_info != NULL) {
    if ((current = gc_info)->memBlock == mem) {
      struct GCinfo* next = gc_info->next;
      RTJ_FREE(gc_info);
      gc_info = next;
    } else {
      while ((current->next != NULL) && 
	     (current->next->memBlock != mem)) {
	current = current->next;
      }
      if (current->next != NULL) {
	struct GCinfo* next = current->next->next;
	RTJ_FREE(current->next);
	current->next = next;
      } 
#ifdef RTJ_DEBUG
      else {
	printf("  NOT FOUND!!!\n");
      }
#endif
    }
  } 
#ifdef RTJ_DEBUG
  else {  
    printf("  NOT FOUND!!! (gc_info is null)\n");
  }
  printf("  AFTER roots: ");
  current = gc_info;
  while (current != NULL) {
    printf("0x%08x, ", current->memBlock); 
    current = current->next;
  }
  printf("\n");
#endif
  flex_mutex_unlock(&gc_info_lock);
}
#endif

#ifdef RTJ_DEBUG_REF
void printPointerInfo(void* obj, int getClassInfo) {
  struct MemBlock* memBlock;
  struct PTRinfo* topPtrInfo;
  struct PTRinfo* ptrInfo = NULL;
  JNIEnv* env = FNI_GetJNIEnv();
  flex_mutex_lock(&ptr_info_lock);
  topPtrInfo = ptr_info;
  printf("pointer at 0x%08x ", obj);
  while ((topPtrInfo != NULL)&&(ptrInfo == NULL)) {
    for (ptrInfo = (memBlock = topPtrInfo->memBlock)->ptr_info;
	 (ptrInfo != NULL)&&(ptrInfo->ptr != obj); 
	 ptrInfo = ptrInfo->next) {}
    topPtrInfo = topPtrInfo->next;
  }
  if (ptrInfo == NULL) {
    printf("not found in MemBlocks\n");
    if (getClassInfo) {
      printf("pointing to a %s of size %d\n", className(obj), 
	     FNI_ObjectSize(FNI_UNWRAP_MASKED(obj)));
    } 
  } else {
    printf("found in MemBlock = 0x%08x, \n", memBlock);
    if (memBlock->block_info->memoryArea == NULL) {
      printf("allocated during the main thread in the initial HeapMemory\n");
    } else {
      if (getClassInfo) {
	printf("belonging to %s = 0x%08x\n",
	       className(memBlock->block_info->memoryArea), 
	       memBlock->block_info->memoryArea);
	printf("allocated during execution of %s = 0x%08x\n", 
	       className(memBlock->block_info->realtimeThread), 
	       memBlock->block_info->realtimeThread);
      } else {
	printf("belonging to MemoryArea = 0x%08x\n", 
	       memBlock->block_info->memoryArea);
	printf("allocated during execution of 0x%08x\n", 
	       memBlock->block_info->realtimeThread);
      }
    }
    printf("at location %s:%d", ptrInfo->file, ptrInfo->line);
    if (getClassInfo) {
      printf(" pointing to an %s of size %d\n", className(obj), ptrInfo->size);
    } else {
      printf(" pointing to a location of size %d\n", ptrInfo->size);
    }
  }
  flex_mutex_unlock(&ptr_info_lock);
}

void dumpMemoryInfo(int classInfo) {
  struct PTRinfo *topPtrInfo, *ptrInfo;
  struct MemBlock* memBlock;
  flex_mutex_lock(&ptr_info_lock);
  topPtrInfo = ptr_info;
  while (topPtrInfo != NULL) {
    if (classInfo) {
      printf("%s (0x%08x) x %s (0x%08x):\n", 
	     className(topPtrInfo->memBlock->block_info->memoryArea), 
	     topPtrInfo->memBlock->block_info->memoryArea,
	     className(topPtrInfo->memBlock->block_info->realtimeThread),
	     topPtrInfo->memBlock->block_info->realtimeThread);
    } else {
      printf("MemoryArea (0x%08x) x Thread (0x%08x)\n",
	     topPtrInfo->memBlock->block_info->memoryArea,
	     topPtrInfo->memBlock->block_info->realtimeThread);
    }
    printf("  ");
    for (ptrInfo = (memBlock = topPtrInfo->memBlock)->ptr_info;
	 (ptrInfo != NULL); ptrInfo = ptrInfo->next) {
      printf("0x%08x (%s:%d of %d bytes), ", ptrInfo->ptr, ptrInfo->file, ptrInfo->line, ptrInfo->size);
    }
    printf("\n");
    topPtrInfo = topPtrInfo->next;
  }

  flex_mutex_unlock(&ptr_info_lock);
}

#endif

#ifdef WITH_NOHEAP_SUPPORT
inline void _heapCheck_leap(struct oobj* ptr, const int line, const char* file) {
#ifdef WITH_PRECISE_GC
  JNIEnv* env;
  if ((((int)ptr)&1)&&(((struct FNI_Thread_State*)(env = FNI_GetJNIEnv()))->noheap)) {
    char desc[200];
    jclass excls = (*env)->FindClass(env, "java/lang/IllegalAccessException");
    snprintf(desc, 200, "attempted heap reference 0x%08x in native code at %s:%d\n", 
	     ptr, file, line); 
    (*env)->ThrowNew(env, excls, desc);
#ifdef RTJ_DEBUG
    checkException();
#endif
  }  
#endif
}

#ifdef RTJ_DEBUG_REF
inline void heapCheckRef(struct oobj* ptr, const int line, const char* file, 
			 const char* op) {
#else
inline void heapCheckJava(struct oobj* ptr) {
#endif 
  JNIEnv* env = FNI_GetJNIEnv();
  if (((struct FNI_Thread_State*)env)->noheap) {
    char desc[400];
    jclass excls = (*env)->FindClass(env, "java/lang/IllegalAccessException");
#ifdef RTJ_DEBUG
#ifdef RTJ_DEBUG_REF
    printf("attempted heap reference 0x%08x in java code at %s:%d during %s\n", 
	   ptr, file, line, op);
    printPointerInfo(ptr, 0);
    exit(1);
#else
    printf("attempted heap reference 0x%08x in java code\n", ptr);
    exit(1);
#endif
#endif
    (*env)->ThrowNew(env, excls, 
		     "Illegal heap access.  Use RTJ_DEBUG_REF for more information.");
#ifdef RTJ_DEBUG
    checkException();
#endif
  }
}

inline int IsNoHeapRealtimeThread(JNIEnv *env, 
				  jobject realtimeThread) {
#ifdef RTJ_DEBUG  
  printf("IsNoHeapRealtimeThread(0x%08x, 0x%08x)\n", env, realtimeThread);
  checkException();
#endif
  return (*env)->IsInstanceOf(env, realtimeThread, 
			      (*env)->
			      FindClass(env, 
					"javax/realtime/NoHeapRealtimeThread")) 
    == JNI_TRUE;
}
#endif
