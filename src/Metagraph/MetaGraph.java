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
 * Creation and representation of the MetaGraph
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

    /* The collection of nodes which have been pruned from the graph,
    mapped to the parents of that node */
    private HashMap<Node, ArrayList<Node>> pruned;


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

        // Initialize the empty collection of pruned nodes
        pruned = new HashMap<>();

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

    /********** Graph **********/

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

    /**
     * Writes the metagraph to a .dot file in the graphs directory
     * @param filename The name of the written file
     * @throws IOException
     */
    public void writeToFile(String filename) throws IOException {
        FileSink fs = new FileSinkDOT();
        fs.writeAll(meta, "graphs/" + filename + ".dot");
    }

    /********** Meta Neighbor Search **********/

    /**
     * Finds all possible metanbrs of input metanode
     * @param current The metanode to find nbrs for
     * @return A list of all metanbrs of the input node
     */
    private ArrayList<HashMap<String, Double>> findNbrs(Node current) {
        // Easily access the current metanode's state
        HashMap<String, Double> currentState = current.getAttribute("state");

        /*
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
        /*
         * At this point, partialState maps each inner node of current to a list
         * of partial states indicating where the flow at each inner node could
         * be moved to
         */
        // Combine the found partials into all combinations of valid flow moves from this current state
        return generateCompleteStates(partialStates);
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
            Node nbr = nbrs.get(j);
            /**
             * For the jth nbr of parent, generate the all possible partial states
             * by incrementing flow until either the capacity of the edge is
             * reached, or all flow has been distributed.
             */
            for (double i = 0; i <= Math.min(n, capacity(parent, nbr)); i++) {
                HashMap<String, Double>  partialState = new HashMap<>();

                // If 0 flow, don't include in the state
                if (i != 0.0) {
                    // Move i amount of flow to this nbr
                    partialState.put(nbr.getId(), i);
                }

                // Check if any flow remains after moving i flow to jth nbr
                if (n - i > 0) {
                    // Distribute the remaining flow among the remaining nbrs
                    ArrayList<HashMap<String, Double>> foundStates = recursiveNbrSearch(n - i, parent, nbrs, j - 1);

                    // Merge the flow move to this nbr with each possible move of the remaining flow to remaining nbrs
                    for (HashMap<String, Double> state : foundStates) {
                        HashMap<String, Double> mergedState = mergePartialStates(partialState, state);
                        states.add(mergedState);
                    }
                } else {
                    // If no flow remains, add this partial state and return
                    states.add(partialState);
                }
            }
        }

        // Return the list of partials states possible when moving n flow to j nbrs
        return states;
    }

    /**
     * This functions simply merges two maps. If there is key overlap, the values are added
     * @param partialState1 The first state to merge
     * @param partialState2 The seconds state to merge
     * @return The resulting merged state
     */
    private HashMap<String, Double> mergePartialStates(HashMap<String, Double> partialState1, HashMap<String, Double> partialState2) {
        HashMap<String, Double> merged = new HashMap<>();
        // Copy over the first state into the merged state
        merged.putAll(partialState1);

        // Copy over the second state, while checking for overlap
        for (String key : partialState2.keySet()) {
            if (merged.containsKey(key)) {
                // If there is key overlap, add the values
                merged.put(key, merged.get(key) + partialState2.get(key));
            } else {
                // If no overlap, simply copy over the value
                merged.put(key, partialState2.get(key));
            }
        }

        return merged;
    }

    /**
     * Finds all possible nbrs by combining partial states to form valid,
     * complete states
     * @param partialStates The mapping of inner nodes to partial states
     * @return A list of valid neighboring states
     */
    private ArrayList<HashMap<String, Double>> generateCompleteStates(HashMap<String, ArrayList<HashMap<String, Double>>> partialStates) {
        ArrayList<ArrayList<HashMap<String, Double>>> listOfPartialStateLists = new ArrayList<>();

        /*
         * Reorganize the map of partial state lists into a list of lists.
         * Each index of the outer list corresponds to a inner nbr and the list
         * at that index contains the partial states possible for that inner nbr
         */
        for (ArrayList<HashMap<String, Double>> list : partialStates.values()) {
            listOfPartialStateLists.add(list);
        }

        ArrayList<HashMap<String, Double>> potentialStates =
                recursiveStateAccumulator(listOfPartialStateLists);

        ArrayList<HashMap<String, Double>> validStates = new ArrayList<>();

        for (HashMap<String, Double> state : potentialStates) {
            if (isValid(state)) {
                validStates.add(state);
            }
        }

        return validStates;
    }

    /**
     * Enumerates all combinations of nbrs by recursively picking a flow move
     * (partial state) from each inner nbr and combining them to form a complete state
     * which accounts for all the flow the needs to be moved between metanodes
     * @param valueLists List of lists of partial states. Each index of the
     *        outer list corresponds to a inner nbr and the list at that
     *        index contains the partial states possible for that inner nbr
     * @return A list of states as a result of trying all possible moves of
     * flow for all inner nbrs. Some of these states may not be valid, in the
     * case that all required flow was not moved due to capacity constraints when
     * the partial states were intially created.
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
                    HashMap<String, Double> newState = mergePartialStates(currentPartial, restPartial);
                    merged.add(newState);
                }
            }

            return merged;
        }
    }

    /********** Pruning **********/

    /**
     * Modifes the metagraph so that terminal branches are removed
     * @param terminus The terminal node that was found
     */
    private void prune(Node terminus) {
        HashMap<String, Double> endState = terminus.getAttribute("state");

        // Find the terminal node's parents
        ArrayList<Node> parents = new ArrayList<>();
        for (Edge edge : terminus.getEachEnteringEdge()) {
            parents.add(edge.getSourceNode());
        }
        // Add this node to the pruned set
        pruned.put(terminus, parents);

        // Remove it from the MG
        meta.removeNode(terminus);

        // Check if any of its parents can be pruned
        for (Node parent : parents) {
            if (parent.getLeavingEdgeSet().size() == 0) {
                // If the parent has no children, it is also a dead end and can be pruned
                prune(parent);
            }
        }
    }

    /********** Utilities **********/

    /**
     * Looks up the capacity of the edge between two nodes in the input graph
     * @param parent Source node
     * @param node Target node
     * @return The capacity of the source-target edge
     */
    private double capacity(Node parent, Node node) {
        if (parent.getId().equals(node.getId())) {
            return Double.MAX_VALUE;
        } else {
            Edge nbrEdge = parent.getEdgeToward(node);
            return nbrEdge.getAttribute("capacity");
        }
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
     * Tests whether the input metanode is the target metanode
     * @param node The metanode to test
     * @return Whether the node is in fact the target
     */
    private boolean isTarget(Node node) {
        String state = node.getAttribute("state").toString();
        String target = targetState.toString();
        return state.equals(target);
    }

    /**
     * Determines whether a state is valid by check that the sum of all flow in
     * the state exactly equals the user input flow.
     * @param state The state to test
     * @return Whether the state is valid or not
     */
    private boolean isValid(HashMap<String, Double> state) {
        double total = 0;

        // Sum up the flow at each inner node
        for (double val : state.values()) {
            total += val;
        }

        return total == flow;
    }
}
