// for the File System benchmark

#include <stdlib.h>
#include "DefaultGuidance2.h"
#include "model.h"
#include "dmodel.h"
#include "tmodel.h"
#include "element.h"
#include "common.h"
  /* This class tells the analysis stuff */
  /* For each set:
     1. Source for atoms if the set is too small - can be another set or function call (assumed to be no set)
     2. Source for atoms if relation requires atom of this set - can be another set or function call (assumed to be no set)
     3. Removal from set - where to insert objects from this set
     4. Insertion into set - which subset to put objects in  */

DefGuidance2::DefGuidance2(model *m) {
  globalmodel=m;
}

Source DefGuidance2::sourceforsetsize(char *set) {
  DomainRelation *dr=globalmodel->getdomainrelation();
  DomainSet *ds=dr->getset(set);
  while(dr->getsuperset(ds)!=NULL)
    ds=dr->getsuperset(ds);
  if(equivalentstrings(ds->getname(),"Block"))
     return Source(copystr("FreeBlock"));
  if(equivalentstrings(ds->getname(),"Inode"))
     return Source(copystr("FreeInode"));
}

Source DefGuidance2::sourceforrelation(char *set) {
  return Source(set);
}

char * DefGuidance2::removefromset(char * set) {
  DomainRelation *dr=globalmodel->getdomainrelation();
  DomainSet *ds=dr->getset(set);
  if (equivalentstrings(set,"token"))
    return NULL;

  if (equivalentstrings(dr->getsuperset(ds)->getname(),"UsedBlock"))
    return "FreeBlock";
  if (equivalentstrings(dr->getsuperset(ds)->getname(),"UsedInode"))
    return "FreeInode";

  DomainSet *ss=dr->getsuperset(ds);
  while(ss!=NULL&&ss->gettype()==DOMAINSET_PARTITION) {
    for(int i=0;i<ss->getnumsubsets();i++) {
      char *name=ss->getsubset(i);
      if (!equivalentstrings(ds->getname(),name)&&
	  !equivalentstrings(ds->getname(),name)) {
	/* Do search */
	ss=dr->getset(name);
	while(ss->gettype()==DOMAINSET_PARTITION) {
	  char *name=ss->getsubset(0);
	  ss=dr->getset(name);
	}
	return ss->getname();
      }
    }
    ds=ss;
    ss=dr->getsuperset(ss);
  }
  if (ss!=NULL)
    return ss->getname();
  else
    return NULL;
}

char * DefGuidance2::insertiontoset(char *set) {
  if (equivalentstrings(set,"token"))
    return NULL;
  if (equivalentstrings(set,"UsedBlock"))
    return "FileBlock";
  if (equivalentstrings(set,"UsedInode"))
    return "FileInode";
  
  DomainRelation *dr=globalmodel->getdomainrelation();
  DomainSet *ds=dr->getset(set);
  while (ds->gettype()==DOMAINSET_PARTITION) {
    ds=dr->getset(ds->getsubset(0));
    /* have to look for subset; */
  }
  return ds->getname();
}

Element * allocatebytes2(structure * st, model *m) {
  int size=st->getsize(m->getbitreader(),m,m->gethashtable());
  Element * e=new Element(new char[size],st);
  return e;
}
