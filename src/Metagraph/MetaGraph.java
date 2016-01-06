package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;

import java.io.IOException;
import java.util.*;

/**
 * Representation of the MetaGraph
 */
public class MetaGraph {
    /* Input graph of metabolic reations */
    private Graph input;
    /* The internal representation of the metagraph */
    private Graph meta;

    /* Flow/number of atoms to conserve */
    private double flow;

    /* Create the start and target states */
    private HashMap<String, Double> startState = new HashMap<>();
    private HashMap<String, Double> targetState = new HashMap<>();
    private String startID;
    private String targetID;

    /* Set of visited metanodes that don't need to be explored */
    private Set<Node> visited;


    /**
     * Initialize the metagraph with the start state
     * @param inputGraph The input graph of metabolic reactions
     * @param name The name of metagraph (becomes filename when written)
     * @param startNode Start node from input graph
     * @param targetNode Target node from input graph
     * @param inputFlow The flow to move (number of atoms to conserve)
     */
    public MetaGraph(Graph inputGraph, String name, String startNode,
                          String targetNode, double inputFlow) {
        // Store the input graph and needed flow
        input = inputGraph;
        flow = inputFlow;

        // Create the empty metagraph
        meta = new SingleGraph(name, false, false);

        // Initialize the set of found metanodes
        visited = new HashSet<>();

        // Create the start and target states, where all flow is in one node
        startState.put(startNode, flow);
        targetState.put(targetNode, flow);
        startID = startNode;
        targetID = targetNode;

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
        visited.add(meta.getNode(startState.toString()));

        // Continue until there are no more nodes to search
        while (!known.empty()) {
            // Metanode to explore on this iteration
            Node current = known.pop();

            // Get all the states that are reachable (one move away) from the current state
            ArrayList<HashMap<String, Double>> nbrStates = findNbrs(current);

            if (enablePruning && nbrStates.size() == 0 && !isTarget(current)) {
                // If pruning is enabled, no nbrs were found, and current is
                // not the target, prune the current metanode from the graph
                prune(current);
            } else {
                // If metanbrs were found, add them to the metagraph
                for (HashMap<String, Double> state : nbrStates) {
                    // TODO: Should this validity check exist? It seems better
                    // TODO: to have only valid states be returned by findNbrs
                    if (isValid(state)) {
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
                        } else {
                            if (!visited.contains(nbr)) {
                                // If nbr hasn't been seen, push it onto the stack for exploration
                                known.push(nbr);
                                visited.add(nbr);
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: Try and get rid of the need for this
    private boolean isValid(HashMap<String, Double> state) {
        double total = 0;
        for (double val : state.values()) {
            total += val;
        }
        return total == flow;
    }

    /**
     * Finds all possible metanbrs of input metanode
     * @param current The metanode to find nbrs for
     * @return A list of all metanbrs of the input node
     */
    private ArrayList<HashMap<String, Double>> findNbrs(Node current) {
        // Easily access the current metanode's state
        HashMap<String, Double> currentState = current.getAttribute("state");

        /**
         * A partial state is map of similar form to a regular state/metanode,
         * with the exception that the partial state only accounts for the potential
         * flow move of one inner node (not the total that we're working with).
         * All of the partial states associated with a single metanode can be
         * combined to form a complete state which represents a metanode nbr
         * This map associates each inner node in the current to a partial state.
         */
        HashMap<String, ArrayList<HashMap<String, Double>>> partialStates = new HashMap<>();

        // Iterate through each inner node in the current state
        for (String nodeID : currentState.keySet()) {
            // Get a handle on the inner node
            Node node = input.getNode(nodeID);

            // Get the amount of flow residing at this inner node that needs to be moved
            double requiredFlow = currentState.get(node.getId());

            // Get list of nodes that we could move flow to
            ArrayList<Node> innerNbrs = getOutgoingNbrs(input, node);

            // Index variable for iterating through innerNbrs
            int j = innerNbrs.size() - 1;

            if (j >= 0) {
                // Get all the partial states resulting from moving the flow at this inner 'node'
                partialStates.put(node.getId(), recursiveNbrSearch(requiredFlow, node, innerNbrs, j));
            } else {
                continue;
            }
        }
        // Combine the found partials into all combinations of valid flow moves from this current state
        return generateCompleteStates(partialStates);
    }


    /**
     * Modifes the metagraph so that terminal branches are removed
     * @param terminus The terminal node that was found
     */
    private void prune(Node terminus) {

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











    /**
     * Returns a list of all nbrs that can be reached from n
     * @param g The graph to work with
     * @param n The source node
     * @return A list of reachable nbrs
     */
    private ArrayList<Node> getOutgoingNbrs(Graph g, Node n) {
        ArrayList<Node> nbrs = new ArrayList<>();

        for (Edge e : n.getEachLeavingEdge()) {
            nbrs.add(e.getTargetNode());
        }

        return nbrs;
    }

    /**
     * This function recursively distributes the flow residing at the parent node.
     * @param n The amount of flow that needs to be distributed
     * @param parent The node from input graph which is the source of flow
     * @param nbrs Neighbors of parent, that must receive remaining flow
     * @param j The jth nbr to move flow to
     * @return A list of partial states, each of which maps the jth nbr of parent
     *         to the various amounts of flow that it can be moved to that nbr
     */
    private ArrayList<HashMap<String, Double>> recursiveNbrSearch(double n, Node parent, ArrayList<Node> nbrs, int j) {
        /* List of all possible flow moves from parent to the jth nbr */
        ArrayList<HashMap<String, Double>> states = new ArrayList<>();

        // If there are nbrs left
        if (j >= 0) {
            // 1. for each inner node in current's state, generate the possible partial states
            Node nbr = nbrs.get(j);
            for (double i = 0; i <= Math.min(n, capacity(parent, nbr)); i++) {
                HashMap<String, Double>  partialState = new HashMap<>();
                // move i flow to this nbr
//                System.out.println("Moving i=" + i + " flow to nbr: " + nbr);

                if (i != 0.0) {
                    // if 0 flow, don't include in the state
                    partialState.put(nbr.getId(), i);
                }

                // allocate the remaining flow for the remaining nbrs
                ArrayList<HashMap<String, Double>> foundStates = recursiveNbrSearch(n - i, parent, nbrs, j - 1);
//                System.out.println("Found rec. partial states: " + foundStates);

                // Check if flow was distributed to any of the other nbrs
                if (foundStates.size() == 0) {
                    states.add(partialState);
                } else {
                    for (HashMap<String, Double> state : foundStates) {
                        // merge the flow move to this nbr with flow that went to the other nbrs
                        HashMap<String, Double> mergedState = mergePartialStates(partialState, state);
                        states.add(mergedState);
//                        System.out.println("Found partial state: " + mergedState);
                    }
                }
            }
        }

        return states;
    }

    private HashMap<String, Double> mergePartialStates(HashMap<String, Double> partialState1, HashMap<String, Double> partialState2) {
        HashMap<String, Double> merged = new HashMap<>();
        merged.putAll(partialState1);
        for (String key : partialState2.keySet()) {
            if (merged.containsKey(key)) {
                merged.put(key, merged.get(key) + partialState2.get(key));
            } else {
                merged.put(key, partialState2.get(key));
            }
        }
        return merged;
    }

    private ArrayList<HashMap<String, Double>> generateCompleteStates(HashMap<String, ArrayList<HashMap<String, Double>>> partialStates) {
        ArrayList<ArrayList<HashMap<String, Double>>> listOfPartialStateLists = new ArrayList<>();
        for (ArrayList<HashMap<String, Double>> list : partialStates.values()) {
            listOfPartialStateLists.add(list);
        }

        return recursiveStateAccumulator(listOfPartialStateLists);
    }

    /**
     *
     * @param valueLists
     * @return
     */
    private ArrayList<HashMap<String, Double>> recursiveStateAccumulator(ArrayList<ArrayList<HashMap<String, Double>>> valueLists) {
        ArrayList<HashMap<String, Double>> merged = new ArrayList<>();
        if (valueLists.size() == 0) {
            return merged;
        } else if (valueLists.size() == 1) {
            return valueLists.get(0);
        } else {
            ArrayList<HashMap<String, Double>> current = valueLists.remove(0);
            ArrayList<HashMap<String, Double>> rest = recursiveStateAccumulator(valueLists);
            for (HashMap<String, Double> currentPartial : current) {
                for (HashMap<String, Double> restPartial : rest) {
                    merged.add(mergePartialStates(currentPartial, restPartial));
                }
            }
            return merged;
        }
    }

    private double capacity(Node parent, Node node) {
        if (parent.getId().equals(node.getId())) {
            return Double.MAX_VALUE;
        } else {
            Edge nbrEdge = parent.getEdgeToward(node);
            return nbrEdge.getAttribute("capacity");
        }
    }

    public void writeToFile(String filename) throws IOException {
        FileSink fs = new FileSinkDOT();
        fs.writeAll(meta, "graphs/" + filename + ".dot");
    }
}
