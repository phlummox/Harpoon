// PAEdgeSet.java, created Fri Jun 30 14:25:53 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import harpoon.Temp.Temp;


/**
 * <code>PAEdgeSet</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEdgeSet.java,v 1.1.2.17 2000-07-01 23:10:48 salcianu Exp $
 */
public interface PAEdgeSet {
    

    public void addEdge(Temp v, PANode node);

    public void addEdges(Temp v, Collection nodes);


    public void removeEdge(Temp v, PANode node);

    public void removeEdges(Temp v);


    public Set pointedNodes(Temp v);


    public Set allVariables();




    public void addEdge(PANode node1, String f, PANode node2);

    public void addEdges(PANode node1, String f, Collection node2s);

    public void addEdges(Collection node1s, String f, PANode node2);

    public void addEdges(Collection node1s, String f, Collection node2s);


    public void removeEdge(PANode node1, String f, PANode node2);
    
    public void removeEdges(PANode node1, String f);

    public void removeEdges(PANode node1);


    public Set pointedNodes(PANode node1, String f);

    public Set pointedNodes(Collection node1s, String f);

    public Set pointedNodes(PANode node);


    public Set allFlagsForNode(PANode node);


    public Set allSourceNodes();



    public Set getEdgesFrom(PANode node, String f);



    public void forAllPointedNodes(Temp v, PANodeVisitor visitor);

    public void forAllPointedNodes(PANode node, String f,
				   PANodeVisitor visitor);

    public void forAllPointedNodes(PANode node, PANodeVisitor visitor);

    public void forAllNodes(PANodeVisitor visitor);

    

    public void forAllEdges(Temp v, PAEdgeVisitor visitor);

    public void forAllEdges(PANode node, PAEdgeVisitor visitor);

    public void forAllEdges(PAEdgeVisitor visitor);


    public void copyEdges(PANode node, PAEdgeSet es2);


    public PAEdgeSet specialize(final Map map);


    public void remove(Set set);


    public void union(PAEdgeSet edges2);


    public Object clone();
}
