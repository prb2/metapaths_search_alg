package Metagraph;

import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Map;

/**
 * Wrapper for nodes in a MetaGraph
 */
public class MetaNode {
    private String id;
    private Map<String, Double> state;

    public MetaNode(String name, Map<String, Double> counts) {
        id = name;
        state = counts;
    }

    /**
     * A meta-node is only valid if the sum of the nodes' flow is equal to
     * the required flow.
     * @param requiredFlow The specified flow need for each node in the meta graph
     */
    public Boolean isValid(double requiredFlow) {
        double stateFlow = 0;

//        System.out.println("Checking validity of: " + state.toString());
        for (Map.Entry<String, Double> entry : state.entrySet()) {
            stateFlow += entry.getValue();
        }

        System.out.println("State flow: " + stateFlow + " Required flow: " + requiredFlow);
        return stateFlow == requiredFlow;
    }

    public Boolean isTarget(String targetID, double targetFlow) {
        if (state.containsKey(targetID)) {
            if (state.get(targetID) == targetFlow) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Double> getState() {
        return state;
    }

    public void setState(Map<String, Double> state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
