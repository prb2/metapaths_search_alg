package Metagraph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.SingleNode;
import scala.util.regexp.Base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prudhvi on 10/16/15.
 */
public class MetaGraph {
    /**
     * Maps each meta-node to its state
     * {meta-node : {node1:count1, node2:count2}}
     */
    Map<String, Map<String, Double>> stateMap;
    Map<String, MetaNode> metaNodes;

    private Graph internal;
    private double flow;

    protected MetaGraph(String id, double desiredFlow) {
        internal = new SingleGraph(id);
        flow = desiredFlow;
        stateMap = new HashMap<>();
        metaNodes = new HashMap<>();
    }

    public Boolean addMetaNode(MetaNode metanode) {
        if (metanode.isValid(flow)) {
            internal.addNode(metanode.getId());
            Node node = internal.getNode(metanode.getId());
            node.setAttribute("state", metanode.getState().toString());
            metaNodes.put(metanode.getId(), metanode);
            stateMap.put(metanode.getId(), metanode.getState());
            return true;
        } else {
            System.err.println("State was not valid. Node was not added to meta-graph.");
            return false;
        }
    }

    public void addDirectedMetaEdge(String from, String to) {
        internal.addEdge(from + "->" + to, from, to, true);
    }

    public void display() {
        for (Node n : internal) {
            n.setAttribute("ui.label", stateMap.get(n.getId()).toString());
        }
        //TODO: Uncomment to display meta graph
        internal.display();
    }

    public MetaNode getMetaNode(String metaNodeID) {
//        return new MetaNode(metaNodeID, stateMap.get(metaNodeID));
        return metaNodes.get(metaNodeID);
    }

}
