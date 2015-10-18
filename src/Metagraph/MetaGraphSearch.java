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
    private double flow;
    private String targetID;

    public void constructMetaGraph(Graph inputG, String s, String t, double desiredFlow) {
        meta = new MetaGraph("MetaGraph", desiredFlow);
        base = inputG;
        flow = desiredFlow;
        targetID = t;
        explored = new Stack<MetaNode>();

        // Create the start state
        HashMap<String, Double> state = new HashMap<>();
        // All the flow is in the starting node
        state.put(s, desiredFlow);
        // Add the start node to the MG
        meta.addMetaNode(new MetaNode(s, state));

        // Find reachable states
        explored.push(meta.getMetaNode(s));
        populateMetaGraph();
        meta.display();
    }

    private void populateMetaGraph() {
        while (!explored.empty()) {
            MetaNode current = explored.pop();
            if (current.isTarget(targetID, flow)) {
                System.out.println("Current is target");
                break;
            } else {
                // Find all neighbors of this meta node
                System.out.println("Finding new meta nbr for: " + current.getState().toString());
                MetaNode newNbr = findMetaNbr(current);
                // add metanode to metagraph
                meta.addMetaNode(newNbr);
                meta.addDirectedMetaEdge(current.getId(), newNbr.getId());
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
        ArrayList<Node> innerNodes = getInnerNodes(current.getId());
        System.out.println("Inner nodes: " + innerNodes);
        Map<String, Double> currentState = current.getState();
        ArrayList<Map<String, Double>> subStates = new ArrayList<>();
        HashMap<String, Double> newState = new HashMap<>();

        for (Node innerNode : innerNodes) {
            // Distribute the flow among the inner nodes' neighbors
            subStates.add(distributeFlow(innerNode.getId(), currentState.get(innerNode.getId())));
        }

        // Consolidate the substates in to a single state
        for (Map<String, Double> subState : subStates) {
            newState.putAll(subState);
        }
        // Make a new metanode with this state
        MetaNode newMetaNode = new MetaNode(newState.keySet().toString(), newState);
        System.out.println("Newly found metanode: " + newMetaNode.getState().toString());

        return newMetaNode;
    }

    private Map<String, Double> distributeFlow(String parentID, double flow) {
        // Currently only returning the first suitable distribution
        HashMap<String, Double> distribution = new HashMap<>();
        double remainingFlow = flow;
        Node parent = base.getNode(parentID);

        System.out.println("Parent is: " + parent);
        for (Edge e : parent.getEachLeavingEdge()) {
            Node nbr = e.getTargetNode();
            while (remainingFlow > 0) {
                System.out.println("Looking at nbr: " + nbr);
                Edge edge = parent.getEdgeBetween(nbr);
                double capacity = edge.getAttribute("capacity");

                if (capacity >= remainingFlow) {
                    distribution.put(nbr.getId(), remainingFlow);
                    remainingFlow = 0;
                } else {
                    distribution.put(nbr.getId(), capacity);
                    remainingFlow -= capacity;
                }
            }
        }
    return distribution;
    // TODO: Not possible to distribute this flow to node's neighbors
    // TODO: Reached a terminus, will have to discard this whole metanode
    }

    /**
     * Get the nodes that are contained in the specified metanode
     * @param metaNodeID
     * @return
     */
    public ArrayList<Node> getInnerNodes(String metaNodeID) {
        ArrayList<Node> innerNodes = new ArrayList<>();

        for (String n : meta.getMetaNode(metaNodeID).getState().keySet()) {

            System.out.println("inner: " + base.getNode(n));
            innerNodes.add(base.getNode(n));
        }

        return innerNodes;
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
