// PTEdgesSet.java, created Sat Jan  8 23:37:30 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Collections;
import java.util.Set;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;

import harpoon.Temp.Temp;

/**
 * <code>PTEdgesSet</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAEdgeSet.java,v 1.1.2.2 2000-01-15 03:38:16 salcianu Exp $
 */
public class PAEdgeSet {

    // There are two kinds of edges: variable --> node and 
    // node --f--> node where f is a field. This is reflected in
    // the two different structures we use to store the edges.

    // mapping variable -> set of pointed nodes
    private Relation vars;

    // mapping node -> a <code>Relation</code> flag -> set of pointed nodes
    private Hashtable nodes;


    /** Creates a <code>PTEdgesSet</code>. */
    public PAEdgeSet() {
	vars  = new Relation();
	nodes = new Hashtable();
    }

    // OPERATIONS ON THE SET OF EDGES


    // A. Operations on edges starting from variables.

    // Returns the set of all the nodes pointed to by the variable v,
    // i.e. all the nodes n such that <v,n> exists.
    public Set pointedNodes(Temp v){
	return vars.getValuesSet(v);
    }

    /** Adds the edges <v,n> where n ranges over the set pointed_nodes.
     *  This function is more efficient than just iterating over the
     *  set and calling <code>addEdge</code> for each element. */
    public void addEdges(Temp v, Set pointed_nodes){
	vars.addAll(v,pointed_nodes);
    }
    
    /** Adds the edge <v,n> */
    public void addEdge(Temp v,PANode n){
	vars.add(v,n);
    }

    // Removes all the edges from v.
    public void removeEdges(Temp v){
	vars.removeAll(v);
    }


    // B. Operations on edges starting from nodes.

    /** Returns all the nodes pointed to by the node <code>n</code> through
     *  the field <code>f</code>; i.e. all the nodes <code>n1</code> such
     *  that &lt;&lt;n,f&gt;,n1&gt; exists */
    public Set pointedNodes(PANode n, String f){
	Relation rel = (Relation) nodes.get(n);
	if(rel==null) return Collections.EMPTY_SET;
	return rel.getValuesSet(f);
    }

    /** Returns all the nodes which are pointed to by nodes from the set
     *  <code>sources</code> */
    public Set pointedNodes(Set sources, String f){
	Iterator it = sources.iterator();
	HashSet pointed_nodes = new HashSet(); 
	while(it.hasNext())
	    pointed_nodes.addAll(pointedNodes((PANode)it.next(),f));
	return pointed_nodes;
    }

    // DIFFERENT addEdge(s) functions
    /** Adds all the edges &lt;&lt;n,f&lt;,n1&lt; where <code>n1</code>
     *  ranges over the set <code>dests</code>. */
    public void addEdges(PANode n, String f, Set dests){
	Relation rel = (Relation)nodes.get(n);
	if(rel==null) 
	    nodes.put(n,rel=new Relation());
	rel.addAll(f,dests);
    }

    /** Adds all the edges <code>&lt;&lt;n1,f&lt;,n2&lt;</code> for every node
     *  <code>n1</code> from the set <code>sources</code> and every node
     *  <code>n2</code> from the set <code>dests</code> */
    public void addEdges(Set sources, String f, Set dests){
	Iterator it = sources.iterator();
	while(it.hasNext())
	    addEdges((PANode)it.next(),f,dests);
    }

    /** Adds all the edges <code>&lt;&lt;n1,f&lt;,n&lt;</code> for every node
     *  <code>n1</code> from the set <code>sources</code> */
    public void addEdges(Set sources, String f, PANode dest){
	Iterator it = sources.iterator();
	while(it.hasNext())
	    addEdge((PANode)it.next(),f,dest);
    }

    // Adds the edge <<n,f>,n1>. If you want to add many edges, consider
    // using addEdges (repeatedly calling addEdge over all the edges of
    // a set is VERY expensive.
    public void addEdge(PANode n, String f, PANode n1){
	Relation rel = (Relation)nodes.get(n);
	if(rel==null) 
	    nodes.put(n,rel=new Relation());
	rel.add(f,n1);
    }

    // Deletes all the edges of the type <<n,f>,n1>
    public void removeEdges(PANode n, String f){
	Relation rel = (Relation)nodes.get(n);
	if(rel==null) return;
	rel.removeAll(f);
    }

    /** Returns an <code>Enumeration</code> of the set of all the
     * variables that are mentioned in <code>this</code> set of edges
     * (i.e. v such that we have at least one <v,n> edge) */
    public Enumeration allVariables(){
	return vars.keys();
    }

    /** Returns an <code>Enumeration</code> of the set of all the
     * nodes that are mentioned in <code>this</code> set of edges
     * (i.e. n such that we have at least one <<n,f>,n1> edge) */
    public Enumeration allNodes(){
	return nodes.keys();
    }

    /** Enumeration over an empty set; ugly, let me know if you know
     * something better */ // TODO
    static final private Enumeration EMPTY_ENUM = (new Hashtable()).keys();

    /** Returns an <code>Enumeration</code> of all the flags from a
     * specific node n, i.e. f such that we have at least one 
     * &lt;&lt;n,f&gt;,n1&gt; edge */
    public Enumeration allFlagsForNode(PANode n){
	Relation rel = (Relation)nodes.get(n);
	if(rel==null)
	    return EMPTY_ENUM;
	return rel.keys();
    }

    /** Visits all the nodes pointed by <coden</code> and executes
     *  <code>visitor.visit</code> on each of them. */
    public void forAllPointedNodes(PANode n, PANodeVisitor visitor){
	// Go through all the fields attached to n;
	Enumeration enum = allFlagsForNode(n);
	while(enum.hasMoreElements()){
	    String str = (String)enum.nextElement();
	    // for each such field, visit all the pointed nodes.
	    Iterator it = pointedNodes(n,str).iterator();
	    while(it.hasNext()){
		PANode node = (PANode) it.next();
		visitor.visit(node);
	    }
	}
    }
    

    /** <code>union</code> computes the union of two sets of edges
     * (in a control-flow join point, for example) */
    public void union(PAEdgeSet edges2){
	// union of the edges from the variables
	vars.union(edges2.vars);

	// union of the edges from the nodes
	Enumeration en = edges2.allNodes();
	while(en.hasMoreElements()){
	    PANode n = (PANode)en.nextElement();
	    Relation rel = (Relation)nodes.get(n);
	    if(rel==null){
		rel = new Relation();
		nodes.put(n,rel);
	    }
	    rel.union((Relation)edges2.nodes.get(n));
	}
    }


    /** Checks the equality of two <code>PAEdgeSet</code>s */
    public boolean equals(Object o){
	if(o==null) return false;
	PAEdgeSet es2 = (PAEdgeSet) o;
	if(!vars.equals(es2.vars)) return false;
	if(!nodes.keySet().equals(es2.nodes.keySet())) return false;
	Enumeration enum = nodes.keys();
	while(enum.hasMoreElements()){
	    PANode node = (PANode)enum.nextElement();
	    Relation rel1 = (Relation)nodes.get(node);
	    Relation rel2 = (Relation)es2.nodes.get(node);
	    if(!rel1.equals(rel2)) return false;
	}
	return true;
    }

    /** Private constructor, used only by <code>clone()</code> */
    private PAEdgeSet(Relation _vars, Hashtable _nodes) {
	vars  = _vars;
	nodes = _nodes;
    }

    /** <code>clone</code> clones <code>this</code> collection of
     * edges */	
    public Object clone(){
	Relation new_vars = (Relation) vars.clone();

	Hashtable map_nodes = new Hashtable();
	Iterator it_nodes = nodes.entrySet().iterator();

	while(it_nodes.hasNext()){
	    Map.Entry entry = (Map.Entry) it_nodes.next();
	    Relation rel = (Relation)((Relation)entry.getValue()).clone();
	    map_nodes.put(entry.getKey(),rel);
	}

	return new PAEdgeSet(new_vars, map_nodes);
    }

    // Pretty-print function for debug purposes
    public String toString(){
	StringBuffer buffer = new StringBuffer();

	buffer.append("{\n");
	buffer.append(vars);
	buffer.append("\n");

	Iterator it = nodes.entrySet().iterator();

	while(it.hasNext()){
	    Map.Entry entry = (Map.Entry) it.next();
	    buffer.append("  ");
	    buffer.append((PANode)entry.getKey());
	    buffer.append(" -> ");
	    buffer.append((Relation)entry.getValue());
	}

	buffer.append("\n }");
	
	return buffer.toString();
    }
}


