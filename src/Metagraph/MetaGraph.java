package Metagraph;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.SingleNode;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDOT;
import scala.util.regexp.Base;

import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by prudhvi on 10/16/15.
 */
public class MetaGraph {
    /**
     * Maps each meta-node to its state
     * {meta-node : {node1:count1, node2:count2}}
     */
    Map<String, Map<String, Double>> stateMap;
    Map<String, MetaNode> metaNodes;

    /* The collection of nodes which have been pruned from the graph */
    HashMap<MetaNode, ArrayList<MetaNode>> deadset = new HashMap<>(); // Maps the pruned node to a list of its parents

    public Graph getInternal() {
        return internal;
    }

    private Graph internal;

    private String startID;

    private String targetID;

    public double getFlow() {
        return flow;
    }

    private double flow;

    protected MetaGraph(String id, double desiredFlow) {
        internal = new SingleGraph(id);
        flow = desiredFlow;
        stateMap = new HashMap<>();
        metaNodes = new HashMap<>();
    }

    public Boolean addMetaNode(MetaNode metanode) {
        if (metanode.isValid(flow)) {
            internal.addNode(metanode.getId());
            Node node = internal.getNode(metanode.getId());
            node.setAttribute("state", metanode.getState().toString());
            metaNodes.put(metanode.getId(), metanode);
            stateMap.put(metanode.getId(), metanode.getState());
//            System.out.println("Added new meta nbr to MG with ID: " + metanode.getId() + " and value: " + metanode.getState());
            return true;
        } else {
            System.out.println("State was not valid. Node was not added to meta-graph.");
            return false;
        }
    }

    public Boolean hasNode(String nodeID) {
        return metaNodes.containsKey(nodeID);
    }

    public void addDirectedMetaEdge(String from, String to) {
        if (internal.getEdge(from + "->" + to) == null) {
            internal.addEdge(from + "->" + to, from, to, true);
        }
    }

    // whether this state is already a node in the meta graph
    public Boolean contains(HashMap<String, Double> state) {
        return stateMap.containsValue(state);
    }

    public void display() {
        for (Node n : internal) {
            n.setAttribute("ui.label", stateMap.get(n.getId()));
        }
        //TODO: Uncomment to display meta graph
        System.out.println(stateMap);
        internal.display();
    }

    public void writeToFile(String filename) throws IOException {
        FileSink fs = new FileSinkDOT();
        fs.writeAll(internal, "graphs/" + filename + ".dot");
    }

    public MetaNode getMetaNode(String metaNodeID) {
//        return new MetaNode(metaNodeID, stateMap.get(metaNodeID));
        return metaNodes.get(metaNodeID);
    }

    public boolean hasState(Map<String, Double> newState) {
        return stateMap.containsValue(newState);
    }

    public void setStartID(String startID) {
        this.startID = startID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public String getStartID() {
        return startID;
    }

    public String getTargetID() {
        return targetID;
    }

    public boolean inDeadset(MetaNode node) {
        return deadset.containsKey(node);
    }

    /**
     * Prunes the metagraph to remove terminal branches
     * @param terminus The terminal node to start pruning from
     */
    public void prune(MetaNode terminus) {
        System.out.println("Received node for pruning: " + terminus.getId());
        HashMap<String, Double> endState = terminus.getState();
        Node endNode = internal.getNode(endState.toString());
        // Find the terminal node's parents
        ArrayList<MetaNode> parents = new ArrayList<>();
        for (Edge edge : endNode.getEachEnteringEdge()) {
            Node parent = edge.getSourceNode();
            parents.add(getMetaNode(parent.getId()));
        }
        // Add this node to the dead set
        deadset.put(terminus, parents);

        // Remove it from the MG
        internal.removeNode(endNode);

        // Check if any of its parents can be pruned
        for (MetaNode parent : parents) {
            Node node = internal.getNode(parent.getState().toString());
            if (node.getLeavingEdgeSet().size() == 0) {
                // if the parent has no children, it is also a dead end and can be pruned
                prune(parent);
            }
        }
    }

}
