#ifndef ROLE_INFER
#define ROLE_INFER


#include "ObjectSet.h"
#include "ObjectPair.h"
#include "Hashtable.h"
#include "GenericHashtable.h"
#include "Names.h"
#include <stdio.h>

#define EFFECTS

struct heap_object {
  struct classname *class; /*class of object*/
  long long uid; /* unique id of object */
  struct fieldlist *fl; /*list of objects field*/
  struct arraylist *al; /*list of objects array elements*/
  struct referencelist *rl;/*reachable from these roots*/

  struct fieldlist *reversefield;/* Objects pointing at us*/
  struct arraylist *reversearray;/* arrays pointing at us*/

  struct rolechange *rc; /* track role changes outside of methods*/
  int * methodscalled; /* array of methods called on heap object*/

  int reachable;/* low order bit=reachable*/
  /* 2nd bit=root*/
  /* if OPTION_FCONTAINERS set
   * 3rd& 4th bit: 3rd bit 0 until first heap object reference, then 1
   *               4th bit 0 if suitable as container, 1 if not suitable
   *               We include heap objects that have state 3rd=1, 4th=0 for output*/
};
#define REACHABLEMASK 0x03
#define OTHERMASK 0x0C
#define FIRSTREF 0x04
#define NOTCONTAINER 0x08

#define stringsize 1000
#define compilernamesize 100
#define sourcenamesize 200


struct method {
  struct methodname * methodname; /* method name structure*/
  struct method *caller; /* caller of this method*/
  struct localvars *lv; /* list of this methods local variables */
  struct heap_object ** params; /* List of parameters called with */
  struct rolemethod * rm; /* Appropriate role instantiated method for this method*/
  int numobjectargs; /* Number of object type arguments*/
  short isStatic; /* Is method static */
  struct genhashtable *rolechangetable;
#ifdef EFFECTS
  struct hashtable * pathtable; /* table of paths for effect use*/
  struct effectlist *effects; /* list of effects for methods */
#endif
};

/* Data structure to keep track of references to kill in kill stage of
   incremental reachability analysis*/
struct killtuplelist {
  struct heap_object * ho;
  struct referencelist * rl;
  struct killtuplelist * next;
  short reachable; /* Native reachable bits */
};

struct referencelist {
  struct globallist *gl;
  struct localvars *lv;
  struct referencelist *next;
};

struct globallist {
  struct fieldname * fieldname;
  struct heap_object *object;
  struct globallist * next;
  long long age;
  short invalid;
};

struct fieldlist {
  struct fieldname * fieldname;
  struct heap_object *src;
  struct fieldlist * dstnext; /*Next fieldlist entry for reverse list*/
  struct heap_object *object;
  struct fieldlist * next; /* Next fieldlist entry for forward list*/
  char propagaterole;
};

struct arraylist {
  int index;
  struct heap_object *src;
  struct arraylist *dstnext;
  struct heap_object *object;
  struct arraylist * next;
  char propagaterole;
};

struct localvars {
  struct heap_object *object;
  long int linenumber;
  char name[sourcenamesize];
  char sourcename[compilernamesize];
  struct method *m;
  int lvnumber;
  struct localvars *next;
  long long age;
  short invalid;/* if 1, then the reference isn't valid anymore...it has been killed*/
};

struct heap_state {
  /* Pointer to top of method stack */
  struct namer * namer;
  struct method *methodlist;
  struct globallist *gl;
  struct referencelist *newreferences;

  struct method *freemethodlist;
  struct referencelist *freelist;
  struct objectpair * K;
  struct objectset * N;

  struct objectset *changedset;
  struct genhashtable *roletable;
  struct genhashtable *reverseroletable;
  struct genhashtable *methodtable;
  struct genhashtable *rolereferencetable;

  long long currentmethodcount;
  struct genhashtable *atomicmethodtable;
  
  struct genhashtable *statechangemethodtable;
  struct genhashtable *includedfieldtable;
  struct genhashtable *excludedclasstable;
  struct hashtable *statechangereversetable;
  int statechangesize;
  unsigned int options;

  FILE *container;
  struct hashtable *containedobjects;

  struct genhashtable *policytable;

  char *prefix;
  FILE *rolefile, *methodfile, *dotfile,*rolediagramfile, *rolediagramfilemerge;
};

#define OPTION_FCONTAINERS 0x1
#define OPTION_DEFAULTONEATTIME 0x40
#define OPTION_UCONTAINERS 0x2
#define OPTION_NORCEXPR 0x4
#define OPTION_NOEFFECTS 0x8
#define OPTION_LIMITFIELDS 0x10
#define OPTION_LIMITARRAYS 0x20
#define OPTION_WEB 0x40
#define OPTION_ECLASS 0x80


struct identity_relation {
  struct fieldname * fieldname1;
  struct fieldname * fieldname2;
  struct identity_relation * next;
};

struct statechangeinfo {
  int id;
};

void doincrementalreachability(struct heap_state *hs, struct hashtable *ht, int enterexit);
struct objectset * dokills(struct heap_state *hs, struct hashtable *ht);
void donews(struct heap_state *hs, struct objectset * os, struct hashtable *ht);
void removelvlist(struct heap_state *, char * lvname, struct method * method);
void addtolvlist(struct heap_state *,struct localvars *, struct method *);
void freemethod(struct heap_state *heap, struct method * m);
void getfile();
void doanalysis(int argc, char **argv);
char *getline();
char * copystr(const char *);
void showmethodstack(struct heap_state * heap);
void printmethod(struct method m);
void dofieldassignment(struct heap_state *hs, struct heap_object * src, struct fieldname * field, struct heap_object * dst);
void doarrayassignment(struct heap_state *hs, struct heap_object * src, int lindex, struct heap_object *dst);
void doglobalassignment(struct heap_state *hs, struct fieldname * field, struct heap_object * dst);
void doaddfield(struct heap_state *hs, struct heap_object *ho);
void dodelfield(struct heap_state *hs, struct heap_object *src,struct heap_object *dst);
void freelv(struct heap_state *hs,struct localvars * lv);
void freeglb(struct heap_state *hs,struct globallist * glb);
void dodellvfield(struct heap_state *hs, struct localvars *src, struct heap_object *dst);
void dodelglbfield(struct heap_state *hs, struct globallist *src, struct heap_object *dst);
int matchrl(struct referencelist *key, struct referencelist *list);
void freekilltuplelist(struct killtuplelist * tl);
void doaddglobal(struct heap_state *hs, struct globallist *gl);
void doaddlocal(struct heap_state *hs, struct localvars *lv);
void propagaterinfo(struct objectset * set, struct heap_object *src, struct heap_object *dst);
int lvnumber(char *lv);
int matchlist(struct referencelist *list1, struct referencelist *list2);
void removereversearrayreference(struct arraylist * al);
void removereversefieldreference(struct fieldlist * al);
void removeforwardarrayreference(struct arraylist * al);
void removeforwardfieldreference(struct fieldlist * al);
void freemethodlist(struct heap_state *hs);
void calculatenumobjects(struct method * m);
void doreturnmethodinference(struct heap_state *heap, long long uid, struct hashtable *ht);
int atomic(struct heap_state *heap);
void loadatomics(struct heap_state *heap);
int atomicmethod(struct heap_state *hs, struct method *m);
void atomiceval(struct heap_state *heap);
void loadstatechange(struct heap_state *heap);
int convertnumberingobjects(char *sig, int isStatic, int orignumber);
void parseoptions(int argc, char **argv, struct heap_state *heap);
void openoutputfiles(struct heap_state *heap);
void checksafety(struct heap_object *src, struct heap_object *dst);
#endif
