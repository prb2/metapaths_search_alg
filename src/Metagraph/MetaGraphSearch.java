package Metagraph;

import org.graphstream.graph.Edge;
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
        // All the flow is in the starting node
        state.put(s, desiredFlow);
        // Add the start node to the MG
        meta.addMetaNode(new MetaNode(s, state));

        // Find reachable states
        explored.push(meta.getMetaNode(s));
        populateMetaGraph();
        meta.display();
        try {
            meta.writeToFile("MG_" + inputG.getId());
        } catch (IOException e){
            System.out.println(e);
        }
    }

    /**
     * Searches for nbrs of the already explored meta-nbrs
     */
    private void populateMetaGraph() {
        while (!explored.empty()) {
            MetaNode current = explored.pop();
            System.out.println("Popped from stack: " + current.getState());
            if (current.isTarget(targetID, flow)) {
                // Reached the target state, but keep going to generate all possible meta-nbrs
                System.out.println("Current is target");
            } else {
                // Find all neighbors of this meta node
                System.out.println("Finding new meta nbr for: " + current.getState().toString());
                findMetaNbrs(current);
            }
        }

    }


    private void findMetaNbrs(MetaNode current) {
        Queue<Node> innerNodes = getInnerNodes(current.getId());
        Map<String, Double> currentState = current.getState();
        Map<String, Double> newState = new HashMap<>();
        boolean completed = true;

        while (!innerNodes.isEmpty()) {
            // Get the next inner node
            Node innerNode = innerNodes.poll();

            // Get the flow that needs to be moved from this node
            double flow = currentState.get(innerNode.getId());

            // Get the neighboring edges
            Queue<Edge> nbrEdges = getInnerNodeNbrEdges(innerNode);
//            System.out.println(innerNode.getId() + " has nbr edges: " + nbrEdges);

            // If a complete state was found in a previous, iteration, start
            // with a fresh state. Otherwise, build upon the previous state
            // TODO: This probably isn't the best way to handle this. Think about it.
            if (completed) {
                newState = new HashMap<>();
            }

            // Recursively finds potential metanode nbrs and adds them to the graph if they are valid
            completed = recursiveMetaNodeCompletion(newState, nbrEdges, flow, current);
        }
    }

    private Boolean recursiveMetaNodeCompletion(Map<String, Double> newState, Queue<Edge> nbrEdges, double remainingFlow, MetaNode parent) {
//        System.out.println("Called with: " + newState + " remaining flow: " + remainingFlow);
        if (nbrEdges.isEmpty()) {
            return false;
        } else {
            // Get the next nbr and its capacity
            Edge nbrEdge = nbrEdges.poll(); // know it's not empty
            Node nbrNode = nbrEdge.getTargetNode();
            double nbrCapacity = nbrEdge.getAttribute("capacity");

            // While we have flow to move, or we hit the capacity
            // explore available states from moving i balls to nbr
            for (double i = 1.0; i <= Math.min(remainingFlow, nbrCapacity); i += 1.0) {
                if (newState.containsKey(nbrNode.getId())) {
                    newState.put(nbrNode.getId(), newState.get(nbrNode.getId()) + 1); // add this flow move to the state
                } else {
                    newState.put(nbrNode.getId(), i); // add this flow move to the state
                }
//                System.out.println("State after flow move: " + newState);

                // See if the flow move resulted in a valid state
                MetaNode potential = new MetaNode(newState.toString(), newState);
                if (potential.isValid(meta.getFlow())) {
                    if (meta.hasNode(potential.getId())) {
                        meta.addDirectedMetaEdge(parent.getId(), potential.getId());
                        explored.push(potential);
                    } else {
                        if (meta.addMetaNode(potential)) {
                            meta.addDirectedMetaEdge(parent.getId(), potential.getId());
                            explored.push(potential);
                        }
                    }
                }
               // Recurse down to find states involving other neighbors
                HashMap<String, Double> temp = new HashMap<>();
                temp.putAll(newState);
                recursiveMetaNodeCompletion(temp, nbrEdges, remainingFlow - i, parent);
            }
            nbrEdges.add(nbrEdge);
            return false;
        }
    }

    /**
     * Get the nodes that are contained in the specified metanode
     * @param metaNodeID
     * @return
     */
    public Queue<Node> getInnerNodes(String metaNodeID) {
        Queue<Node> innerNodes = new LinkedBlockingQueue<>();

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
