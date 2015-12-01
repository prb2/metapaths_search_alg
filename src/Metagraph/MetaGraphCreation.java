package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import scala.Int;
import scala.util.parsing.combinator.testing.Str;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Constructs a meta-graph in which nodes contain mappings of input graph nodes
 * to flow counts. After construction, the meta-graph can be searched.
 */
public class MetaGraphCreation {
    /* The constructed meta-graph a.k.a MG */
    private MetaGraph meta;
    /* The input graph */
    private Graph base;
    /* The stack of metanodes awaiting further exploration*/
    private Stack<MetaNode> explored;
    /* The flow required at the target node. */
    private double flow;
    /* The ID of the target node in the input graph */
    private String targetID;

    /**
     * Initializes the meta-graph, adds the starting meta-nbr, and starts the
     * search for neighborhing meta-nbrs
     */
    public void constructMetaGraph(Graph inputG, String s, String t, double desiredFlow) {
        meta = new MetaGraph("MetaGraph", desiredFlow);
        base = inputG;
        flow = desiredFlow;
        targetID = t;
        explored = new Stack<MetaNode>();

        // Create the start state
        HashMap<String, Double> state = new HashMap<>();
        // All the flow starts off in the starting node
        state.put(s, desiredFlow);
        String startNodeName = state.toString();
        // Add the starting metanode to the metagraph
        meta.addMetaNode(new MetaNode(startNodeName, state));

        // Add the starting node into the stack of node to be explored
        explored.push(meta.getMetaNode(startNodeName));

        // Find reachable states
        populateMetaGraph();

        // Print out the solution state map and display the metagraph
//        meta.display();

        // Save the metagraph to file
//        try {
//            meta.writeToFile("MG_" + inputG.getId());
//        } catch (IOException e){
//            System.out.println(e);
//        }
    }

    /**
     * Searches for neighbors of the already explored metanbrs
     */
    private void populateMetaGraph() {
        // As long as there are metanodes to explore, keep searching
        while (!explored.empty()) {
            MetaNode current = explored.pop();
            System.out.println("Popped from stack: " + current.getState());
            if (current.isTarget(targetID, flow)) {
                // Reached the target state, don't need to find neighbors of target
                System.out.println("Current is target");
                // Will continue to explore metanodes left on the stack
            } else {
                System.out.println("Finding new meta nbr for: " + current.getState().toString());
                // Find all neighbors of this meta node, then continue explores metanodes from the stack
                iterativeFindMetaNbrs(current);
            }
        }

    }

    /**
     * Finds all neighbors that are reachable with a valid flow move.
     */
    private void findMetaNbrs(MetaNode current) {
        ArrayList<Node> innerNodes = getInnerNodes(current.getId());
        HashMap<String, Double> currentState = current.getState();
        HashMap<String, Double> newState = new HashMap<>();
        boolean completed = true;

        while (!innerNodes.isEmpty()) {
            // Get the next inner node
            Node innerNode = innerNodes.remove(0);

            // Get the flow that needs to be moved from this node
            double flow = currentState.get(innerNode.getId());

            // Get the neighboring edges
            Queue<Edge> nbrEdges = getInnerNodeNbrEdges(innerNode);
//            System.out.println(innerNode.getId() + " has nbr edges: " + nbrEdges);

            // If a complete state was found in a previous, iteration, start
            // with a fresh state. Otherwise, build upon the previous state
            // TODO: This probably isn't the best way to handle this
            if (completed) {
                newState = new HashMap<>();
            }

            // Recursively finds potential metanode nbrs and adds them to the graph if they are valid
            completed = recursiveMetaNodeCompletion(newState, nbrEdges, flow, current);
        }
    }

    /**
     * Given a state, will create new states by moving remaining flow across the available nbrEdge
     */
    private Boolean recursiveMetaNodeCompletion(HashMap<String, Double> newState, Queue<Edge> nbrEdges, double remainingFlow, MetaNode parent) {
        System.out.println("Called with: " + newState + " remaining flow: " + remainingFlow);
        if (nbrEdges.isEmpty()) {
            // If no more edges exist, all possible neighboring states have been explored, so stop search for neighbors
            return false;
        } else {
            // Get the next nbr and its capacity
            Edge nbrEdge = nbrEdges.poll(); // Known to be non-empty
            Node nbrNode = nbrEdge.getTargetNode();
            double nbrCapacity = nbrEdge.getAttribute("capacity");

            // While we have flow to move, or we hit the capacity
            // explore available states from moving "i" flow to the nbr
            for (double i = 1.0; i <= Math.min(remainingFlow, nbrCapacity); i += 1.0) {
                if (newState.containsKey(nbrNode.getId())) {
                    // If there is already flow at this node add on to it
                    newState.put(nbrNode.getId(), newState.get(nbrNode.getId()) + 1);
                } else {
                    // If there is no flow already at this node, place "i" flow there
                    newState.put(nbrNode.getId(), 1.0);
                }

//                System.out.println("State after flow move: " + newState);

                // See if the flow move resulted in a valid state
                MetaNode potential = new MetaNode(newState.toString(), newState);
                System.out.println("Potential: " + potential.getState());

                if (potential.isValid(meta.getFlow())) {
                    if (meta.hasNode(potential.getId())) {
                        // If this metanode already exists in the graph, simply add and edge
                        meta.addDirectedMetaEdge(parent.getId(), potential.getId());
                        explored.push(potential);
                        System.out.println("Added: " + potential.getState());
                    } else {
                        // If this metanode is not in the graph, add the node and make an edge
                        if (meta.addMetaNode(potential)) {
                            meta.addDirectedMetaEdge(parent.getId(), potential.getId());
                            explored.push(potential);
                            System.out.println("Added: " + potential.getState());
                        }
                    }
                } else {
                    // If a valid state cannot be formed by moving all the flow
                    // Try moving only some of the flow and keeping some at the node
                    System.out.println("have: " + newState + " with: " + remainingFlow);
                    HashMap<String, Double> partial = new HashMap<>();
                    partial.putAll(newState);
                    partial.put(nbrEdge.getSourceNode().toString(), remainingFlow - 1);
                    System.out.println("partial move into: " + partial);

                    // Now check that the partial flow move state is valid
                    potential = new MetaNode(partial.toString(), partial);
                    if (potential.isValid(meta.getFlow())) {
                        if (meta.hasNode(potential.getId())) {
                            // If this metanode already exists in the graph, simply add and edge
                            meta.addDirectedMetaEdge(parent.getId(), potential.getId());
                            explored.push(potential);
                            System.out.println("Added: " + potential.getState());
                        } else {
                            // If this metanode is not in the graph, add the node and make an edge
                            if (meta.addMetaNode(potential)) {
                                meta.addDirectedMetaEdge(parent.getId(), potential.getId());
                                explored.push(potential);
                                System.out.println("Added: " + potential.getState());
                            }
                        }
                    }
                }
               // Recurse down to find states involving other neighbors
                HashMap<String, Double> temp = new HashMap<>();
                temp.putAll(newState);
                recursiveMetaNodeCompletion(temp, nbrEdges, remainingFlow - i, parent);
            }
            // TODO: WHAT DOES THIS DO?
            // Without it, states in which no flow remains at the start node after the
            // first step are not found, i.e. only partial states are explored (which we don't want)
            // So, it needs to be here.
            nbrEdges.add(nbrEdge);

            return false;
        }
    }

    /**
     * Get the nodes that are contained in the specified metanode
     */
    public ArrayList<Node> getInnerNodes(String metaNodeID) {
        ArrayList<Node> innerNodes = new ArrayList<>();

        for (String n : meta.getMetaNode(metaNodeID).getState().keySet()) {
            innerNodes.add(base.getNode(n));
        }

        return innerNodes;
    }

    public Queue<Edge> getInnerNodeNbrEdges(Node innerNode) {
        Queue<Edge> nbrEdges = new LinkedBlockingQueue<>();

        for (Edge e: innerNode.getEachLeavingEdge()) {
            nbrEdges.add(e);
        }
        return nbrEdges;
    }

    public void findMetaPath() {
        // TODO: Finds a metapath on a constructed metagraph
        if (meta == null) {
            System.out.println("Must construct the meta graph first.");
            return;
        }
        System.out.println("Finding path...");

    }

    public MetaGraph getMeta() {
        return meta;
    }


    ////////////////////////////////////////////////////////////////////////////

    /**
     * Finds the meta nbrs of current and adds them to the stack.
     */
    private void iterativeFindMetaNbrs(MetaNode current) {
        // Maps each inner node of current to a list of all possible moves for the flow at that node
        HashMap<String, ArrayList<HashMap<String, Double>>> partialStates = new HashMap<>();

        ArrayList<Node> innerNodes = getInnerNodes(current.getId());
        for (Node node : innerNodes) {
            double requiredFlow = current.getState().get(node.getId());
            ArrayList<Node> nbrs = getInnerNodeNbrs(node);
//            System.out.println("Nbrs: " + nbrs);
            int j = nbrs.size() - 1; // start with the first nbr of node

            // 2. Merge each possible combination of partial states to create complete states
            if (j >= 0) {
                // get all the partial states resulting from moving the flow at this 'node'
                partialStates.put(node.getId(), recursiveNbrSearch(requiredFlow, node, nbrs, j));
            } else {
                continue;
            }
        }
//        System.out.println("Partial states: " + partialStates);
        ArrayList<HashMap<String, Double>> completeStates = generateCompleteStates(partialStates);
//        System.out.println("Complete states: " + completeStates);


        // 3. Push all found completed states (metanodes) onto the stack, then return
        for (HashMap<String, Double> state : completeStates) {
            MetaNode metaNode = new MetaNode(state.toString(), state);
            if (metaNode.isValid(meta.getFlow())) {
                if (meta.hasNode(metaNode.getId())) {
                    // if this is a valid metanode and it is already in the graph
                    // just add an edge, don't push onto stack (it's already been explored)
                    meta.addDirectedMetaEdge(current.getId(), metaNode.getId());
                } else {
                    // if this is a valid metanode and we haven't seen it already
                    explored.push(metaNode);

                    meta.addMetaNode(metaNode);
                    meta.addDirectedMetaEdge(current.getId(), metaNode.getId());
                }
            }
        }

    }

    private ArrayList<HashMap<String, Double>> generateCompleteStates(HashMap<String, ArrayList<HashMap<String, Double>>> partialStates) {
        ArrayList<ArrayList<HashMap<String, Double>>> listOfPartialStateLists = new ArrayList<>();
        for (ArrayList<HashMap<String, Double>> list : partialStates.values()) {
            listOfPartialStateLists.add(list);
        }

        return recursiveStateAccumulator(listOfPartialStateLists);
    }

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

    /**
     *
     * @param n The amount of flow that needs to be distributed
     * @param j The jth nbr to move flow to
     * @return A partial state
     */
    private ArrayList<HashMap<String, Double>> recursiveNbrSearch(double n, Node parent, ArrayList<Node> nbrs, int j) {
//        System.out.println("Rec. nbr. search with: n=" + n + " parent=" + parent + " nbrs=" + nbrs + " j=" + j);

        ArrayList<HashMap<String, Double>> states = new ArrayList<>();
        if (j < 0) {
            HashMap<String, Double> state = new HashMap<>();
            if (n >= 0.0 && parent.getId() == targetID)
                // if there is excess flow and we're at the target, we can leave it there
                state.put(parent.getId(), n);
            states.add(state);
        } else {
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

    private double capacity(Node parent, Node node) {
        if (parent.getId() == node.getId()) {
            return Double.MAX_VALUE;
        } else {
            Edge nbrEdge = parent.getEdgeToward(node);
            return nbrEdge.getAttribute("capacity");
        }
    }

    private ArrayList<Node> getInnerNodeNbrs(Node n) {
        ArrayList<Node> nbrs = new ArrayList<>();
        for (Edge e : n.getEachLeavingEdge()) {
            nbrs.add(e.getTargetNode());
        }
        return nbrs;
    }


}
