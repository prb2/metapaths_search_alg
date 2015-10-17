package Metagraph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.SingleNode;
import scala.util.regexp.Base;

import java.util.Map;

/**
 * Created by prudhvi on 10/16/15.
 */
public class MetaGraph {
    /**
     * Maps each meta-node to its state
     * {meta-node : {node1:count1, node2:count2}}
     */
    Map<String, Map<String, Integer>> stateMap;

    private Graph g;
    private int flow;

    protected MetaGraph(String id, int desiredFlow) {
        g = new SingleGraph(id);
        flow = desiredFlow;
    }

    public void addMetaNode(MetaNode metanode) {
        if (metanode.isValid(flow)) {
            g.addNode(metanode.getId());
            Node node = g.getNode(metanode.getId());
            node.setAttribute("state", metanode.getState().toString());
        } else {
            System.out.println("State was not valid. Node was not added to meta-graph.");
        }
    }


    public MetaNode getMetaNode(String metaNodeID) {
        return new MetaNode(metaNodeID, stateMap.get(metaNodeID));
    }

    public Map<String, Integer> getState(String metaNodeID) {
        return stateMap.get(metaNodeID);
    }
}
