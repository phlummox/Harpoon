/* ImmortalMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "ImmortalMemory.h"

/*
 * Class:     ImmortalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
  struct MemBlock* mb = MemBlock_new(env, memoryArea);
#ifdef RTJ_DEBUG
  printf("ImmortalMemory.initNative(0x%08x, 0x%08x, %d)\n",
	 env, memoryArea, (size_t)size);
  checkException();
#endif
  mb->alloc_union.lls       = LListAllocator_new(0);
  mb->alloc                 = Immortal_MemBlock_alloc;
  mb->finalize              = Immortal_MemBlock_finalize;
  MemBlock_INCREF(mb);
  mb->memoryArea            = (*env)->NewGlobalRef(env, memoryArea);
#ifdef WITH_PRECISE_GC
  mb->gc                    = Immortal_MemBlock_gc; /* must be the last set */
#endif
}

void* Immortal_MemBlock_alloc(struct MemBlock* mem, size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_MemBlock_alloc(0x%08x, %d)\n", mem, size);
#endif
  return LListAllocator_alloc(mem->alloc_union.lls, size);
}

#ifdef WITH_PRECISE_GC
void Immortal_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_MemBlock_gc(0x%08x)\n", mem);
#endif
  LListAllocator_gc(mem->alloc_union.lls);
}
#endif

void Immortal_MemBlock_finalize(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_MemBlock_finalize(0x%08x)\n", mem);
#endif
  LListAllocator_free(mem->alloc_union.lls);
}