package Metagraph;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Representation of the MetaGraph
 */
public class MetaGraph {
    /* Input graph of metabolic reations */
    private Graph input;
    /* The internal representation of the metagraph */
    private Graph meta;

    /* Flow/number of atoms to conserve */
    private int flow;

    /* Create the start and target states */
    private HashMap<String, Integer> startState = new HashMap<>();
    private HashMap<String, Integer> targetState = new HashMap<>();

    /**
     * Initialize the metagraph with the start state
     * @param inputGraph The input graph of metabolic reactions
     * @param name The name of metagraph (becomes filename when written)
     * @param startNode Start node from input graph
     * @param targetNode Target node from input graph
     * @param inputFlow The flow to move (number of atoms to conserve)
     */
    public void MetaGraph(Graph inputGraph, String name, String startNode,
                          String targetNode, int inputFlow) {
        // Store the input graph and needed flow
        input = inputGraph;
        flow = inputFlow;

        // Create the empty metagraph
        meta = new SingleGraph(name);

        // Create the start and target states, where all flow is in one node
        startState.put(startNode, flow);
        targetState.put(targetNode, flow);

        // Place the first node into the metagraph
        meta.addNode(startState.toString());

        // Store the state flow as an attribute of the metanode
        meta.getNode(startState.toString()).setAttribute("state",
                startState);

        // Create a self edge on the target node (allows flow to remain stationary at the target)
        input.addEdge("TargetSelf", targetNode, targetNode, true);
        input.getEdge("TargetSelf").setAttribute("capacity", Double.MAX_VALUE);
    }

    /**
     * Fills the metagraph with metanodes by searching for neighbors
     * of existing metanodes, initially only the start metanode
     * @param stopOnTarget If TRUE, search will stop as soon as target state is found
     */
    public void populate(boolean stopOnTarget, boolean enablePruning) {
        /* The stack of nodes that need to be explored for neighbors */
        Stack<Node> known = new Stack<>();

        // Push the start node onto the known stack
        known.push(meta.getNode(startState.toString()));

        // Continue until there are no more nodes to search
        while (!known.empty()) {
            // Metanode to explore on this iteration
            Node current = known.pop();

            ArrayList<HashMap<String, Integer>> nbrStates = findNbrs(current);

            if (enablePruning && nbrStates.size() == 0 && !isTarget(current)) {
                // If pruning is enabled, no nbrs were found, and current is
                // not the target, prune the current metanode from the graph
                prune(current);
            } else {
                // If metanbrs were found, add them to the metagraph
                for (HashMap<String, Integer> state : nbrStates) {
                    // Add the neighboring node and store the state
                    meta.addNode(state.toString());
                    Node nbr = meta.getNode(state.toString());
                    nbr.setAttribute("state", state);

                    // Add a directed edge from current to nbr
                    meta.addEdge(current.toString() + " -> " + nbr.toString(),
                            current, nbr, true);

                    if (stopOnTarget && isTarget(nbr)) {
                        // Stop neighbor search since target was found
                        break;
                    }
                }

            }

        }
    }


    /**
     * Tests whether the input metanode is the target metanode
     * @param node The metanode to test
     * @return Whether the node is in fact the target
     */
    private boolean isTarget(Node node) {
        String state = node.getAttribute("state");
        String target = targetState.toString();
        return state.equals(target);
    }
}
