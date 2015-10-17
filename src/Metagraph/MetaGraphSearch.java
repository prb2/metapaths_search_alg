package Metagraph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import scala.util.parsing.combinator.testing.Str;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Constructs a meta-graph in which nodes contain mappings of input graph nodes
 * to flow counts. After construction, the meta-graph can be searched.
 */
public class MetaGraphSearch {
    /**
     * The constructed meta-graph
     * a.k.a MG
     */
    private MetaGraph meta;
    private Stack<MetaNode> explored;
    private int flow;

    public void constructMetaGraph(Graph inputG, String s, String t, int desiredFlow) {
        meta = new MetaGraph("MetaGraph", desiredFlow);
        flow = desiredFlow;
        explored = new Stack<MetaNode>();

        // Create the start state
        HashMap<String, Integer> state = new HashMap<String, Integer>();
        // All the flow is in the starting node
        state.put(s, desiredFlow);
        // Add the start node to the MG
        meta.addMetaNode(new MetaNode(s, state));

        // Find reachable states
        findReachable(inputG, meta.getMetaNode(s));
    }

    private void findReachable(Graph inputG, MetaNode node) {
        explored.push(node);
        MetaNode nbr = findMetaNbr(node);
        findMetaNbr(nbr);

    }

    private MetaNode findMetaNbr(MetaNode current) {
        ArrayList<Node> innerNodes = meta.getInnerNodes(current.getId());
        int neededFlow = flow;
        ArrayList<String> potential = new ArrayList<>();

        while (neededFlow > 0) {
            // Keep adding nbrs of the inner nodes until the necessary flow has been achieved
        }
        // distribute the flow among potential nodes
        // Create the state
        // make a new metanode
        // add metanode to metagraph
        return null;
    }

    public void findMetaPath() {
        if (meta == null) {
            System.out.println("Must construct the meta graph first.");
            return;
        }
        System.out.println("Finding path...");

    }

    public MetaGraph getMeta() {
        return meta;
    }
}
