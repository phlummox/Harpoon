// MZFChooser.java, created Mon Nov 12 22:20:58 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Util.Default.PairList;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>MZFChooser</code> adds code to instantiate the correct
 * MZF-compressed version of a class at run-time.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MZFChooser.java,v 1.6 2002-09-03 16:40:23 cananian Exp $
 */
class MZFChooser extends MethodMutator<Quad> {
    /** an oracle to determine the properties of constructors. */
    final ConstructorClassifier cc;
    /** a map from HClasses to a list of sorted fields; the splitting has
     *  been done in the order of the list. */
    final Map<HClass,List<PairList<HField,Number>>> listmap;
    /** a map from <code>HField</code>s to the <code>HClass</code> which
     *  eliminates that field. */
    final Map<HField,HClass> field2class;
    /** Creates a <code>MZFChooser</code>. */
    public MZFChooser(HCodeFactory hcf, ConstructorClassifier cc,
		      Map<HClass,List<PairList<HField,Number>>> listmap,
		      Map<HField,HClass> field2class) {
        super(QuadSSA.codeFactory(hcf));
	this.cc = cc;
	this.listmap = listmap;
	this.field2class = field2class;
    }
    protected String mutateCodeName(String codeName) {
	// input SSA, output RSSx.
	return QuadRSSx.codename;
    }
    protected HCodeAndMaps<Quad> cloneHCode(HCode<Quad> hc, HMethod newmethod){
	// make SSA into RSSx.
	assert hc.getName().equals(QuadSSA.codename);
	return MyRSSx.cloneToRSSx((harpoon.IR.Quads.Code)hc, newmethod);
    }
    private static class MyRSSx extends QuadRSSx {
	private MyRSSx(HMethod m) { super(m, null); }
	public static HCodeAndMaps<Quad> cloneToRSSx(harpoon.IR.Quads.Code c,
					       HMethod m) {
	    MyRSSx r = new MyRSSx(m);
	    return r.cloneHelper(c, r);
	}
    }
    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	HCode<Quad> hc = input.hcode();
	// first, find set of constructor-calls in this HCode.
	QuadFinder qfinder = new QuadFinder();
	for (Iterator<Quad> it=hc.getElementsI(); it.hasNext(); )
	    it.next().accept(qfinder);
	// for each constructor:
	for (Iterator<CALL> it=qfinder.constructors.iterator();
	     it.hasNext(); ) {
	    CALL qC = it.next();
	    assert !qC.isVirtual();
	    // match it to a 'NEW'.
	    NEW qN = qfinder.temp2new.get(qC.params(0));
	    if (qN==null) continue; // typically call to super-constructor.
	    // if we know anything about this constructor...
	    if (!cc.isGood(qC.method())) continue;
	    assert qN.hclass().equals(qC.method().getDeclaringClass()) : qN+" / "+qC;
	    // fetch field list for this class.
	    List<PairList<HField,Number>> sortedFields =
		listmap.get(qN.hclass());
	    if (sortedFields==null) continue; // nothing known about this class
	    // delete the NEW.
	    qN.remove();
	    // replace CALL with test-and-NEW-and-CALL.
	    refactor(qC.method(), qC, sortedFields);
	}
	return hc;
    }
    static class QuadFinder extends QuadVisitor {
	/** map temps to 'NEW's where they are defined. */
	Map<Temp,NEW> temp2new = new HashMap<Temp,NEW>();
	/** set of constructor CALLs. */
	Set<CALL> constructors = new HashSet<CALL>();
	public void visit(Quad q) { /* not interesting */ }
	public void visit(NEW q) {
	    assert !temp2new.containsKey(q.dst()) : "SSx form";
	    temp2new.put(q.dst(), q);
	}
	public void visit(CALL q) {
	    if (isConstructor(q.method()))
		constructors.add(q);
	}
	///////// copied from harpoon.Analysis.Quads.DefiniteInitOracle.
	/** return an approximation to whether this is a constructor
	 *  or not.  it's always safe to return false. */
	private static boolean isConstructor(HMethod hm) {
	    // this is tricky, because we want split constructors to
	    // count, too, even though renamed constructors (such as
	    // generated by initcheck, for instance) won't always be
	    // instanceof HConstructor.  Look for names starting with
	    // '<init>', as well.
	    if (hm instanceof HConstructor) return true;
	    if (hm.getName().startsWith("<init>")) return true;
	    // XXX: what about methods generated by RuntimeMethod Cloner?
	    // we could try methods ending with <init>, but then the
	    // declaringclass information would be wrong.
	    //if (hm.getName().endsWidth("<init>")) return true;//not safe yet.
	    return false;
	}
    }
    void refactor(HMethod constructor, CALL qC,
		  List<PairList<HField,Number>> sortedFields) {
	QuadFactory qf = qC.getFactory();
	if (sortedFields.size()==0) { // base case.
	    Quad qN = new NEW(qf, qC, qC.params(0),
			      qC.method().getDeclaringClass());
	    addAt(qC.prevEdge(0), qN);
	    return;
	}
	PairList<HField,Number> pair = sortedFields.get(0);
	HField hf = (HField) pair.get(0); // field
	Number num = (Number) pair.get(1); // 'mostly-what?'
	// lookup 'spiffy constructor'
	HClass newC = field2class.get(hf);
	HMethod newM = newC.getDeclaredMethod
	    (constructor.getName(), constructor.getDescriptor());
	// check whether this field is constant w/ this constructor.
	if (cc.isConstant(constructor, hf)) {
	    Object val = cc.constantValue(constructor, hf);
	    if (val==null ? num.longValue()==0 :
		(val instanceof Number && /* no String, etc constants */
		 ((Number)val).doubleValue()==num.doubleValue())) {
		// okay, always a constant of the right value.
		// we can replace original constructor with alternate.
		CALL qCN = new CALL(qf, qC, newM,
				    qC.params(), qC.retval(), qC.retex(),
				    qC.isVirtual(), qC.isTailCall(),
				    qC.dst(), qC.src());
		Quad.replace(qC, qCN);
		// now recursively invoke on rest of list.
		refactor(constructor, qCN,
			 sortedFields.subList(1, sortedFields.size()));
		return;
	    }
	}
	// check whether this field is a function of a param.
	// (oh, it also can't be a function of param#0, which isn't
	//  defined yet)
	if (cc.isParam(constructor, hf) &&
	    0 != cc.paramNumber(constructor, hf)) {
	    int which = cc.paramNumber(constructor, hf);
	    // test whether param(which) is equal to 'num'.
	    // if so, use specialized class, else fall back.
	    Temp tZ = new Temp(qf.tempFactory(), "mostly");
	    Temp tC = new Temp(qf.tempFactory(), "lucky");
	    // tZ = CONST(mostlyN)
	    // tC = CMPEQ(tZ, param(which))
	    // if (tC)
	    //   call spiffy constructor (recurse here)
	    // else
	    //   NEW boring class
	    //   call new boring constructor.
	    // PHI/return (output is SSx?)     PHI/throw
	    HClass ty = widen(hf.getType());
	    Quad q0 = ty.isPrimitive() ?
		new CONST(qf, qC, tZ, makeValue(ty, num), ty) :
		new CONST(qf, qC, tZ, null, HClass.Void);
	    assert ty.isPrimitive()?true:num.intValue()==0;
	    Quad q1 = new OPER(qf, qC, makeEqOp(ty), tC,
			       new Temp[] { tZ, qC.params(which) });
	    Quad q2 = new CJMP(qf, qC, tC, new Temp[0]);
	    CALL q3 = new CALL(qf, qC, newM /* spiffy */,
			       qC.params(), qC.retval(), qC.retex(),
			       qC.isVirtual(), qC.isTailCall(),
			       qC.dst(), qC.src());
	    assert qC.retval()==null; // constructor should be void
	    Quad q4 = new NEW(qf, qC, qC.params(0), /* boring */
			      qC.method().getDeclaringClass());
	    Quad q5 = new PHI(qf, qC, new Temp[0], 2); //retval phi
	    Quad q6 = new PHI(qf, qC, new Temp[0], 2); //retex phi
	    Edge in = qC.prevEdge(0);
	    Edge retout=qC.nextEdge(0);
	    Edge exout=qC.nextEdge(1);
	    in = addAt(in, q0);
	    in = addAt(in, q1);
	    in = addAt(in, q2);
	    in = addAt(in, q4);
	    addAt(retout, q5);
	    addAt(exout, q6);
	    Quad.addEdge(q2, 1, q3, 0);
	    Quad.addEdge(q3, 0, q5, 1);
	    Quad.addEdge(q3, 1, q6, 1);
	    // now recursively invoke on rest of list.
	    refactor(constructor, q3,
		     sortedFields.subList(1, sortedFields.size()));
	    return;
	}
	// nothing known about this field w/ this constructor.
	// clean up (add NEW) and go home.
	Quad qN = new NEW(qf, qC, qC.params(0),
			  qC.method().getDeclaringClass());
	addAt(qC.prevEdge(0), qN);
	return;
    }
    // private helper functions.
    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	Quad frm = e.from(); int frm_succ = e.which_succ();
	Quad to  = e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return to.prevEdge(to_pred);
    }
    // widen sub-int primitive types.
    private static HClass widen(HClass hc) {
	return MZFCompressor.widen(hc);
    }
    // wrap a value w/ an object of the appropriate type.
    private static Object makeValue(HClass type, Number num) {
	return MZFCompressor.wrap(type, num);
    }
    private static int makeEqOp(HClass type) {
	if (!type.isPrimitive()) return Qop.ACMPEQ;
	if (type==HClass.Int) return Qop.ICMPEQ;
	if (type==HClass.Long) return Qop.LCMPEQ;
	if (type==HClass.Float) return Qop.FCMPEQ;
	if (type==HClass.Double) return Qop.DCMPEQ;
	assert false : ("unknown type: "+type);
	return -1;
    }
}
