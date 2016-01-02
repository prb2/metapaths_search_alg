package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.*;

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
    /* Whether terminal brances in the MG should be pruned during the search. */
    private boolean PRUNING_ENABLED = true;

    /**
     * Initializes the meta-graph, adds the starting meta-nbr, and starts the
     * search for neighborhing meta-nbrs
     */
    private void constructMetaGraph(Graph inputG, String s, String t, double desiredFlow) {
        meta = new MetaGraph("MetaGraph", desiredFlow);
        base = inputG;
        flow = desiredFlow;
        targetID = t;
        explored = new Stack<MetaNode>();

        // Create a self edge on the target node
        base.addEdge("TargetSelf", t, t, true);
        base.getEdge("TargetSelf").setAttribute("capacity", Double.MAX_VALUE);

        // Create the start state
        HashMap<String, Double> startState = new HashMap<>();
        // All the flow starts off in the starting node
        startState.put(s, desiredFlow);
        String startNodeName = startState.toString();
        // Add the starting metanode to the metagraph
        meta.addMetaNode(new MetaNode(startNodeName, startState));
        meta.setStartID(startNodeName);

        HashMap<String, Double> targetState = new HashMap<>();
        targetState.put(t, desiredFlow);
        meta.setTargetID(targetState.toString());

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
     * Same as previous definition of constructMetaGraph, except allows for an
     * addition parameter for enabling/disabling pruning to be set. Only this
     * version of the function is public; users should always call it to ensure
     * that pruning is explicitly defined to be enabled or disabled.
     */
    public void constructMetaGraph(Graph inputG, String s, String t, double desiredFlow, boolean pruning) {
        PRUNING_ENABLED = pruning;
        constructMetaGraph(inputG, s, t, desiredFlow);
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
//                System.out.println("Finding new meta nbr for: " + current.getState().toString());
                // Find all neighbors of this meta node, then continue explores metanodes from the stack
                int nbrCount = iterativeFindMetaNbrs(current);
                if (PRUNING_ENABLED && nbrCount == 0 && !current.isTarget(targetID, flow)) {
                    // If current has no nbrs, we will prune it
                    meta.prune(current);
                }
            }
        }

    }

    /**
     * Finds the meta nbrs of current and adds them to the stack.
     */
    private int iterativeFindMetaNbrs(MetaNode current) {
        int nbrCount = 0;
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
            if (metaNode.isValid(meta.getFlow()) && !meta.inDeadset(metaNode)) {
                if (meta.hasNode(metaNode.getId())) {
                    // if this is a valid metanode and it is already in the graph
                    // just add an edge, don't push onto stack (it's already been explored)
                    meta.addDirectedMetaEdge(current.getId(), metaNode.getId());

                    nbrCount += 1;
                } else {
                    // if this is a valid metanode and we haven't seen it already
                    explored.push(metaNode);

                    meta.addMetaNode(metaNode);
                    meta.addDirectedMetaEdge(current.getId(), metaNode.getId());

                    nbrCount += 1;
                }
            }
        }

        return nbrCount;
    }

    /********** Neighbor Search Methods **********/

    /**
     *
     * @param partialStates
     * @return
     */
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

    /**
     *
     * @param partialState1
     * @param partialState2
     * @return
     */
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


    /********** Helper Methods **********/

    /**
     *
     * @param parent
     * @param node
     * @return
     */
    private double capacity(Node parent, Node node) {
        if (parent.getId() == node.getId()) {
            return Double.MAX_VALUE;
        } else {
            Edge nbrEdge = parent.getEdgeToward(node);
            return nbrEdge.getAttribute("capacity");
        }
    }

    /**
     *
     * @param n
     * @return
     */
    private ArrayList<Node> getInnerNodeNbrs(Node n) {
        ArrayList<Node> nbrs = new ArrayList<>();
        for (Edge e : n.getEachLeavingEdge()) {
            nbrs.add(e.getTargetNode());
        }
        return nbrs;
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

    /**
     *
     * @return
     */
    public MetaGraph getMeta() {
        return meta;
    }

}
