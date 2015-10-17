package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import scala.Int;
import scala.util.parsing.combinator.testing.Str;

import java.util.*;

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
    private Graph base;
    private Stack<MetaNode> explored;
    private int flow;
    private String targetID;

    public void constructMetaGraph(Graph inputG, String s, String t, int desiredFlow) {
        meta = new MetaGraph("MetaGraph", desiredFlow);
        base = inputG;
        flow = desiredFlow;
        targetID = t;
        explored = new Stack<MetaNode>();

        // Create the start state
        HashMap<String, Integer> state = new HashMap<String, Integer>();
        // All the flow is in the starting node
        state.put(s, desiredFlow);
        // Add the start node to the MG
        meta.addMetaNode(new MetaNode(s, state));

        // Find reachable states
        explored.push(meta.getMetaNode(s));
        populateMetaGraph();
    }

    private void populateMetaGraph() {
        while (!explored.empty()) {
            MetaNode current = explored.pop();
            if (current.isTarget(targetID, flow)) {
                break;
            } else {
                // Find all neighbors of this meta node
                MetaNode newNbr = findMetaNbr(current);
                explored.push(newNbr);
                System.out.println("Added new meta nbr: " + newNbr.getState().toString());

                // Maintain the reachable nbrs to be visited later
//                for (MetaNode mn : metaNbrs) {
//                    explored.push(mn);
//                }
            }
        }

    }

    private MetaNode findMetaNbr(MetaNode current) {
        // Get the node from original graph that are in this meta node
        ArrayList<Node> innerNodes = meta.getInnerNodes(current.getId());
        Map<String, Integer> currentState = current.getState();
        ArrayList<Map<String, Integer>> subStates = new ArrayList<Map<String, Integer>>();
        HashMap<String, Integer> newState = new HashMap<String, Integer>();

        for (Node innerNode : innerNodes) {
            // Distribute the flow among the inner nodes' neighbors
            subStates.add(distributeFlow(innerNode, currentState.get(innerNode.getId())));
        }

        // Consolidate the substates in to a single state
        for (Map<String, Integer> subState : subStates) {
            newState.putAll(subState);
        }
        // Make a new metanode with this state
        MetaNode newMetaNode = new MetaNode(newState.keySet().toString(), newState);

        // add metanode to metagraph
        meta.addMetaNode(newMetaNode);

        return newMetaNode;
    }

    private Map<String, Integer> distributeFlow(Node parent, int flow) {
        // Currently only returning the first suitable distribution
        HashMap<String, Integer> distribution = new HashMap<String, Integer>();
        int remainingFlow = flow;
        Iterator<Node> nbrIter = parent.getNeighborNodeIterator();
        while (remainingFlow > 0) {
            if (nbrIter.hasNext()) {
                Node nbr = nbrIter.next();
                Edge edge = parent.getEdgeBetween(nbr);
                int capacity = edge.getAttribute("capacity");

                if (capacity >= remainingFlow) {
                    distribution.put(nbr.getId(), remainingFlow);
                    remainingFlow = 0;
                } else {
                    distribution.put(nbr.getId(), capacity);
                    remainingFlow -= capacity;
                }
            } else {
                return null;
                // TODO: Not possible to distribute this flow to node's neighbors
                // TODO: Reached a terminus, will have to discard this whole metanode
            }
        }
        return distribution;
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
