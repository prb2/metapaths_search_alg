package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import scala.Int;
import scala.util.parsing.combinator.testing.Str;

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
            System.out.println("Popped from stack: " + current.getState());
            if (current.isTarget(targetID, flow)) {
                System.out.println("Current is target");
                break;
            } else {
                // Find all neighbors of this meta node
                System.out.println("Finding new meta nbr for: " + current.getState().toString());
                findMetaNbrs(current);


                // Maintain the reachable nbrs to be visited later
//                for (MetaNode mn : metaNbrs) {
//                    explored.push(mn);
//                }
            }
        }

    }


    private void findMetaNbrs(MetaNode current) {
        Queue<Node> innerNodes = getInnerNodes(current.getId());
        Map<String, Double> currentState = current.getState();
        Map<String, Double> newState = new HashMap<>();
        boolean completed = true;

        while (!innerNodes.isEmpty()) {
            Node innerNode = innerNodes.poll();
            double flow = currentState.get(innerNode.getId());
            Queue<Edge> nbrEdges = getInnerNodeNbrEdges(innerNode);
            System.out.println(innerNode.getId() + " has nbr edges: " + nbrEdges);
            if (completed) {
                newState = new HashMap<>();
            }

            // recursively finds potential metanode nbrs and adds them to the graph if they are valid
            newState = recursiveMetaNodeCompletion(newState, nbrEdges, flow, current, completed);
            System.out.println("Exited recursion with:" + newState);
            System.out.println("Need metaflow:" + meta);
            MetaNode verifyNode = new MetaNode(newState.toString(), newState);
            completed = verifyNode.isValid(meta.getFlow());
            System.out.println("Completed: " + completed);
            // make new meta node with newState
        }
    }

    private Map<String, Double> recursiveMetaNodeCompletion(Map<String, Double> newState, Queue<Edge> nbrEdges, double remainingFlow, MetaNode parent, boolean completed) {
        if (remainingFlow == 0.0 && nbrEdges.isEmpty()) {
            // add metanode to metagraph if flow has been satisfied and there are no more nbrs to consider
            MetaNode potential = new MetaNode(newState.toString(), newState);
            if (meta.addMetaNode(potential)) {
                meta.addDirectedMetaEdge(parent.getId(), potential.getId());
                explored.push(potential);
                System.out.println("Added new meta nbr: " + potential.getState().toString());
                completed = true;
            } else {
                System.out.println("Not valid, but ran out of edges, need to pop up: " + newState);
                return newState;
            }
        }
        System.out.println("Exploring new state: " + newState + "with remaining flow: " + remainingFlow);
        if (nbrEdges.isEmpty()) {
            return newState;
        }
        Edge nbrEdge = nbrEdges.poll(); // know it's not empty
        System.out.println("Current edge: " + nbrEdge);

        // get the first nbr and its capacity
        Node nbrNode = nbrEdge.getTargetNode();
        double nbrCapacity = nbrEdge.getAttribute("capacity");
        System.out.println("Nbr cap: " + nbrCapacity);

        // While we have flow to move, or we hit the capacity
        // explore availiable states from move i balls to nbr
        for (double i = 0.0; i <= Math.min(remainingFlow, nbrCapacity); i += 1.0) {
            System.out.println("Min of " + remainingFlow + " and " + nbrCapacity + ". i =" + i);
            if (completed) {
                newState.put(nbrNode.getId(), i); // add this flow move to the state
            } else {
                if (newState.containsKey(nbrNode.getId())) {
                    newState.put(nbrNode.getId(), newState.get(nbrNode.getId()) + i); // add this flow move to the state
                } else {
                    newState.put(nbrNode.getId(), i); // add this flow move to the state
                }
            }
            System.out.println("Added to state: " + newState);
            newState = recursiveMetaNodeCompletion(newState, nbrEdges, remainingFlow - i, parent, completed);
        }
        nbrEdges.add(nbrEdge);
        if (completed){
            return newState;
        }
        return new HashMap<>();
    }
//
//    private Map<String, Double> mergeStates(Map<String, Double> first, Map<String, Double> second) {
//
//    }

    /**
     * Get the nodes that are contained in the specified metanode
     * @param metaNodeID
     * @return
     */
    public Queue<Node> getInnerNodes(String metaNodeID) {
        Queue<Node> innerNodes = new LinkedBlockingQueue<>();

        for (String n : meta.getMetaNode(metaNodeID).getState().keySet()) {

            System.out.println("inner: " + base.getNode(n));
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
