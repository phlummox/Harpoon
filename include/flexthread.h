/* FLEX thread definitions */
#ifndef INCLUDED_FLEXTHREAD_H
#define INCLUDED_FLEXTHREAD_H
#include "config.h"

#ifdef WITH_HEAVY_THREADS
#include <pthread.h>
#include <sched.h>	/* for sched_yield */
#define pthread_yield_np	sched_yield


#ifndef HAVE_PTHREAD_RWLOCK_T
/* work-around for missing read/write lock. */
/* a mutex is a conservative approximation to a read/write lock. */
#define pthread_rwlock_t	pthread_mutex_t
#define pthread_rwlock_init	pthread_mutex_init
#define pthread_rwlock_rdlock	pthread_mutex_lock
#define pthread_rwlock_wrlock	pthread_mutex_lock
#define pthread_rwlock_unlock	pthread_mutex_unlock
#define pthread_rwlock_destroy	pthread_mutex_destroy
#endif /* !HAVE_PTHREAD_RWLOCK_T */

/* Make sure the BDW collector has a chance to redefine 
 * pthread_create/sigmask/join for its own nefarious purposes. */
#ifndef FLEXTHREAD_TYPEDEFS_ONLY/*sometimes we don't want to pull all this in*/
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#endif /* FLEXTHREAD_TYPEDEFS_ONLY */

#endif /* WITH_HEAVY_THREADS */

#ifdef WITH_PTH_THREADS
#define PTH_SYSCALL_SOFT 1
#include <pth.h>
#include <errno.h>

#ifdef BDW_CONSERVATIVE_GC
# error GC wants to redefine pthread_create/pthread_sigmask/pthread_join
#endif

#define PTHERR(x) ((x)?0:errno)

#define pthread_t			pth_t
#define pthread_self			pth_self
#define pthread_equal(x,y)		((x) == (y))
#define pthread_yield_np()		({ pth_yield(NULL); 0; })

#define pthread_create(thread,attr,start,arg)				\
    ({ pth_attr_t *_na = (attr);					\
       *(thread)=pth_spawn(_na?*_na:PTH_ATTR_DEFAULT,start, arg);	\
       *(thread) ? 0 : EAGAIN; })
#define pthread_attr_t			pth_attr_t
#define pthread_attr_init(attr)	\
    ({ *(attr)=pth_attr_new(); *(attr) ? 0 : errno; })
#define pthread_attr_destroy(attr) \
    ({ pth_attr_destroy(*(attr)); *(attr)=NULL; 0; })
#define PTHREAD_CREATE_DETACHED 0
#define PTHREAD_CREATE_JOINABLE 1
#define pthead_attr_setdetachstate(attr, state) \
    PTHERR(pth_attr_set(*(attr), PTH_ATTR_JOINABLE, state))

#define PTHREAD_MUTEX_INITIALIZER	PTH_MUTEX_INIT
#define pthread_mutex_t			pth_mutex_t
#define pthread_mutex_init(m, a)	PTHERR(pth_mutex_init((m)))
#define pthread_mutex_lock(m)	    PTHERR(pth_mutex_acquire((m), FALSE, NULL))
#define pthread_mutex_trylock(m)    PTHERR(pth_mutex_acquire((m), TRUE, NULL))
#define pthread_mutex_unlock(m)		PTHERR(pth_mutex_release((m)))
#define pthread_mutex_destroy(m)	(/* do nothing */0)

#define PTHREAD_RWLOCK_INITIALIZER	PTH_RWLOCK_INIT
#define pthread_rwlock_t		pth_rwlock_t
#define pthread_rwlock_init(l, a)	PTHERR(pth_rwlock_init((l)))
#define pthread_rwlock_rdlock(l)	PTHERR(pth_rwlock_acquire((l), \
					       PTH_RWLOCK_RD, FALSE, NULL))
#define pthread_rwlock_tryrdlock(l)	PTHERR(pth_rwlock_acquire((l), \
					       PTH_RWLOCK_RD, TRUE, NULL))
#define pthread_rwlock_wrlock(l)	PTHERR(pth_rwlock_acquire((l), \
                                               PTH_RWLOCK_RW, FALSE, NULL))
#define pthread_rwlock_trywrlock(l)	PTHERR(pth_rwlock_acquire((l), \
					       PTH_RWLOCK_RW, TRUE, NULL))
#define pthread_rwlock_unlock(l)	PTHERR(pth_rwlock_release((l)))
#define pthread_rwlock_destroy(l)	(/* do nothing */0)

#define PTHREAD_COND_INITIALIZER	PTH_COND_INIT
#define pthread_cond_t			pth_cond_t
#define pthread_cond_init(c, a)		PTHERR(pth_cond_init((c)))
#define pthread_cond_broadcast(c)	PTHERR(pth_cond_notify((c), TRUE))
#define pthread_cond_signal(c)		PTHERR(pth_cond_notify((c), FALSE))
#define pthread_cond_wait(c, m)		PTHERR(pth_cond_await((c), (m), NULL))
#define pthread_cond_destroy(c)		(/* do nothing */0)
#define pthread_cond_timedwait(c, m, abstime)			\
({ const struct timespec *_abstime = (abstime);			\
   pth_event_t _ev = pth_event(PTH_EVENT_TIME|PTH_MODE_STATIC,	\
			       &flex_timedwait_key,		\
			       pth_time(_abstime->tv_sec,	\
					_abstime->tv_nsec/1000));\
   (!pth_cond_await((c), (m), _ev)) ? errno :			\
   pth_event_occurred(_ev) ? ETIMEDOUT : 0; })
extern pth_key_t flex_timedwait_key; /* defined in java_lang_Thread.c */

/* thread-specific key creation */
#define pthread_key_t			pth_key_t
#define pthread_key_create(k,d)		PTHERR(pth_key_create((k),(d)))
#define pthread_key_delete(k)		PTHERR(pth_key_delete((k)))
#define pthread_setspecific(k,v)	PTHERR(pth_key_setdata((k),(v)))
#define pthread_getspecific(k)		pth_key_getdata((k))

#endif /* WITH_PTH_THREADS */


/* define flex_mutex_t ops. */
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS

#define FLEX_MUTEX_INITIALIZER	PTHREAD_MUTEX_INITIALIZER
#define flex_mutex_t		pthread_mutex_t
#define flex_mutex_init(x)	pthread_mutex_init((x), NULL)
#define flex_mutex_lock		pthread_mutex_lock
#define flex_mutex_unlock	pthread_mutex_unlock
#define flex_mutex_destroy	pthread_mutex_destroy

#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS */

/* define flex_mutex_t ops. */
#if WITH_USER_THREADS
#include "../src/user/threads.h"

/* work-around for missing read/write lock. */
/* a mutex is a conservative approximation to a read/write lock. */
#define pthread_rwlock_t	pthread_mutex_t
#define pthread_rwlock_init	pthread_mutex_init
#define pthread_rwlock_rdlock	pthread_mutex_lock
#define pthread_rwlock_wrlock	pthread_mutex_lock
#define pthread_rwlock_unlock	pthread_mutex_unlock
#define pthread_rwlock_destroy	pthread_mutex_destroy
#define pthread_self()		gtl
#define pthread_t               struct thread_list

#define FLEX_MUTEX_INITIALIZER	USER_MUTEX_INITIALIZER
#define flex_mutex_t		user_mutex_t
#define flex_mutex_init(x)	user_mutex_init((x), NULL)
#define flex_mutex_lock		user_mutex_lock
#define flex_mutex_unlock	user_mutex_unlock
#define flex_mutex_destroy	user_mutex_destroy


#define PTHREAD_MUTEX_INITIALIZER  USER_MUTEX_INITIALIZER
#define pthread_mutex_t		user_mutex_t
#define pthread_mutex_init(x,a)	user_mutex_init((x), NULL)
#define pthread_mutex_lock		user_mutex_lock
#define pthread_mutex_unlock	user_mutex_unlock
#define pthread_mutex_destroy	user_mutex_destroy
#define pthread_mutex_trylock   user_mutex_trylock

#define PTHREAD_COND_INITIALIZER	USER_COND_INIT
#define pthread_cond_t			user_cond_t
#define pthread_cond_init		user_cond_init
#define pthread_cond_broadcast	        user_cond_broadcast
#define pthread_cond_signal		user_cond_signal
#define pthread_cond_wait		user_cond_wait
#define pthread_cond_destroy		user_cond_destroy
#define pthread_cond_timedwait          user_cond_timedwait


#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS */

/* simply declarations to avoid lots of tedious #ifdef WITH_THREAD'ing. */
/* declare nop-variants of mutex ops if WITH_THREADS not defined */
#ifdef WITH_THREADS
#define FLEX_MUTEX_DECLARE_STATIC(name) \
	static flex_mutex_t name = FLEX_MUTEX_INITIALIZER
#define FLEX_MUTEX_LOCK flex_mutex_lock
#define FLEX_MUTEX_UNLOCK flex_mutex_unlock
#else /* if WITH_THREADS not defined, then mutex lock/unlock does nothing. */
#define FLEX_MUTEX_DECLARE_STATIC(name)
#define FLEX_MUTEX_LOCK(x) 0
#define FLEX_MUTEX_UNLOCK(x) 0
#endif /* WITH_THREADS */

#endif /* INCLUDED_FLEXTHREAD_H */


