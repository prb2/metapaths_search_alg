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

    Graph g;

    protected MetaGraph(String id) {
        g = new SingleGraph(id);
    }

    public void addMetaNode(String id, int flow, Map<String, Integer> state) {
        if (validateState(flow, state)) {
            g.addNode(id);
            Node node = g.getNode(id);
            node.setAttribute("state", state.toString());
        } else {
            System.out.println("State was not valid. Node was not added to meta-graph.");
        }
    }

    /**
     * A meta-node is only valid if the sum of the nodes' flow is equal to
     * the required flow.
     * @param requiredFlow The specified flow need for each node in the meta graph
     */
    protected Boolean validateState(int requiredFlow, Map<String, Integer> state) {
        int stateFlow = 0;

        for (Map.Entry<String, Integer> entry : state.entrySet()) {
            stateFlow += entry.getValue();
        }

        return stateFlow != requiredFlow;
    }

    public Map<String, Integer> getState(String nodeID) {
        return stateMap.get(nodeID);
    }
}
